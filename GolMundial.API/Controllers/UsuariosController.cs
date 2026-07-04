using System.Security.Claims;
using GolMundial.API.Data;
using GolMundial.Modelos;
using GolMundial.Modelos.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace GolMundial.API.Controllers;

[ApiController]
[Route("api/usuarios")]
[Authorize] 
public class UsuariosController : ControllerBase
{
    private readonly AppDbContext _db;
    public UsuariosController(AppDbContext db) => _db = db;

    [HttpGet("me")]
    public async Task<ActionResult<UsuarioDto>> Me()
    {
        var id = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)
                           ?? User.FindFirstValue("sub")!);
        var u = await _db.Usuarios.Include(x => x.Rol).FirstOrDefaultAsync(x => x.Id == id);
        if (u is null) return NotFound();
        return Ok(ToDtoRaw(u));
    }

    [HttpGet]
    [Authorize(Roles = "ADMINISTRADOR")]
    public async Task<ActionResult<IEnumerable<UsuarioDto>>> Listar()
    {
        var lista = await _db.Usuarios.Include(u => u.Rol)
            .OrderBy(u => u.Id)
            .Select(u => ToDtoRaw(u))
            .ToListAsync();
        return Ok(lista);
    }

    [HttpGet("{id:int}")]
    [Authorize(Roles = "ADMINISTRADOR")]
    public async Task<ActionResult<UsuarioDto>> Obtener(int id)
    {
        var u = await _db.Usuarios.Include(x => x.Rol).FirstOrDefaultAsync(x => x.Id == id);
        if (u is null) return NotFound();
        return Ok(ToDtoRaw(u));
    }

    [HttpPut("{id:int}")]
    [Authorize(Roles = "ADMINISTRADOR")]
    public async Task<ActionResult<UsuarioDto>> Actualizar(int id, UpdateUsuarioDto dto)
    {
        var u = await _db.Usuarios.Include(x => x.Rol).FirstOrDefaultAsync(x => x.Id == id);
        if (u is null) return NotFound();

        if (dto.Nombre is not null) u.Nombre = dto.Nombre;
        if (dto.Email is not null) u.Email = dto.Email;
        if (dto.Activo is not null) u.Activo = dto.Activo.Value;

        if (dto.Rol is not null)
        {
            var rol = await _db.Roles.FirstOrDefaultAsync(r => r.Nombre == dto.Rol);
            if (rol is null) return BadRequest(new { mensaje = "Rol inexistente." });
            u.RolId = rol.Id;
            u.Rol = rol;
        }

        await _db.SaveChangesAsync();
        return Ok(ToDtoRaw(u));
    }

    [HttpDelete("{id:int}")]
    [Authorize(Roles = "ADMINISTRADOR")]
    public async Task<IActionResult> Desactivar(int id)
    {
        var u = await _db.Usuarios.FindAsync(id);
        if (u is null) return NotFound();
        u.Activo = false;
        await _db.SaveChangesAsync();
        return NoContent();
    }

    private static UsuarioDto ToDtoRaw(Usuario u) => new()
    {
        Id = u.Id, Username = u.Username, Nombre = u.Nombre,
        Email = u.Email, Rol = u.Rol!.Nombre, Activo = u.Activo
    };
}
