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

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

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

    private boolean validarNuevaSede() {

        FacesContext contexto = FacesContext.getCurrentInstance();

        if (nuevaSede == null) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Datos inválidos",
                            "No se recibieron los datos de la sede."
                    ));

            contexto.validationFailed();
            return false;
        }

        String nombre = nuevaSede.getNombre();
        String ciudad = nuevaSede.getCiudad();
        String pais = nuevaSede.getPais();
        Integer capacidad = nuevaSede.getCapacidadAprox();

        if (nombre == null || nombre.isBlank()) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Nombre obligatorio",
                            "Ingrese el nombre de la sede."
                    ));

            contexto.validationFailed();
            return false;
        }

        final String nombreNormalizado = nombre.trim();

        if (nombreNormalizado.length() < 3) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Nombre inválido",
                            "El nombre de la sede debe tener al menos 3 caracteres."
                    ));

            contexto.validationFailed();
            return false;
        }

        if (ciudad == null || ciudad.isBlank()) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Ciudad obligatoria",
                            "Ingrese la ciudad de la sede."
                    ));

            contexto.validationFailed();
            return false;
        }

        final String ciudadNormalizada = ciudad.trim();

        if (pais == null || pais.isBlank()) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "País obligatorio",
                            "Ingrese el país de la sede."
                    ));

            contexto.validationFailed();
            return false;
        }

        final String paisNormalizado = pais.trim();

        if (capacidad == null) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Capacidad obligatoria",
                            "Ingrese la capacidad aproximada."
                    ));

            contexto.validationFailed();
            return false;
        }

        if (capacidad <= 0) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Capacidad inválida",
                            "La capacidad debe ser mayor que cero."
                    ));

            contexto.validationFailed();
            return false;
        }

        boolean sedeDuplicada = sedes.stream()
                .anyMatch(sede ->
                        sede.getNombre() != null
                                && sede.getCiudad() != null
                                && sede.getNombre().trim()
                                .equalsIgnoreCase(nombreNormalizado)
                                && sede.getCiudad().trim()
                                .equalsIgnoreCase(ciudadNormalizada)
                );

        if (sedeDuplicada) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Sede duplicada",
                            "Ya existe una sede con ese nombre en la misma ciudad."
                    ));

            contexto.validationFailed();
            return false;
        }

        nuevaSede.setNombre(nombreNormalizado);
        nuevaSede.setCiudad(ciudadNormalizada);
        nuevaSede.setPais(paisNormalizado);

        return true;
    }
    
    // Método para guardar una nueva sede mediante la API (POST)
    public void guardarSede() {

        if (!validarNuevaSede()) {
            return;
        }

        FacesContext contexto = FacesContext.getCurrentInstance();

        try (Client cliente = ClientBuilder.newClient();
            Response respuesta = cliente
                    .target(API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .post(
                            Entity.entity(
                                    nuevaSede,
                                    MediaType.APPLICATION_JSON
                            )
                    )) {

            if (respuesta.getStatus()
                    == Response.Status.CREATED.getStatusCode()
                    || respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                cargarSedesDesdeApi();

                nuevaSede = new SedeDTO();

                contexto.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_INFO,
                                "Éxito",
                                "Sede registrada correctamente."
                        ));

                return;
            }

            String detalle = "El backend rechazó el registro de la sede.";

            if (respuesta.hasEntity()) {
                detalle = respuesta.readEntity(String.class);
            }

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "No se pudo registrar",
                            detalle
                    ));

            contexto.validationFailed();

        } catch (Exception e) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Error de comunicación",
                            "No fue posible conectarse con la API de sedes."
                    ));

            contexto.validationFailed();
            e.printStackTrace();
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
