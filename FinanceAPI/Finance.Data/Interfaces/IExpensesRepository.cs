using API.Data.Interfaces;
using Finance.Models.Models;

namespace Finance.Data.Interfaces
{
    public interface IExpensesRepository : IRepository<Expense>
    {
        public Task<Expense> GetByIdAsync(int id);
    }
}
