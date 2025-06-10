package sn.svs.backoffice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.svs.backoffice.domain.ennumeration.ShipClassification;
import sn.svs.backoffice.domain.ennumeration.ShipFlag;
import sn.svs.backoffice.domain.ennumeration.ShipType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs pour l'entité Ship
 * SVS - Dakar, Sénégal
 */
public class ShipDTO {

    /**
     * DTO pour la création d'un navire
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour créer un nouveau navire")
    public static class CreateRequest {

        @NotBlank(message = "Le nom du navire est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        @Schema(description = "Nom du navire", example = "MV Dakar Express")
        private String nom;

        @NotBlank(message = "Le numéro IMO est obligatoire")
        @Size(min = 7, max = 10, message = "Le numéro IMO doit contenir entre 7 et 10 caractères")
        @Schema(description = "Numéro IMO du navire", example = "9123456")
        private String numeroIMO;

        @NotNull(message = "Le pavillon est obligatoire")
        @Schema(description = "Pavillon du navire", example = "SENEGAL")
        private ShipFlag pavillon;

        @NotNull(message = "Le type de navire est obligatoire")
        @Schema(description = "Type de navire", example = "CARGO")
        private ShipType typeNavire;

        @Min(value = 0, message = "Le nombre de passagers ne peut pas être négatif")
        @Schema(description = "Nombre de passagers (optionnel)", example = "150")
        private Integer nombrePassagers;

        @NotNull(message = "La compagnie est obligatoire")
        @Schema(description = "Identifiant de la compagnie propriétaire", example = "1")
        private Long compagnieId;

        @NotBlank(message = "Le port d'attache est obligatoire")
        @Size(min = 2, max = 100, message = "Le port d'attache doit contenir entre 2 et 100 caractères")
        @Schema(description = "Port d'attache", example = "Dakar")
        private String portAttache;

        @NotBlank(message = "Le numéro d'appel est obligatoire")
        @Size(min = 3, max = 20, message = "Le numéro d'appel doit contenir entre 3 et 20 caractères")
        @Schema(description = "Numéro d'appel radio", example = "6V7ABC")
        private String numeroAppel;

        @NotBlank(message = "Le numéro MMSI est obligatoire")
        @Size(min = 9, max = 9, message = "Le numéro MMSI doit contenir exactement 9 caractères")
        @Schema(description = "Numéro MMSI", example = "663123456")
        private String numeroMMSI;

        @NotNull(message = "La classification est obligatoire")
        @Schema(description = "Organisme de classification", example = "BUREAU_VERITAS")
        private ShipClassification classification;
    }

    /**
     * DTO pour la mise à jour d'un navire
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour mettre à jour un navire")
    public static class UpdateRequest {

        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        @Schema(description = "Nom du navire")
        private String nom;

        @Schema(description = "Pavillon du navire")
        private ShipFlag pavillon;

        @Schema(description = "Type de navire")
        private ShipType typeNavire;

        @Min(value = 0, message = "Le nombre de passagers ne peut pas être négatif")
        @Schema(description = "Nombre de passagers")
        private Integer nombrePassagers;

        @Schema(description = "Identifiant de la compagnie propriétaire")
        private Long compagnieId;

        @Size(min = 2, max = 100, message = "Le port d'attache doit contenir entre 2 et 100 caractères")
        @Schema(description = "Port d'attache")
        private String portAttache;

        @Size(min = 3, max = 20, message = "Le numéro d'appel doit contenir entre 3 et 20 caractères")
        @Schema(description = "Numéro d'appel radio")
        private String numeroAppel;

        @Size(min = 9, max = 9, message = "Le numéro MMSI doit contenir exactement 9 caractères")
        @Schema(description = "Numéro MMSI")
        private String numeroMMSI;

        @Schema(description = "Organisme de classification")
        private ShipClassification classification;

        @Schema(description = "Statut actif/inactif")
        private Boolean active;
    }

    /**
     * DTO de réponse pour un navire
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données d'un navire")
    public static class Response {

        @Schema(description = "Identifiant unique", example = "1")
        private Long id;

        @Schema(description = "Nom du navire", example = "MV Dakar Express")
        private String nom;

        @Schema(description = "Numéro IMO", example = "9123456")
        private String numeroIMO;

        @Schema(description = "Pavillon", example = "SENEGAL")
        private ShipFlag pavillon;

        @Schema(description = "Type de navire", example = "CARGO")
        private ShipType typeNavire;

        @Schema(description = "Nombre de passagers", example = "150")
        private Integer nombrePassagers;

        @Schema(description = "Identifiant de la compagnie", example = "1")
        private Long compagnieId;

        @Schema(description = "Informations de la compagnie")
        private CompanyDTO.Response compagnie;

        @Schema(description = "Port d'attache", example = "Dakar")
        private String portAttache;

        @Schema(description = "Numéro d'appel", example = "6V7ABC")
        private String numeroAppel;

        @Schema(description = "Numéro MMSI", example = "663123456")
        private String numeroMMSI;

        @Schema(description = "Classification", example = "BUREAU_VERITAS")
        private ShipClassification classification;

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
    @Schema(description = "Filtres pour la recherche de navires")
    public static class SearchFilter {

        @Schema(description = "Recherche textuelle (nom, IMO, MMSI, port d'attache)", example = "Dakar")
        private String search;

        @Schema(description = "Filtre par compagnie", example = "1")
        private Long compagnieId;

        @Schema(description = "Filtre par type de navire", example = "CARGO")
        private ShipType typeNavire;

        @Schema(description = "Filtre par pavillon", example = "SENEGAL")
        private ShipFlag pavillon;

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
    @Schema(description = "Réponse paginée des navires")
    public static class PageResponse {

        @Schema(description = "Liste des navires")
        private List<Response> ships;

        @Schema(description = "Nombre total d'éléments", example = "75")
        private Long total;

        @Schema(description = "Numéro de page actuelle", example = "0")
        private Integer page;

        @Schema(description = "Taille de la page", example = "20")
        private Integer size;

        @Schema(description = "Nombre total de pages", example = "4")
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
    @Schema(description = "Navire simplifié pour les listes")
    public static class Summary {

        @Schema(description = "Identifiant", example = "1")
        private Long id;

        @Schema(description = "Nom du navire", example = "MV Dakar Express")
        private String nom;

        @Schema(description = "Numéro IMO", example = "9123456")
        private String numeroIMO;

        @Schema(description = "Type de navire", example = "CARGO")
        private ShipType typeNavire;

        @Schema(description = "Nom de la compagnie", example = "CMS SARL")
        private String compagnieNom;
    }
}
