using Finance.Data.Interfaces;
using Finance.Data.Repository;

namespace FinanceAPI.DependencyInjection
{
    public static class ServiceCollectionExtentions
    {
        public static IServiceCollection AddServices(this IServiceCollection services)
        {
            services.AddScoped<IExpensesRepository, ExpensesRepository>();
            services.AddScoped<IUserRepository, UserRepository>();
            return services;
        }

        public static IServiceCollection AddCustomProblemDetails(this IServiceCollection services)
        {
            services.AddProblemDetails(options =>
            {
                options.CustomizeProblemDetails = context =>
                {
                    context.ProblemDetails.Instance = $"{context.HttpContext.Request.Method} {context.HttpContext.Request.Path}";
                    context.ProblemDetails.Extensions.Add("requestId", context.HttpContext.TraceIdentifier);
                    context.ProblemDetails.Extensions.Add("date", DateTime.UtcNow.ToString("R"));
                };
            });
            return services;
        }
    }
}
