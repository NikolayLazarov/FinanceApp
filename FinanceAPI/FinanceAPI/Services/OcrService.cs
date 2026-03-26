using System.Net.Http.Headers;
using System.Text.Json;
using Finance.Models.DTOs;
using Serilog;

namespace FinanceAPI.Services
{
    public class OcrService : IOcrService
    {
        private readonly HttpClient _httpClient;
        private static readonly JsonSerializerOptions JsonOptions = new()
        {
            PropertyNamingPolicy = JsonNamingPolicy.SnakeCaseLower
        };

        public OcrService(HttpClient httpClient)
        {
            _httpClient = httpClient;
        }

        public async Task<OcrReceiptResult> ProcessReceiptAsync(Stream imageStream, string fileName, string contentType)
        {
            using var content = new MultipartFormDataContent();
            var streamContent = new StreamContent(imageStream);
            streamContent.Headers.ContentType = new MediaTypeHeaderValue(contentType);
            content.Add(streamContent, "file", fileName);

            var response = await _httpClient.PostAsync("/process-receipt", content);

            if (!response.IsSuccessStatusCode)
            {
                var errorBody = await response.Content.ReadAsStringAsync();
                Log.Error("OCR service returned {StatusCode}: {Error}", response.StatusCode, errorBody);
                throw new HttpRequestException($"OCR service failed with status {response.StatusCode}");
            }

            var json = await response.Content.ReadAsStringAsync();
            var result = JsonSerializer.Deserialize<OcrReceiptResult>(json, JsonOptions);

            return result ?? throw new InvalidOperationException("OCR service returned empty result");
        }
    }
}
