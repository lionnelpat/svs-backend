package sn.svs.backoffice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entité InvoiceLineItem représentant une ligne de prestation dans une facture
 * Chaque ligne correspond à une opération maritime facturée
 */
@Entity
@Table(name = "invoice_line_items", indexes = {
        @Index(name = "idx_line_item_invoice", columnList = "invoiceId"),
        @Index(name = "idx_line_item_operation", columnList = "operationId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InvoiceLineItem extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID de la facture parente
     */
    @Column(name = "invoiceId", nullable = false)
    @NotNull(message = "L'ID de facture est obligatoire")
    private Long invoiceId;

    /**
     * ID de l'opération maritime concernée
     */
    @Column(name = "operationId", nullable = false)
    @NotNull(message = "L'opération est obligatoire")
    private Long operationId;

    /**
     * Description de la prestation
     */
    @Column(name = "description", nullable = false, length = 500)
    @NotBlank(message = "La description est obligatoire")
    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    /**
     * Quantité de la prestation
     */
    @Column(name = "quantite", nullable = false)
    @NotNull(message = "La quantité est obligatoire")
    @DecimalMin(value = "0.01", message = "La quantité doit être supérieure à 0")
    private BigDecimal quantite;

    /**
     * Prix unitaire en Francs CFA (XOF)
     */
    @Column(name = "prixUnitaireXOF", precision = 15, scale = 2, nullable = false)
    @NotNull(message = "Le prix unitaire XOF est obligatoire")
    @DecimalMin(value = "0.00", message = "Le prix unitaire XOF ne peut pas être négatif")
    private BigDecimal prixUnitaireXOF;

    /**
     * Prix unitaire en Euros (optionnel)
     */
    @Column(name = "prixUnitaireEURO", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "Le prix unitaire EURO ne peut pas être négatif")
    private BigDecimal prixUnitaireEURO;

    /**
     * Montant total de la ligne en XOF (quantité × prix unitaire XOF)
     */
    @Column(name = "montantXOF", precision = 15, scale = 2, nullable = false)
    @NotNull(message = "Le montant XOF est obligatoire")
    @DecimalMin(value = "0.00", message = "Le montant XOF ne peut pas être négatif")
    private BigDecimal montantXOF;

    /**
     * Montant total de la ligne en EURO (optionnel)
     */
    @Column(name = "montantEURO", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "Le montant EURO ne peut pas être négatif")
    private BigDecimal montantEURO;

    // Relations JPA

    /**
     * Relation avec la facture parente
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoiceId", insertable = false, updatable = false)
    private Invoice invoice;

    /**
     * Relation avec l'opération maritime
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operationId", insertable = false, updatable = false)
    private Operation operation;

    // Méthodes utilitaires

    /**
     * Calcule automatiquement les montants avant sauvegarde
     */
    public void calculerMontants() {

        // Calcul du montant XOF
        if (quantite != null && prixUnitaireXOF != null) {
            montantXOF = quantite.multiply(prixUnitaireXOF);
        }

        // Calcul du montant EURO si prix unitaire EURO est défini
        if (quantite != null && prixUnitaireEURO != null) {
            montantEURO = quantite.multiply(prixUnitaireEURO);
        }
    }

    /**
     * Calcule le prix total de la ligne
     */
    @Transient
    public BigDecimal getPrixTotal() {
        return montantXOF != null ? montantXOF : BigDecimal.ZERO;
    }

    /**
     * Vérifie si la ligne a un montant en euros
     */
    @Transient
    public boolean hasEuroAmount() {
        return montantEURO != null && montantEURO.compareTo(BigDecimal.ZERO) > 0;
    }
}
