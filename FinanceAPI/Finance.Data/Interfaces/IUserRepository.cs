using API.Data.Interfaces;
using Finance.Models.Models;

namespace Finance.Data.Interfaces
{
    public interface IUserRepository : IRepository<ApplicationUser>
    {
        public Task<ApplicationUser> GetUserByRefreshTokenAsync(string refreshToken);
    }
}
