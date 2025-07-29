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
 * DTOs pour l'entité ExpenseCategory
 * SVS - Dakar, Sénégal
 */
public class ExpenseCategoryDTO {

    /**
     * DTO pour la création d'une catégorie de dépense
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour créer une nouvelle catégorie de dépense")
    public static class CreateRequest {

        @NotBlank(message = "Le nom de la catégorie est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        @Schema(description = "Nom de la catégorie", example = "Transport")
        private String nom;

//        @Size(min = 2, max = 20, message = "Le code doit contenir entre 2 et 20 caractères")
//        @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Le code ne peut contenir que des lettres majuscules, chiffres, tirets et underscores")
//        @Schema(description = "Code unique de la catégorie", example = "TRANSPORT")
//        private String code;

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        @Schema(description = "Description de la catégorie", example = "Frais de transport et déplacement")
        private String description;
    }

    /**
     * DTO pour la mise à jour d'une catégorie de dépense
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour mettre à jour une catégorie de dépense")
    public static class UpdateRequest {

        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        @Schema(description = "Nom de la catégorie")
        private String nom;

//        @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Le code ne peut contenir que des lettres majuscules, chiffres, tirets et underscores")
//        @Schema(description = "Code unique de la catégorie")
//        private String code;

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        @Schema(description = "Description de la catégorie")
        private String description;

        @Schema(description = "Statut actif/inactif")
        private Boolean active;
    }

    /**
     * DTO de réponse pour une catégorie de dépense
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données d'une catégorie de dépense")
    public static class Response {

        @Schema(description = "Identifiant unique", example = "1")
        private Long id;

        @Schema(description = "Nom de la catégorie", example = "Transport")
        private String nom;

        @Schema(description = "Code de la catégorie", example = "TRANSPORT")
        private String code;

        @Schema(description = "Description de la catégorie", example = "Frais de transport et déplacement")
        private String description;

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
    @Schema(description = "Filtres pour la recherche de catégories")
    public static class SearchFilter {

        @Schema(description = "Recherche textuelle (nom, code, description)", example = "transport")
        private String search;

        @Schema(description = "Filtre par statut actif", example = "true")
        private Boolean active;

        @Schema(description = "Numéro de page (commence à 0)", example = "0", defaultValue = "0")
        @Builder.Default
        private Integer page = 0;

        @Schema(description = "Taille de la page", example = "20", defaultValue = "20")
        @Builder.Default
        private Integer size = 20;

        @Schema(description = "Champ de tri", example = "nom", defaultValue = "id")
        @Builder.Default
        private String sortBy = "id";

        @Schema(description = "Direction du tri", example = "asc", defaultValue = "asc")
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
    @Schema(description = "Réponse paginée des catégories")
    public static class PageResponse {

        @Schema(description = "Liste des catégories")
        private List<Response> categories;

        @Schema(description = "Nombre total d'éléments", example = "25")
        private Long total;

        @Schema(description = "Numéro de page actuelle", example = "0")
        private Integer page;

        @Schema(description = "Taille de la page", example = "20")
        private Integer size;

        @Schema(description = "Nombre total de pages", example = "2")
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
    @Schema(description = "Catégorie simplifiée pour les listes")
    public static class Summary {

        @Schema(description = "Identifiant", example = "1")
        private Long id;

        @Schema(description = "Nom de la catégorie", example = "Transport")
        private String nom;

        @Schema(description = "Code de la catégorie", example = "TRANSPORT")
        private String code;

    }
}
