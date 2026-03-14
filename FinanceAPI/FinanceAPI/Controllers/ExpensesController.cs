using AutoMapper;
using Finance.Data.Interfaces;
using Finance.Models.DTOs;
using Finance.Models.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Serilog;

namespace FinanceAPI.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class ExpensesController : ControllerBase
    {
        private readonly IExpensesRepository _expensesRepository;
        private readonly IMapper _mapper;

        public ExpensesController(
            IExpensesRepository expenseRepository,
            IMapper mapper)
        {
            _expensesRepository = expenseRepository;
            _mapper = mapper;
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
        public async Task<IActionResult> CreateOrUpdateExpense(CreateOrUpdateExpenseInput input)
        {
            if (input.Id.HasValue)
            {
                return await UpdateExpense(input);
            }
            return await CreateExpense(input);
        }

        [HttpPost("DeleteExpense")]
        public async Task<IActionResult> DeleteExpense(DeleteExpenseInput input)
        {
            var expense = await _expensesRepository.GetByIdAsync(input.Id);

            if (expense is null)
            {
                ProblemDetails problemDetails = new()
                {
                    Type = "Expense Error",
                    Title = "Expense not found",
                    Status = StatusCodes.Status400BadRequest,
                    Detail = $"No expense found with id: {expense?.Id}",
                    Instance = $"{HttpContext.Request.Method} {HttpContext.Request.Path}",
                    Extensions = { ["date"] = DateTime.UtcNow.ToString("R") }

                };
                Log.Error("Bad Request {@problemDetails}", problemDetails);
                return BadRequest(problemDetails);
            }

            expense.DeletionTime = DateTime.Now;
            //expense.DeleterUserId = userId;

            _expensesRepository.Delete(expense);
            await _expensesRepository.SaveChangesAsync();

            return Ok(new { message = "Expense was deleted successfully!" });
        }

        private async Task<IActionResult> CreateExpense(CreateOrUpdateExpenseInput input)
        {
            var newExpense = _mapper.Map<Expense>(input);
            newExpense.CreationDate = DateTime.Now;
            //newExpense.CreatorUserId = userId;

            _expensesRepository.Add(newExpense);
            await _expensesRepository.SaveChangesAsync();

            return Ok(new { message = "Expense was created successfully!" });
        }

        private async Task<IActionResult> UpdateExpense(CreateOrUpdateExpenseInput input)
        {
            var expense = await _expensesRepository.GetByIdAsync(input.Id.Value);

            if (expense is null)
            {
                ProblemDetails problemDetails = new()
                {
                    Type = "Expense Error",
                    Title = "Expense not found",
                    Status = StatusCodes.Status400BadRequest,
                    Detail = $"No expense found with id: {expense?.Id}",
                    Instance = $"{HttpContext.Request.Method} {HttpContext.Request.Path}",
                    Extensions = { ["date"] = DateTime.UtcNow.ToString("R") }

                };
                Log.Error("Bad Request {@problemDetails}", problemDetails);
                return BadRequest(problemDetails);
            }

            var newExpense = _mapper.Map(input, expense);

            newExpense.LastModified = DateTime.Now;
            _expensesRepository.Update(newExpense);
            await _expensesRepository.SaveChangesAsync();

            return Ok(new { message = "Expense was updated successfully!" });
        }
    }
}
