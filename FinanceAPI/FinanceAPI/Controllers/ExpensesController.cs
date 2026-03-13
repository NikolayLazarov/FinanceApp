using Finance.Data.Interfaces;
using Finance.Models.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace FinanceAPI.Controllers
{
    public class ExpensesController : ControllerBase
    {
        private readonly IExpensesRepository _expensesRepository;

        public ExpensesController(IExpensesRepository expenseRepository)
        {
            _expensesRepository = expenseRepository;
        }

        [HttpGet("GetExpenses")]
        public async Task<IEnumerable<Expense>> GetExpenses()
        {
            return await _expensesRepository
                .GetAll()
                .AsNoTracking()
                .ToListAsync();
        }

        public void CreateExpense()
        {

        }
    }
}
