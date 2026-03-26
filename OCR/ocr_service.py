import io
import re
import cv2
import numpy as np
import pytesseract
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
from PIL import Image

pytesseract.pytesseract.tesseract_cmd = r"C:\Program Files\Tesseract-OCR\tesseract.exe"

app = FastAPI(title="OCR Service", description="Invoice/receipt OCR processing")


# --- Image Preprocessing (from main.py) ---

def grayscale(image):
    return cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)


def noise_removal(image):
    kernel = np.ones((1, 1), np.uint8)
    image = cv2.dilate(image, kernel, iterations=1)
    image = cv2.erode(image, kernel, iterations=1)
    image = cv2.morphologyEx(image, cv2.MORPH_CLOSE, kernel)
    image = cv2.medianBlur(image, 3)
    return image


def threshold_image(gray_image):
    _, im_bw = cv2.threshold(gray_image, 210, 230, cv2.THRESH_BINARY)
    return im_bw


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
    deskewed = deskew(img)
    gray_image = grayscale(deskewed)
    bw_image = threshold_image(gray_image)
    no_noise = noise_removal(bw_image)
    no_borders = remove_borders(no_noise)
    return no_borders


# --- Receipt Parsing ---

def parse_receipt_text(raw_text: str) -> dict:
    lines = [line.strip() for line in raw_text.split("\n") if line.strip()]

    items = []
    total = None
    store_name = lines[0] if lines else None

    price_pattern = re.compile(r"(\d+[.,]\d{2})\s*$")
    total_pattern = re.compile(r"(total|totaal|suma|gesamt|zusammen|subtotal)", re.IGNORECASE)

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
    raw_text = pytesseract.image_to_string(pil_image)

    parsed = parse_receipt_text(raw_text)
    parsed["raw_text"] = raw_text

    return JSONResponse(content=parsed)


@app.get("/health")
async def health():
    return {"status": "ok"}
