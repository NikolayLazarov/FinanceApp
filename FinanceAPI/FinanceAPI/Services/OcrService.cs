using System.Net.Http.Headers;
using System.Text.Json;
using Finance.Models.DTOs;

namespace FinanceAPI.Services
{
    public class OcrService : IOcrService
    {
        private readonly HttpClient _httpClient;
        private readonly ILogger<OcrService> _logger;
        private static readonly JsonSerializerOptions JsonOptions = new()
        {
            PropertyNamingPolicy = JsonNamingPolicy.SnakeCaseLower
        };

        public OcrService(HttpClient httpClient, ILogger<OcrService> logger)
        {
            _httpClient = httpClient;
            _logger = logger;
        }

        public async Task<OcrReceiptResult> ProcessReceiptAsync(Stream imageStream, string fileName, string contentType)
        {
            using var content = new MultipartFormDataContent();
            var streamContent = new StreamContent(imageStream);
            streamContent.Headers.ContentType = new MediaTypeHeaderValue(contentType);
            content.Add(streamContent, "file", fileName);

            var startedAt = DateTime.UtcNow;

            _logger.LogInformation(
                "Forwarding receipt to Python OCR service | url={Url} | fileName={FileName} | contentType={ContentType}",
                new Uri(_httpClient.BaseAddress!, "process-receipt"),
                fileName,
                contentType);

            HttpResponseMessage response;
            try
            {
                response = await _httpClient.PostAsync("process-receipt", content);
            }
            catch (TaskCanceledException ex) when (!ex.CancellationToken.IsCancellationRequested)
            {
                var timeoutSeconds = _httpClient.Timeout.TotalSeconds;
                _logger.LogError(ex, "Python OCR service timed out after {TimeoutSeconds} seconds", timeoutSeconds);
                throw new TimeoutException($"OCR service timed out after {timeoutSeconds:0} seconds");
            }
            catch (HttpRequestException ex)
            {
                _logger.LogError(ex, "Could not reach Python OCR service at {BaseAddress}", _httpClient.BaseAddress);
                throw new HttpRequestException($"Could not reach OCR service at {_httpClient.BaseAddress}", ex);
            }

            if (!response.IsSuccessStatusCode)
            {
                var errorBody = await response.Content.ReadAsStringAsync();
                _logger.LogError(
                    "Python OCR service returned {StatusCode} after {ElapsedMs} ms | body={Error}",
                    (int)response.StatusCode,
                    (DateTime.UtcNow - startedAt).TotalMilliseconds,
                    errorBody);
                throw new HttpRequestException($"OCR service failed with status {(int)response.StatusCode}: {errorBody}");
            }

            var json = await response.Content.ReadAsStringAsync();
            _logger.LogInformation(
                "Python OCR service responded successfully in {ElapsedMs} ms",
                (DateTime.UtcNow - startedAt).TotalMilliseconds);

            var result = JsonSerializer.Deserialize<OcrReceiptResult>(json, JsonOptions);

            return result ?? throw new InvalidOperationException("OCR service returned empty result");
        }
    }
}
