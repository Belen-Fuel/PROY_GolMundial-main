package ec.edu.utn.golmundial.service;

import ec.edu.utn.golmundial.dto.ActualizarSeleccionRequest;
import ec.edu.utn.golmundial.dto.CrearSeleccionRequest;
import ec.edu.utn.golmundial.dto.SeleccionDTO;
import ec.edu.utn.golmundial.entity.Auditoria;
import ec.edu.utn.golmundial.entity.Grupo;
import ec.edu.utn.golmundial.entity.Seleccion;
import ec.edu.utn.golmundial.exception.ReglaNegocioException;
import ec.edu.utn.golmundial.exception.SeleccionDuplicadaException;
import ec.edu.utn.golmundial.exception.SeleccionNoEncontradaException;
import ec.edu.utn.golmundial.exception.SolicitudInvalidaException;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Locale;

/**
 * Gestiona la creación, actualización y eliminación
 * de selecciones del torneo.
 */
@Stateless
public class AdministracionSeleccionService {

    @PersistenceContext(unitName = "GolMundialPU")
    private EntityManager entityManager;

    /**
     * Registra una nueva selección.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public SeleccionDTO crear(
            CrearSeleccionRequest solicitud,
            String administrador
    ) {

        validarSolicitudCrear(solicitud);
        validarAdministrador(administrador);

        String codigoFifa =
                normalizarCodigoFifa(
                        solicitud.getCodigoFifa()
                );

        String nombre =
                solicitud.getNombre().trim();

        verificarDuplicados(
                codigoFifa,
                nombre,
                null
        );

        Grupo grupo =
                buscarGrupo(solicitud.getGrupo());

        Long nuevoId =
                obtenerSiguienteId();

        Seleccion seleccion =
                new Seleccion(
                        nuevoId,
                        codigoFifa,
                        nombre,
                        grupo,
                        solicitud.getConfederacion()
                                .trim()
                                .toUpperCase(Locale.ROOT),
                        solicitud.getEsAnfitrion(),
                        limpiarTextoOpcional(
                                solicitud.getClasificacion()
                        )
                );

        entityManager.persist(seleccion);
        entityManager.flush();

        registrarAuditoria(
                "CREAR_SELECCION",
                seleccion.getId(),
                administrador,
                "Se creó la selección "
                        + seleccion.getNombre()
                        + " con código FIFA "
                        + seleccion.getCodigoFifa()
        );

        entityManager.flush();

        return convertir(seleccion);
    }

    /**
     * Actualiza la información de una selección.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public SeleccionDTO actualizar(
            Long id,
            ActualizarSeleccionRequest solicitud,
            String administrador
    ) {

        validarSolicitudActualizar(solicitud);
        validarAdministrador(administrador);

        Seleccion seleccion =
                buscarEntidad(id);

        String codigoFifa =
                normalizarCodigoFifa(
                        solicitud.getCodigoFifa()
                );

        String nombre =
                solicitud.getNombre().trim();

        verificarDuplicados(
                codigoFifa,
                nombre,
                seleccion.getId()
        );

        Grupo nuevoGrupo =
                buscarGrupo(solicitud.getGrupo());

        /*
         * No permitimos cambiar de grupo cuando la selección
         * ya está incluida en el calendario, porque dejaría
         * inconsistencias entre el grupo y sus partidos.
         */
        if (!seleccion.getGrupo()
                .getCodigo()
                .equals(nuevoGrupo.getCodigo())) {

            Long partidosRelacionados =
                    contarPartidosRelacionados(
                            seleccion.getId()
                    );

            if (partidosRelacionados > 0) {
                throw new ReglaNegocioException(
                        "No se puede cambiar el grupo de una "
                                + "selección que ya tiene partidos"
                );
            }
        }

        seleccion.setCodigoFifa(codigoFifa);
        seleccion.setNombre(nombre);
        seleccion.setGrupo(nuevoGrupo);

        seleccion.setConfederacion(
                solicitud.getConfederacion()
                        .trim()
                        .toUpperCase(Locale.ROOT)
        );

