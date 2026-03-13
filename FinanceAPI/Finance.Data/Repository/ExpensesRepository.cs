using Finance.Data.Data;
using Finance.Data.Interfaces;
using Finance.Models.Models;

namespace Finance.Data.Repository
{
    public class ExpensesRepository : Repository<Expense>, IExpensesRepository
    {
        public ExpensesRepository(ApplicationDbContext dbContext)
            : base(dbContext)
        {
        }

    }
}
