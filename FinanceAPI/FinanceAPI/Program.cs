using FinanceAPI.DependencyInjection;

namespace FinanceAPI
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            builder.Services
                .InjectDBContext(builder.Configuration)
                .AddServices()
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
