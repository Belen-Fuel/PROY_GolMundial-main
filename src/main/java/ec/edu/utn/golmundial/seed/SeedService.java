package ec.edu.utn.golmundial.seed;

import ec.edu.utn.golmundial.dto.PasswordHashDTO;
import ec.edu.utn.golmundial.entity.EstadoPartido;
import ec.edu.utn.golmundial.entity.Fase;
import ec.edu.utn.golmundial.entity.Grupo;
import ec.edu.utn.golmundial.entity.Partido;
import ec.edu.utn.golmundial.entity.Rol;
import ec.edu.utn.golmundial.entity.Sede;
import ec.edu.utn.golmundial.entity.Seleccion;
import ec.edu.utn.golmundial.entity.Usuario;
import ec.edu.utn.golmundial.service.PasswordService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Carga los datos iniciales proporcionados para
 * el proyecto UTN GolMundial 2026.
 *
 * La carga es idempotente:
 * únicamente inserta registros que todavía no existen.
 *
 * La contraseña inicial del administrador no está
 * almacenada en el repositorio. Se obtiene desde la
 * variable de entorno:
 *
 * GOLMUNDIAL_ADMIN_PASSWORD
 */
@Stateless
public class SeedService {

    private static final Logger LOGGER =
            Logger.getLogger(SeedService.class.getName());

    private static final String ARCHIVO_SEED =
            "/seed/seed-utn-golmundial-2026.json";

    private static final String VARIABLE_PASSWORD_ADMIN =
            "GOLMUNDIAL_ADMIN_PASSWORD";

    private static final String PROPIEDAD_PASSWORD_ADMIN =
            "golmundial.admin.password";

    @PersistenceContext(unitName = "GolMundialPU")
    private EntityManager entityManager;

    @EJB
    private PasswordService passwordService;

    /**
     * Ejecuta toda la carga inicial dentro de una
     * transacción administrada por WildFly.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void cargarDatosIniciales() {

        JsonObject raiz =
                leerArchivoSeed();

        /*
         * Los roles deben cargarse antes de los usuarios
         * porque Usuario contiene una clave foránea hacia Rol.
         */
        Map<Long, Rol> roles =
                cargarRoles(
                        raiz.getJsonArray("roles")
                );

        cargarUsuariosIniciales(
                raiz.getJsonArray("usuariosIniciales"),
                roles
        );

        Map<String, Fase> fases =
                cargarFases(
                        raiz.getJsonArray("fases")
                );

        Map<String, Grupo> grupos =
                cargarGrupos(
                        raiz.getJsonArray("grupos")
                );

        Map<Long, Sede> sedes =
                cargarSedes(
                        raiz.getJsonArray("sedes")
                );

        Map<Long, Seleccion> selecciones =
                cargarSelecciones(
                        raiz.getJsonArray("selecciones"),
                        grupos
                );

        cargarPartidos(
                raiz.getJsonArray("partidos"),
                fases,
                grupos,
                sedes,
                selecciones
        );

        entityManager.flush();

