package sn.svs.backoffice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import sn.svs.backoffice.domain.ennumeration.Currency;
import sn.svs.backoffice.domain.ennumeration.ExpenseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entité représentant une dépense maritime
 * SVS - Dakar, Sénégal
 */
@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_numero", columnList = "numero"),
        @Index(name = "idx_expense_titre", columnList = "titre"),
        @Index(name = "idx_expense_categorie", columnList = "categorie_id"),
        @Index(name = "idx_expense_fournisseur", columnList = "fournisseur_id"),
        @Index(name = "idx_expense_date", columnList = "date_depense"),
        @Index(name = "idx_expense_statut", columnList = "statut"),
        @Index(name = "idx_expense_devise", columnList = "devise"),
        @Index(name = "idx_expense_payment_method", columnList = "payment_method_id"),
        @Index(name = "idx_expense_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@ToString(exclude = {"categorie", "fournisseur", "paymentMethod"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Expense extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "expense_seq")
    @SequenceGenerator(name = "expense_seq", sequenceName = "expense_sequence", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Le numéro de dépense est obligatoire")
    @Size(max = 20, message = "Le numéro ne peut pas dépasser 20 caractères")
    @Column(name = "numero", nullable = false, unique = true, length = 20)
    private String numero;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 2, max = 255, message = "Le titre doit contenir entre 2 et 255 caractères")
    @Column(name = "titre", nullable = false)
    private String titre;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull(message = "La catégorie est obligatoire")
    @Column(name = "categorie_id", nullable = false)
    private Long categorieId;

    @Column(name = "fournisseur_id")
    private Long fournisseurId;

    @NotNull(message = "La date de dépense est obligatoire")
    @Column(name = "date_depense", nullable = false)
    private LocalDate dateDepense;

    @NotNull(message = "Le montant XOF est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant XOF doit être supérieur à 0")
    @Column(name = "montant_xof", nullable = false, precision = 21, scale = 2)
    private BigDecimal montantXOF;

    @DecimalMin(value = "0.01", message = "Le montant EURO doit être supérieur à 0")
    @Column(name = "montant_euro", precision = 21, scale = 2)
    private BigDecimal montantEURO;

    @DecimalMin(value = "0.01", message = "Le taux de change doit être supérieur à 0")
    @Column(name = "taux_change", precision = 21, scale = 6)
    private BigDecimal tauxChange;

    @NotNull(message = "La devise est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "devise", nullable = false, length = 10)
    private Currency devise;

    @NotNull(message = "Le mode de paiement est obligatoire")
    @Column(name = "payment_method_id", nullable = false)
    private Long paymentMethodId;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private ExpenseStatus statut = ExpenseStatus.EN_ATTENTE;

    // Relations JPA (avec fetch LAZY pour optimisation)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", insertable = false, updatable = false)
    private ExpenseCategory categorie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id", insertable = false, updatable = false)
    private ExpenseSupplier fournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", insertable = false, updatable = false)
    private PaymentMethod paymentMethod;

    // Méthodes utilitaires
    public void approuver() {
        this.statut = ExpenseStatus.APPROUVEE;
    }

    public void rejeter() {
        this.statut = ExpenseStatus.REJETEE;
    }

    public void marquerPayee() {
        this.statut = ExpenseStatus.PAYEE;
    }

    public void remettreEnAttente() {
        this.statut = ExpenseStatus.EN_ATTENTE;
    }

    public boolean isApprouvee() {
        return ExpenseStatus.APPROUVEE.equals(this.statut);
    }

    public boolean isPayee() {
        return ExpenseStatus.PAYEE.equals(this.statut);
    }

    public boolean isRejetee() {
        return ExpenseStatus.REJETEE.equals(this.statut);
    }

    public boolean isEnAttente() {
        return ExpenseStatus.EN_ATTENTE.equals(this.statut);
    }

    public boolean isDeviseXOF() {
        return Currency.XOF.equals(this.devise);
    }

    public boolean isDeviseEUR() {
        return Currency.EUR.equals(this.devise);
    }

    /**
     * Calcule le montant en EURO si le taux de change est défini et la devise est XOF
     */
    public void calculerMontantEURO() {
        if (this.devise == Currency.XOF && this.tauxChange != null && this.montantXOF != null) {
            this.montantEURO = this.montantXOF.divide(this.tauxChange, 2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * Calcule le montant en XOF si le taux de change est défini et la devise est EURO
     */
    public void calculerMontantXOF() {
        if (this.devise == Currency.EUR && this.tauxChange != null && this.montantEURO != null) {
            this.montantXOF = this.montantEURO.multiply(this.tauxChange);
        }
    }

    /**
     * Retourne le montant principal selon la devise
     */
    public BigDecimal getMontantPrincipal() {
        return this.devise == Currency.XOF ? this.montantXOF : this.montantEURO;
    }

    /**
     * Génère un numéro de dépense automatique si non défini
     */
    public void genererNumeroSiAbsent() {
        if (this.numero == null || this.numero.trim().isEmpty()) {
            // Format: DEP-YYYYMMDD-XXX
            String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            this.numero = "DEP-" + dateStr + "-" + System.currentTimeMillis() % 1000;
        }
    }
}