namespace API.Data.Interfaces
{
    public interface IRepository<T>
    {
        public void Add(T entity);
        public void Delete(T entity);
        public void Update(T entity);
        public Task SaveChangesAsync();
        public IQueryable<T> GetAll();
    }
}
