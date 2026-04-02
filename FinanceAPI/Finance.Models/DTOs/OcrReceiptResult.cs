namespace Finance.Models.DTOs
{
    public class OcrReceiptResult
    {
        public string? StoreName { get; set; }
        public List<OcrReceiptItem> Items { get; set; } = [];
        public decimal? Total { get; set; }
        public int ItemCount { get; set; }
        public string? RawText { get; set; }
        public string? CorrectedText { get; set; }
    }

    public class OcrReceiptItem
    {
        public string Name { get; set; } = string.Empty;
        public decimal Price { get; set; }
    }
}
