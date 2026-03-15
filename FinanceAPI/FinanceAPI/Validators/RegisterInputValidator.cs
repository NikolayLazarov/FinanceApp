using Finance.Models.DTOs;
using FluentValidation;

namespace FinanceAPI.Validators
{
    public class RegisterInputValidator : AbstractValidator<RegisterInput>
    {
        public RegisterInputValidator()
        {

            RuleFor(x => x.FirstName)
                .NotEmpty()
                .WithMessage("First name is required.")
                .MaximumLength(50)
                .WithMessage("First name must not exceed 50 characters.");

            RuleFor(x => x.LastName)
                .NotEmpty()
                .WithMessage("Last name is required.")
                .MaximumLength(50)
                .WithMessage("Last name must not exceed 50 characters.");

            RuleFor(x => x.Age)
                .InclusiveBetween(13, 120)
                .WithMessage("Age must be between 13 and 120.");

            RuleFor(x => x.Gender)
                .IsInEnum()
                .WithMessage("Gender is invalid.");

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
