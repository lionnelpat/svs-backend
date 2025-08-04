package sn.svs.backoffice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.svs.backoffice.domain.ennumeration.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs pour l'entité Invoice
 * SVS - Dakar, Sénégal
 */
public class InvoiceDTO {

    /**
     * DTO pour la création d'une facture
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour créer une nouvelle facture")
    public static class CreateRequest {

        @NotNull(message = "La compagnie est obligatoire")
        @Schema(description = "ID de la compagnie maritime", example = "1")
        private Long compagnieId;

        @NotNull(message = "Le navire est obligatoire")
        @Schema(description = "ID du navire", example = "1")
        private Long navireId;

        @NotNull(message = "La date de facture est obligatoire")
        @Schema(description = "Date d'émission de la facture", example = "2024-12-15")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateFacture;

        @NotNull(message = "La date d'échéance est obligatoire")
        @Schema(description = "Date d'échéance de paiement", example = "2025-01-15")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateEcheance;

        @NotNull(message = "Le taux de TVA est obligatoire")
        @DecimalMin(value = "0.00", message = "Le taux de TVA ne peut pas être négatif")
        @DecimalMax(value = "100.00", message = "Le taux de TVA ne peut pas dépasser 100%")
        @Schema(description = "Taux de TVA en pourcentage", example = "18.00")
        private BigDecimal tauxTva;

        @NotNull(message = "Les prestations sont obligatoires")
        @NotEmpty(message = "Au moins une prestation est obligatoire")
        @Valid
        @Schema(description = "Liste des prestations facturées")
        private List<CreateInvoiceLineItemRequest> prestations;

        @Size(max = 1000, message = "Les notes ne peuvent pas dépasser 1000 caractères")
        @Schema(description = "Notes ou commentaires sur la facture", example = "Facturation des prestations portuaires")
        private String notes;
    }

    /**
     * DTO pour la création d'une ligne de prestation
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour créer une ligne de prestation")
    public static class CreateInvoiceLineItemRequest {

        @NotNull(message = "L'opération est obligatoire")
        @Schema(description = "ID de l'opération maritime", example = "1")
        private Long operationId;

        @NotBlank(message = "La description est obligatoire")
        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        @Schema(description = "Description de la prestation", example = "Pilotage d'entrée")
        private String description;

        @NotNull(message = "La quantité est obligatoire")
        @DecimalMin(value = "0.01", message = "La quantité doit être supérieure à 0")
        @Schema(description = "Quantité de la prestation", example = "2.00")
        private BigDecimal quantite;

        @NotNull(message = "Le prix unitaire XOF est obligatoire")
        @DecimalMin(value = "0.00", message = "Le prix unitaire XOF ne peut pas être négatif")
        @Schema(description = "Prix unitaire en Francs CFA", example = "150000.00")
        private BigDecimal prixUnitaireXOF;

        @DecimalMin(value = "0.00", message = "Le prix unitaire EURO ne peut pas être négatif")
        @Schema(description = "Prix unitaire en Euros (optionnel)", example = "228.67")
        private BigDecimal prixUnitaireEURO;
    }

    /**
     * DTO pour la mise à jour d'une facture
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour mettre à jour une facture")
    public static class UpdateRequest {

        @Schema(description = "ID de la compagnie maritime")
        private Long compagnieId;

        @Schema(description = "ID du navire")
        private Long navireId;

        @Schema(description = "Date d'émission de la facture")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateFacture;

        @Schema(description = "Date d'échéance de paiement")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateEcheance;

        @DecimalMin(value = "0.00", message = "Le taux de TVA ne peut pas être négatif")
        @DecimalMax(value = "100.00", message = "Le taux de TVA ne peut pas dépasser 100%")
        @Schema(description = "Taux de TVA en pourcentage")
        private BigDecimal tauxTva;

        @Valid
        @Schema(description = "Liste des prestations facturées")
        private List<CreateInvoiceLineItemRequest> prestations;

        @Schema(description = "Statut de la facture")
        private InvoiceStatus statut;

        @Size(max = 1000, message = "Les notes ne peuvent pas dépasser 1000 caractères")
        @Schema(description = "Notes ou commentaires sur la facture")
        private String notes;

        @Schema(description = "Statut actif/inactif")
        private Boolean active;
    }

    /**
     * DTO de réponse pour une facture
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données d'une facture")
    public static class Response {

        @Schema(description = "Identifiant unique", example = "1")
        private Long id;

        @Schema(description = "Numéro de facture", example = "FAC-2024-000001")
        private String numero;

        @Schema(description = "ID de la compagnie", example = "1")
        private Long compagnieId;

        @Schema(description = "Informations de la compagnie")
        private CompanyDTO.Response compagnie;

        @Schema(description = "ID du navire", example = "1")
        private Long navireId;

        @Schema(description = "Informations du navire")
        private ShipDTO.Response navire;

        @Schema(description = "Date d'émission", example = "2024-12-15")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateFacture;

        @Schema(description = "Date d'échéance", example = "2025-01-15")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateEcheance;

        @Schema(description = "Sous-total HT", example = "300000.00")
        private BigDecimal sousTotal;

        @Schema(description = "Montant TVA", example = "54000.00")
        private BigDecimal tva;

        @Schema(description = "Taux TVA en %", example = "18.00")
        private BigDecimal tauxTva;

        @Schema(description = "Montant total TTC", example = "354000.00")
        private BigDecimal montantTotal;

        @Schema(description = "Statut de la facture", example = "EMISE")
        private InvoiceStatus statut;

        @Schema(description = "Notes", example = "Facturation des prestations portuaires")
        private String notes;

        @Schema(description = "Liste des prestations")
        private List<InvoiceLineItemResponse> prestations;

        @Schema(description = "Date de création")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        @Schema(description = "Date de modification")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;

        @Schema(description = "Statut actif", example = "true")
        private Boolean active;

        @Schema(description = "Facture en retard", example = "false")
        private Boolean enRetard;

        @Schema(description = "Facture modifiable", example = "true")
        private Boolean modifiable;

        @Schema(description = "Facture supprimable", example = "true")
        private Boolean supprimable;
    }

    /**
     * DTO de réponse pour une ligne de prestation
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données d'une ligne de prestation")
    public static class InvoiceLineItemResponse {

        @Schema(description = "Identifiant unique", example = "1")
        private Long id;

        @Schema(description = "ID de l'opération", example = "1")
        private Long operationId;

        @Schema(description = "Informations de l'opération")
        private OperationDTO.Response operation;

        @Schema(description = "Description", example = "Pilotage d'entrée")
        private String description;

        @Schema(description = "Quantité", example = "2.00")
        private BigDecimal quantite;

        @Schema(description = "Prix unitaire XOF", example = "150000.00")
        private BigDecimal prixUnitaireXOF;

        @Schema(description = "Prix unitaire EURO", example = "228.67")
        private BigDecimal prixUnitaireEURO;

        @Schema(description = "Montant total XOF", example = "300000.00")
        private BigDecimal montantXOF;

        @Schema(description = "Montant total EURO", example = "457.34")
        private BigDecimal montantEURO;
    }

    /**
     * DTO pour les filtres de recherche
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Filtres pour la recherche de factures")
    public static class SearchFilter {

        @Schema(description = "Recherche textuelle (numéro, notes)", example = "FAC-2024")
        private String search;

        @Schema(description = "Filtre par compagnie", example = "1")
        private Long compagnieId;

        @Schema(description = "Filtre par navire", example = "1")
        private Long navireId;

        @Schema(description = "Filtre par statut", example = "EMISE")
        private InvoiceStatus statut;

        @Schema(description = "Date de début", example = "2024-01-01")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateDebut;

        @Schema(description = "Date de fin", example = "2024-12-31")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateFin;

        @Schema(description = "Montant minimum", example = "100000")
        private BigDecimal minAmount;

        @Schema(description = "Montant maximum", example = "500000")
        private BigDecimal maxAmount;

        @Schema(description = "Filtre par mois", example = "12")
        @Min(value = 1, message = "Le mois doit être entre 1 et 12")
        @Max(value = 12, message = "Le mois doit être entre 1 et 12")
        private Integer mois;

        @Schema(description = "Filtre par année", example = "2024")
        @Min(value = 2020, message = "L'année doit être supérieure ou égale à 2020")
        private Integer annee;

        @Schema(description = "Filtre par statut actif", example = "true")
        private Boolean active;

        @Schema(description = "Numéro de page (commence à 0)", example = "0", defaultValue = "0")
        @Builder.Default
        private Integer page = 0;

        @Schema(description = "Taille de la page", example = "20", defaultValue = "20")
        @Builder.Default
        private Integer size = 20;

        @Schema(description = "Champ de tri", example = "dateFacture", defaultValue = "id")
        @Builder.Default
        private String sortBy = "id";

        @Schema(description = "Direction du tri", example = "desc", defaultValue = "asc")
        @Builder.Default
        private String sortDirection = "asc";
    }

    /**
     * DTO de réponse pour la liste paginée
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Réponse paginée des factures")
    public static class PageResponse {

        @Schema(description = "Liste des factures")
        private List<Response> invoices;

        @Schema(description = "Nombre total d'éléments", example = "150")
        private Long total;

        @Schema(description = "Numéro de page actuelle", example = "0")
        private Integer page;

        @Schema(description = "Taille de la page", example = "20")
        private Integer size;

        @Schema(description = "Nombre total de pages", example = "8")
        private Integer totalPages;

        @Schema(description = "Indique s'il s'agit de la première page", example = "true")
        private Boolean first;

        @Schema(description = "Indique s'il s'agit de la dernière page", example = "false")
        private Boolean last;

        @Schema(description = "Indique s'il y a une page suivante", example = "true")
        private Boolean hasNext;

        @Schema(description = "Indique s'il y a une page précédente", example = "false")
        private Boolean hasPrevious;
    }

    /**
     * DTO pour les statistiques des factures
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Statistiques des factures")
    public static class StatisticsResponse {

        @Schema(description = "Nombre total de factures", example = "150")
        private Long totalFactures;

        @Schema(description = "Montant total en XOF", example = "50000000.00")
        private BigDecimal totalMontantXOF;

        @Schema(description = "Montant total en EURO", example = "76218.00")
        private BigDecimal totalMontantEURO;

        @Schema(description = "Nombre de factures en attente", example = "25")
        private Long facturesEnAttente;

        @Schema(description = "Nombre de factures payées", example = "100")
        private Long facturesPayees;

        @Schema(description = "Nombre de factures en retard", example = "15")
        private Long facturesEnRetard;

        @Schema(description = "Évolution mensuelle")
        private List<MonthlyInvoiceStatsResponse> facturesParMois;

        @Schema(description = "Top compagnies par chiffre d'affaires")
        private List<CompanyInvoiceStatsResponse> topCompagnies;
    }

    /**
     * DTO pour les statistiques mensuelles
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Statistiques mensuelles des factures")
    public static class MonthlyInvoiceStatsResponse {

        @Schema(description = "Mois", example = "12")
        private Integer mois;

        @Schema(description = "Année", example = "2024")
        private Integer annee;

        @Schema(description = "Nombre de factures", example = "25")
        private Long nombreFactures;

        @Schema(description = "Montant total XOF", example = "5000000.00")
        private BigDecimal montantTotalXOF;

        @Schema(description = "Montant total EURO", example = "7621.80")
        private BigDecimal montantTotalEURO;
    }

    /**
     * DTO pour les statistiques par compagnie
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Statistiques des factures par compagnie")
    public static class CompanyInvoiceStatsResponse {

        @Schema(description = "ID de la compagnie", example = "1")
        private Long compagnieId;

        @Schema(description = "Nom de la compagnie", example = "Compagnie Maritime du Sénégal")
        private String compagnieNom;

        @Schema(description = "Nombre de factures", example = "45")
        private Long nombreFactures;

        @Schema(description = "Montant total XOF", example = "15000000.00")
        private BigDecimal montantTotalXOF;

        @Schema(description = "Montant total EURO", example = "22865.40")
        private BigDecimal montantTotalEURO;
    }

    /**
     * DTO pour les données d'impression de facture
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour l'impression d'une facture")
    public static class PrintDataResponse {

        @Schema(description = "Données de la facture")
        private Response invoice;

        @Schema(description = "Informations de l'entreprise")
        private EntrepriseInfoResponse entreprise;
    }

    /**
     * DTO pour les informations de l'entreprise
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Informations de l'entreprise pour l'impression")
    public static class EntrepriseInfoResponse {

        @Schema(description = "Nom de l'entreprise", example = "SVS Maritime Services")
        private String nom;

        @Schema(description = "Adresse", example = "Port de Dakar, Sénégal")
        private String adresse;

        @Schema(description = "Téléphone", example = "+221 33 123 45 67")
        private String telephone;

        @Schema(description = "Email", example = "contact@svs.sn")
        private String email;

        @Schema(description = "Numéro NINEA", example = "123456789")
        private String ninea;

        @Schema(description = "Numéro RCCM", example = "SN-DKR-2023-B-12345")
        private String rccm;

        @Schema(description = "URL du logo", example = "https://www.svs.sn/logo.png")
        private String logo;
    }

    /**
     * DTO pour les données d'export
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données d'export des factures")
    public static class ExportDataResponse {

        @Schema(description = "Numéro de facture", example = "FAC-2024-000001")
        private String numero;

        @Schema(description = "Nom de la compagnie", example = "Compagnie Maritime du Sénégal")
        private String compagnie;

        @Schema(description = "Nom du navire", example = "MV DAKAR")
        private String navire;

        @Schema(description = "Date de facture", example = "2024-12-15")
        private String dateFacture;

        @Schema(description = "Montant XOF", example = "354000.00")
        private BigDecimal montantXOF;

        @Schema(description = "Montant EURO", example = "539.67")
        private BigDecimal montantEURO;

        @Schema(description = "Statut", example = "EMISE")
        private String statut;

        @Schema(description = "Date d'échéance", example = "2025-01-15")
        private String dateEcheance;
    }

    /**
     * Classe interne pour les montants calculés
     */
    public static class CalculatedAmounts {
        private final BigDecimal sousTotal;
        private final BigDecimal tva;
        private final BigDecimal montantTotal;
        private final BigDecimal montantTotalEuro;

