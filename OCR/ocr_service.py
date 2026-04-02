import io
import re
import cv2
import httpx
import numpy as np
import pytesseract
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
from PIL import Image

pytesseract.pytesseract.tesseract_cmd = r"C:\Program Files\Tesseract-OCR\tesseract.exe"

OLLAMA_BASE_URL = "http://localhost:11434"
OLLAMA_MODEL = "gemma3:4b"

app = FastAPI(title="OCR Service", description="Invoice/receipt OCR processing with LLM correction")


# --- Image Preprocessing optimized for Bulgarian receipts ---

def grayscale(image):
    return cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)


def noise_removal(image):
    image = cv2.medianBlur(image, 3)
    kernel = np.ones((1, 1), np.uint8)
    image = cv2.dilate(image, kernel, iterations=1)
    image = cv2.erode(image, kernel, iterations=1)
    image = cv2.morphologyEx(image, cv2.MORPH_CLOSE, kernel)
    return image


def threshold_image(gray_image):
    return cv2.adaptiveThreshold(
        gray_image, 255,
        cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
        cv2.THRESH_BINARY,
        31, 10
    )


def get_skew_angle(cv_image) -> float:
    new_image = cv_image.copy()
    gray = cv2.cvtColor(new_image, cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(gray, (9, 9), 0)
    thresh = cv2.threshold(blur, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)[1]

    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (30, 5))
    dilate = cv2.dilate(thresh, kernel, iterations=2)

    contours, _ = cv2.findContours(dilate, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    contours = sorted(contours, key=cv2.contourArea, reverse=True)

    if not contours:
        return 0.0

    largest_contour = contours[0]
    min_area_rect = cv2.minAreaRect(largest_contour)
    angle = min_area_rect[-1]
    if angle < -45:
        angle = 90 + angle
    return -1.0 * angle


def rotate_image(cv_image, angle: float):
    new_image = cv_image.copy()
    (h, w) = new_image.shape[:2]
    center = (w // 2, h // 2)
    m = cv2.getRotationMatrix2D(center, angle, 1.0)
    new_image = cv2.warpAffine(new_image, m, (w, h), flags=cv2.INTER_CUBIC, borderMode=cv2.BORDER_REPLICATE)
    return new_image


def deskew(cv_image):
    angle = get_skew_angle(cv_image)
    if abs(angle) < 0.5:
        return cv_image
    return rotate_image(cv_image, -1.0 * angle)


def resize_for_ocr(image, target_dpi=300):
    """Upscale small images to improve OCR accuracy."""
    h, w = image.shape[:2]
    if max(h, w) < 1500:
        scale = 2.0
        image = cv2.resize(image, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)
    return image


def sharpen_image(image):
    """Apply sharpening to make text edges crisper."""
    kernel = np.array([[-1, -1, -1],
                       [-1,  9, -1],
                       [-1, -1, -1]])
    return cv2.filter2D(image, -1, kernel)


def remove_borders(image):
    contours, _ = cv2.findContours(image, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    if not contours:
        return image
    cnts_sorted = sorted(contours, key=lambda x: cv2.contourArea(x))
    cnt = cnts_sorted[-1]
    x, y, w, h = cv2.boundingRect(cnt)
    crop = image[y:y + h, x:x + w]
    return crop


def preprocess_image(img: np.ndarray) -> np.ndarray:
    img = resize_for_ocr(img)
    deskewed = deskew(img)
    gray_image = grayscale(deskewed)
    gray_image = sharpen_image(gray_image)
    bw_image = threshold_image(gray_image)
    no_noise = noise_removal(bw_image)
    no_borders = remove_borders(no_noise)
    return no_borders


# --- Tesseract OCR for Bulgarian receipts ---

def ocr_image(pil_image: Image.Image) -> str:
    """Run Tesseract with Bulgarian + English config optimized for receipts."""
    custom_config = (
        "--oem 3 "          # LSTM neural net engine
        "--psm 6 "          # Assume a single uniform block of text
        "-l bul+eng "       # Bulgarian primary, English fallback (for numbers/brands)
        "-c preserve_interword_spaces=1"
    )
    return pytesseract.image_to_string(pil_image, config=custom_config)


# --- LLM Correction via Ollama ---

async def correct_text_with_llm(raw_text: str) -> str:
    """Send OCR text to local Ollama model for correction."""
    prompt = (
        "You are an OCR post-processor for Bulgarian store receipts. "
        "The following text was extracted via OCR from a Bulgarian receipt photo and may contain errors.\n\n"
        "Your task:\n"
        "1. Fix OCR misreadings in Bulgarian text (wrong Cyrillic characters, broken words)\n"
        "2. Keep all numbers and prices EXACTLY as they are - do NOT change any digits or decimal values\n"
        "3. Keep the original line structure\n"
        "4. Do NOT add any commentary, explanation or extra text\n"
        "5. Return ONLY the corrected receipt text, nothing else\n\n"
        f"OCR Text:\n{raw_text}"
    )

    try:
        async with httpx.AsyncClient(timeout=60.0) as client:
            response = await client.post(
                f"{OLLAMA_BASE_URL}/api/generate",
                json={
                    "model": OLLAMA_MODEL,
                    "prompt": prompt,
                    "stream": False,
                    "options": {
                        "temperature": 0.1,
                        "num_predict": 2048,
                    }
                }
            )
            response.raise_for_status()
            result = response.json()
            return result.get("response", raw_text).strip()
    except Exception as e:
        print(f"LLM correction failed, returning raw OCR text: {e}")
        return raw_text


# --- Receipt Parsing ---

def parse_receipt_text(raw_text: str) -> dict:
    lines = [line.strip() for line in raw_text.split("\n") if line.strip()]

    items = []
    total = None
    store_name = lines[0] if lines else None

    price_pattern = re.compile(r"(\d+[.,]\d{2})\s*$")
    total_pattern = re.compile(
        r"(total|totaal|suma|gesamt|zusammen|subtotal|общо|тотал|всичко|сума|дължима)",
        re.IGNORECASE
    )

    for line in lines:
        if total_pattern.search(line):
            match = price_pattern.search(line)
            if match:
                total = float(match.group(1).replace(",", "."))
            continue

        match = price_pattern.search(line)
        if match:
            price = float(match.group(1).replace(",", "."))
            name = line[:match.start()].strip()
            name = re.sub(r"^\d+\s*[xX*]\s*", "", name).strip()
            if name and price > 0:
                items.append({"name": name, "price": price})

    return {
        "store_name": store_name,
        "items": items,
        "total": total,
        "item_count": len(items),
    }


# --- API Endpoint ---

@app.post("/process-receipt")
async def process_receipt(file: UploadFile = File(...)):
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File must be an image")

    contents = await file.read()
    np_arr = np.frombuffer(contents, np.uint8)
    img = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)

    if img is None:
        raise HTTPException(status_code=400, detail="Could not decode image")

    processed = preprocess_image(img)

    pil_image = Image.fromarray(processed)
    raw_text = ocr_image(pil_image)

    # Correct OCR text with local LLM
    corrected_text = await correct_text_with_llm(raw_text)

    parsed = parse_receipt_text(corrected_text)
    parsed["raw_text"] = raw_text
    parsed["corrected_text"] = corrected_text

    return JSONResponse(content=parsed)


@app.get("/health")
async def health():
    return {"status": "ok"}
