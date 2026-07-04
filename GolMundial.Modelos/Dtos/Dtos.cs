using System.ComponentModel.DataAnnotations;

namespace GolMundial.Modelos.Dtos;

// Lo que envía el frontend al registrarse.
public class RegisterDto
{
    [Required, MinLength(3), MaxLength(50)]
    public string Username { get; set; } = string.Empty;

    [Required, MaxLength(120)]
    public string Nombre { get; set; } = string.Empty;

    [EmailAddress]
    public string? Email { get; set; }

    [Required, MinLength(6)]
    public string Password { get; set; } = string.Empty;
}

// Login.
public class LoginDto
{
    [Required] public string Username { get; set; } = string.Empty;
    [Required] public string Password { get; set; } = string.Empty;
}

// Respuesta loginel token que el frontend guardará y enviará en cada request.
public class AuthResponseDto
{
    public string Token { get; set; } = string.Empty;
    public UsuarioDto Usuario { get; set; } = new();
}
public class UsuarioDto
{
    public int Id { get; set; }
    public string Username { get; set; } = string.Empty;
    public string Nombre { get; set; } = string.Empty;
    public string? Email { get; set; }
    public string Rol { get; set; } = string.Empty;
    public bool Activo { get; set; }
}

public class UpdateUsuarioDto
{
    [MaxLength(120)] public string? Nombre { get; set; }
    [EmailAddress] public string? Email { get; set; }
    public string? Rol { get; set; }          // ADMINISTRADOR / USUARIO / INVITADO
    public bool? Activo { get; set; }
}
