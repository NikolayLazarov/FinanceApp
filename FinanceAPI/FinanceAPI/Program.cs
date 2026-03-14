using FinanceAPI.DependencyInjection;
using FinanceAPI.Exceptions;

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
                .AddAutoMapper(cfg => { }, AppDomain.CurrentDomain.GetAssemblies())
                .AddSwagger()
                .AddCustomProblemDetails()
                .AddExceptionHandler<GlobalExceptionHandler>()
                .AddSwaggerDocumentation()
                .AddControllers();

            var app = builder.Build();

            if (app.Environment.IsDevelopment())
            {
                app.UseSwagger();
                app.UseSwaggerUI();
            }

            app.UseRouting();
            //app.UseHttpsRedirection();
            app.UseExceptionHandler();
            app.UseAuthentication();
            app.UseAuthorization();
            app.MapControllers();

            app.Run();
        }
    }
}
