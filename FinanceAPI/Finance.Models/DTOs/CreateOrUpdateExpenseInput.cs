using Finance.Models.Enums;

namespace Finance.Models.DTOs
{
    public class CreateOrUpdateExpenseInput
    {
        public int? Id { get; set; }
        public string Title { get; set; }
        public ExpenseCategory Category { get; set; }
        public DateTime Date { get; set; }
        public decimal Amount { get; set; }
    }
}
