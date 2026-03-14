using Microsoft.OpenApi;
namespace FinanceAPI.DependencyInjection
{
    public static class SwaggerExtensions
    {
        public static IServiceCollection AddSwaggerDocumentation(this IServiceCollection services)
        {
            const string schemeId = "Bearer";

            services.AddEndpointsApiExplorer();

            services.AddSwaggerGen(options =>
            {
                options.SwaggerDoc("v1", new OpenApiInfo
                {
                    Title = "Finance API",
                    Version = "v1"
                });

                options.AddSecurityDefinition(schemeId, new OpenApiSecurityScheme
                {
                    Name = "Authorization",
                    Type = SecuritySchemeType.Http,
                    Scheme = "bearer",
                    BearerFormat = "JWT",
                    In = ParameterLocation.Header,
                    Description = "Paste only your JWT token here"
                });

                options.AddSecurityRequirement(document => new OpenApiSecurityRequirement
                {
                    [new OpenApiSecuritySchemeReference(schemeId, document)] = []
                });
            });

            return services;
        }
        public static IServiceCollection AddSwagger(this IServiceCollection services)
        {
            services.AddEndpointsApiExplorer();
            services.AddSwaggerGen();

            return services;
        }
        public static WebApplication AddSwaggerInDev(this WebApplication app)
        {
            if (app.Environment.IsDevelopment())
            {
                app.UseSwagger();
                app.UseSwaggerUI();
            }
            return app;
        }
    }
}
