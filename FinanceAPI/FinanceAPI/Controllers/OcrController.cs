using FinanceAPI.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace FinanceAPI.Controllers
{
    [ApiController]
    [Route("[controller]")]
    [Authorize(Policy = "UserOnly")]
    public class OcrController : ControllerBase
    {
        private readonly IOcrService _ocrService;
        private readonly ILogger<OcrController> _logger;

        public OcrController(IOcrService ocrService, ILogger<OcrController> logger)
        {
            _ocrService = ocrService;
            _logger = logger;
        }

        [HttpPost("ScanReceipt")]
        public async Task<IActionResult> ScanReceipt(IFormFile file)
        {
            if (file == null || file.Length == 0)
                return BadRequest(new ProblemDetails
                {
                    Title = "No file provided",
                    Detail = "Please upload a receipt image",
                    Status = StatusCodes.Status400BadRequest
                });

            if (!file.ContentType.StartsWith("image/"))
                return BadRequest(new ProblemDetails
                {
                    Title = "Invalid file type",
                    Detail = "Only image files are accepted",
                    Status = StatusCodes.Status400BadRequest
                });

            _logger.LogInformation(
                "Received OCR request | fileName={FileName} | contentType={ContentType} | size={Size}",
                file.FileName,
                file.ContentType,
                file.Length);

            using var stream = file.OpenReadStream();
            var result = await _ocrService.ProcessReceiptAsync(stream, file.FileName, file.ContentType);

            _logger.LogInformation(
                "OCR request completed | fileName={FileName} | items={ItemCount} | total={Total}",
                file.FileName,
                result.ItemCount,
                result.Total);

            return Ok(result);
        }
    }
}
