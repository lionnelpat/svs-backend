package sn.svs.backoffice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Classe abstraite de base pour toutes les entités avec audit automatique
 * Fournit les champs d'audit standards : created_at, updated_at, created_by, updated_by
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Date de création de l'entité
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Utilisateur qui a créé l'entité
     */
    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    @JsonIgnore
    private String createdBy;

    /**
     * Date de dernière modification
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    @JsonIgnore
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Utilisateur qui a modifié l'entité en dernier
     */
    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    @JsonIgnore
    private String updatedBy;

    /**
     * Indicateur d'activité de l'entité
     * Permet la suppression logique
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Met à jour la date de modification avant la persistance
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
    }

    /**
     * Met à jour la date de modification avant la mise à jour
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Suppression logique : marque l'entité comme inactive
     */
    public void softDelete() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Restauration : marque l'entité comme active
     */
    public void restore() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Vérifie si l'entité est active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
    }
}
