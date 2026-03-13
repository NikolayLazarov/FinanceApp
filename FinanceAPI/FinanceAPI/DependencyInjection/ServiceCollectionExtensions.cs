using Finance.Data.Interfaces;
using Finance.Data.Repository;

namespace FinanceAPI.DependencyInjection
{
    public static class ServiceCollectionExtentions
    {
        public static IServiceCollection AddServices(this IServiceCollection services)
        {
            services.AddScoped<IExpensesRepository, ExpensesRepository>();
            return services;
        }
    }
}