        public CalculatedAmounts(BigDecimal sousTotal, BigDecimal tva, BigDecimal montantTotal, BigDecimal montantTotalEuro) {
            this.sousTotal = sousTotal;
            this.tva = tva;
            this.montantTotal = montantTotal;
            this.montantTotalEuro = montantTotalEuro;
        }

        public BigDecimal getSousTotal() { return sousTotal; }
        public BigDecimal getTva() { return tva; }
        public BigDecimal getMontantTotal() { return montantTotal; }
        public BigDecimal getMontantTotalEuro() { return montantTotalEuro; }
    }

    /**
     * Classe interne pour les transitions de statut autorisées
     */
    public static class StatusTransition {
        /**
         * Définit les transitions de statut autorisées
         * BROUILLON -> EMISE, ANNULEE
         * EMISE -> PAYEE, ANNULEE, EN_RETARD
         * PAYEE -> (aucune transition)
         * ANNULEE -> BROUILLON (uniquement)
         * EN_RETARD -> PAYEE, ANNULEE
         */
        public static boolean isAllowed(InvoiceStatus from, InvoiceStatus to) {
            if (from == to) return true;

            return switch (from) {
                case BROUILLON -> to == InvoiceStatus.EMISE || to == InvoiceStatus.ANNULEE;
                case EMISE -> to == InvoiceStatus.PAYEE || to == InvoiceStatus.ANNULEE || to == InvoiceStatus.EN_RETARD;
                case PAYEE -> false; // Aucune transition depuis PAYEE
                case ANNULEE -> to == InvoiceStatus.BROUILLON;
                case EN_RETARD -> to == InvoiceStatus.PAYEE || to == InvoiceStatus.ANNULEE;
            };
        }
    }
}
