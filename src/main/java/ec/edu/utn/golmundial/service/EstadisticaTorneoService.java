package ec.edu.utn.golmundial.service;

import ec.edu.utn.golmundial.dto.EstadisticaSeleccionDTO;
import ec.edu.utn.golmundial.dto.PosicionDTO;
import ec.edu.utn.golmundial.entity.EstadoPartido;
import ec.edu.utn.golmundial.entity.Grupo;
import ec.edu.utn.golmundial.entity.Partido;
import ec.edu.utn.golmundial.entity.Seleccion;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Calcula las estadísticas deportivas usando
 * los resultados oficiales almacenados en PostgreSQL.
 *
 * Únicamente se consideran partidos con estado FINALIZADO
 * y con un marcador válido.
 */
@Stateless
public class EstadisticaTorneoService {

    @PersistenceContext(unitName = "GolMundialPU")
    private EntityManager entityManager;

    /**
     * Calcula la tabla de posiciones de un grupo.
     *
     * Criterios de ordenamiento:
     * 1. Puntos.
     * 2. Diferencia de goles.
     * 3. Goles a favor.
     * 4. Nombre de la selección.
     */
    public List<PosicionDTO> obtenerPosicionesGrupo(
            String codigoGrupo
    ) {

        String codigoNormalizado =
                normalizarGrupo(codigoGrupo);

        Grupo grupo = entityManager.find(
                Grupo.class,
                codigoNormalizado
        );

        if (grupo == null) {
            return List.of();
        }

        List<Seleccion> selecciones = entityManager
                .createQuery(
                        "SELECT s FROM Seleccion s "
                                + "WHERE s.grupo.codigo = :grupo "
                                + "ORDER BY s.nombre",
                        Seleccion.class
                )
                .setParameter("grupo", codigoNormalizado)
                .getResultList();

        Map<Long, AcumuladorEstadistica> acumuladores =
                new LinkedHashMap<>();

        for (Seleccion seleccion : selecciones) {
            acumuladores.put(
                    seleccion.getId(),
                    new AcumuladorEstadistica(seleccion)
            );
        }

        List<Partido> partidosFinalizados = entityManager
                .createQuery(
                        "SELECT p FROM Partido p "
                                + "WHERE p.estado = :estado "
                                + "AND p.fase.codigo = :fase "
                                + "AND p.grupo.codigo = :grupo "
                                + "AND p.golesLocal IS NOT NULL "
                                + "AND p.golesVisitante IS NOT NULL "
                                + "ORDER BY p.fechaHoraUtc",
                        Partido.class
                )
                .setParameter(
                        "estado",
                        EstadoPartido.FINALIZADO
                )
                .setParameter("fase", "GRUPOS")
                .setParameter("grupo", codigoNormalizado)
                .getResultList();

        for (Partido partido : partidosFinalizados) {
            registrarPartido(
                    partido,
                    acumuladores
            );
        }

        List<AcumuladorEstadistica> ordenados =
                new ArrayList<>(acumuladores.values());

        ordenados.sort(comparadorPosiciones());

        List<PosicionDTO> posiciones =
                new ArrayList<>();

        int posicion = 1;

        for (AcumuladorEstadistica acumulador : ordenados) {
            posiciones.add(
                    acumulador.convertirAPosicion(posicion)
            );

            posicion++;
        }

        return posiciones;
    }

    /**
     * Devuelve las tablas de los doce grupos.
     */
    public List<PosicionDTO> obtenerTodasLasPosiciones() {

        List<Grupo> grupos = entityManager
                .createQuery(
                        "SELECT g FROM Grupo g "
                                + "ORDER BY g.codigo",
                        Grupo.class
                )
                .getResultList();

        List<PosicionDTO> resultado =
                new ArrayList<>();

        for (Grupo grupo : grupos) {
            resultado.addAll(
                    obtenerPosicionesGrupo(
                            grupo.getCodigo()
                    )
            );
        }

        return resultado;
    }

    /**
     * Obtiene las estadísticas acumuladas de una
     * selección durante todo el torneo.
     */
    public EstadisticaSeleccionDTO obtenerEstadisticasSeleccion(
            Long seleccionId
    ) {

        Seleccion seleccion =
                entityManager.find(
                        Seleccion.class,
                        seleccionId
                );

        if (seleccion == null) {
            return null;
        }

        AcumuladorEstadistica acumulador =
                new AcumuladorEstadistica(seleccion);

        List<Partido> partidos = listarPartidosFinalizados();

        for (Partido partido : partidos) {

            if (partido.getSeleccionLocal() != null
                    && seleccionId.equals(
                            partido
                                    .getSeleccionLocal()
                                    .getId()
                    )) {

                acumulador.registrarPartido(
                        partido.getGolesLocal(),
                        partido.getGolesVisitante()
                );

            } else if (
                    partido.getSeleccionVisitante() != null
                            && seleccionId.equals(
                                    partido
                                            .getSeleccionVisitante()
                                            .getId()
                            )
            ) {

                acumulador.registrarPartido(
                        partido.getGolesVisitante(),
                        partido.getGolesLocal()
                );
            }
        }

        return acumulador.convertirAEstadistica();
    }

