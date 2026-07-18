package ec.edu.utn.golmundial.service;

import ec.edu.utn.golmundial.dto.ResultadoPartidoDTO;
import ec.edu.utn.golmundial.dto.ResultadoPartidoRequest;
import ec.edu.utn.golmundial.entity.EstadoPartido;
import ec.edu.utn.golmundial.entity.Partido;
import ec.edu.utn.golmundial.entity.Resultado1X2;
import ec.edu.utn.golmundial.exception.PartidoNoEncontradoException;
import ec.edu.utn.golmundial.exception.ReglaNegocioException;
import ec.edu.utn.golmundial.exception.SolicitudInvalidaException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Contiene las operaciones administrativas
 * relacionadas con los partidos del Mundial 2026.
 */
@Stateless
public class AdministracionPartidoService {

    @PersistenceContext(unitName = "GolMundialPU")
    private EntityManager entityManager;

    @EJB
    private RegistroOperacionalService
            registroOperacionalService;

    /**
     * Registra el marcador oficial del partido.
     *
     * @param partidoId identificador del partido.
     * @param solicitud marcador recibido.
     * @param usuarioReferencia administrador autenticado.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public ResultadoPartidoDTO registrarResultado(
            Long partidoId,
            ResultadoPartidoRequest solicitud,
            String usuarioReferencia
    ) {

        validarSolicitud(
                partidoId,
                solicitud
        );

        validarUsuarioReferencia(
                usuarioReferencia
        );

        Partido partido = entityManager.find(
                Partido.class,
                partidoId
        );

        if (partido == null) {
            throw new PartidoNoEncontradoException(
                    "No existe un partido con el identificador "
                            + partidoId
            );
        }

        validarEstadoPartido(partido);
        validarSelecciones(partido);
        validarFecha(partido);

        partido.setGolesLocal(
                solicitud.getGolesLocal()
        );

        partido.setGolesVisitante(
                solicitud.getGolesVisitante()
        );

        partido.setEstado(
                EstadoPartido.FINALIZADO
        );

        partido.setResultadoNotificado(false);

        /*
         * Ejecuta @PreUpdate y actualiza
         * fechaActualizacion.
         */
        entityManager.flush();

        Resultado1X2 resultado =
                determinarResultado(
                        partido.getGolesLocal(),
                        partido.getGolesVisitante()
                );

        /*
         * Registra la auditoría con el usuario real
         * y crea el evento pendiente para UTNGolCoin.
         */
        registroOperacionalService
                .registrarResultadoOficial(
                        partido,
                        resultado,
                        usuarioReferencia
                );

        entityManager.flush();

        return construirRespuesta(
                partido,
                resultado
        );
    }

    private void validarSolicitud(
            Long partidoId,
            ResultadoPartidoRequest solicitud
    ) {

        if (partidoId == null || partidoId <= 0) {
            throw new SolicitudInvalidaException(
                    "El identificador del partido no es válido"
            );
        }

        if (solicitud == null) {
            throw new SolicitudInvalidaException(
                    "Debe enviar el marcador del partido"
            );
        }

        if (solicitud.getGolesLocal() == null) {
            throw new SolicitudInvalidaException(
                    "Los goles del equipo local son obligatorios"
            );
        }

        if (solicitud.getGolesVisitante() == null) {
            throw new SolicitudInvalidaException(
                    "Los goles del equipo visitante son obligatorios"
            );
        }

        if (solicitud.getGolesLocal() < 0) {
            throw new SolicitudInvalidaException(
                    "Los goles del equipo local no pueden ser negativos"
            );
        }

        if (solicitud.getGolesVisitante() < 0) {
            throw new SolicitudInvalidaException(
                    "Los goles del equipo visitante no pueden ser negativos"
            );
        }
    }

    private void validarUsuarioReferencia(
            String usuarioReferencia
    ) {

        if (usuarioReferencia == null
                || usuarioReferencia.isBlank()) {

            throw new SolicitudInvalidaException(
                    "No se pudo identificar al administrador"
            );
        }
    }

    private void validarEstadoPartido(
            Partido partido
    ) {

        if (partido.getEstado()
                == EstadoPartido.FINALIZADO) {

            throw new ReglaNegocioException(
                    "El partido ya tiene un resultado oficial registrado"
            );
        }

        if (partido.getEstado()
                == EstadoPartido.CANCELADO) {

            throw new ReglaNegocioException(
                    "No se puede registrar el resultado "
                            + "de un partido cancelado"
            );
        }

        if (partido.getEstado()
                == EstadoPartido.SUSPENDIDO) {

            throw new ReglaNegocioException(
                    "No se puede registrar el resultado "
                            + "mientras el partido esté suspendido"
            );
        }
    }

    private void validarSelecciones(
            Partido partido
    ) {

        if (partido.getSeleccionLocal() == null
                || partido.getSeleccionVisitante() == null) {

            throw new ReglaNegocioException(
                    "El partido todavía no tiene definidas "
                            + "las dos selecciones"
            );
        }
    }

    private void validarFecha(
            Partido partido
    ) {

        if (partido.getFechaHoraUtc() == null) {
            return;
        }

        OffsetDateTime ahoraUtc =
                OffsetDateTime.now(ZoneOffset.UTC);

        if (ahoraUtc.isBefore(
                partido.getFechaHoraUtc()
        )) {

            throw new ReglaNegocioException(
                    "No se puede registrar un resultado "
                            + "antes del inicio del partido"
            );
        }
    }

    private Resultado1X2 determinarResultado(
            int golesLocal,
            int golesVisitante
    ) {

        if (golesLocal > golesVisitante) {
            return Resultado1X2.LOCAL;
        }

        if (golesLocal < golesVisitante) {
            return Resultado1X2.VISITANTE;
        }

        return Resultado1X2.EMPATE;
    }

    private ResultadoPartidoDTO construirRespuesta(
            Partido partido,
            Resultado1X2 resultado
    ) {

        return new ResultadoPartidoDTO(
                partido.getId(),
                partido.getNumeroPartidoFifa(),

                partido.getSeleccionLocal().getId(),
                partido.getSeleccionLocal().getNombre(),

                partido.getSeleccionVisitante().getId(),
                partido.getSeleccionVisitante().getNombre(),

                partido.getGolesLocal(),
                partido.getGolesVisitante(),

                resultado.name(),
                partido.getEstado().name(),

                partido.isResultadoNotificado(),

                partido.getFechaActualizacion() == null
                        ? null
                        : partido
                                .getFechaActualizacion()
                                .toString(),

                "Resultado oficial registrado correctamente"
        );
    }
}
