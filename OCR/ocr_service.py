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
DEBUG_IMAGES_DIR = BASE_DIR / "debug_images"
DEBUG_IMAGES_DIR.mkdir(exist_ok=True)

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
OCR_MIN_DIMENSION = int(os.getenv("OCR_MIN_DIMENSION", "2400"))
OCR_MAX_DIMENSION = int(os.getenv("OCR_MAX_DIMENSION", "3200"))
LLM_MAX_INPUT_CHARS = int(os.getenv("LLM_MAX_INPUT_CHARS", "3000"))
OCR_LANG = os.getenv("OCR_LANG", "bul+eng")
OCR_PSM_MODES = tuple(
    int(mode.strip())
    for mode in os.getenv("OCR_PSM_MODES", "4,6,11").split(",")
    if mode.strip().isdigit()
) or (4, 6, 11)
OCR_MIN_CONFIDENCE = int(os.getenv("OCR_MIN_CONFIDENCE", "20"))
OCR_USE_LLM = os.getenv("OCR_USE_LLM", "true").lower() == "true"
OCR_LLM_MIN_SCORE = int(os.getenv("OCR_LLM_MIN_SCORE", "4"))
OCR_NOISE_RATIO_THRESHOLD = float(os.getenv("OCR_NOISE_RATIO_THRESHOLD", "0.12"))

ITEM_PRICE_PATTERN = re.compile(
    r"(?P<price>(?:\d+[.,]\d{2,3}|[.,]\d{2,3}))\s*(?:[A-Za-z\u0410-\u044f0-9]{0,4}\.?)?\s*$",
    re.IGNORECASE,
)
DATE_TIME_PATTERN = re.compile(r"\d{2}:\d{2}(?::\d{2})?|\d{2}[-/.]\d{2}[-/.]\d{2,4}")
LEVA_TOTAL_PATTERN = re.compile(r"(лева|лв\.?|bgn|leva)", re.IGNORECASE)
EURO_TOTAL_PATTERN = re.compile(r"(евро|eur|euro|ebpo)", re.IGNORECASE)
NOISY_SYMBOL_PATTERN = re.compile(r"[|¦`~<>\\]")
SUMMARY_REGEX = re.compile(
    r"(обща\s+сума|междинна\s+сума|сума\s+евро|сума\s+в\s+лв|total|subtotal|дължима|ресто|обменен\s+курс|eur|euro|bgn)",
    re.IGNORECASE,
)
NON_ITEM_REGEX = re.compile(
    r"(еик|зддс|ддс|унп|каса|\bбон\b|\bнул\b|ид\.?\s*но|артикул|фискал|фантастико|reserved|оператор|касиер"
    r"|обменен\s*курс|обм\.?\s*курс|кредитна|дебитна|mastercard|contactless|борика|уникред"
    r"|покупка|подпис|благодар|запазет|разписк|справк|rrn#|ед\.?\s*цена)",
    re.IGNORECASE,
)

SUMMARY_MARKERS = (
    "CYMA",
    "SUMA",
    "MEJDIN",
    "MEXDIN",
    "OBWA",
    "OBSHT",
    "TOTAL",
    "SUBTOTAL",
    "DYLJIMA",
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
    "KARTA",
    "KREDIT",
    "DEBIT",
    "POKYPK",
    "POKUPK",
    "BLAGOD",
    "BGN",
)

NON_ITEM_MARKERS = (
    "EIK",
    "ZDDS",
    "DDS",
    "YNP",
    "KACA",
    "IDNO",
    "ARTIKYL",
    "FISKAL",
    "FANTASTIKO",
    "RESERVED",
    "OPERATOR",
    "KASIER",
    "OBMENEN",
    "OBM.KYPC",
    "OBMKYPC",
    "KREDITNA",
    "DEBITNA",
    "MASTERCARD",
    "CONTACTLESS",
    "BORIKA",
    "YNIKREDIT",
    "POKUPKA",
    "PODPIS",
    "BLAGODAR",
    "ZAPAZET",
    "RAZPISK",
    "SPRABK",
    "RRN",
    "MCCONTACTLESS",
    "ED.CENA",
    "EDCENA",
)

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
    kernel = np.ones((2, 2), np.uint8)
    image = cv2.dilate(image, kernel, iterations=1)
    image = cv2.erode(image, kernel, iterations=1)
    image = cv2.morphologyEx(image, cv2.MORPH_CLOSE, kernel)
    return image


