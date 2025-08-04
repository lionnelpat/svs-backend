package sn.svs.backoffice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
 * DTOs pour l'entité ExpenseSupplier
 * SVS - Dakar, Sénégal
 */
public class ExpenseSupplierDTO {

    /**
     * DTO pour la création d'un fournisseur
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour créer un nouveau fournisseur")
    public static class CreateRequest {

        @NotBlank(message = "Le nom du fournisseur est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        @Schema(description = "Nom du fournisseur", example = "Entreprise DIALLO & Fils")
        private String nom;

        @Size(max = 500, message = "L'adresse ne peut pas dépasser 500 caractères")
        @Schema(description = "Adresse complète", example = "Avenue Bourguiba, Dakar, Sénégal")
        private String adresse;

        @Pattern(regexp = "^[+]?[0-9\\s\\-()]{8,20}$", message = "Format de téléphone invalide")
        @Schema(description = "Numéro de téléphone", example = "+221 77 123 45 67")
        private String telephone;

        @Email(message = "Format d'email invalide")
        @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
        @Schema(description = "Adresse email", example = "contact@diallo-fils.sn")
        private String email;

        @Size(max = 50, message = "Le numéro de registre de commerce ne peut pas dépasser 50 caractères")
        @Schema(description = "Numéro de registre de commerce", example = "SN-DKR-2023-A-1234")
        private String rccm;

        @Size(max = 20, message = "Le numéro NINEA ne peut pas dépasser 20 caractères")
        @Pattern(regexp = "^[0-9]{10}$", message = "Le numéro NINEA doit contenir exactement 10 chiffres")
        @Schema(description = "Numéro NINEA", example = "1234567890")
        private String ninea;

    }

    /**
     * DTO pour la mise à jour d'un fournisseur
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour mettre à jour un fournisseur")
    public static class UpdateRequest {

        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        @Schema(description = "Nom du fournisseur")
        private String nom;

        @Size(max = 500, message = "L'adresse ne peut pas dépasser 500 caractères")
        @Schema(description = "Adresse complète")
        private String adresse;

        @Pattern(regexp = "^[+]?[0-9\\s\\-()]{8,20}$", message = "Format de téléphone invalide")
        @Schema(description = "Numéro de téléphone")
        private String telephone;

        @Email(message = "Format d'email invalide")
        @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
        @Schema(description = "Adresse email")
        private String email;

        @Size(max = 50, message = "Le numéro de registre de commerce ne peut pas dépasser 50 caractères")
        @Schema(description = "Numéro de registre de commerce")
        private String rccm;

        @Size(max = 20, message = "Le numéro NINEA ne peut pas dépasser 20 caractères")
        @Pattern(regexp = "^[0-9]{10}$", message = "Le numéro NINEA doit contenir exactement 10 chiffres")
        @Schema(description = "Numéro NINEA")
        private String ninea;

        @Schema(description = "Statut actif/inactif")
        private Boolean active;
    }

    /**
     * DTO de réponse pour un fournisseur
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données d'un fournisseur")
    public static class Response {

        @Schema(description = "Identifiant unique", example = "1")
        private Long id;

        @Schema(description = "Nom du fournisseur", example = "Entreprise DIALLO & Fils")
        private String nom;

        @Schema(description = "Adresse complète", example = "Avenue Bourguiba, Dakar, Sénégal")
        private String adresse;

        @Schema(description = "Numéro de téléphone", example = "+221 77 123 45 67")
        private String telephone;

        @Schema(description = "Adresse email", example = "contact@diallo-fils.sn")
        private String email;

        @Schema(description = "Numéro de registre de commerce", example = "SN-DKR-2023-A-1234")
        private String rccm;

        @Schema(description = "Numéro NINEA", example = "1234567890")
        private String ninea;

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
    @Schema(description = "Filtres pour la recherche de fournisseurs")
    public static class SearchFilter {

        @Schema(description = "Recherche textuelle (nom, ninea, email, téléphone)", example = "diallo")
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
    @Schema(description = "Réponse paginée des fournisseurs")
    public static class PageResponse {

        @Schema(description = "Liste des fournisseurs")
        private List<Response> suppliers;

        @Schema(description = "Nombre total d'éléments", example = "45")
        private Long total;

        @Schema(description = "Numéro de page actuelle", example = "0")
        private Integer page;

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
    @Schema(description = "Fournisseur simplifié pour les listes")
    public static class Summary {

        @Schema(description = "Identifiant", example = "1")
        private Long id;

        @Schema(description = "Nom du fournisseur", example = "Entreprise DIALLO & Fils")
        private String nom;

        @Schema(description = "Numéro de téléphone", example = "+221 77 123 45 67")
        private String telephone;

        @Schema(description = "Adresse email", example = "contact@diallo-fils.sn")
        private String email;
    }
}
