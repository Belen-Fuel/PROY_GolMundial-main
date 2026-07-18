package ec.edu.utn.golmundial.service;

import ec.edu.utn.golmundial.dto.ActualizarSedeRequest;
import ec.edu.utn.golmundial.dto.CrearSedeRequest;
import ec.edu.utn.golmundial.dto.SedeDTO;
import ec.edu.utn.golmundial.entity.Auditoria;
import ec.edu.utn.golmundial.entity.Sede;
import ec.edu.utn.golmundial.exception.ReglaNegocioException;
import ec.edu.utn.golmundial.exception.SedeDuplicadaException;
import ec.edu.utn.golmundial.exception.SedeNoEncontradaException;
import ec.edu.utn.golmundial.exception.SolicitudInvalidaException;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Gestiona la creación, actualización y eliminación
 * de las sedes del torneo.
 */
@Stateless
public class AdministracionSedeService {

    @PersistenceContext(unitName = "GolMundialPU")
    private EntityManager entityManager;

    /**
     * Crea una nueva sede.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public SedeDTO crear(
            CrearSedeRequest solicitud,
            String administrador
    ) {

        validarSolicitudCrear(solicitud);
        validarAdministrador(administrador);

        String nombre =
                solicitud.getNombre().trim();

        verificarNombreDisponible(
                nombre,
                null
        );

        Long nuevoId =
                obtenerSiguienteId();

        Sede sede = new Sede(
                nuevoId,
                nombre,
                solicitud.getCiudad().trim(),
                solicitud.getPais().trim(),
                solicitud.getCapacidadAprox()
        );

        entityManager.persist(sede);
        entityManager.flush();

        registrarAuditoria(
                "CREAR_SEDE",
                sede.getId(),
                administrador,
                "Se creó la sede "
                        + sede.getNombre()
                        + " en "
                        + sede.getCiudad()
                        + ", "
                        + sede.getPais()
        );

        entityManager.flush();

        return convertir(sede);
    }

    /**
     * Actualiza una sede existente.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public SedeDTO actualizar(
            Long id,
            ActualizarSedeRequest solicitud,
            String administrador
    ) {

        validarSolicitudActualizar(solicitud);
        validarAdministrador(administrador);

        Sede sede =
                buscarEntidad(id);

        String nuevoNombre =
                solicitud.getNombre().trim();

        verificarNombreDisponible(
                nuevoNombre,
                sede.getId()
        );

        sede.setNombre(nuevoNombre);

        sede.setCiudad(
                solicitud.getCiudad().trim()
        );

        sede.setPais(
                solicitud.getPais().trim()
        );

        sede.setCapacidadAprox(
                solicitud.getCapacidadAprox()
        );

        entityManager.flush();

        registrarAuditoria(
                "ACTUALIZAR_SEDE",
                sede.getId(),
                administrador,
                "Se actualizó la sede "
                        + sede.getNombre()
                        + " en "
                        + sede.getCiudad()
                        + ", "
                        + sede.getPais()
        );

        entityManager.flush();

        return convertir(sede);
    }

    /**
     * Elimina una sede solamente cuando no está
     * relacionada con ningún partido.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void eliminar(
            Long id,
            String administrador
    ) {

        validarAdministrador(administrador);

        Sede sede =
                buscarEntidad(id);

        Long partidosRelacionados =
                contarPartidosRelacionados(id);

        if (partidosRelacionados > 0) {

            throw new ReglaNegocioException(
                    "No se puede eliminar la sede porque "
                            + "está relacionada con "
                            + partidosRelacionados
                            + " partido(s)"
            );
        }

        String detalle =
                "Se eliminó la sede "
                        + sede.getNombre()
                        + " ubicada en "
                        + sede.getCiudad()
                        + ", "
                        + sede.getPais();

        /*
         * La auditoría se registra antes de eliminar
         * la entidad para conservar sus datos.
         */
        registrarAuditoria(
                "ELIMINAR_SEDE",
                sede.getId(),
                administrador,
                detalle
        );

