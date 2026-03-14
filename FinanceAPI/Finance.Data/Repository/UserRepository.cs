using Finance.Data.Data;
using Finance.Data.Interfaces;
using Finance.Models.Models;
using Microsoft.EntityFrameworkCore;

namespace Finance.Data.Repository
{
    public class UserRepository : Repository<ApplicationUser>, IUserRepository
    {
        public UserRepository(ApplicationDbContext dbContext) : base(dbContext)
        {
        }

        public async Task<ApplicationUser> GetUserByRefreshTokenAsync(string refreshToken)
        {
            return await dbSet.Include(u => u.RefreshTokens)
                .SingleOrDefaultAsync(u => u.RefreshTokens.Any(t => t.Token == refreshToken));
        }
    }
}
