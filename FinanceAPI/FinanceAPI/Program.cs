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
                .AddCustomProblemDetails()
                .AddExceptionHandler<GlobalExceptionHandler>()
                .AddControllers();

            var app = builder.Build();

            using (var scope = app.Services.CreateScope())
            {
                var db = scope.ServiceProvider.GetRequiredService<ApplicationDbContext>();
                db.Database.Migrate();
            }

            if (app.Environment.IsDevelopment())
            {
                app.UseSwagger();
                app.UseSwaggerUI();
            }

            app.UseRouting();
            //app.UseHttpsRedirection();
            app.MapOpenApi();
            app.UseExceptionHandler();
            app.UseAuthorization();
            app.MapControllers();

            app.Run();
        }
    }
}
