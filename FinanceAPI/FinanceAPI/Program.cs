using FinanceAPI.DependencyInjection;
using FinanceAPI.Exceptions;
using FluentValidation;

namespace FinanceAPI
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            builder.Services
                .AddServices()
                .AddIdentity()
                .AddJWTAuthentication(builder.Configuration)
                .AddAuthorizationPolicies()
                .ConfigureIdentity()
                .InjectDBContext(builder.Configuration)
                .AddValidatorsFromAssembly(typeof(Program).Assembly, includeInternalTypes: true)
                .AddAutoMapper(cfg => { }, AppDomain.CurrentDomain.GetAssemblies())
                .AddSwagger()
                .AddCustomProblemDetails()
                .AddExceptionHandler<GlobalExceptionHandler>()
                .AddSwaggerDocumentation()
                .AddControllers();

            var app = builder.Build();

            app.UseRouting();
            app.AddSwaggerInDev();
            app.UseExceptionHandler();
            app.UseAuthentication();
            app.UseAuthorization();
            app.MapControllers();

            app.Run();
        }
    }
}
