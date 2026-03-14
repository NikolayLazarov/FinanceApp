using FinanceAPI.DependencyInjection;

namespace FinanceAPI
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            builder.Services
                .AddServices()
                .InjectDBContext(builder.Configuration)
                .AddAutoMapper(cfg => { }, AppDomain.CurrentDomain.GetAssemblies())
                .AddOpenApi()
                .AddSwaggerGen()
                .AddOpenApi()
                .AddControllers();

            var app = builder.Build();
            if (app.Environment.IsDevelopment())
            {
                app.UseSwagger();
                app.UseSwaggerUI();
            }

            app.UseHttpsRedirection();
            app.MapOpenApi();

            app.UseAuthorization();

            app.MapControllers();

            app.Run();
        }
    }
}
