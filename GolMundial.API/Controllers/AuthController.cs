using GolMundial.API.Data;
using GolMundial.Modelos;
using GolMundial.Modelos.Dtos;
using GolMundial.API.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace GolMundial.API.Controllers;

[ApiController]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    private readonly AppDbContext _db;
    private readonly JwtService _jwt;

    public AuthController(AppDbContext db, JwtService jwt)
    {
        _db = db;
        _jwt = jwt;
    }

    [HttpPost("register")]
    public async Task<ActionResult<UsuarioDto>> Register(RegisterDto dto)
    {
        if (await _db.Usuarios.AnyAsync(u => u.Username == dto.Username))
            return Conflict(new { mensaje = "El nombre de usuario ya existe." });

        var rolUsuario = await _db.Roles.FirstAsync(r => r.Nombre == "USUARIO");

        var usuario = new Usuario
        {
            Username = dto.Username,
            Nombre = dto.Nombre,
            Email = dto.Email,
            RolId = rolUsuario.Id,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password),
            Activo = true
        };

        _db.Usuarios.Add(usuario);
        await _db.SaveChangesAsync();

        return CreatedAtAction(nameof(Register), MapToDto(usuario, rolUsuario.Nombre));
    }

    [HttpPost("login")]
    public async Task<ActionResult<AuthResponseDto>> Login(LoginDto dto)
    {
        var usuario = await _db.Usuarios
            .Include(u => u.Rol)
            .FirstOrDefaultAsync(u => u.Username == dto.Username);

        if (usuario is null || !usuario.Activo ||
            !BCrypt.Net.BCrypt.Verify(dto.Password, usuario.PasswordHash))
            return Unauthorized(new { mensaje = "Credenciales incorrectas." });

        return Ok(new AuthResponseDto
        {
            Token = _jwt.GenerarToken(usuario),
            Usuario = MapToDto(usuario, usuario.Rol!.Nombre)
        });
    }

    private static UsuarioDto MapToDto(Usuario u, string rol) => new()
    {
        Id = u.Id,
        Username = u.Username,
        Nombre = u.Nombre,
        Email = u.Email,
        Rol = rol,
        Activo = u.Activo
    };
}