    /**
     * Devuelve las estadísticas de todas las selecciones
     * o únicamente las de un grupo.
     */
    public List<EstadisticaSeleccionDTO> listarEstadisticas(
            String codigoGrupo
    ) {

        List<Seleccion> selecciones;

        if (codigoGrupo == null || codigoGrupo.isBlank()) {

            selecciones = entityManager
                    .createQuery(
                            "SELECT s FROM Seleccion s "
                                    + "ORDER BY s.nombre",
                            Seleccion.class
                    )
                    .getResultList();

        } else {

            String grupoNormalizado =
                    normalizarGrupo(codigoGrupo);

            selecciones = entityManager
                    .createQuery(
                            "SELECT s FROM Seleccion s "
                                    + "WHERE s.grupo.codigo = :grupo "
                                    + "ORDER BY s.nombre",
                            Seleccion.class
                    )
                    .setParameter(
                            "grupo",
                            grupoNormalizado
                    )
                    .getResultList();
        }

        Map<Long, AcumuladorEstadistica> acumuladores =
                new LinkedHashMap<>();

        for (Seleccion seleccion : selecciones) {
            acumuladores.put(
                    seleccion.getId(),
                    new AcumuladorEstadistica(seleccion)
            );
        }

        for (Partido partido : listarPartidosFinalizados()) {
            registrarPartido(
                    partido,
                    acumuladores
            );
        }

        return acumuladores
                .values()
                .stream()
                .map(
                        AcumuladorEstadistica
                                ::convertirAEstadistica
                )
                .toList();
    }

    private List<Partido> listarPartidosFinalizados() {

        return entityManager
                .createQuery(
                        "SELECT p FROM Partido p "
                                + "WHERE p.estado = :estado "
                                + "AND p.golesLocal IS NOT NULL "
                                + "AND p.golesVisitante IS NOT NULL "
                                + "ORDER BY p.fechaHoraUtc",
                        Partido.class
                )
                .setParameter(
                        "estado",
                        EstadoPartido.FINALIZADO
                )
                .getResultList();
    }

    /**
     * Registra el resultado para el equipo local
     * y para el visitante.
     */
    private void registrarPartido(
            Partido partido,
            Map<Long, AcumuladorEstadistica> acumuladores
    ) {

        if (partido.getSeleccionLocal() == null
                || partido.getSeleccionVisitante() == null
                || partido.getGolesLocal() == null
                || partido.getGolesVisitante() == null) {
            return;
        }

        AcumuladorEstadistica local =
                acumuladores.get(
                        partido
                                .getSeleccionLocal()
                                .getId()
                );

        AcumuladorEstadistica visitante =
                acumuladores.get(
                        partido
                                .getSeleccionVisitante()
                                .getId()
                );

        if (local != null) {
            local.registrarPartido(
                    partido.getGolesLocal(),
                    partido.getGolesVisitante()
            );
        }

        if (visitante != null) {
            visitante.registrarPartido(
                    partido.getGolesVisitante(),
                    partido.getGolesLocal()
            );
        }
    }

    private Comparator<AcumuladorEstadistica>
    comparadorPosiciones() {

        return Comparator
                .comparingInt(
                        AcumuladorEstadistica::getPuntos
                )
                .reversed()

                .thenComparing(
                        Comparator
                                .comparingInt(
                                        AcumuladorEstadistica
                                                ::getDiferenciaGoles
                                )
                                .reversed()
                )

                .thenComparing(
                        Comparator
                                .comparingInt(
                                        AcumuladorEstadistica
                                                ::getGolesFavor
                                )
                                .reversed()
                )

                .thenComparing(
                        acumulador ->
                                acumulador
                                        .getSeleccion()
                                        .getNombre(),
                        String.CASE_INSENSITIVE_ORDER
                );
    }

    private String normalizarGrupo(String codigoGrupo) {

        if (codigoGrupo == null
                || codigoGrupo.isBlank()) {
            return "";
        }

        return codigoGrupo
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    /**
     * Clase interna utilizada para acumular resultados
     * antes de construir los DTO.
     */
    private static final class AcumuladorEstadistica {

        private final Seleccion seleccion;

        private int jugados;
        private int ganados;
        private int empatados;
        private int perdidos;

        private int golesFavor;
        private int golesContra;

        private int puntos;

        private AcumuladorEstadistica(
                Seleccion seleccion
        ) {
            this.seleccion = seleccion;
        }

        private void registrarPartido(
                int golesMarcados,
                int golesRecibidos
        ) {

            jugados++;

            golesFavor += golesMarcados;
            golesContra += golesRecibidos;

            if (golesMarcados > golesRecibidos) {

                ganados++;
                puntos += 3;

            } else if (golesMarcados == golesRecibidos) {

                empatados++;
                puntos += 1;

            } else {

                perdidos++;
            }
        }

        private int getDiferenciaGoles() {
            return golesFavor - golesContra;
        }

        private PosicionDTO convertirAPosicion(
                int posicion
        ) {

            return new PosicionDTO(
                    posicion,
                    seleccion.getId(),
                    seleccion.getCodigoFifa(),
                    seleccion.getNombre(),
                    seleccion.getGrupo().getCodigo(),
                    jugados,
                    ganados,
                    empatados,
                    perdidos,
                    golesFavor,
                    golesContra,
                    getDiferenciaGoles(),
                    puntos
            );
        }

        private EstadisticaSeleccionDTO
        convertirAEstadistica() {

            return new EstadisticaSeleccionDTO(
                    seleccion.getId(),
                    seleccion.getCodigoFifa(),
                    seleccion.getNombre(),
                    seleccion.getGrupo().getCodigo(),
                    seleccion.getConfederacion(),
                    jugados,
                    ganados,
                    empatados,
                    perdidos,
                    golesFavor,
                    golesContra,
                    getDiferenciaGoles(),
                    puntos
            );
        }

        private Seleccion getSeleccion() {
            return seleccion;
        }

        private int getPuntos() {
            return puntos;
        }

        private int getGolesFavor() {
            return golesFavor;
        }
    }
}
