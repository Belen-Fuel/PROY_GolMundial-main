package ec.edu.utn.golmundial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Representa una sesión autenticada.
 *
 * Nunca se guarda el token real. Únicamente se almacena
 * su hash SHA-256.
 */
@Entity
@Table(
        name = "sesiones_usuario",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sesion_token_hash",
                        columnNames = "token_hash"
                )
        }
)
public class SesionUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "usuario_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_sesion_usuario"
            )
    )
    private Usuario usuario;

    @Column(
            name = "token_hash",
            nullable = false,
            length = 64
    )
    private String tokenHash;

    @Column(
            name = "fecha_creacion_utc",
            nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private OffsetDateTime fechaCreacionUtc;

    @Column(
            name = "fecha_expiracion_utc",
            nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private OffsetDateTime fechaExpiracionUtc;

    @Column(
            name = "ultimo_acceso_utc",
            nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private OffsetDateTime ultimoAccesoUtc;

    @Column(
            name = "revocada",
            nullable = false
    )
    private boolean revocada = false;

    public SesionUsuario() {
    }

    public SesionUsuario(
            Usuario usuario,
            String tokenHash,
            OffsetDateTime fechaExpiracionUtc
    ) {
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.fechaExpiracionUtc = fechaExpiracionUtc;
    }

    @PrePersist
    public void antesDeInsertar() {

        OffsetDateTime ahora =
                OffsetDateTime.now(ZoneOffset.UTC);

        if (fechaCreacionUtc == null) {
            fechaCreacionUtc = ahora;
        }

        if (ultimoAccesoUtc == null) {
            ultimoAccesoUtc = ahora;
        }
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getFechaCreacionUtc() {
        return fechaCreacionUtc;
    }

    public OffsetDateTime getFechaExpiracionUtc() {
        return fechaExpiracionUtc;
    }

    public OffsetDateTime getUltimoAccesoUtc() {
        return ultimoAccesoUtc;
    }

    public void setUltimoAccesoUtc(
            OffsetDateTime ultimoAccesoUtc
    ) {
        this.ultimoAccesoUtc = ultimoAccesoUtc;
    }

    public boolean isRevocada() {
        return revocada;
    }

    public void setRevocada(boolean revocada) {
        this.revocada = revocada;
    }
}
