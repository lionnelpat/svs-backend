package sn.svs.backoffice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entité ExpenseCategory - Représente une catégorie de dépense
 * SVS - Dakar, Sénégal
 */
@Entity
@Table(name = "expense_categories", indexes = {
        @Index(name = "idx_expense_category_code", columnList = "code", unique = true),
        @Index(name = "idx_expense_category_nom", columnList = "nom"),
        @Index(name = "idx_expense_category_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class ExpenseCategory extends  AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

}
