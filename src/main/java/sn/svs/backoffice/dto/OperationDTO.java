package sn.svs.backoffice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs pour l'entité Operation
 * SVS - Dakar, Sénégal
 */
public class OperationDTO {

    /**
     * DTO pour la création d'une opération
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour créer une nouvelle opération maritime")
    public static class CreateRequest {

        @NotBlank(message = "Le nom de l'opération est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        @Schema(description = "Nom de l'opération", example = "Pilotage d'entrée")
        private String nom;

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        @Schema(description = "Description de l'opération", example = "Service de pilotage pour l'entrée au port")
        private String description;

        @NotBlank(message = "Le code de l'opération est obligatoire")
        @Size(min = 2, max = 20, message = "Le code doit contenir entre 2 et 20 caractères")
        @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Le code ne peut contenir que des lettres majuscules, chiffres, tirets et underscores")
        @Schema(description = "Code unique de l'opération", example = "PILOT_ENTRY")
        private String code;

        @NotNull(message = "Le prix en XOF est obligatoire")
        @DecimalMin(value = "0.0", inclusive = false, message = "Le prix en XOF doit être positif")
        @Digits(integer = 13, fraction = 2, message = "Le prix en XOF ne peut pas dépasser 13 chiffres avant la virgule et 2 après")
        @Schema(description = "Prix en francs CFA", example = "50000.00")
        private BigDecimal prixXOF;

        @DecimalMin(value = "0.0", inclusive = false, message = "Le prix en EURO doit être positif")
        @Digits(integer = 13, fraction = 2, message = "Le prix en EURO ne peut pas dépasser 13 chiffres avant la virgule et 2 après")
        @Schema(description = "Prix en euros (optionnel)", example = "76.22")
        private BigDecimal prixEURO;
    }

    /**
     * DTO pour la mise à jour d'une opération
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour mettre à jour une opération maritime")
    public static class UpdateRequest {

        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        @Schema(description = "Nom de l'opération")
        private String nom;

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        @Schema(description = "Description de l'opération")
        private String description;

        @Size(min = 2, max = 20, message = "Le code doit contenir entre 2 et 20 caractères")
        @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Le code ne peut contenir que des lettres majuscules, chiffres, tirets et underscores")
        @Schema(description = "Code unique de l'opération")
        private String code;

        @DecimalMin(value = "0.0", inclusive = false, message = "Le prix en XOF doit être positif")
        @Digits(integer = 13, fraction = 2, message = "Le prix en XOF ne peut pas dépasser 13 chiffres avant la virgule et 2 après")
        @Schema(description = "Prix en francs CFA")
        private BigDecimal prixXOF;

        @DecimalMin(value = "0.0", inclusive = false, message = "Le prix en EURO doit être positif")
        @Digits(integer = 13, fraction = 2, message = "Le prix en EURO ne peut pas dépasser 13 chiffres avant la virgule et 2 après")
        @Schema(description = "Prix en euros")
        private BigDecimal prixEURO;

        @Schema(description = "Statut actif/inactif")
        private Boolean active;
    }

    /**
     * DTO de réponse pour une opération
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données d'une opération maritime")
    public static class Response {

        @Schema(description = "Identifiant unique", example = "1")
        private Long id;

        @Schema(description = "Nom de l'opération", example = "Pilotage d'entrée")
        private String nom;

        @Schema(description = "Description de l'opération", example = "Service de pilotage pour l'entrée au port")
        private String description;

        @Schema(description = "Code de l'opération", example = "PILOT_ENTRY")
        private String code;

        @Schema(description = "Prix en francs CFA", example = "50000.00")
        private BigDecimal prixXOF;

        @Schema(description = "Prix en euros", example = "76.22")
        private BigDecimal prixEURO;

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
    @Schema(description = "Filtres pour la recherche d'opérations")
    public static class SearchFilter {

        @Schema(description = "Recherche textuelle (nom, code, description)", example = "pilotage")
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
    @Schema(description = "Réponse paginée des opérations")
    public static class PageResponse {

        @Schema(description = "Liste des opérations")
        private List<Response> operations;

        @Schema(description = "Nombre total d'éléments", example = "50")
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
    @Schema(description = "Opération simplifiée pour les listes")
    public static class Summary {

        @Schema(description = "Identifiant", example = "1")
        private Long id;

        @Schema(description = "Nom de l'opération", example = "Pilotage d'entrée")
        private String nom;

        @Schema(description = "Code de l'opération", example = "PILOT_ENTRY")
        private String code;

        @Schema(description = "Prix en francs CFA", example = "50000.00")
        private BigDecimal prixXOF;
    }
}
