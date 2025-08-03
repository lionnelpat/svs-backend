package sn.svs.backoffice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sn.svs.backoffice.domain.ennumeration.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Entité Invoice représentant une facture de prestations maritimes
 * Gère les factures émises aux compagnies maritimes pour leurs navires
 */
@Entity
@Table(name = "invoices", indexes = {
        @Index(name = "idx_invoice_numero", columnList = "numero"),
        @Index(name = "idx_invoice_compagnie", columnList = "compagnieId"),
        @Index(name = "idx_invoice_navire", columnList = "navireId"),
        @Index(name = "idx_invoice_date_facture", columnList = "dateFacture"),
        @Index(name = "idx_invoice_statut", columnList = "statut"),
        @Index(name = "idx_invoice_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Invoice extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Numéro unique de la facture (généré automatiquement)
     * Format: FAC-YYYY-NNNNNN
     */
    @Column(name = "numero", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Le numéro de facture est obligatoire")
    @Size(max = 20, message = "Le numéro de facture ne peut pas dépasser 20 caractères")
    private String numero;

    /**
     * ID de la compagnie maritime cliente
     */
    @Column(name = "compagnieId", nullable = false)
    @NotNull(message = "La compagnie est obligatoire")
    private Long compagnieId;

    /**
     * ID du navire concerné par les prestations
     */
    @Column(name = "navireId", nullable = false)
    @NotNull(message = "Le navire est obligatoire")
    private Long navireId;

    /**
     * Date d'émission de la facture
     */
    @Column(name = "dateFacture", nullable = false)
    @NotNull(message = "La date de facture est obligatoire")
    private LocalDate dateFacture;

    /**
     * Date d'échéance de paiement
     */
    @Column(name = "dateEcheance", nullable = false)
    @NotNull(message = "La date d'échéance est obligatoire")
    private LocalDate dateEcheance;

    /**
     * Sous-total de la facture (avant TVA)
     */
    @Column(name = "sousTotal", precision = 15, scale = 2, nullable = false)
    @NotNull(message = "Le sous-total est obligatoire")
    @DecimalMin(value = "0.00", message = "Le sous-total ne peut pas être négatif")
    private BigDecimal sousTotal;

    /**
     * Montant de la TVA
     */
    @Column(name = "tva", precision = 15, scale = 2, nullable = false)
    @NotNull(message = "Le montant TVA est obligatoire")
    @DecimalMin(value = "0.00", message = "Le montant TVA ne peut pas être négatif")
    private BigDecimal tva;

    /**
     * Taux de TVA appliqué (en pourcentage)
     */
    @Column(name = "tauxTva", precision = 5, scale = 2, nullable = false)
    @NotNull(message = "Le taux de TVA est obligatoire")
    @DecimalMin(value = "0.00", message = "Le taux de TVA ne peut pas être négatif")
    @DecimalMax(value = "100.00", message = "Le taux de TVA ne peut pas dépasser 100%")
    private BigDecimal tauxTva;

    /**
     * Montant total de la facture (sous-total + TVA)
     */
    @Column(name = "montantTotal", precision = 15, scale = 2, nullable = false)
    @NotNull(message = "Le montant total est obligatoire")
    @DecimalMin(value = "0.00", message = "Le montant total ne peut pas être négatif")
    private BigDecimal montantTotal;

    /**
     * Statut de la facture
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    @NotNull(message = "Le statut de la facture est obligatoire")
    private InvoiceStatus statut;

    /**
     * Notes ou commentaires sur la facture
     */
    @Column(name = "notes", length = 1000)
    @Size(max = 1000, message = "Les notes ne peuvent pas dépasser 1000 caractères")
    private String notes;

    /**
     * Indicateur de suppression logique
     */
    @Column(name = "active", nullable = false)
    @NotNull
    private Boolean active = true;

    // Relations JPA

    /**
     * Relation avec l'entité Company (compagnie maritime)
     * Fetch LAZY pour optimiser les performances
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compagnieId", insertable = false, updatable = false)
    private Company compagnie;

    /**
     * Relation avec l'entité Ship (navire)
     * Fetch LAZY pour optimiser les performances
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "navireId", insertable = false, updatable = false)
    private Ship navire;

    /**
     * Liste des prestations (lignes de facture)
     * Cascade ALL pour gérer automatiquement les opérations CRUD
     */
    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceLineItem> prestations;

    // Méthodes utilitaires

    /**
     * Vérifie si la facture est en retard
     */
    @Transient
    public boolean isEnRetard() {
        return dateEcheance.isBefore(LocalDate.now()) &&
                (statut == InvoiceStatus.EMISE || statut == InvoiceStatus.BROUILLON);
    }

    /**
     * Vérifie si la facture peut être modifiée
     */
    @Transient
    public boolean isModifiable() {
        return statut == InvoiceStatus.BROUILLON;
    }

    /**
     * Vérifie si la facture peut être supprimée
     */
    @Transient
    public boolean isSupprimable() {
        return statut == InvoiceStatus.BROUILLON || statut == InvoiceStatus.ANNULEE;
    }

    /**
     * Calcule automatiquement les montants (appelé avant la sauvegarde)
     */
    @PrePersist
    @PreUpdate
    public void calculerMontants() {
        if (prestations != null && !prestations.isEmpty()) {
            sousTotal = prestations.stream()
                    .map(InvoiceLineItem::getMontantXOF)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            tva = sousTotal.multiply(tauxTva).divide(BigDecimal.valueOf(100));
            montantTotal = sousTotal.add(tva);
        }

        // Mise à jour automatique du statut si en retard
        if (isEnRetard() && statut == InvoiceStatus.EMISE) {
            statut = InvoiceStatus.EN_RETARD;
        }
    }
}
