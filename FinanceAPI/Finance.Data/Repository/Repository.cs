using API.Data.Interfaces;
using Finance.Data.Data;
using Microsoft.EntityFrameworkCore;

namespace Finance.Data.Repository
{
    public class Repository<T> : IRepository<T> where T : class
    {
        private readonly ApplicationDbContext dbContext;
        internal DbSet<T> dbSet;
        public Repository(ApplicationDbContext dbContext)
        {
            this.dbContext = dbContext;
        }

        public void Add(T entity)
        {
            //if ()
            //{

            //}
            dbContext.Add(entity);
        }

        public void Delete(T entity)
        {
            dbContext.Remove(entity);
        }

        public IQueryable<T> GetAll()
        {
            return dbSet.AsQueryable();
        }

        public async Task SaveChangesAsync()
        {
            await dbContext.SaveChangesAsync();
        }

        public void Update(T entity)
        {
            dbSet.Update(entity);
        }
    }
}
