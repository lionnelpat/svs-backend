package sn.svs.backoffice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs pour l'entité Company
 * SVS - Dakar, Sénégal
 */
public class CompanyDTO {

    /**
     * DTO pour la création d'une compagnie
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour créer une nouvelle compagnie")
    public static class CreateRequest {

        @NotBlank(message = "Le nom de la compagnie est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        @Schema(description = "Nom de la compagnie", example = "Compagnie Maritime du Sénégal")
        private String nom;

        @NotBlank(message = "La raison sociale est obligatoire")
        @Size(min = 2, max = 150, message = "La raison sociale doit contenir entre 2 et 150 caractères")
        @Schema(description = "Raison sociale de la compagnie", example = "CMS SARL")
        private String raisonSociale;

        @NotBlank(message = "L'adresse est obligatoire")
        @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
        @Schema(description = "Adresse de la compagnie", example = "Avenue Léopold Sédar Senghor, Dakar")
        private String adresse;

        @NotBlank(message = "La ville est obligatoire")
        @Size(min = 2, max = 100, message = "La ville doit contenir entre 2 et 100 caractères")
        @Schema(description = "Ville", example = "Dakar")
        private String ville;

        @NotBlank(message = "Le pays est obligatoire")
        @Size(min = 2, max = 100, message = "Le pays doit contenir entre 2 et 100 caractères")
        @Schema(description = "Pays", example = "Sénégal")
        private String pays;

        @NotBlank(message = "Le téléphone est obligatoire")
        @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
        @Schema(description = "Numéro de téléphone", example = "+221 33 123 45 67")
        private String telephone;

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Le format de l'email n'est pas valide")
        @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
        @Schema(description = "Adresse email", example = "contact@cms.sn")
        private String email;

        @Size(max = 100, message = "Le contact principal ne peut pas dépasser 100 caractères")
        @Schema(description = "Nom du contact principal", example = "Amadou Diallo")
        private String contactPrincipal;

        @Size(max = 20, message = "Le téléphone du contact ne peut pas dépasser 20 caractères")
        @Schema(description = "Téléphone du contact", example = "+221 77 123 45 67")
        private String telephoneContact;

        @Email(message = "Le format de l'email du contact n'est pas valide")
        @Size(max = 100, message = "L'email du contact ne peut pas dépasser 100 caractères")
        @Schema(description = "Email du contact", example = "a.diallo@cms.sn")
        private String emailContact;

        @Size(max = 50, message = "Le RCCM ne peut pas dépasser 50 caractères")
        @Schema(description = "Numéro RCCM", example = "SN-DKR-2023-B-12345")
        private String rccm;

        @Size(max = 20, message = "Le NINEA ne peut pas dépasser 20 caractères")
        @Schema(description = "Numéro NINEA", example = "123456789")
        private String ninea;

        @Size(max = 255, message = "Le site web ne peut pas dépasser 255 caractères")
        @Schema(description = "Site web", example = "https://www.cms.sn")
        private String siteWeb;
    }

    /**
     * DTO pour la mise à jour d'une compagnie
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données pour mettre à jour une compagnie")
    public static class UpdateRequest {

        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        @Schema(description = "Nom de la compagnie")
        private String nom;

        @Size(min = 2, max = 150, message = "La raison sociale doit contenir entre 2 et 150 caractères")
        @Schema(description = "Raison sociale de la compagnie")
        private String raisonSociale;

        @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
        @Schema(description = "Adresse de la compagnie")
        private String adresse;

        @Size(min = 2, max = 100, message = "La ville doit contenir entre 2 et 100 caractères")
        @Schema(description = "Ville")
        private String ville;

        @Size(min = 2, max = 100, message = "Le pays doit contenir entre 2 et 100 caractères")
        @Schema(description = "Pays")
        private String pays;

        @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
        @Schema(description = "Numéro de téléphone")
        private String telephone;

        @Email(message = "Le format de l'email n'est pas valide")
        @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
        @Schema(description = "Adresse email")
        private String email;

        @Size(max = 100, message = "Le contact principal ne peut pas dépasser 100 caractères")
        @Schema(description = "Nom du contact principal")
        private String contactPrincipal;

        @Size(max = 20, message = "Le téléphone du contact ne peut pas dépasser 20 caractères")
        @Schema(description = "Téléphone du contact")
        private String telephoneContact;

        @Email(message = "Le format de l'email du contact n'est pas valide")
        @Size(max = 100, message = "L'email du contact ne peut pas dépasser 100 caractères")
        @Schema(description = "Email du contact")
        private String emailContact;

        @Size(max = 50, message = "Le RCCM ne peut pas dépasser 50 caractères")
        @Schema(description = "Numéro RCCM")
        private String rccm;

        @Size(max = 20, message = "Le NINEA ne peut pas dépasser 20 caractères")
        @Schema(description = "Numéro NINEA")
        private String ninea;

        @Size(max = 255, message = "Le site web ne peut pas dépasser 255 caractères")
        @Schema(description = "Site web")
        private String siteWeb;

        @Schema(description = "Statut actif/inactif")
        private Boolean active;
    }

    /**
     * DTO de réponse pour une compagnie
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Données d'une compagnie")
    public static class Response {

        @Schema(description = "Identifiant unique", example = "1")
        private Long id;

        @Schema(description = "Nom de la compagnie", example = "Compagnie Maritime du Sénégal")
        private String nom;

        @Schema(description = "Raison sociale", example = "CMS SARL")
        private String raisonSociale;

        @Schema(description = "Adresse", example = "Avenue Léopold Sédar Senghor, Dakar")
        private String adresse;

        @Schema(description = "Ville", example = "Dakar")
        private String ville;

        @Schema(description = "Pays", example = "Sénégal")
        private String pays;

        @Schema(description = "Téléphone", example = "+221 33 123 45 67")
        private String telephone;

        @Schema(description = "Email", example = "contact@cms.sn")
        private String email;

        @Schema(description = "Contact principal", example = "Amadou Diallo")
        private String contactPrincipal;

        @Schema(description = "Téléphone du contact", example = "+221 77 123 45 67")
        private String telephoneContact;

        @Schema(description = "Email du contact", example = "a.diallo@cms.sn")
        private String emailContact;

        @Schema(description = "Numéro RCCM", example = "SN-DKR-2023-B-12345")
        private String rccm;

        @Schema(description = "Numéro NINEA", example = "123456789")
        private String ninea;

        @Schema(description = "Site web", example = "https://www.cms.sn")
        private String siteWeb;

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
    @Schema(description = "Filtres pour la recherche de compagnies")
    public static class SearchFilter {

        @Schema(description = "Recherche textuelle (nom, raison sociale, email)", example = "Maritime")
        private String search;

        @Schema(description = "Filtre par pays", example = "Sénégal")
        private String pays;

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
    @Schema(description = "Réponse paginée des compagnies")
    public static class PageResponse {

        @Schema(description = "Liste des compagnies")
        private List<Response> companies;

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
}
