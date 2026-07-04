using GolMundial.Modelos;
using Microsoft.EntityFrameworkCore;

namespace GolMundial.API.Data;

// EF Core crea/actualiza las tablas de PostgreSQL a partir de estas entidades
// (dotnet ef migrations add ... / dotnet ef database update).
public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

    public DbSet<Usuario> Usuarios => Set<Usuario>();
    public DbSet<Rol> Roles => Set<Rol>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Usuario>(e =>
        {
            e.ToTable("usuarios");
            e.HasIndex(u => u.Username).IsUnique();
            e.HasIndex(u => u.Email).IsUnique();
            e.Property(u => u.Username).HasMaxLength(50).IsRequired();
            e.Property(u => u.Nombre).HasMaxLength(120).IsRequired();
            e.Property(u => u.PasswordHash).HasMaxLength(255).IsRequired();
            e.HasOne(u => u.Rol).WithMany(r => r.Usuarios).HasForeignKey(u => u.RolId);
        });

        modelBuilder.Entity<Rol>(e =>
        {
            e.ToTable("roles");
            e.HasIndex(r => r.Nombre).IsUnique();
            e.Property(r => r.Nombre).HasMaxLength(20).IsRequired();
        });

        // Datos iniciales (coinciden con el seed del docente).
        modelBuilder.Entity<Rol>().HasData(
            new Rol { Id = 1, Nombre = "ADMINISTRADOR", Descripcion = "Gestiona torneo, resultados, cuotas y usuarios" },
            new Rol { Id = 2, Nombre = "USUARIO", Descripcion = "Consulta y realiza predicciones" },
            new Rol { Id = 3, Nombre = "INVITADO", Descripcion = "Solo consulta pública" }
        );

        // Usuario administrador inicial. Contraseña por defecto: Admin123!
        // (cámbiala en el primer uso; el hash BCrypt está pre-generado).
        modelBuilder.Entity<Usuario>().HasData(new Usuario
        {
            Id = 1,
            Username = "admin",
            Nombre = "Administrador del Torneo",
            Email = "admin@golmundial.utn",
            RolId = 1,
            Activo = true,
            PasswordHash = "$2b$11$paNaMjffGpZ74ut9JXKdrOuWfoyAmoavu5FtoWIgvRVgHZVnVDCOG", // Admin123!
            FechaRegistro = new DateTime(2026, 1, 1, 0, 0, 0, DateTimeKind.Utc)
        });
    }
}