        entityManager.remove(sede);
        entityManager.flush();
    }

    private void validarSolicitudCrear(
            CrearSedeRequest solicitud
    ) {

        if (solicitud == null) {

            throw new SolicitudInvalidaException(
                    "Debe enviar los datos de la sede"
            );
        }

        validarDatosComunes(
                solicitud.getNombre(),
                solicitud.getCiudad(),
                solicitud.getPais(),
                solicitud.getCapacidadAprox()
        );
    }

    private void validarSolicitudActualizar(
            ActualizarSedeRequest solicitud
    ) {

        if (solicitud == null) {

            throw new SolicitudInvalidaException(
                    "Debe enviar los datos de la sede"
            );
        }

        validarDatosComunes(
                solicitud.getNombre(),
                solicitud.getCiudad(),
                solicitud.getPais(),
                solicitud.getCapacidadAprox()
        );
    }

    /**
     * Valida los campos utilizados tanto en creación
     * como en actualización.
     */
    private void validarDatosComunes(
            String nombre,
            String ciudad,
            String pais,
            Integer capacidadAprox
    ) {

        if (nombre == null || nombre.isBlank()) {

            throw new SolicitudInvalidaException(
                    "El nombre de la sede es obligatorio"
            );
        }

        if (nombre.trim().length() > 180) {

            throw new SolicitudInvalidaException(
                    "El nombre de la sede no puede superar "
                            + "los 180 caracteres"
            );
        }

        if (ciudad == null || ciudad.isBlank()) {

            throw new SolicitudInvalidaException(
                    "La ciudad de la sede es obligatoria"
            );
        }

        if (ciudad.trim().length() > 100) {

            throw new SolicitudInvalidaException(
                    "La ciudad no puede superar "
                            + "los 100 caracteres"
            );
        }

        if (pais == null || pais.isBlank()) {

            throw new SolicitudInvalidaException(
                    "El país de la sede es obligatorio"
            );
        }

        if (pais.trim().length() > 80) {

            throw new SolicitudInvalidaException(
                    "El país no puede superar "
                            + "los 80 caracteres"
            );
        }

        if (capacidadAprox == null) {

            throw new SolicitudInvalidaException(
                    "La capacidad aproximada es obligatoria"
            );
        }

        if (capacidadAprox <= 0) {

            throw new SolicitudInvalidaException(
                    "La capacidad aproximada debe ser "
                            + "mayor que cero"
            );
        }

        if (capacidadAprox > 500_000) {

            throw new SolicitudInvalidaException(
                    "La capacidad aproximada no puede "
                            + "superar los 500000 espectadores"
            );
        }
    }

    private void validarAdministrador(
            String administrador
    ) {

        if (administrador == null
                || administrador.isBlank()) {

            throw new SolicitudInvalidaException(
                    "No se pudo identificar al administrador"
            );
        }
    }

    /**
     * Busca la entidad y genera 404 cuando no existe.
     */
    private Sede buscarEntidad(Long id) {

        if (id == null || id <= 0) {

            throw new SolicitudInvalidaException(
                    "El identificador de la sede no es válido"
            );
        }

        Sede sede =
                entityManager.find(
                        Sede.class,
                        id
                );

        if (sede == null) {

            throw new SedeNoEncontradaException(
                    "No existe una sede con el identificador "
                            + id
            );
        }

        return sede;
    }

    /**
     * Evita registrar nombres duplicados,
     * sin distinguir mayúsculas y minúsculas.
     */
    private void verificarNombreDisponible(
            String nombre,
            Long idExcluir
    ) {

        String jpql =
                "SELECT s FROM Sede s "
                        + "WHERE LOWER(s.nombre) = LOWER(:nombre)";

        if (idExcluir != null) {
            jpql += " AND s.id <> :idExcluir";
        }

        TypedQuery<Sede> consulta =
                entityManager.createQuery(
                        jpql,
                        Sede.class
                );

        consulta.setParameter(
                "nombre",
                nombre
        );

        if (idExcluir != null) {

            consulta.setParameter(
                    "idExcluir",
                    idExcluir
            );
        }

        List<Sede> existentes =
                consulta
                        .setMaxResults(1)
                        .getResultList();

        if (!existentes.isEmpty()) {

            throw new SedeDuplicadaException(
                    "Ya existe una sede con el nombre "
                            + nombre
            );
        }
    }

    /**
     * Obtiene un nuevo identificador después
     * del ID máximo existente.
     */
    private Long obtenerSiguienteId() {

        Long maximo = entityManager
                .createQuery(
                        "SELECT COALESCE(MAX(s.id), 0) "
                                + "FROM Sede s",
                        Long.class
                )
                .getSingleResult();

        return maximo + 1;
    }

    /**
     * Cuenta los partidos que utilizan una sede.
     */
    private Long contarPartidosRelacionados(
            Long sedeId
    ) {

        return entityManager
                .createQuery(
                        "SELECT COUNT(p) FROM Partido p "
                                + "WHERE p.sede.id = :sedeId",
                        Long.class
                )
                .setParameter(
                        "sedeId",
                        sedeId
                )
                .getSingleResult();
    }

    private void registrarAuditoria(
            String accion,
            Long sedeId,
            String administrador,
            String detalle
    ) {

        Auditoria auditoria =
                new Auditoria(
                        accion,
                        "SEDE",
                        sedeId,
                        administrador,
                        detalle
                );

        entityManager.persist(auditoria);
    }

    private SedeDTO convertir(
            Sede sede
    ) {

        return new SedeDTO(
                sede.getId(),
                sede.getNombre(),
                sede.getCiudad(),
                sede.getPais(),
                sede.getCapacidadAprox()
        );
    }
}
