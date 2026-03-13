using Finance.Data.Interfaces;
using Finance.Models.DTOs;
using Finance.Models.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace FinanceAPI.Controllers
{
    [ApiController]
    [Route("[controller]")]
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

        [HttpPost("CreateOrUpdateExpense")]
        public async Task CreateOrUpdateExpense(CreateOrUpdateExpenseInput input)
        {
            if (input.Id.HasValue)
            {
                await UpdateExpense(input);
                return;
            }
            await CreateExpense(input);
        }

        [HttpPost("DeleteExpense")]
        public async Task DeleteExpense(DeleteExpenseInput input)
        {
            var expense = await _expensesRepository.GetByIdAsync(input.Id);

            if (expense is null)
            {
                throw new Exception("Expense is not found");
                //TODO : ADD LOGGING 
            }

            _expensesRepository.Delete(expense);
            await _expensesRepository.SaveChangesAsync();
        }

        private async Task CreateExpense(CreateOrUpdateExpenseInput input)
        {
            throw new NotImplementedException();
        }

        private async Task UpdateExpense(CreateOrUpdateExpenseInput input)
        {
            // add mapper and update logic here
            throw new NotImplementedException();
        }
    }
}
