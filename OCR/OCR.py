import pytesseract
from PIL import Image
pytesseract.pytesseract.tesseract_cmd = r"C:\Program Files\Tesseract-OCR\tesseract.exe"

not_ProcessedImage = "data/img1.png"
processedImage = "temp/rotated_fixed.jpg"

img = Image.open(processedImage)

result = pytesseract.image_to_string(img, )

print(result)