using System.Text;

namespace GolMundial.Consumer;

/// <summary>
/// Cliente HTTP genérico para consumir cualquier endpoint REST de la API.
/// Uso: CRUD&lt;UsuarioDto&gt;.Endpoint = "https://localhost:7XXX/api/usuarios";
/// </summary>
public static class CRUD<T>
{
    public static string Endpoint { get; set; } = string.Empty;

    // Token JWT para endpoints protegidos. Asignarlo después del login.
    public static string? BearerToken { get; set; }

    // ------------------------------------------------------------------ //
    //  Helpers privados
    // ------------------------------------------------------------------ //

    private static HttpClient BuildClient()
    {
        var client = new HttpClient();
        client.DefaultRequestHeaders.Accept.Add(
            new System.Net.Http.Headers.MediaTypeWithQualityHeaderValue("application/json"));

        if (!string.IsNullOrWhiteSpace(BearerToken))
            client.DefaultRequestHeaders.Authorization =
                new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", BearerToken);

        return client;
    }

    private static StringContent ToJson(T data) =>
        new(Newtonsoft.Json.JsonConvert.SerializeObject(data), Encoding.UTF8, "application/json");

    // ------------------------------------------------------------------ //
    //  CRUD
    // ------------------------------------------------------------------ //

    /// <summary>POST {Endpoint} — crea un recurso y devuelve el objeto creado.</summary>
    public static async Task<T?> Create(T data)
    {
        using var client = BuildClient();
        var response = await client.PostAsync(Endpoint, ToJson(data));
        response.EnsureSuccessStatusCode();
        var json = await response.Content.ReadAsStringAsync();
        return Newtonsoft.Json.JsonConvert.DeserializeObject<T>(json);
    }

    /// <summary>GET {Endpoint}/{id} — obtiene un recurso por su identificador.</summary>
    public static async Task<T?> ReadById(string id)
    {
        using var client = BuildClient();
        var response = await client.GetAsync($"{Endpoint}/{id}");
        response.EnsureSuccessStatusCode();
        var json = await response.Content.ReadAsStringAsync();
        return Newtonsoft.Json.JsonConvert.DeserializeObject<T>(json);
    }

    /// <summary>GET {Endpoint} — devuelve la lista completa de recursos.</summary>
    public static async Task<List<T>?> ReadAll()
    {
        using var client = BuildClient();
        var response = await client.GetAsync(Endpoint);
        response.EnsureSuccessStatusCode();
        var json = await response.Content.ReadAsStringAsync();
        return Newtonsoft.Json.JsonConvert.DeserializeObject<List<T>>(json);
    }

    /// <summary>PUT {Endpoint}/{id} — actualiza un recurso existente.</summary>
    public static async Task<bool> Update(string id, T data)
    {
        using var client = BuildClient();
        var response = await client.PutAsync($"{Endpoint}/{id}", ToJson(data));
        return response.IsSuccessStatusCode;
    }

    /// <summary>DELETE {Endpoint}/{id} — elimina (o desactiva) un recurso.</summary>
    public static async Task<bool> Delete(string id)
    {
        using var client = BuildClient();
        var response = await client.DeleteAsync($"{Endpoint}/{id}");
        return response.IsSuccessStatusCode;
    }
}
