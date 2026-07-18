package ec.edu.utn.golmundial.service;

import ec.edu.utn.golmundial.dto.GrupoDTO;
import ec.edu.utn.golmundial.dto.PartidoDTO;
import ec.edu.utn.golmundial.dto.SedeDTO;
import ec.edu.utn.golmundial.dto.SeleccionDTO;
import ec.edu.utn.golmundial.entity.EstadoPartido;
import ec.edu.utn.golmundial.entity.Grupo;
import ec.edu.utn.golmundial.entity.Partido;
import ec.edu.utn.golmundial.entity.Sede;
import ec.edu.utn.golmundial.entity.Seleccion;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Servicio encargado de las consultas públicas del torneo.
 */
@Stateless
public class ConsultaTorneoService {

    @PersistenceContext(unitName = "GolMundialPU")
    private EntityManager entityManager;

    public List<GrupoDTO> listarGrupos() {

        return entityManager
                .createQuery(
                        "SELECT g FROM Grupo g ORDER BY g.codigo",
                        Grupo.class
                )
                .getResultList()
                .stream()
                .map(this::convertirGrupo)
                .toList();
    }

    public GrupoDTO buscarGrupo(String codigo) {

        if (codigo == null || codigo.isBlank()) {
            return null;
        }

        Grupo grupo = entityManager.find(
                Grupo.class,
                codigo.trim().toUpperCase(Locale.ROOT)
        );

        return grupo == null ? null : convertirGrupo(grupo);
    }

    public List<SeleccionDTO> listarSelecciones(
            String grupoCodigo
    ) {

        List<Seleccion> selecciones;

        if (grupoCodigo == null || grupoCodigo.isBlank()) {

            selecciones = entityManager
                    .createQuery(
                            "SELECT s FROM Seleccion s "
                                    + "ORDER BY s.nombre",
                            Seleccion.class
                    )
                    .getResultList();

        } else {

            selecciones = entityManager
                    .createQuery(
                            "SELECT s FROM Seleccion s "
                                    + "WHERE UPPER(s.grupo.codigo) = :grupo "
                                    + "ORDER BY s.nombre",
                            Seleccion.class
                    )
                    .setParameter(
                            "grupo",
                            grupoCodigo.trim()
                                    .toUpperCase(Locale.ROOT)
                    )
                    .getResultList();
        }

        return selecciones
                .stream()
                .map(this::convertirSeleccion)
                .toList();
    }

    public SeleccionDTO buscarSeleccion(Long id) {

        Seleccion seleccion =
                entityManager.find(Seleccion.class, id);

        return seleccion == null
                ? null
                : convertirSeleccion(seleccion);
    }

    public List<SedeDTO> listarSedes() {

        return entityManager
                .createQuery(
                        "SELECT s FROM Sede s "
                                + "ORDER BY s.pais, s.ciudad, s.nombre",
                        Sede.class
                )
                .getResultList()
                .stream()
                .map(this::convertirSede)
                .toList();
    }

    public SedeDTO buscarSede(Long id) {

        Sede sede = entityManager.find(Sede.class, id);

        return sede == null ? null : convertirSede(sede);
    }

    public List<PartidoDTO> listarPartidos(
            String grupo,
            String fase,
            String estado
    ) {

        CriteriaBuilder constructor =
                entityManager.getCriteriaBuilder();

        CriteriaQuery<Partido> consulta =
                constructor.createQuery(Partido.class);

        Root<Partido> partido =
                consulta.from(Partido.class);

        List<Predicate> condiciones =
                new ArrayList<>();

        if (grupo != null && !grupo.isBlank()) {

            condiciones.add(
                    constructor.equal(
                            constructor.upper(
                                    partido
                                            .get("grupo")
                                            .get("codigo")
                            ),
                            grupo.trim()
                                    .toUpperCase(Locale.ROOT)
                    )
            );
        }

        if (fase != null && !fase.isBlank()) {

            condiciones.add(
                    constructor.equal(
                            constructor.upper(
                                    partido
                                            .get("fase")
                                            .get("codigo")
                            ),
                            fase.trim()
                                    .toUpperCase(Locale.ROOT)
                    )
            );
        }

        if (estado != null && !estado.isBlank()) {

            EstadoPartido estadoPartido;

            try {
                estadoPartido = EstadoPartido.valueOf(
                        estado.trim()
                                .toUpperCase(Locale.ROOT)
                );
            } catch (IllegalArgumentException excepcion) {
                throw new IllegalArgumentException(
                        "Estado de partido no válido: " + estado
                );
            }

            condiciones.add(
                    constructor.equal(
                            partido.get("estado"),
                            estadoPartido
                    )
            );
        }

        consulta
                .select(partido)
                .where(
                        condiciones.toArray(
                                new Predicate[0]
                        )
                )
                .orderBy(
                        constructor.asc(
                                partido.get("fechaHoraUtc")
                        )
                );

        return entityManager
                .createQuery(consulta)
                .getResultList()
                .stream()
                .map(this::convertirPartido)
                .toList();
    }

    public PartidoDTO buscarPartido(Long id) {

        Partido partido =
                entityManager.find(Partido.class, id);

        return partido == null
                ? null
                : convertirPartido(partido);
    }

    private GrupoDTO convertirGrupo(Grupo grupo) {

        return new GrupoDTO(
                grupo.getCodigo(),
                grupo.getNombre()
        );
    }

    private SeleccionDTO convertirSeleccion(
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

    private SedeDTO convertirSede(Sede sede) {

        return new SedeDTO(
                sede.getId(),
                sede.getNombre(),
                sede.getCiudad(),
                sede.getPais(),
                sede.getCapacidadAprox()
        );
    }

    private PartidoDTO convertirPartido(
            Partido partido
    ) {

        Seleccion local =
                partido.getSeleccionLocal();

        Seleccion visitante =
                partido.getSeleccionVisitante();

        return new PartidoDTO(
                partido.getId(),
                partido.getNumeroPartidoFifa(),
                partido.getFase().getCodigo(),
                partido.getGrupo() == null
                        ? null
                        : partido.getGrupo().getCodigo(),

                local == null ? null : local.getId(),
                local == null ? null : local.getNombre(),

                visitante == null
                        ? null
                        : visitante.getId(),

                visitante == null
                        ? null
                        : visitante.getNombre(),

                partido.getFechaHoraUtc() == null
                        ? null
                        : partido.getFechaHoraUtc().toString(),

                partido.getFechaHoraEt() == null
                        ? null
                        : partido.getFechaHoraEt().toString(),

                partido.getSede().getId(),
                partido.getSede().getNombre(),
                partido.getSede().getCiudad(),
                partido.getSede().getPais(),

                partido.getEstado().name(),

                partido.getGolesLocal(),
                partido.getGolesVisitante(),

                partido.getCuotaLocal(),
                partido.getCuotaEmpate(),
                partido.getCuotaVisitante(),

                partido.isResultadoNotificado()
        );
    }
}
