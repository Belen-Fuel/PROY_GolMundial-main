package ec.edu.utn.golmundial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Representa una cuenta de usuario del sistema.
 *
 * La contraseña nunca se guarda en texto plano.
 * Se almacenan únicamente:
 *
 * - Hash de la contraseña.
 * - Sal criptográfica.
 * - Cantidad de iteraciones utilizadas.
 */
@Entity
@Table(
        name = "usuarios",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_usuario_username",
                        columnNames = "username"
                )
        }
)
public class Usuario {

    /**
     * Identificador original del seed.
     *
     * Para usuarios nuevos asignaremos el ID desde
     * el servicio de registro.
     */
    @Id
    @NotNull
    @Column(
            name = "id",
            nullable = false
    )
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(
            name = "username",
            nullable = false,
            length = 60
    )
    private String username;

    @NotBlank
    @Size(max = 150)
    @Column(
            name = "nombre",
            nullable = false,
            length = 150
    )
    private String nombre;

    /**
     * Hash de la contraseña codificado en Base64.
     */
    @NotBlank
    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;

    /**
     * Sal aleatoria codificada en Base64.
     */
    @NotBlank
    @Column(
            name = "password_salt",
            nullable = false,
            length = 255
    )
    private String passwordSalt;

    /**
     * Cantidad de iteraciones utilizadas para generar
     * el hash PBKDF2.
     */
    @Column(
            name = "password_iteraciones",
            nullable = false
    )
    private Integer passwordIteraciones;

    @NotNull
    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "rol_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_usuario_rol"
            )
    )
    private Rol rol;

    /**
     * Permite bloquear una cuenta sin eliminarla.
     */
    @Column(
            name = "activo",
            nullable = false
    )
    private boolean activo = true;

    /**
     * El administrador inicial deberá cambiar su
     * contraseña después del primer acceso.
     */
    @Column(
            name = "cambio_password_obligatorio",
            nullable = false
    )
    private boolean cambioPasswordObligatorio = false;

    @Column(
            name = "fecha_creacion_utc",
            nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private OffsetDateTime fechaCreacionUtc;

    @Column(
            name = "fecha_actualizacion_utc",
            nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private OffsetDateTime fechaActualizacionUtc;

    public Usuario() {
    }

    public Usuario(
            Long id,
            String username,
            String nombre,
            String passwordHash,
            String passwordSalt,
            Integer passwordIteraciones,
            Rol rol,
            boolean activo,
            boolean cambioPasswordObligatorio
    ) {
        this.id = id;
        this.username = username;
        this.nombre = nombre;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.passwordIteraciones = passwordIteraciones;
        this.rol = rol;
        this.activo = activo;
        this.cambioPasswordObligatorio =
                cambioPasswordObligatorio;
    }

    @PrePersist
    public void antesDeInsertar() {

        OffsetDateTime ahora =
                OffsetDateTime.now(ZoneOffset.UTC);

        if (fechaCreacionUtc == null) {
            fechaCreacionUtc = ahora;
        }

        fechaActualizacionUtc = ahora;
    }

    @PreUpdate
    public void antesDeActualizar() {

        fechaActualizacionUtc =
                OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(
            String passwordHash
    ) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(
            String passwordSalt
    ) {
        this.passwordSalt = passwordSalt;
    }

    public Integer getPasswordIteraciones() {
        return passwordIteraciones;
    }

    public void setPasswordIteraciones(
            Integer passwordIteraciones
    ) {
        this.passwordIteraciones =
                passwordIteraciones;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isCambioPasswordObligatorio() {
        return cambioPasswordObligatorio;
    }

    public void setCambioPasswordObligatorio(
            boolean cambioPasswordObligatorio
    ) {
        this.cambioPasswordObligatorio =
                cambioPasswordObligatorio;
    }

    public OffsetDateTime getFechaCreacionUtc() {
        return fechaCreacionUtc;
    }

    public void setFechaCreacionUtc(
            OffsetDateTime fechaCreacionUtc
    ) {
        this.fechaCreacionUtc = fechaCreacionUtc;
    }

    public OffsetDateTime getFechaActualizacionUtc() {
        return fechaActualizacionUtc;
    }

    public void setFechaActualizacionUtc(
            OffsetDateTime fechaActualizacionUtc
    ) {
        this.fechaActualizacionUtc =
                fechaActualizacionUtc;
    }
}
