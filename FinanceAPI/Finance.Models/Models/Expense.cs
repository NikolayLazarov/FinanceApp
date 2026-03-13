using Finance.Models.Enums;
using System.ComponentModel.DataAnnotations.Schema;

namespace Finance.Models.Models
{
    [Table("Expenses")]
    public class Expense : FullAuditedEntity
    {
        public Expense()
        {

        }
        public Expense(string title, ExpenseCategory category, decimal amount)
        {
            Title = title;
            Category = category;
            Amount = amount;
        }

        public int Id { get; set; }
        public string Title { get; set; } = null!;
        public ExpenseCategory Category { get; set; }
        public DateTime Date { get; set; }
        public decimal Amount { get; set; }
    }
}
