import logging
from logging.handlers import RotatingFileHandler
import os
from pathlib import Path
import re
import time
import traceback
import uuid

import cv2
import httpx
import numpy as np
import pytesseract
from fastapi import FastAPI, File, HTTPException, Request, UploadFile
from fastapi.responses import JSONResponse
from PIL import Image

BASE_DIR = Path(__file__).resolve().parent
LOGS_DIR = BASE_DIR / "logs"
LOGS_DIR.mkdir(exist_ok=True)

logger = logging.getLogger("ocr_service")
if not logger.handlers:
    formatter = logging.Formatter(
        "%(asctime)s | %(levelname)s | %(name)s | %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
    )

    stream_handler = logging.StreamHandler()
    stream_handler.setFormatter(formatter)

    file_handler = RotatingFileHandler(
        LOGS_DIR / "ocr-service.log",
        maxBytes=2 * 1024 * 1024,
        backupCount=3,
        encoding="utf-8",
    )
    file_handler.setFormatter(formatter)

    logger.setLevel(logging.INFO)
    logger.addHandler(stream_handler)
    logger.addHandler(file_handler)
    logger.propagate = False

TESSERACT_CMD = os.getenv("TESSERACT_CMD", r"C:\Program Files\Tesseract-OCR\tesseract.exe")
OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://127.0.0.1:11434")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "gemma3:4b")
OLLAMA_TIMEOUT_SECONDS = float(os.getenv("OLLAMA_TIMEOUT_SECONDS", "45"))
OCR_MIN_DIMENSION = int(os.getenv("OCR_MIN_DIMENSION", "1800"))
OCR_MAX_DIMENSION = int(os.getenv("OCR_MAX_DIMENSION", "2400"))
LLM_MAX_INPUT_CHARS = int(os.getenv("LLM_MAX_INPUT_CHARS", "3000"))

pytesseract.pytesseract.tesseract_cmd = TESSERACT_CMD

app = FastAPI(title="OCR Service", description="Invoice/receipt OCR processing with LLM correction")


def log_stage(request_id: str, stage: str, started_at: float, extra: str | None = None) -> None:
    elapsed_ms = (time.perf_counter() - started_at) * 1000
    suffix = f" | {extra}" if extra else ""
    logger.info("[%s] %s finished in %.0f ms%s", request_id, stage, elapsed_ms, suffix)


@app.on_event("startup")
async def startup_event():
    logger.info(
        "Starting OCR service | tesseract=%s | ollama_base_url=%s | ollama_model=%s | logs=%s",
        TESSERACT_CMD,
        OLLAMA_BASE_URL,
        OLLAMA_MODEL,
        LOGS_DIR / "ocr-service.log",
    )
    logger.info("Tesseract executable exists: %s", Path(TESSERACT_CMD).exists())

    try:
        timeout = httpx.Timeout(connect=3.0, read=5.0, write=3.0, pool=3.0)
        async with httpx.AsyncClient(timeout=timeout) as client:
            response = await client.get(f"{OLLAMA_BASE_URL}/api/tags")
            response.raise_for_status()
            models = [model.get("name") for model in response.json().get("models", [])]
            logger.info("Ollama is reachable. Available models: %s", ", ".join(models) if models else "(none)")
    except Exception as exc:
        logger.warning("Ollama startup check failed: %s", exc)


@app.middleware("http")
async def log_requests(request: Request, call_next):
    request_id = request.headers.get("x-request-id") or uuid.uuid4().hex[:8]
    request.state.request_id = request_id
    started_at = time.perf_counter()
    content_length = request.headers.get("content-length", "unknown")
    logger.info(
        "[%s] Incoming %s %s | content-length=%s | client=%s",
        request_id,
        request.method,
        request.url.path,
        content_length,
        request.client.host if request.client else "unknown",
    )

    try:
        response = await call_next(request)
    except Exception:
        logger.exception("[%s] Unhandled error while processing %s", request_id, request.url.path)
        raise

    elapsed_ms = (time.perf_counter() - started_at) * 1000
    logger.info("[%s] Completed %s %s with %s in %.0f ms", request_id, request.method, request.url.path, response.status_code, elapsed_ms)
    return response


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


