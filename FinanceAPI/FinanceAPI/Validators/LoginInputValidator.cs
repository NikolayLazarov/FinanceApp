using Finance.Models.DTOs;
using FluentValidation;

namespace FinanceAPI.Validators
{
    public class LoginInputValidator : AbstractValidator<LoginInput>
    {
        public LoginInputValidator()
        {
            RuleFor(x => x.Email)
               .NotEmpty()
               .WithMessage("Email is required.")
               .EmailAddress()
               .WithMessage("Email format is invalid.")
               .MaximumLength(100)
               .WithMessage("Email must not exceed 100 characters.");

            RuleFor(x => x.Password)
                .NotEmpty().WithMessage("Email is required.")
                .MinimumLength(5).WithMessage("Email must be greater than 5 characters long.")
                .MaximumLength(100).WithMessage("Email must be at most 100 characters long.");
        }
    }
}
