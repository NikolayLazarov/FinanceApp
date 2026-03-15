using Finance.Models.DTOs;
using FluentValidation;

namespace FinanceAPI.Validators
{
    public class DeleteExpenseInputValidator : AbstractValidator<DeleteExpenseInput>
    {
        public DeleteExpenseInputValidator()
        {
            RuleFor(x => x.Id)
                .NotEmpty().WithMessage("Id is required.")
                .GreaterThan(0).WithMessage("Id must be greater than 0.");
        }
    }
}
