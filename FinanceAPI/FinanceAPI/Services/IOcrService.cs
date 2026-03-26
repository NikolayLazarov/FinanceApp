using Finance.Models.DTOs;

namespace FinanceAPI.Services
{
    public interface IOcrService
    {
        Task<OcrReceiptResult> ProcessReceiptAsync(Stream imageStream, string fileName, string contentType);
    }
}
