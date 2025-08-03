package sn.svs.backoffice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.svs.backoffice.domain.ennumeration.Currency;
import sn.svs.backoffice.domain.ennumeration.ExpenseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs pour l'entité Expense
 * SVS - Dakar, Sénégal
 */
public class ExpenseDTO {

    /**
     * DTO pour la création d'une dépense
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour créer une nouvelle dépense")
    public static class CreateRequest {

//        @Size(max = 20, message = "Le numéro ne peut pas dépasser 20 caractères")
//        @Schema(description = "Numéro de la dépense (généré automatiquement si vide)", example = "DEP-20250611-001")
//        private String numero;

        @NotBlank(message = "Le titre est obligatoire")
        @Size(min = 2, max = 255, message = "Le titre doit contenir entre 2 et 255 caractères")
        @Schema(description = "Titre de la dépense", example = "Carburant pour navire CMS-001")
        private String titre;

        @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
        @Schema(description = "Description détaillée", example = "Achat de 500L de gasoil pour le navire CMS-001 en mission vers Saint-Louis")
        private String description;

        @NotNull(message = "La catégorie est obligatoire")
        @Schema(description = "ID de la catégorie de dépense", example = "1")
        private Long categorieId;

        @Schema(description = "ID du fournisseur (optionnel)", example = "2")
        private Long fournisseurId;

        @NotNull(message = "La date de dépense est obligatoire")
        @Schema(description = "Date de la dépense", example = "2025-06-11")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateDepense;

        @NotNull(message = "Le montant XOF est obligatoire")
        @DecimalMin(value = "0.01", message = "Le montant XOF doit être supérieur à 0")
        @Schema(description = "Montant en Francs CFA", example = "325000.00")
        private BigDecimal montantXOF;

        @DecimalMin(value = "0.01", message = "Le montant EURO doit être supérieur à 0")
        @Schema(description = "Montant en Euros (optionnel)", example = "495.42")
        private BigDecimal montantEURO;

        @DecimalMin(value = "0.01", message = "Le taux de change doit être supérieur à 0")
        @Schema(description = "Taux de change EUR/XOF (optionnel)", example = "656.00")
        private BigDecimal tauxChange;

        @NotNull(message = "La devise est obligatoire")
        @Schema(description = "Devise de la dépense", example = "XOF")
        private Currency devise;

        @NotNull(message = "Le mode de paiement est obligatoire")
        @Schema(description = "ID du mode de paiement", example = "1")
        private Long paymentMethodId;

        @Schema(description = "Statut de la dépense", example = "EN_ATTENTE", defaultValue = "EN_ATTENTE")
        @Builder.Default
        private ExpenseStatus statut = ExpenseStatus.EN_ATTENTE;
    }

    /**
     * DTO pour la mise à jour d'une dépense
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour mettre à jour une dépense")
    public static class UpdateRequest {

        @Size(max = 20, message = "Le numéro ne peut pas dépasser 20 caractères")
        @Schema(description = "Numéro de la dépense")
        private String numero;

        @Size(min = 2, max = 255, message = "Le titre doit contenir entre 2 et 255 caractères")
        @Schema(description = "Titre de la dépense")
        private String titre;

        @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
        @Schema(description = "Description détaillée")
        private String description;

        @Schema(description = "ID de la catégorie de dépense")
        private Long categorieId;

        @Schema(description = "ID du fournisseur")
        private Long fournisseurId;

        @Schema(description = "Date de la dépense")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateDepense;

        @DecimalMin(value = "0.01", message = "Le montant XOF doit être supérieur à 0")
        @Schema(description = "Montant en Francs CFA")
        private BigDecimal montantXOF;

        @DecimalMin(value = "0.01", message = "Le montant EURO doit être supérieur à 0")
        @Schema(description = "Montant en Euros")
        private BigDecimal montantEURO;

        @DecimalMin(value = "0.01", message = "Le taux de change doit être supérieur à 0")
        @Schema(description = "Taux de change EUR/XOF")
        private BigDecimal tauxChange;

        @Schema(description = "Devise de la dépense")
        private Currency devise;

        @Schema(description = "ID du mode de paiement")
        private Long paymentMethodId;

        @Schema(description = "Statut de la dépense")
        private ExpenseStatus statut;

        @Schema(description = "Statut actif/inactif")
        private Boolean active;
    }

    /**
     * DTO de réponse pour une dépense
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données d'une dépense")
    public static class Response {

        @Schema(description = "Identifiant unique", example = "1")
        private Long id;

        @Schema(description = "Numéro de la dépense", example = "DEP-20250611-001")
        private String numero;

        @Schema(description = "Titre de la dépense", example = "Carburant pour navire CMS-001")
        private String titre;

        @Schema(description = "Description", example = "Achat de 500L de gasoil")
        private String description;

        @Schema(description = "ID de la catégorie", example = "1")
        private Long categorieId;

        @Schema(description = "Nom de la catégorie", example = "Carburant")
        private String categorieNom;

        @Schema(description = "ID du fournisseur", example = "2")
        private Long fournisseurId;

        @Schema(description = "Nom du fournisseur", example = "Total Sénégal")
        private String fournisseurNom;

        @Schema(description = "Date de la dépense", example = "2025-06-11")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateDepense;

        @Schema(description = "Montant en Francs CFA", example = "325000.00")
        private BigDecimal montantXOF;

        @Schema(description = "Montant en Euros", example = "495.42")
        private BigDecimal montantEURO;

        @Schema(description = "Taux de change EUR/XOF", example = "656.00")
        private BigDecimal tauxChange;

        @Schema(description = "Devise", example = "XOF")
        private Currency devise;

        @Schema(description = "ID du mode de paiement", example = "1")
        private Long paymentMethodId;

        @Schema(description = "Nom du mode de paiement", example = "Virement bancaire")
        private String paymentMethodNom;

        @Schema(description = "Statut", example = "EN_ATTENTE")
        private ExpenseStatus statut;

        @Schema(description = "Libellé du statut", example = "En attente")
        private String statutLabel;

        @Schema(description = "Date de création")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        @Schema(description = "Date de modification")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;

        @Schema(description = "Statut actif", example = "true")
        private Boolean active;
    }

    /**
     * DTO pour les filtres de recherche
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Filtres pour la recherche de dépenses")
    public static class SearchFilter {

        @Schema(description = "Recherche textuelle (titre, numéro, description)", example = "carburant")
        private String search;

        @Schema(description = "Filtre par catégorie", example = "1")
        private Long categorieId;

        @Schema(description = "Filtre par fournisseur", example = "2")
        private Long fournisseurId;

        @Schema(description = "Filtre par statut", example = "EN_ATTENTE")
        private ExpenseStatus statut;

        @Schema(description = "Filtre par mode de paiement", example = "1")
        private Long paymentMethodId;

        @Schema(description = "Filtre par devise", example = "XOF")
        private Currency devise;

        @Schema(description = "Montant minimum", example = "100000")
        private BigDecimal minAmount;

        @Schema(description = "Montant maximum", example = "500000")
        private BigDecimal maxAmount;

        @Schema(description = "Date de début", example = "2025-01-01")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @Schema(description = "Date de fin", example = "2025-12-31")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        @Schema(description = "Année", example = "2025")
        private Integer year;

        @Schema(description = "Mois", example = "6")
        private Integer month;

        @Schema(description = "Jour", example = "11")
        private Integer day;

        @Schema(description = "Filtre par statut actif", example = "true")
        private Boolean active;

        @Schema(description = "Numéro de page (commence à 0)", example = "0", defaultValue = "0")
        @Builder.Default
        private Integer page = 0;

        @Schema(description = "Taille de la page", example = "20", defaultValue = "20")
        @Builder.Default
        private Integer size = 20;

        @Schema(description = "Champ de tri", example = "dateDepense", defaultValue = "id")
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
    @Schema(description = "Réponse paginée des dépenses")
    public static class PageResponse {

        @Schema(description = "Liste des dépenses")
        private List<Response> expenses;

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
     * DTO pour les statistiques des dépenses
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Statistiques des dépenses")
    public static class StatsResponse {

        @Schema(description = "Nombre total de dépenses", example = "245")
        private Long totalExpenses;

        @Schema(description = "Montant total en XOF", example = "15750000.00")
        private BigDecimal totalAmountXOF;

        @Schema(description = "Montant total en EUR", example = "24000.50")
        private BigDecimal totalAmountEUR;

        @Schema(description = "Répartition par statut")
        private List<StatutCount> statutRepartition;

        @Schema(description = "Répartition par catégorie")
        private List<CategorieCount> categorieRepartition;

        @Schema(description = "Évolution mensuelle")
        private List<MonthlyExpense> evolutionMensuelle;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(description = "Compteur par statut")
        public static class StatutCount {
            @Schema(description = "Statut", example = "EN_ATTENTE")
            private ExpenseStatus statut;

            @Schema(description = "Libellé", example = "En attente")
            private String label;

            @Schema(description = "Nombre", example = "45")
            private Long count;

            @Schema(description = "Montant total", example = "2500000.00")
            private BigDecimal totalAmount;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(description = "Compteur par catégorie")
        public static class CategorieCount {
            @Schema(description = "ID catégorie", example = "1")
            private Long categorieId;

            @Schema(description = "Nom catégorie", example = "Carburant")
            private String categorieNom;

            @Schema(description = "Nombre", example = "67")
            private Long count;

            @Schema(description = "Montant total", example = "8500000.00")
            private BigDecimal totalAmount;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(description = "Dépenses mensuelles")
        public static class MonthlyExpense {
            @Schema(description = "Année", example = "2025")
            private Integer year;

            @Schema(description = "Mois", example = "6")
            private Integer month;

            @Schema(description = "Libellé du mois", example = "Juin 2025")
            private String monthLabel;

            @Schema(description = "Nombre de dépenses", example = "42")
            private Long count;

            @Schema(description = "Montant total", example = "3200000.00")
            private BigDecimal totalAmount;
        }
    }

    /**
     * DTO pour le changement de statut
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour changer le statut d'une dépense")
    public static class StatusChangeRequest {

        @NotNull(message = "Le nouveau statut est obligatoire")
        @Schema(description = "Nouveau statut", example = "APPROUVEE")
        private ExpenseStatus statut;

        @Size(max = 500, message = "Le commentaire ne peut pas dépasser 500 caractères")
        @Schema(description = "Commentaire optionnel", example = "Dépense approuvée par le manager")
        private String commentaire;
    }
}