def resize_for_ocr(image):
    """Normalize image size to keep OCR reliable without huge scan latency."""
    h, w = image.shape[:2]
    max_dim = max(h, w)
    scale = 1.0

    if max_dim < OCR_MIN_DIMENSION:
        scale = OCR_MIN_DIMENSION / max_dim
    elif max_dim > OCR_MAX_DIMENSION:
        scale = OCR_MAX_DIMENSION / max_dim

    if abs(scale - 1.0) > 0.01:
        interpolation = cv2.INTER_CUBIC if scale > 1 else cv2.INTER_AREA
        image = cv2.resize(image, None, fx=scale, fy=scale, interpolation=interpolation)

    return image


def sharpen_image(image):
    """Apply unsharp mask for crisper text."""
    blurred = cv2.GaussianBlur(image, (0, 0), 3)
    return cv2.addWeighted(image, 1.5, blurred, -0.5, 0)


def normalize_match_text(text: str) -> str:
    replacements = {
        "\u0410": "A", "\u0430": "A",
        "\u0412": "B", "\u0432": "B",
        "\u0421": "C", "\u0441": "C",
        "\u0415": "E", "\u0435": "E",
        "\u041d": "H", "\u043d": "H",
        "\u041a": "K", "\u043a": "K",
        "\u041c": "M", "\u043c": "M",
        "\u041e": "O", "\u043e": "O",
        "\u0420": "P", "\u0440": "P",
        "\u0422": "T", "\u0442": "T",
        "\u0423": "Y", "\u0443": "Y",
        "\u0425": "X", "\u0445": "X",
        "\u0411": "B", "\u0431": "B",
        "\u0417": "3", "\u0437": "3",
        "\u0427": "4", "\u0447": "4",
        "\u0428": "W", "\u0448": "W",
        "\u0429": "W", "\u0449": "W",
    }
    normalized = "".join(replacements.get(char, char.upper()) for char in text)
    return re.sub(r"[^A-Z0-9.,:/ -]", "", normalized)


