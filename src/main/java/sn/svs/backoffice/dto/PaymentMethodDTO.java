package sn.svs.backoffice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs pour l'entité PaymentMethod
 */
public class PaymentMethodDTO {

    /**
     * DTO pour la création d'un mode de paiement
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour créer un nouveau mode de paiement")
    public static class CreateRequest {

        @NotBlank(message = "Le nom du mode de paiement est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        @Schema(description = "Nom du mode de paiement", example = "Carte de crédit")
        private String nom;

        @NotBlank(message = "Le code du mode de paiement est obligatoire")
        @Size(min = 2, max = 20, message = "Le code doit contenir entre 2 et 20 caractères")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "Le code ne peut contenir que des lettres majuscules, chiffres et underscores")
        @Schema(description = "Code unique du mode de paiement", example = "CREDIT_CARD")
        private String code;

        @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
        @Schema(description = "Description du mode de paiement", example = "Paiement par carte de crédit Visa/Mastercard")
        private String description;

        @Schema(description = "Indique si le mode de paiement est actif", example = "true")
        @Builder.Default
        private Boolean actif = true;
    }

    /**
     * DTO pour la mise à jour d'un mode de paiement
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour mettre à jour un mode de paiement")
    public static class UpdateRequest {

        @NotBlank(message = "Le nom du mode de paiement est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        @Schema(description = "Nom du mode de paiement", example = "Carte de crédit Visa")
        private String nom;

        @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
        @Schema(description = "Description du mode de paiement", example = "Paiement par carte de crédit Visa uniquement")
        private String description;

        @Schema(description = "Indique si le mode de paiement est actif", example = "true")
        private Boolean actif;
    }

    /**
     * DTO de réponse pour un mode de paiement
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Informations d'un mode de paiement")
    public static class Response {

        @Schema(description = "Identifiant unique du mode de paiement", example = "1")
        private Long id;

        @Schema(description = "Nom du mode de paiement", example = "Carte de crédit")
        private String nom;

        @Schema(description = "Code unique du mode de paiement", example = "CREDIT_CARD")
        private String code;

        @Schema(description = "Description du mode de paiement", example = "Paiement par carte de crédit Visa/Mastercard")
        private String description;

        @Schema(description = "Indique si le mode de paiement est actif", example = "true")
        private Boolean actif;

        @Schema(description = "Date de création", example = "2024-12-07T10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        @Schema(description = "Date de dernière modification", example = "2024-12-07T15:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;

        @Schema(description = "Utilisateur qui a créé le mode de paiement", example = "admin@svs-maritime.sn")
        private String createdBy;

        @Schema(description = "Utilisateur qui a modifié le mode de paiement", example = "manager@svs-maritime.sn")
        private String updatedBy;
    }

    /**
     * DTO pour les filtres de recherche
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Filtres pour la recherche de modes de paiement")
    public static class SearchFilter {

        @Schema(description = "Recherche textuelle (nom, code, description)", example = "carte")
        private String search;

        @Schema(description = "Filtre par statut actif", example = "true")
        private Boolean active;

        @Schema(description = "Numéro de page (commence à 0)", example = "0", defaultValue = "0")
        @Builder.Default
        private Integer page = 0;

        @Schema(description = "Taille de la page", example = "20", defaultValue = "20")
        @Builder.Default
        private Integer size = 20;

        @Schema(description = "Champ de tri", example = "nom", defaultValue = "nom")
        @Builder.Default
        private String sortBy = "nom";

        @Schema(description = "Direction du tri", example = "asc", defaultValue = "asc")
        @Builder.Default
        private String sortDirection = "asc";
    }

    /**
     * DTO de réponse paginée
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Réponse paginée des modes de paiement")
    public static class PageResponse {

        @Schema(description = "Liste des modes de paiement")
        private List<Response> paymentMethods;

        @Schema(description = "Nombre total d'éléments", example = "25")
        private Long totalElements;

        @Schema(description = "Numéro de page actuelle", example = "0")
        private Integer currentPage;

        @Schema(description = "Taille de la page", example = "20")
        private Integer size;

        @Schema(description = "Nombre total de pages", example = "3")
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
     * DTO simplifié pour les listes déroulantes
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Mode de paiement simplifié")
    public static class Summary {

        @Schema(description = "Identifiant unique", example = "1")
        private Long id;

        @Schema(description = "Nom du mode de paiement", example = "Carte de crédit")
        private String nom;

        @Schema(description = "Code du mode de paiement", example = "CREDIT_CARD")
        private String code;
    }



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Stats sur les methodes de paiement")
    public static class Stats {
        private long totalCount;
        private long activeCount;
        private long inactiveCount;
    }
}
