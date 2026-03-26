using FinanceAPI.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Serilog;

namespace FinanceAPI.Controllers
{
    [ApiController]
    [Route("[controller]")]
    [Authorize(Policy = "UserOnly")]
    public class OcrController : ControllerBase
    {
        private readonly IOcrService _ocrService;

        public OcrController(IOcrService ocrService)
        {
            _ocrService = ocrService;
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

            using var stream = file.OpenReadStream();
            var result = await _ocrService.ProcessReceiptAsync(stream, file.FileName, file.ContentType);

            return Ok(result);
        }
    }
}