def find_receipt_region(image: np.ndarray) -> np.ndarray:
    gray_image = grayscale(image)
    blurred = cv2.GaussianBlur(gray_image, (5, 5), 0)
    _, thresholded = cv2.threshold(blurred, 160, 255, cv2.THRESH_BINARY)

    contours, _ = cv2.findContours(thresholded, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    if not contours:
        return image

    image_area = image.shape[0] * image.shape[1]
    for contour in sorted(contours, key=cv2.contourArea, reverse=True):
        area = cv2.contourArea(contour)
        if area < image_area * 0.15:
            break

        x, y, w, h = cv2.boundingRect(contour)
        aspect_ratio = w / max(h, 1)
        if 0.25 <= aspect_ratio <= 0.9:
            return image[y:y + h, x:x + w]

    return image


def build_ocr_lines(image: np.ndarray) -> list[dict]:
    config = (
        "--oem 3 "
        "--psm 6 "
        "-l bul+eng "
        "-c preserve_interword_spaces=1"
    )

    data = pytesseract.image_to_data(image, config=config, output_type=pytesseract.Output.DICT)
    lines_by_key = {}

    for index, word in enumerate(data["text"]):
        word = word.strip()
        if not word:
            continue

        key = (data["block_num"][index], data["par_num"][index], data["line_num"][index])
        line = lines_by_key.setdefault(key, {
            "words": [],
            "left": 10**9,
            "top": 10**9,
            "right": 0,
            "bottom": 0,
        })

        left = data["left"][index]
        top = data["top"][index]
        width = data["width"][index]
        height = data["height"][index]

        line["words"].append(word)
        line["left"] = min(line["left"], left)
        line["top"] = min(line["top"], top)
        line["right"] = max(line["right"], left + width)
        line["bottom"] = max(line["bottom"], top + height)

    lines = []
    for line in lines_by_key.values():
        line["text"] = " ".join(line["words"])
        lines.append(line)

    return sorted(lines, key=lambda current: current["top"])


def is_summary_line(text: str) -> bool:
    normalized = normalize_match_text(text)
    summary_markers = (
        "CYMA",
        "SUMA",
        "MEJDIN",
        "MEXDIN",
        "OBWA",
        "OBSHT",
        "EBPO",
        "EURO",
        "LEBA",
        "LEVA",
        "KYPC",
        "KURS",
        "PECTO",
        "RESTO",
        "BPOJ",
        "BPOY",
        "BROY",
    )
    return any(marker in normalized for marker in summary_markers)


def find_items_region(receipt_image: np.ndarray) -> np.ndarray:
    height, width = receipt_image.shape[:2]
    ocr_lines = build_ocr_lines(grayscale(receipt_image))
    price_pattern = re.compile(r"\d+[.,]\d{2}\s*[A-Za-z\u0410-\u044f]?$")
    candidates = []

    for line in ocr_lines:
        text = line["text"].strip()
        has_price = bool(price_pattern.search(text))
        has_alpha = any(char.isalpha() for char in text)
        starts_too_high = line["top"] < height * 0.18
        reaches_price_column = line["right"] > width * 0.72

        if has_price and has_alpha and reaches_price_column and not starts_too_high:
            candidates.append(line)

    if not candidates:
        return receipt_image

    filtered_candidates = []
    first_summary_top = None
    for line in candidates:
        if is_summary_line(line["text"]):
            first_summary_top = line["top"]
            break
        filtered_candidates.append(line)

    candidates_to_crop = filtered_candidates if len(filtered_candidates) >= 2 else candidates

    top = max(0, min(line["top"] for line in candidates_to_crop) - 18)
    bottom = min(height, max(line["bottom"] for line in candidates_to_crop) + 18)
    left = max(0, min(line["left"] for line in candidates_to_crop) - 20)
    right = min(width, max(line["right"] for line in candidates_to_crop) + 18)

    if first_summary_top is not None:
        bottom = min(bottom, first_summary_top - 8)

    if bottom - top < 40 or right - left < 120:
        return receipt_image

    return receipt_image[top:bottom, left:right]


def preprocess_image(img: np.ndarray) -> np.ndarray:
    img = resize_for_ocr(img)
    deskewed = deskew(img)
    gray_image = grayscale(deskewed)
    gray_image = sharpen_image(gray_image)
    bw_image = threshold_image(gray_image)
    no_noise = noise_removal(bw_image)
    return no_noise


# --- Tesseract OCR for Bulgarian receipts ---

def ocr_image(pil_image: Image.Image) -> str:
    """Run Tesseract with Bulgarian + English config optimized for receipt items."""
    custom_config = (
        "--oem 3 "          # LSTM neural net engine
        "--psm 6 "          # Assume uniform block of text
        "-l bul+eng "       # Bulgarian primary, English fallback (numbers/brands)
        "-c preserve_interword_spaces=1"
    )
    return pytesseract.image_to_string(pil_image, config=custom_config)


# --- LLM Correction via Ollama ---

async def correct_text_with_llm(raw_text: str) -> str:
    """Send OCR text to local Ollama model for correction."""
    normalized_text = raw_text.strip()
    if not normalized_text:
        logger.warning("Skipping LLM correction because OCR text is empty")
        return raw_text

    if len(normalized_text) > LLM_MAX_INPUT_CHARS:
        logger.warning(
            "Truncating OCR text before LLM correction from %s to %s characters",
            len(normalized_text),
            LLM_MAX_INPUT_CHARS,
        )
        normalized_text = normalized_text[:LLM_MAX_INPUT_CHARS]

    prompt = (
        "You are an OCR post-processor for Bulgarian store receipts.\n\n"
        "Below is OCR-extracted text from a Bulgarian receipt photo. It contains item names and prices. "
        "The text may have OCR errors in the Cyrillic characters.\n\n"
        "Rules:\n"
        "- Fix misspelled Bulgarian/Cyrillic words (e.g. wrong letters, merged/split words)\n"
        "- DO NOT change any numbers, prices, or decimal values\n"
        "- DO NOT change Latin characters (brand names like OREO, MARETTI, etc.)\n"
        "- Keep each line exactly as a separate line\n"
        "- Output ONLY the corrected text, no explanations\n\n"
        f"OCR Text:\n{normalized_text}"
    )

    try:
        timeout = httpx.Timeout(
            connect=10.0,
            read=OLLAMA_TIMEOUT_SECONDS,
            write=10.0,
            pool=10.0,
        )
        async with httpx.AsyncClient(timeout=timeout) as client:
            llm_started_at = time.perf_counter()
            logger.info("Sending %s chars to Ollama model %s", len(normalized_text), OLLAMA_MODEL)
            response = await client.post(
                f"{OLLAMA_BASE_URL}/api/generate",
                json={
                    "model": OLLAMA_MODEL,
                    "prompt": prompt,
                    "stream": False,
                    "options": {
                        "temperature": 0.1,
                        "num_predict": 1024,
                    }
                }
            )
            response.raise_for_status()
            result = response.json()
            corrected = result.get("response", raw_text).strip()
            logger.info(
                "Ollama correction completed in %.0f ms and returned %s chars",
                (time.perf_counter() - llm_started_at) * 1000,
                len(corrected),
            )
            return corrected or raw_text
    except Exception as e:
        logger.warning("LLM correction failed [%s]: %s", type(e).__name__, repr(e))
        logger.debug("LLM correction traceback:\n%s", traceback.format_exc())
        return raw_text


# --- Receipt Parsing ---

def parse_receipt_text(raw_text: str) -> dict:
    lines = [line.strip() for line in raw_text.split("\n") if line.strip()]

    items = []
    total_leva = None
    total_euro = None
    store_name = None
    price_pattern = re.compile(r"(\d+[.,]\d{2})\s*[A-Za-z\u0410-\u044f]?\s*$", re.IGNORECASE)

    for line in lines:
        if line and not re.match(r"^[\d\W]+$", line) and not price_pattern.search(line):
            store_name = line
            break

    # Price at end of line, optionally followed by Bulgarian VAT code letter
    price_pattern = re.compile(r"(\d+[.,]\d{2})\s*[АБВГABCDабвгabcd]?\s*$", re.IGNORECASE)

    leva_total_pattern = re.compile(r"(лева|лв\.?|bgn)", re.IGNORECASE)
    euro_total_pattern = re.compile(r"(евро|eur|euro)", re.IGNORECASE)
    total_line_pattern = re.compile(
        r"(total|suma|subtotal|общо|тотал|всичко|обща\s+сума|дължима|межди)",
        re.IGNORECASE
    )
    skip_pattern = re.compile(
        r"(еик|ддс|зддс|унп|каса|бон|нул|ид\.?\s*но|артикул|благ|фискал"
        r"|fantastico|фантастико|курс|брой|рест|дата|час|касиер|оператор"
        r"|\*{3,}|-{3,}|={3,}|\d{4}-\d{2}-\d{2}|\d{2}:\d{2}:\d{2})",
        re.IGNORECASE
    )
    price_pattern = re.compile(r"(\d+[.,]\d{2})\s*(?:[A-Za-z\u0410-\u044f]{0,3}\.?)?\s*$", re.IGNORECASE)
    leva_total_pattern = re.compile(r"(\u043b\u0435\u0432\u0430|\u043b\u0432\.?|bgn|leva)", re.IGNORECASE)
    euro_total_pattern = re.compile(r"(\u0435\u0432\u0440\u043e|eur|euro|ebpo)", re.IGNORECASE)

    for line in lines:
        if skip_pattern.search(line):
            continue

        is_total_line = bool(total_line_pattern.search(line) or is_summary_line(line))

        if is_total_line:
            match = price_pattern.search(line)
            if match:
                value = float(match.group(1).replace(",", "."))
                if leva_total_pattern.search(line):
                    if total_leva is None:
                        total_leva = value
                elif euro_total_pattern.search(line):
                    if total_euro is None:
                        total_euro = value
                else:
                    if total_leva is None:
                        total_leva = value
            continue

        match = price_pattern.search(line)
        if match:
            price = float(match.group(1).replace(",", "."))
            name = line[:match.start()].strip()
            name = re.sub(r"^\d+\s*[xX×*]\s*", "", name).strip()
            name = name.lstrip("*").strip()
            if name and price > 0:
                items.append({"name": name, "price": price})

    total = total_leva if total_leva is not None else total_euro

    return {
        "store_name": store_name,
        "items": items,
        "total": total,
        "item_count": len(items),
    }


def parse_items_only_text(raw_text: str) -> dict:
    lines = [line.strip() for line in raw_text.split("\n") if line.strip()]
    items = []
    price_pattern = re.compile(
        r"(?P<price>(?:\d+[.,]\d{2}|[.,]\d{2}))\s*(?:[A-Za-z\u0410-\u044f]{0,3}\.?)?\s*$",
        re.IGNORECASE,
    )

    for line in lines:
        if is_summary_line(line):
            continue

        match = price_pattern.search(line)
        if not match:
            continue

        raw_price = match.group("price")
        if raw_price.startswith((".", ",")):
            raw_price = f"0{raw_price}"

        try:
            price = float(raw_price.replace(",", "."))
        except ValueError:
            continue

        name = line[:match.start()].strip(" -*#\u00ab\u00bb")
        name = re.sub(r"^\d+\s*[xX*]\s*", "", name).strip()

        if name and price > 0:
            items.append({"name": name, "price": price})

    return {
        "store_name": None,
        "items": items,
        "total": None,
        "item_count": len(items),
    }


# --- API Endpoint ---

@app.post("/process-receipt")
async def process_receipt(request: Request, file: UploadFile = File(...)):
    request_id = getattr(request.state, "request_id", uuid.uuid4().hex[:8])

    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File must be an image")

    logger.info("[%s] OCR upload accepted | filename=%s | content_type=%s", request_id, file.filename, file.content_type)

    read_started_at = time.perf_counter()
    contents = await file.read()
    log_stage(request_id, "read-upload", read_started_at, f"bytes={len(contents)}")

    decode_started_at = time.perf_counter()
    np_arr = np.frombuffer(contents, np.uint8)
    img = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
    log_stage(request_id, "decode-image", decode_started_at)

    if img is None:
        logger.warning("[%s] OpenCV could not decode uploaded image", request_id)
        raise HTTPException(status_code=400, detail="Could not decode image")

    original_height, original_width = img.shape[:2]
    logger.info("[%s] Image received: %sx%s", request_id, original_width, original_height)

    receipt_region_started_at = time.perf_counter()
    receipt_region = find_receipt_region(img)
    receipt_height, receipt_width = receipt_region.shape[:2]
    log_stage(
        request_id,
        "crop-receipt",
        receipt_region_started_at,
        f"output={receipt_width}x{receipt_height}",
    )

    items_region_started_at = time.perf_counter()
    items_region = find_items_region(receipt_region)
    items_height, items_width = items_region.shape[:2]
    log_stage(
        request_id,
        "crop-items",
        items_region_started_at,
        f"output={items_width}x{items_height}",
    )

    preprocess_started_at = time.perf_counter()
    processed = preprocess_image(items_region)
    processed_height, processed_width = processed.shape[:2]
    log_stage(
        request_id,
        "preprocess-image",
        preprocess_started_at,
        f"output={processed_width}x{processed_height}",
    )

    ocr_started_at = time.perf_counter()
    pil_image = Image.fromarray(processed)
    raw_text = ocr_image(pil_image)
    log_stage(request_id, "tesseract-ocr", ocr_started_at, f"chars={len(raw_text)}")
    logger.info("[%s] OCR raw text preview: %s", request_id, raw_text[:300].replace("\n", " | "))

    llm_started_at = time.perf_counter()
    corrected_text = await correct_text_with_llm(raw_text)
    log_stage(request_id, "llm-correction", llm_started_at, f"chars={len(corrected_text)}")
    logger.info("[%s] OCR corrected text preview: %s", request_id, corrected_text[:300].replace("\n", " | "))

    parse_started_at = time.perf_counter()
    parsed = parse_items_only_text(corrected_text)
    log_stage(
        request_id,
        "parse-receipt",
        parse_started_at,
        f"items={len(parsed['items'])}; total={parsed['total']}",
    )
    parsed["raw_text"] = raw_text
    parsed["corrected_text"] = corrected_text

    logger.info("[%s] Parsed %s items with total=%s", request_id, len(parsed["items"]), parsed["total"])
    for item in parsed["items"]:
        logger.info("[%s] Parsed item | %s | %s", request_id, item["name"], item["price"])

    return JSONResponse(content=parsed)


@app.get("/health")
async def health():
    return {
        "status": "ok",
        "ollama_base_url": OLLAMA_BASE_URL,
        "ollama_model": OLLAMA_MODEL,
        "tesseract_configured": Path(TESSERACT_CMD).exists(),
    }
