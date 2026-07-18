package ec.edu.utn.golmundial.service;

import ec.edu.utn.golmundial.entity.Auditoria;
import ec.edu.utn.golmundial.entity.EventoIntegracion;
import ec.edu.utn.golmundial.entity.Partido;
import ec.edu.utn.golmundial.entity.Resultado1X2;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Registra auditoría y eventos pendientes
 * de comunicación con UTNGolCoin.
 */
@Stateless
public class RegistroOperacionalService {

    @PersistenceContext(unitName = "GolMundialPU")
    private EntityManager entityManager;

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void registrarResultadoOficial(
            Partido partido,
            Resultado1X2 resultado,
            String usuarioReferencia
    ) {

        String detalle =
                "Resultado oficial del partido "
                        + partido.getNumeroPartidoFifa()
                        + ": "
                        + partido.getSeleccionLocal().getNombre()
                        + " "
                        + partido.getGolesLocal()
                        + " - "
                        + partido.getGolesVisitante()
                        + " "
                        + partido.getSeleccionVisitante().getNombre();

        Auditoria auditoria = new Auditoria(
                "REGISTRAR_RESULTADO",
                "PARTIDO",
                partido.getId(),
                usuarioReferencia,
                detalle
        );

        entityManager.persist(auditoria);

        JsonObject payload = Json
                .createObjectBuilder()
                .add("partidoId", partido.getId())
                .add(
                        "numeroPartidoFifa",
                        partido.getNumeroPartidoFifa()
                )
                .add(
                        "seleccionLocalId",
                        partido.getSeleccionLocal().getId()
                )
                .add(
                        "seleccionVisitanteId",
                        partido.getSeleccionVisitante().getId()
                )
                .add("golesLocal", partido.getGolesLocal())
                .add(
                        "golesVisitante",
                        partido.getGolesVisitante()
                )
                .add("resultado1X2", resultado.name())
                .add(
                        "fechaResultadoUtc",
                        partido.getFechaActualizacion()
                                .toString()
                )
                .build();

        EventoIntegracion evento =
                new EventoIntegracion(
                        "RESULTADO_OFICIAL_REGISTRADO",
                        "PARTIDO",
                        partido.getId(),
                        payload.toString()
                );

        entityManager.persist(evento);
    }
}