def threshold_image(gray_image):
    return cv2.adaptiveThreshold(
        gray_image, 255,
        cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
        cv2.THRESH_BINARY,
        31, 7
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


def contains_marker(text: str, markers: tuple[str, ...]) -> bool:
    normalized = normalize_match_text(text)
    compact = normalized.replace(" ", "")
    return any(marker in normalized or marker.replace(" ", "") in compact for marker in markers)


def split_non_empty_lines(raw_text: str) -> list[str]:
    return [line.strip() for line in raw_text.splitlines() if line.strip()]


def extract_price(text: str) -> tuple[float, int] | None:
    match = ITEM_PRICE_PATTERN.search(text)
    if not match:
        return None

    raw_price = match.group("price")
    if raw_price.startswith((".", ",")):
        raw_price = f"0{raw_price}"

    separator = "." if "." in raw_price else ","
    integer_part, decimal_part = raw_price.split(separator, 1)
    if len(decimal_part) > 2:
        decimal_part = decimal_part[:2]
    raw_price = f"{integer_part}.{decimal_part}"

    try:
        price = float(raw_price.replace(",", "."))
    except ValueError:
        return None

    return price, match.start()


def is_summary_line(text: str) -> bool:
    return SUMMARY_REGEX.search(text) is not None or contains_marker(text, SUMMARY_MARKERS)


def is_non_item_line(text: str) -> bool:
    normalized = normalize_match_text(text)
    if not normalized:
        return True
    if text.startswith("#"):
        return True
    if DATE_TIME_PATTERN.search(text):
        return True
    if re.fullmatch(r"[\W\d_]+", text):
        return True
    if NON_ITEM_REGEX.search(text):
        return True
    if contains_marker(text, NON_ITEM_MARKERS):
        return True
    return normalized.count("*") >= 3 or normalized.count("-") >= 5 or normalized.count("=") >= 3


def looks_like_item_line(text: str) -> bool:
    if is_summary_line(text) or is_non_item_line(text):
        return False
    if not extract_price(text):
        return False
    return any(char.isalpha() for char in text)


def clean_item_name(name: str) -> str:
    cleaned = name.replace("¦", " ").replace("|", " ")
    cleaned = re.sub(r"\s{2,}", " ", cleaned)
    cleaned = re.sub(r"^\d+\s*[xX×*]\s*", "", cleaned).strip()
    cleaned = cleaned.strip(" -*#\"'`.,;:[](){}<>«»")
    return cleaned


def detect_store_name(lines: list[str]) -> str | None:
    for line in lines[:12]:
        if extract_price(line):
            continue
        if re.search(r"(фантастико|reserved)", line, re.IGNORECASE):
            return line
        if is_summary_line(line) or is_non_item_line(line):
            continue
        if sum(char.isalpha() for char in line) >= 4:
            return line
    return None


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


def build_tesseract_config(psm: int) -> str:
    return (
        "--oem 3 "
        f"--psm {psm} "
        f"-l {OCR_LANG} "
        "--dpi 300 "
        "-c preserve_interword_spaces=1"
    )


def build_ocr_lines(image: np.ndarray, psm: int = 6) -> list[dict]:
    data = pytesseract.image_to_data(
        image,
        config=build_tesseract_config(psm),
        output_type=pytesseract.Output.DICT,
    )
    lines_by_key = {}

    for index, word in enumerate(data["text"]):
        word = word.strip()
        if not word:
            continue

        confidence_raw = data["conf"][index]
        try:
            confidence = float(confidence_raw)
        except (TypeError, ValueError):
            confidence = -1.0

        if confidence < OCR_MIN_CONFIDENCE and not re.search(r"\d", word):
            continue

        key = (data["block_num"][index], data["par_num"][index], data["line_num"][index])
        line = lines_by_key.setdefault(key, {
            "words": [],
            "left": 10**9,
            "top": 10**9,
            "right": 0,
            "bottom": 0,
            "confidences": [],
        })

        left = data["left"][index]
        top = data["top"][index]
        width = data["width"][index]
        height = data["height"][index]

        line["words"].append((left, word))
        line["confidences"].append(confidence)
        line["left"] = min(line["left"], left)
        line["top"] = min(line["top"], top)
        line["right"] = max(line["right"], left + width)
        line["bottom"] = max(line["bottom"], top + height)

    lines = []
    for line in lines_by_key.values():
        ordered_words = [word for _, word in sorted(line["words"], key=lambda item: item[0])]
        line["text"] = " ".join(ordered_words)
        line["confidence"] = sum(line["confidences"]) / max(len(line["confidences"]), 1)
        lines.append(line)

    return sorted(lines, key=lambda current: (current["top"], current["left"]))


def find_items_region(receipt_image: np.ndarray) -> np.ndarray:
    height, width = receipt_image.shape[:2]
    ocr_lines = build_ocr_lines(grayscale(receipt_image), psm=6)
    candidates = []
    first_summary_top = None

    for line in ocr_lines:
        text = line["text"].strip()
        if first_summary_top is None and is_summary_line(text):
            first_summary_top = line["top"]
        if is_non_item_line(text) and not is_summary_line(text):
            continue
        has_price = extract_price(text) is not None
        has_alpha = any(char.isalpha() for char in text)
        starts_too_high = line["top"] < height * 0.12
        reaches_price_column = line["right"] > width * 0.60

        if has_price and has_alpha and reaches_price_column and not starts_too_high:
            candidates.append(line)

    if not candidates:
        return receipt_image

    filtered_candidates = []
    for line in candidates:
        if is_summary_line(line["text"]):
            if first_summary_top is None:
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


def preprocess_gray_image(img: np.ndarray) -> np.ndarray:
    img = resize_for_ocr(img)
    deskewed = deskew(img)
    gray_image = grayscale(deskewed)
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    gray_image = clahe.apply(gray_image)
    gray_image = cv2.bilateralFilter(gray_image, 9, 75, 75)
    return sharpen_image(gray_image)


def preprocess_image(img: np.ndarray) -> np.ndarray:
    gray_image = preprocess_gray_image(img)
    bw_image = threshold_image(gray_image)
    return noise_removal(bw_image)


def is_clean_image(gray: np.ndarray) -> bool:
    """Return True when the image is a high-contrast scan/screenshot rather than a photo.

    Clean documents have a strongly bimodal histogram: almost all pixels are
    either near-black (text) or near-white (paper background).  A threshold of
    0.80 means ≥80 % of pixels fall in the darkest or brightest quartile.
    """
    hist = cv2.calcHist([gray], [0], None, [256], [0, 256]).flatten()
    total = float(hist.sum())
    if total == 0:
        return False
    dark_ratio = hist[:64].sum() / total
    bright_ratio = hist[192:].sum() / total
    return (dark_ratio + bright_ratio) > 0.80


def build_ocr_variants(img: np.ndarray) -> dict[str, np.ndarray]:
    raw_gray = grayscale(img) if len(img.shape) == 3 else img.copy()

    if is_clean_image(raw_gray):
        # Lightweight path for scans / screenshots: avoid bilateral blur and CLAHE
        # which smear already-sharp text and introduce ringing artifacts.
        resized = resize_for_ocr(img)
        gray = grayscale(resized) if len(resized.shape) == 3 else resized
        _, otsu = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
        return {
            "gray": gray,
            "adaptive": otsu,
            "otsu": otsu,
        }

    gray_image = preprocess_gray_image(img)
    adaptive = noise_removal(threshold_image(gray_image))
    _, otsu = cv2.threshold(gray_image, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    otsu = noise_removal(otsu)
    return {
        "gray": gray_image,
        "adaptive": adaptive,
        "otsu": otsu,
    }


# --- Tesseract OCR for Bulgarian receipts ---

def score_ocr_lines(lines: list[dict]) -> int:
    score = 0
    item_lines = 0

    for line in lines:
        text = line["text"].strip()
        if not text:
            continue
        if looks_like_item_line(text):
            item_lines += 1
            score += 6
        elif is_summary_line(text):
            score += 1
        elif extract_price(text):
            score += 1
        elif is_non_item_line(text):
            score -= 1

        if NOISY_SYMBOL_PATTERN.search(text):
            score -= 1

    return score + min(item_lines, 6) * 2


def serialize_ocr_lines(lines: list[dict]) -> str:
    return "\n".join(line["text"].strip() for line in lines if line["text"].strip())


def ocr_image(image_variants: dict[str, np.ndarray], request_id: str | None = None) -> tuple[str, int, str]:
    best_text = ""
    best_score = -10**9
    best_source = ""

    for variant_name, image in image_variants.items():
        for psm in OCR_PSM_MODES:
            lines = build_ocr_lines(image, psm=psm)
            text = serialize_ocr_lines(lines)
            score = score_ocr_lines(lines)
            source = f"{variant_name}/psm{psm}"

            if request_id:
                logger.info(
                    "[%s] OCR candidate | source=%s | score=%s | lines=%s | chars=%s",
                    request_id,
                    source,
                    score,
                    len(lines),
                    len(text),
                )

            if score > best_score or (score == best_score and len(text) > len(best_text)):
                best_text = text
                best_score = score
                best_source = source

    if best_text:
        return best_text, best_score, best_source

    fallback_variant = image_variants.get("adaptive") or next(iter(image_variants.values()))
    fallback_psm = OCR_PSM_MODES[0]
    fallback_text = pytesseract.image_to_string(
        Image.fromarray(fallback_variant),
        config=build_tesseract_config(fallback_psm),
    ).strip()
    return fallback_text, 0, f"fallback/psm{fallback_psm}"


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
        "Below is OCR-extracted text from a Bulgarian receipt photo. It may contain item names, "
        "prices, and possibly some header/footer or payment terminal lines.\n"
        "The text may have OCR errors in the Cyrillic characters.\n\n"
        "Rules:\n"
        "- Fix misspelled Bulgarian/Cyrillic words (e.g. wrong letters, merged/split words)\n"
        "- DO NOT change any numbers, prices, or decimal values\n"
        "- DO NOT change Latin characters (brand names like OREO, MARETTI, 7 DAYS, KRAMBALS, RESERVED, etc.)\n"
        "- Keep each line exactly as a separate line\n"
        "- Remove lines starting with '#' (payment terminal / bank card terminal lines)\n"
        "- Remove lines that are clearly not product items: УНП codes, ЗДДС, КАСА, БОН, "
        "timestamps, bank card info (MASTERCARD, БОРИКА, УНИКРЕДИТ, КАРТА NO), "
        "exchange rate lines (ОБМЕНЕН КУРС), thank-you messages (БЛАГОДАРИМ, МОЛЯ ЗАПАЗЕТЕ)\n"
        "- Output ONLY the corrected product item lines with their prices, no explanations\n\n"
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
    lines = split_non_empty_lines(raw_text)

    items = []
    total_leva = None
    total_euro = None
    store_name = detect_store_name(lines)

    for line in lines:
        if is_summary_line(line):
            price_info = extract_price(line)
            if price_info:
                value, _ = price_info
                if LEVA_TOTAL_PATTERN.search(line):
                    if total_leva is None:
                        total_leva = value
                elif EURO_TOTAL_PATTERN.search(line):
                    if total_euro is None:
                        total_euro = value
                elif total_leva is None:
                    total_leva = value
            continue

        if is_non_item_line(line):
            continue

        price_info = extract_price(line)
        if not price_info:
            continue

        price, name_end = price_info
        name = clean_item_name(line[:name_end])
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
    lines = split_non_empty_lines(raw_text)
    items = []

    for line in lines:
        if not looks_like_item_line(line):
            continue

        price_info = extract_price(line)
        if not price_info:
            continue

        price, name_end = price_info
        name = clean_item_name(line[:name_end])
        if name and price > 0:
            items.append({"name": name, "price": price})

    return {
        "store_name": None,
        "items": items,
        "total": None,
        "item_count": len(items),
    }


def estimate_noise_ratio(text: str) -> float:
    stripped = text.strip()
    if not stripped:
        return 1.0
    noisy_chars = sum(1 for char in stripped if NOISY_SYMBOL_PATTERN.search(char))
    return noisy_chars / len(stripped)


def should_run_llm(raw_text: str, parsed: dict, ocr_score: int) -> bool:
    if not OCR_USE_LLM or not raw_text.strip():
        return False
    if parsed["item_count"] > 0 and ocr_score >= OCR_LLM_MIN_SCORE:
        return estimate_noise_ratio(raw_text) >= OCR_NOISE_RATIO_THRESHOLD
    return True


# --- Debug image saving ---

def save_debug_image(image: np.ndarray, name: str) -> None:
    """Save *image* to debug_images/<name>.png, replacing any previous version."""
    path = DEBUG_IMAGES_DIR / f"{name}.png"
    cv2.imwrite(str(path), image)


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
    save_debug_image(receipt_region, "last_receipt_region")
    log_stage(
        request_id,
        "crop-receipt",
        receipt_region_started_at,
        f"output={receipt_width}x{receipt_height}",
    )

    items_region_started_at = time.perf_counter()
    items_region = find_items_region(receipt_region)
    items_height, items_width = items_region.shape[:2]
    save_debug_image(items_region, "last_items_region")
    log_stage(
        request_id,
        "crop-items",
        items_region_started_at,
        f"output={items_width}x{items_height}",
    )

    preprocess_started_at = time.perf_counter()
    item_variants = build_ocr_variants(items_region)
    for variant_name, variant_img in item_variants.items():
        save_debug_image(variant_img, f"last_preprocessed_{variant_name}")
    processed = item_variants["adaptive"]
    processed_height, processed_width = processed.shape[:2]
    log_stage(
        request_id,
        "preprocess-image",
        preprocess_started_at,
        f"output={processed_width}x{processed_height}; variants={','.join(item_variants.keys())}",
    )

    ocr_started_at = time.perf_counter()
    raw_text, raw_score, raw_source = ocr_image(item_variants, request_id=request_id)
    log_stage(request_id, "tesseract-ocr", ocr_started_at, f"chars={len(raw_text)}; score={raw_score}; source={raw_source}")
    logger.info("[%s] OCR raw text preview: %s", request_id, raw_text[:300].replace("\n", " | "))

    parse_started_at = time.perf_counter()
    parsed_raw = parse_items_only_text(raw_text)
    corrected_text = raw_text
    parsed = parsed_raw

    if should_run_llm(raw_text, parsed_raw, raw_score):
        llm_started_at = time.perf_counter()
        llm_text = await correct_text_with_llm(raw_text)
        parsed_llm = parse_items_only_text(llm_text)
        if parsed_llm["item_count"] >= parsed_raw["item_count"]:
            corrected_text = llm_text
            parsed = parsed_llm
        else:
            logger.info(
                "[%s] Keeping raw OCR result because LLM reduced item count from %s to %s",
                request_id,
                parsed_raw["item_count"],
                parsed_llm["item_count"],
            )
        log_stage(request_id, "llm-correction", llm_started_at, f"chars={len(corrected_text)}")
    else:
        logger.info("[%s] Skipping LLM correction because OCR score=%s with %s parsed items", request_id, raw_score, parsed_raw["item_count"])

    logger.info("[%s] OCR corrected text preview: %s", request_id, corrected_text[:300].replace("\n", " | "))

    log_stage(
        request_id,
        "parse-receipt",
        parse_started_at,
        f"items={len(parsed['items'])}; total={parsed['total']}",
    )

    # Fallback: if items-region crop yielded no items, re-run OCR on the full receipt
    if not parsed["items"]:
        logger.info("[%s] Items-region crop yielded 0 items, falling back to full receipt OCR", request_id)
        fallback_started_at = time.perf_counter()
        full_variants = build_ocr_variants(receipt_region)
        for variant_name, variant_img in full_variants.items():
            save_debug_image(variant_img, f"last_preprocessed_{variant_name}")
        full_raw_text, full_score, full_source = ocr_image(full_variants, request_id=request_id)
        full_parsed_raw = parse_receipt_text(full_raw_text)
        full_corrected = full_raw_text
        parsed = full_parsed_raw

        if should_run_llm(full_raw_text, full_parsed_raw, full_score):
            llm_full_text = await correct_text_with_llm(full_raw_text)
            parsed_llm = parse_receipt_text(llm_full_text)
            if parsed_llm["item_count"] >= full_parsed_raw["item_count"]:
                full_corrected = llm_full_text
                parsed = parsed_llm
            else:
                logger.info(
                    "[%s] Keeping raw full-receipt OCR because LLM reduced item count from %s to %s",
                    request_id,
                    full_parsed_raw["item_count"],
                    parsed_llm["item_count"],
                )

        raw_text = full_raw_text
        corrected_text = full_corrected
        log_stage(
            request_id,
            "fallback-full-receipt",
            fallback_started_at,
            f"items={len(parsed['items'])}; total={parsed['total']}; score={full_score}; source={full_source}",
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
