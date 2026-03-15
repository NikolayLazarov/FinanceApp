using Finance.Models.DTOs;
using FluentValidation;

namespace FinanceAPI.Validators
{
    public class CreateOrUpdateExpenseInputValidator : AbstractValidator<CreateOrUpdateExpenseInput>
    {
        public CreateOrUpdateExpenseInputValidator()
        {
            RuleFor(x => x.Id)
                .GreaterThan(0).When(x => x.Id.HasValue).WithMessage("Id must be greater than 0 when provided.");
            RuleFor(x => x.Amount)
                .NotEmpty().WithMessage("Amount is required.")
                .GreaterThan(0).WithMessage("Amount must be greater than 0.")
                .LessThan(1000000).WithMessage("Amount must be less than 1,000,000.");

            RuleFor(x => x.Title)
                .NotEmpty().WithMessage("Title is required.")
                .MaximumLength(200).WithMessage("Title must be at most 200 characters long.");
            RuleFor(x => x.Date)
                .NotEmpty().WithMessage("Date is required.");

            RuleFor(x => x.Category)
                .IsInEnum().WithMessage("Category must be a valid enum value.");
        }
    }
}
