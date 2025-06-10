package sn.svs.backoffice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entité ExpenseSupplier - Représente un fournisseur pour les dépenses
 * SVS - Dakar, Sénégal
 */
@Entity
@Table(name = "expense_suppliers", indexes = {
        @Index(name = "idx_expense_supplier_nom", columnList = "nom"),
        @Index(name = "idx_expense_supplier_email", columnList = "email"),
        @Index(name = "idx_expense_supplier_telephone", columnList = "telephone"),
        @Index(name = "idx_expense_supplier_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class ExpenseSupplier extends  AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "adresse", columnDefinition = "TEXT")
    private String adresse;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "rccm", length = 50)
    private String rccm;

    @Column(name = "ninea", length = 20)
    private String ninea;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

}