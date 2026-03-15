using AutoMapper;
using Finance.Data.Interfaces;
using Finance.Models.DTOs;
using Finance.Models.Models;
using FluentValidation;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Serilog;

namespace FinanceAPI.Controllers
{
    [ApiController]
    [Route("[controller]")]
    [Authorize(Policy = "UserOnly")]
    public class ExpensesController : ControllerBase
    {
        private readonly IExpensesRepository _expensesRepository;
        private readonly IMapper _mapper;
        private readonly UserManager<ApplicationUser> _userManager;
        private readonly IValidator<CreateOrUpdateExpenseInput> _createOrUpdateExpenseInputValidator;
        private readonly IValidator<DeleteExpenseInput> _deleteExpenseInputValidator;
        public ExpensesController(
            IExpensesRepository expenseRepository,
            IMapper mapper,
            UserManager<ApplicationUser> userManager,
            IValidator<CreateOrUpdateExpenseInput> createOrUpdateExpenseInputValidator,
            IValidator<DeleteExpenseInput> deleteExpenseInputValidator)
        {
            _expensesRepository = expenseRepository;
            _mapper = mapper;
            _userManager = userManager;
            _createOrUpdateExpenseInputValidator = createOrUpdateExpenseInputValidator;
            _deleteExpenseInputValidator = deleteExpenseInputValidator;
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
            var validationResult = _createOrUpdateExpenseInputValidator.Validate(input);

            if (!validationResult.IsValid)
            {
                return BadRequest(new HttpValidationProblemDetails(validationResult.ToDictionary()));
            }

            if (input.Id.HasValue)
            {
                return await UpdateExpense(input);
            }
            return await CreateExpense(input);
        }

        [HttpPost("DeleteExpense")]
        public async Task<IActionResult> DeleteExpense(DeleteExpenseInput input)
        {
            var validationResult = _deleteExpenseInputValidator.Validate(input);

            if (!validationResult.IsValid)
            {
                return BadRequest(new HttpValidationProblemDetails(validationResult.ToDictionary()));
            }

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

            var user = await _userManager.GetUserAsync(User);

            expense.DeletionTime = DateTime.UtcNow;
            expense.DeleterUserId = user.Id;

            _expensesRepository.Delete(expense);
            await _expensesRepository.SaveChangesAsync();

            return Ok(new { message = "Expense was deleted successfully!" });
        }

        private async Task<IActionResult> CreateExpense(CreateOrUpdateExpenseInput input)
        {
            var newExpense = _mapper.Map<Expense>(input);

            var user = await _userManager.GetUserAsync(User);

            newExpense.CreationDate = DateTime.UtcNow;
            newExpense.CreatorUserId = user.Id;

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

            newExpense.LastModified = DateTime.UtcNow;
            _expensesRepository.Update(newExpense);
            await _expensesRepository.SaveChangesAsync();

            return Ok(new { message = "Expense was updated successfully!" });
        }
    }
}
