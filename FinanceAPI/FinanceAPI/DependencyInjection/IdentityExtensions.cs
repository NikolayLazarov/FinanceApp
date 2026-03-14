using Finance.Data.Data;
using Finance.Models.Models;
using Microsoft.AspNetCore.Identity;

namespace FinanceAPI.DependencyInjection
{
    public static class IdentityExtensions
    {
        public static IServiceCollection ConfigureIdentity(this IServiceCollection services)
        {
            services.Configure<IdentityOptions>(options =>
            {
                options.Lockout.AllowedForNewUsers = false;
                options.Lockout.MaxFailedAccessAttempts = int.MaxValue;
                options.Lockout.DefaultLockoutTimeSpan = TimeSpan.Zero;
            });
            return services;
        }
        public static IServiceCollection AddIdentity(this IServiceCollection services)
        {
            services.AddIdentity<ApplicationUser, IdentityRole>()
                .AddEntityFrameworkStores<ApplicationDbContext>();
            return services;
        }
    }
}
