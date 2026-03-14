using Finance.Models.StaticDependencies;

namespace FinanceAPI.DependencyInjection
{
    public static class AuthorizationExtensions
    {
        public static IServiceCollection AddAuthorizationPolicies(this IServiceCollection services)
        {
            services.AddAuthorization(o =>
            {
                o.AddPolicy("UserOnly", p => p.RequireRole(UserRole.User));
                o.AddPolicy("AdminOnly", p => p.RequireRole(UserRole.Admin));
            });
            return services;
        }
    }
}
