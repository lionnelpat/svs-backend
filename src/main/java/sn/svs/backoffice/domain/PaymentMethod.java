package sn.svs.backoffice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entité représentant un mode de paiement
 */
@Entity
@Table(name = "payment_methods", indexes = {
        @Index(name = "idx_payment_method_code", columnList = "code", unique = true),
        @Index(name = "idx_payment_method_nom", columnList = "nom", unique = true),
        @Index(name = "idx_payment_method_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class PaymentMethod extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, unique = true, length = 100)
    private String nom;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "actif")
    @Builder.Default
    private Boolean actif = true;

    /**
     * Méthode utilitaire pour vérifier si le mode de paiement est actif
     */
    public boolean isActif() {
        return actif != null && actif;
    }
}
