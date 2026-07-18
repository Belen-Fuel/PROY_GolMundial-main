package ec.edu.utn.golmundial.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ec.edu.utn.golmundial.dto.SedeDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;

@Named("sedeBean")
@ViewScoped // Se crea y destruye en cada petición HTTP
public class SedeBean implements Serializable {

    private List<SedeDTO> sedes = new ArrayList<>();
    
    // Objeto para registrar una nueva sede desde el formulario
    private SedeDTO nuevaSede = new SedeDTO();

    // URL local de la API REST de tu backend
    private static final String API_URL = "http://localhost:8080/golmundial-estadisticas/api/sedes";

    @PostConstruct
    public void init() {
        // Llamamos al método correcto para cargar los datos desde la API
        cargarSedesDesdeApi();
        if (this.sedes == null) {
            this.sedes = new ArrayList<>();
        }
    }

    private void cargarSedesDesdeApi() {
        Client cliente = null;
        try {
            cliente = ClientBuilder.newClient();
            // Consumimos el endpoint GET /api/sedes del backend mediante HTTP REST
            this.sedes = cliente.target(API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<SedeDTO>>() {});
        } catch (Exception e) {
            System.err.println("Error al conectar con la API REST de sedes: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cliente != null) {
                cliente.close();
            }
        }
    }
    
    // Método para guardar una nueva sede mediante la API (POST)
    public void guardarSede() {
        Client cliente = null;
        try {
            cliente = ClientBuilder.newClient();
            // Enviamos la nueva sede usando un POST por HTTP REST
            jakarta.ws.rs.core.Response respuesta = cliente.target(API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .post(jakarta.ws.rs.client.Entity.entity(nuevaSede, MediaType.APPLICATION_JSON));
            
            if (respuesta.getStatus() == 201 || respuesta.getStatus() == 200) {
                // Si se guardó con éxito, recargamos la lista y limpiamos el objeto
                cargarSedesDesdeApi();
                this.nuevaSede = new SedeDTO();
                // Añadimos mensaje de éxito para el p:growl
                jakarta.faces.context.FacesContext.getCurrentInstance().addMessage(null, 
                    new jakarta.faces.application.FacesMessage(
                        jakarta.faces.application.FacesMessage.SEVERITY_INFO, 
                        "¡Éxito!", "Sede registrada correctamente."));
            } else {
                System.err.println("Error en la API al guardar. Status: " + respuesta.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error al conectar con la API para guardar: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cliente != null) {
                cliente.close();
            }
        }
    }

    // GETTERS Y SETTERS necesarios para JSF
    public List<SedeDTO> getSedes() {
        return sedes;
    }

    public void setSedes(List<SedeDTO> sedes) {
        this.sedes = sedes;
    }

    public SedeDTO getNuevaSede() {
        return nuevaSede;
    }

    public void setNuevaSede(SedeDTO nuevaSede) {
        this.nuevaSede = nuevaSede;
    }
}
