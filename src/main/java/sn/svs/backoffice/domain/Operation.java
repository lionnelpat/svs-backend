package sn.svs.backoffice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
/**
 * Entité Operation - Représente une opération maritime
 * SVS - Dakar, Sénégal
 */
@Entity
@Table(name = "operations", indexes = {
        @Index(name = "idx_operation_code", columnList = "code", unique = true),
        @Index(name = "idx_operation_nom", columnList = "nom"),
        @Index(name = "idx_operation_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Operation extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "prix_xof", nullable = false, precision = 15, scale = 2)
    private BigDecimal prixXOF;

    @Column(name = "prix_euro", precision = 15, scale = 2)
    private BigDecimal prixEURO;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    // Méthodes utilitaires
    public void activate() {
        this.setActive(true);
    }

    public void deactivate() {
        this.setActive(false);
    }
}
