using Finance.Data.Data;
using Finance.Data.Interfaces;
using Finance.Models.Models;
using Microsoft.EntityFrameworkCore;

namespace Finance.Data.Repository
{
    public class ExpensesRepository : Repository<Expense>, IExpensesRepository
    {
        public ExpensesRepository(ApplicationDbContext dbContext)
            : base(dbContext)
        {
        }

        public async Task<Expense> GetByIdAsync(int id)
        {
            return await GetAll().FirstOrDefaultAsync(x => x.Id == id);
        }

    }
}
