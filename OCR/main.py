import os

import uvicorn


def main() -> None:
    host = os.getenv("OCR_HOST", "0.0.0.0")
    port = int(os.getenv("OCR_PORT", "8000"))
    reload = os.getenv("OCR_RELOAD", "false").lower() == "true"
    log_level = os.getenv("OCR_LOG_LEVEL", "info")

    uvicorn.run(
        "ocr_service:app",
        host=host,
        port=port,
        reload=reload,
        log_level=log_level,
    )


if __name__ == "__main__":
    main()
