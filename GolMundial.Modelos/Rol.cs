namespace GolMundial.Modelos;

// Roles del sistema ADMINISTRADOR, USUARIO, INVITADO.
public class Rol
{
    public int Id { get; set; }
    public string Nombre { get; set; } = string.Empty;      
    public string? Descripcion { get; set; }

    public ICollection<Usuario> Usuarios { get; set; } = new List<Usuario>();
}