        seleccion.setEsAnfitrion(
                solicitud.getEsAnfitrion()
        );

        seleccion.setClasificacion(
                limpiarTextoOpcional(
                        solicitud.getClasificacion()
                )
        );

        entityManager.flush();

        registrarAuditoria(
                "ACTUALIZAR_SELECCION",
                seleccion.getId(),
                administrador,
                "Se actualizó la selección "
                        + seleccion.getNombre()
        );

        entityManager.flush();

        return convertir(seleccion);
    }

    /**
     * Elimina una selección solamente cuando no está
     * relacionada con ningún partido.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void eliminar(
            Long id,
            String administrador
    ) {

        validarAdministrador(administrador);

        Seleccion seleccion =
                buscarEntidad(id);

        Long partidosRelacionados =
                contarPartidosRelacionados(id);

        if (partidosRelacionados > 0) {
            throw new ReglaNegocioException(
                    "No se puede eliminar la selección porque "
                            + "está relacionada con "
                            + partidosRelacionados
                            + " partido(s)"
            );
        }

        String detalle =
                "Se eliminó la selección "
                        + seleccion.getNombre()
                        + " con código FIFA "
                        + seleccion.getCodigoFifa();

        /*
         * Primero se registra la auditoría y después
         * se elimina la selección.
         */
        registrarAuditoria(
                "ELIMINAR_SELECCION",
                seleccion.getId(),
                administrador,
                detalle
        );

        entityManager.remove(seleccion);
        entityManager.flush();
    }

    private void validarSolicitudCrear(
            CrearSeleccionRequest solicitud
    ) {

        if (solicitud == null) {
            throw new SolicitudInvalidaException(
                    "Debe enviar los datos de la selección"
            );
        }

        validarDatosComunes(
                solicitud.getCodigoFifa(),
                solicitud.getNombre(),
                solicitud.getGrupo(),
                solicitud.getConfederacion(),
                solicitud.getEsAnfitrion(),
                solicitud.getClasificacion()
        );
    }

    private void validarSolicitudActualizar(
            ActualizarSeleccionRequest solicitud
    ) {

        if (solicitud == null) {
            throw new SolicitudInvalidaException(
                    "Debe enviar los datos de la selección"
            );
        }

        validarDatosComunes(
                solicitud.getCodigoFifa(),
                solicitud.getNombre(),
                solicitud.getGrupo(),
                solicitud.getConfederacion(),
                solicitud.getEsAnfitrion(),
                solicitud.getClasificacion()
        );
    }

    private void validarDatosComunes(
            String codigoFifa,
            String nombre,
            String grupo,
            String confederacion,
            Boolean esAnfitrion,
            String clasificacion
    ) {

        if (codigoFifa == null
                || codigoFifa.isBlank()) {

            throw new SolicitudInvalidaException(
                    "El código FIFA es obligatorio"
            );
        }

        String codigoLimpio =
                codigoFifa.trim();

        if (!codigoLimpio.matches(
                "[A-Za-z0-9]{3}"
        )) {

            throw new SolicitudInvalidaException(
                    "El código FIFA debe contener "
                            + "exactamente 3 letras o números"
            );
        }

        if (nombre == null || nombre.isBlank()) {
            throw new SolicitudInvalidaException(
                    "El nombre de la selección es obligatorio"
            );
        }

        if (nombre.trim().length() > 100) {
            throw new SolicitudInvalidaException(
                    "El nombre de la selección no puede "
                            + "superar los 100 caracteres"
            );
        }

        if (grupo == null || grupo.isBlank()) {
            throw new SolicitudInvalidaException(
                    "El grupo es obligatorio"
            );
        }

        if (confederacion == null
                || confederacion.isBlank()) {

            throw new SolicitudInvalidaException(
                    "La confederación es obligatoria"
            );
        }

        if (confederacion.trim().length() > 30) {
            throw new SolicitudInvalidaException(
                    "La confederación no puede superar "
                            + "los 30 caracteres"
            );
        }

        if (esAnfitrion == null) {
            throw new SolicitudInvalidaException(
                    "Debe indicar si la selección es anfitriona"
            );
        }

        if (clasificacion != null
                && clasificacion.trim().length() > 150) {

            throw new SolicitudInvalidaException(
                    "La clasificación no puede superar "
                            + "los 150 caracteres"
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

    private String normalizarCodigoFifa(
            String codigoFifa
    ) {

        return codigoFifa
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String limpiarTextoOpcional(
            String valor
    ) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    private Grupo buscarGrupo(
            String codigo
    ) {

        String codigoNormalizado =
                codigo.trim()
                        .toUpperCase(Locale.ROOT);

        Grupo grupo =
                entityManager.find(
                        Grupo.class,
                        codigoNormalizado
                );

        if (grupo == null) {
            throw new SolicitudInvalidaException(
                    "No existe el grupo "
                            + codigoNormalizado
            );
        }

        return grupo;
    }

    private Seleccion buscarEntidad(Long id) {

        if (id == null || id <= 0) {
            throw new SolicitudInvalidaException(
                    "El identificador de la selección "
                            + "no es válido"
            );
        }

        Seleccion seleccion =
                entityManager.find(
                        Seleccion.class,
                        id
                );

        if (seleccion == null) {
            throw new SeleccionNoEncontradaException(
                    "No existe una selección con el "
                            + "identificador "
                            + id
            );
        }

        return seleccion;
    }

    private void verificarDuplicados(
            String codigoFifa,
            String nombre,
            Long idExcluir
    ) {

        String consulta =
                "SELECT s FROM Seleccion s "
                        + "WHERE (UPPER(s.codigoFifa) = :codigo "
                        + "OR LOWER(s.nombre) = LOWER(:nombre))";

        if (idExcluir != null) {
            consulta += " AND s.id <> :idExcluir";
        }

        var query = entityManager
                .createQuery(
                        consulta,
                        Seleccion.class
                )
                .setParameter(
                        "codigo",
                        codigoFifa
                )
                .setParameter(
                        "nombre",
                        nombre
                );

        if (idExcluir != null) {
            query.setParameter(
                    "idExcluir",
                    idExcluir
            );
        }

        List<Seleccion> existentes =
                query.setMaxResults(1)
                        .getResultList();

        if (!existentes.isEmpty()) {

            Seleccion existente =
                    existentes.get(0);

            if (existente.getCodigoFifa()
                    .equalsIgnoreCase(codigoFifa)) {

                throw new SeleccionDuplicadaException(
                        "Ya existe una selección con el "
                                + "código FIFA "
                                + codigoFifa
                );
            }

            throw new SeleccionDuplicadaException(
                    "Ya existe una selección con el nombre "
                            + nombre
            );
        }
    }

    private Long obtenerSiguienteId() {

        Long maximo = entityManager
                .createQuery(
                        "SELECT COALESCE(MAX(s.id), 0) "
                                + "FROM Seleccion s",
                        Long.class
                )
                .getSingleResult();

        return maximo + 1;
    }

    private Long contarPartidosRelacionados(
            Long seleccionId
    ) {

        return entityManager
                .createQuery(
                        "SELECT COUNT(p) FROM Partido p "
                                + "WHERE p.seleccionLocal.id = :id "
                                + "OR p.seleccionVisitante.id = :id",
                        Long.class
                )
                .setParameter("id", seleccionId)
                .getSingleResult();
    }

    private void registrarAuditoria(
            String accion,
            Long seleccionId,
            String administrador,
            String detalle
    ) {

        Auditoria auditoria =
                new Auditoria(
                        accion,
                        "SELECCION",
                        seleccionId,
                        administrador,
                        detalle
                );

        entityManager.persist(auditoria);
    }

    private SeleccionDTO convertir(
            Seleccion seleccion
    ) {

        return new SeleccionDTO(
                seleccion.getId(),
                seleccion.getCodigoFifa(),
                seleccion.getNombre(),
                seleccion.getGrupo().getCodigo(),
                seleccion.getConfederacion(),
                seleccion.isEsAnfitrion(),
                seleccion.getClasificacion()
        );
    }
}