        LOGGER.info(
                "Seed UTN GolMundial verificado correctamente: "
                        + contar("Rol") + " roles, "
                        + contar("Usuario") + " usuarios, "
                        + contar("Fase") + " fases, "
                        + contar("Grupo") + " grupos, "
                        + contar("Sede") + " sedes, "
                        + contar("Seleccion") + " selecciones y "
                        + contar("Partido") + " partidos."
        );
    }

    /**
     * Lee el JSON incluido dentro del archivo WAR.
     */
    private JsonObject leerArchivoSeed() {

        InputStream entrada =
                SeedService.class.getResourceAsStream(
                        ARCHIVO_SEED
                );

        if (entrada == null) {

            throw new IllegalStateException(
                    "No se encontró el archivo "
                            + ARCHIVO_SEED
            );
        }

        try (
                entrada;
                JsonReader lector =
                        Json.createReader(entrada)
        ) {

            return lector.readObject();

        } catch (Exception excepcion) {

            throw new IllegalStateException(
                    "No se pudo leer el seed "
                            + "del Mundial 2026",
                    excepcion
            );
        }
    }

    /**
     * Carga los roles iniciales:
     *
     * ADMINISTRADOR
     * USUARIO
     * INVITADO
     */
    private Map<Long, Rol> cargarRoles(
            JsonArray arreglo
    ) {

        if (arreglo == null) {

            throw new IllegalStateException(
                    "El seed no contiene la sección roles"
            );
        }

        Map<Long, Rol> resultado =
                new HashMap<>();

        for (JsonValue valor : arreglo) {

            JsonObject objeto =
                    valor.asJsonObject();

            Long id = objeto
                    .getJsonNumber("id")
                    .longValue();

            Rol rol =
                    entityManager.find(
                            Rol.class,
                            id
                    );

            if (rol == null) {

                rol = new Rol(
                        id,
                        objeto.getString("nombre"),
                        textoOpcional(
                                objeto,
                                "descripcion"
                        )
                );

                entityManager.persist(rol);
            }

            resultado.put(id, rol);
        }

        return resultado;
    }

    /**
     * Carga el administrador inicial.
     *
     * Si el usuario ya existe, no modifica su contraseña,
     * rol, nombre ni estado.
     *
     * Cuando el usuario no existe, obtiene la contraseña
     * desde GOLMUNDIAL_ADMIN_PASSWORD.
     */
    private void cargarUsuariosIniciales(
            JsonArray arreglo,
            Map<Long, Rol> roles
    ) {

        if (arreglo == null) {

            throw new IllegalStateException(
                    "El seed no contiene la sección "
                            + "usuariosIniciales"
            );
        }

        for (JsonValue valor : arreglo) {

            JsonObject objeto =
                    valor.asJsonObject();

            Long id = objeto
                    .getJsonNumber("id")
                    .longValue();

            String username =
                    objeto.getString("username")
                            .trim();

            Usuario usuario =
                    entityManager.find(
                            Usuario.class,
                            id
                    );

            /*
             * También se comprueba el username para evitar
             * duplicados si el identificador fuera diferente.
             */
            if (usuario == null) {

                List<Usuario> existentes =
                        entityManager
                                .createQuery(
                                        "SELECT u FROM Usuario u "
                                                + "WHERE LOWER(u.username) "
                                                + "= LOWER(:username)",
                                        Usuario.class
                                )
                                .setParameter(
                                        "username",
                                        username
                                )
                                .setMaxResults(1)
                                .getResultList();

                if (!existentes.isEmpty()) {
                    usuario = existentes.get(0);
                }
            }

            /*
             * Si el administrador ya existe, se conserva
             * toda su información actual.
             *
             * Por eso la contraseña actual del administrador no cambiará.
             */
            if (usuario != null) {
                continue;
            }

            Long rolId = objeto
                    .getJsonNumber("rolId")
                    .longValue();

            Rol rol =
                    roles.get(rolId);

            if (rol == null) {

                throw new IllegalStateException(
                        "No existe el rol "
                                + rolId
                                + " para el usuario "
                                + username
                );
            }

            /*
             * La contraseña no se obtiene del JSON.
             * Se obtiene desde una variable local.
             */
            String passwordInicial =
                    obtenerPasswordAdministradorInicial();

            PasswordHashDTO passwordProtegido =
                    passwordService.generarHash(
                            passwordInicial
                    );

            usuario = new Usuario(
                    id,
                    username,
                    objeto.getString("nombre"),
                    passwordProtegido.getHash(),
                    passwordProtegido.getSalt(),
                    passwordProtegido.getIteraciones(),
                    rol,
                    true,
                    true
            );

            entityManager.persist(usuario);

            LOGGER.info(
                    "Administrador inicial creado: "
                            + username
                            + ". Debe cambiar su contraseña "
                            + "en el primer inicio de sesión."
            );
        }
    }

    /**
     * Obtiene la contraseña inicial sin almacenarla
     * dentro del repositorio.
     *
     * Prioridad:
     *
     * 1. Variable de entorno GOLMUNDIAL_ADMIN_PASSWORD.
     * 2. Propiedad JVM golmundial.admin.password.
     */
    private String obtenerPasswordAdministradorInicial() {

        String password =
                System.getenv(
                        VARIABLE_PASSWORD_ADMIN
                );

        if (password == null
                || password.isBlank()) {

            password =
                    System.getProperty(
                            PROPIEDAD_PASSWORD_ADMIN
                    );
        }

        if (password == null
                || password.isBlank()) {

            throw new IllegalStateException(
                    "No se configuró la contraseña inicial "
                            + "del administrador. Defina la variable "
                            + VARIABLE_PASSWORD_ADMIN
                            + " antes de iniciar WildFly."
            );
        }

        return password;
    }

    /**
     * Carga las fases del torneo.
     */
    private Map<String, Fase> cargarFases(
            JsonArray arreglo
    ) {

        Map<String, Fase> resultado =
                new HashMap<>();

        for (JsonValue valor : arreglo) {

            JsonObject objeto =
                    valor.asJsonObject();

            String codigo =
                    objeto.getString("codigo");

            Fase fase =
                    entityManager.find(
                            Fase.class,
                            codigo
                    );

            if (fase == null) {

                fase = new Fase(
                        codigo,
                        objeto.getString("nombre"),
                        textoOpcional(
                                objeto,
                                "fechas"
                        )
                );

                entityManager.persist(fase);
            }

            resultado.put(codigo, fase);
        }

        return resultado;
    }

    /**
     * Carga los grupos del torneo.
     */
    private Map<String, Grupo> cargarGrupos(
            JsonArray arreglo
    ) {

        Map<String, Grupo> resultado =
                new HashMap<>();

        for (JsonValue valor : arreglo) {

            JsonObject objeto =
                    valor.asJsonObject();

            String codigo =
                    objeto.getString("codigo");

            Grupo grupo =
                    entityManager.find(
                            Grupo.class,
                            codigo
                    );

            if (grupo == null) {

                grupo = new Grupo(
                        codigo,
                        objeto.getString("nombre")
                );

                entityManager.persist(grupo);
            }

            resultado.put(codigo, grupo);
        }

        return resultado;
    }

    /**
     * Carga las sedes del torneo.
     */
    private Map<Long, Sede> cargarSedes(
            JsonArray arreglo
    ) {

        Map<Long, Sede> resultado =
                new HashMap<>();

        for (JsonValue valor : arreglo) {

            JsonObject objeto =
                    valor.asJsonObject();

            Long id = objeto
                    .getJsonNumber("id")
                    .longValue();

            Sede sede =
                    entityManager.find(
                            Sede.class,
                            id
                    );

            if (sede == null) {

                sede = new Sede(
                        id,
                        objeto.getString("nombre"),
                        objeto.getString("ciudad"),
                        objeto.getString("pais"),
                        enteroOpcional(
                                objeto,
                                "capacidadAprox"
                        )
                );

                entityManager.persist(sede);
            }

            resultado.put(id, sede);
        }

        return resultado;
    }

    /**
     * Carga las selecciones participantes.
     */
    private Map<Long, Seleccion> cargarSelecciones(
            JsonArray arreglo,
            Map<String, Grupo> grupos
    ) {

        Map<Long, Seleccion> resultado =
                new HashMap<>();

        for (JsonValue valor : arreglo) {

            JsonObject objeto =
                    valor.asJsonObject();

            Long id = objeto
                    .getJsonNumber("id")
                    .longValue();

            String codigoGrupo =
                    objeto.getString("grupo");

            Grupo grupo =
                    grupos.get(codigoGrupo);

            if (grupo == null) {

                throw new IllegalStateException(
                        "No existe el grupo "
                                + codigoGrupo
                                + " para la selección "
                                + id
                );
            }

            Seleccion seleccion =
                    entityManager.find(
                            Seleccion.class,
                            id
                    );

            if (seleccion == null) {

                seleccion = new Seleccion(
                        id,
                        objeto.getString(
                                "codigoFifa"
                        ),
                        objeto.getString("nombre"),
                        grupo,
                        objeto.getString(
                                "confederacion"
                        ),
                        objeto.getBoolean(
                                "esAnfitrion"
                        ),
                        textoOpcional(
                                objeto,
                                "clasificacion"
                        )
                );

                entityManager.persist(seleccion);
            }

            resultado.put(id, seleccion);
        }

        return resultado;
    }

    /**
     * Carga los partidos iniciales.
     */
    private void cargarPartidos(
            JsonArray arreglo,
            Map<String, Fase> fases,
            Map<String, Grupo> grupos,
            Map<Long, Sede> sedes,
            Map<Long, Seleccion> selecciones
    ) {

        for (JsonValue valor : arreglo) {

            JsonObject objeto =
                    valor.asJsonObject();

            Long id = objeto
                    .getJsonNumber("id")
                    .longValue();

            Partido existente =
                    entityManager.find(
                            Partido.class,
                            id
                    );

            /*
             * No se sobrescriben resultados, cuotas,
             * estados ni modificaciones administrativas.
             */
            if (existente != null) {
                continue;
            }

            Fase fase =
                    fases.get(
                            objeto.getString("fase")
                    );

            String codigoGrupo =
                    textoOpcional(
                            objeto,
                            "grupo"
                    );

            Grupo grupo =
                    codigoGrupo == null
                            ? null
                            : grupos.get(codigoGrupo);

            Long localId =
                    longOpcional(
                            objeto,
                            "seleccionLocalId"
                    );

            Long visitanteId =
                    longOpcional(
                            objeto,
                            "seleccionVisitanteId"
                    );

            Long sedeId = objeto
                    .getJsonNumber("sedeId")
                    .longValue();

            Seleccion local =
                    localId == null
                            ? null
                            : selecciones.get(localId);

            Seleccion visitante =
                    visitanteId == null
                            ? null
                            : selecciones.get(visitanteId);

            Sede sede =
                    sedes.get(sedeId);

            if (fase == null) {

                throw new IllegalStateException(
                        "Fase no encontrada para "
                                + "el partido "
                                + id
                );
            }

            if (sede == null) {

                throw new IllegalStateException(
                        "Sede no encontrada para "
                                + "el partido "
                                + id
                );
            }

            Partido partido =
                    new Partido();

            partido.setId(id);

            partido.setNumeroPartidoFifa(
                    objeto.getInt(
                            "numeroPartidoFifa"
                    )
            );

            partido.setFase(fase);
            partido.setGrupo(grupo);
            partido.setSeleccionLocal(local);
            partido.setSeleccionVisitante(
                    visitante
            );

            partido.setFechaHoraUtc(
                    OffsetDateTime.parse(
                            objeto.getString(
                                    "fechaHoraUtc"
                            )
                    )
            );

            partido.setFechaHoraEt(
                    OffsetDateTime.parse(
                            objeto.getString(
                                    "fechaHoraEt"
                            )
                    )
            );

            partido.setSede(sede);

            partido.setEstado(
                    EstadoPartido.valueOf(
                            objeto.getString("estado")
                    )
            );

            partido.setGolesLocal(
                    enteroOpcional(
                            objeto,
                            "golesLocal"
                    )
            );

            partido.setGolesVisitante(
                    enteroOpcional(
                            objeto,
                            "golesVisitante"
                    )
            );

            JsonObject cuotas =
                    objeto.getJsonObject("cuotas");

            if (cuotas != null) {

                partido.setCuotaLocal(
                        decimalOpcional(
                                cuotas,
                                "local"
                        )
                );

                partido.setCuotaEmpate(
                        decimalOpcional(
                                cuotas,
                                "empate"
                        )
                );

                partido.setCuotaVisitante(
                        decimalOpcional(
                                cuotas,
                                "visitante"
                        )
                );
            }

            partido.setResultadoNotificado(false);

            entityManager.persist(partido);
        }
    }

    private String textoOpcional(
            JsonObject objeto,
            String propiedad
    ) {

        if (!objeto.containsKey(propiedad)
                || objeto.isNull(propiedad)) {

            return null;
        }

        return objeto.getString(propiedad);
    }

    private Integer enteroOpcional(
            JsonObject objeto,
            String propiedad
    ) {

        if (!objeto.containsKey(propiedad)
                || objeto.isNull(propiedad)) {

            return null;
        }

        return objeto.getInt(propiedad);
    }

    private Long longOpcional(
            JsonObject objeto,
            String propiedad
    ) {

        if (!objeto.containsKey(propiedad)
                || objeto.isNull(propiedad)) {

            return null;
        }

        return objeto
                .getJsonNumber(propiedad)
                .longValue();
    }

    private BigDecimal decimalOpcional(
            JsonObject objeto,
            String propiedad
    ) {

        if (!objeto.containsKey(propiedad)
                || objeto.isNull(propiedad)) {

            return null;
        }

        JsonNumber numero =
                objeto.getJsonNumber(propiedad);

        return numero.bigDecimalValue();
    }

    private long contar(
            String entidad
    ) {

        return entityManager
                .createQuery(
                        "SELECT COUNT(e) FROM "
                                + entidad
                                + " e",
                        Long.class
                )
                .getSingleResult();
    }
}
