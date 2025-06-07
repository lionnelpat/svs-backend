package sn.svs.backoffice.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.svs.backoffice.config.GlobalExceptionHandler;
import sn.svs.backoffice.dto.CompanyDTO;
import sn.svs.backoffice.service.CompanyService;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST pour la gestion des compagnies
 * SVS - Dakar, Sénégal
 */
@Slf4j
@RestController
@RequestMapping("api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "API de gestion des compagnies maritimes")
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Créer une nouvelle compagnie
     */
    @PostMapping
    @Operation(
            summary = "Créer une nouvelle compagnie",
            description = "Crée une nouvelle compagnie maritime avec les informations fournies"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compagnie créée avec succès",
                    content = @Content(schema = @Schema(implementation = CompanyDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Compagnie déjà existante",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<CompanyDTO.Response> createCompany(
            @Valid @RequestBody CompanyDTO.CreateRequest createRequest) {

        log.info("Demande de création d'une compagnie: {}", createRequest.getNom());

        CompanyDTO.Response response = companyService.createCompany(createRequest);

        log.info("Compagnie créée avec succès - ID: {}", response.getId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Mettre à jour une compagnie existante
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Mettre à jour une compagnie",
            description = "Met à jour les informations d'une compagnie existante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compagnie mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = CompanyDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Compagnie non trouvée",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflit de données",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<CompanyDTO.Response> updateCompany(
            @Parameter(description = "Identifiant de la compagnie", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CompanyDTO.UpdateRequest updateRequest) {

        log.info("Demande de mise à jour de la compagnie ID: {}", id);

        CompanyDTO.Response response = companyService.updateCompany(id, updateRequest);

        log.info("Compagnie mise à jour avec succès - ID: {}", id);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer une compagnie par son identifiant
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Récupérer une compagnie",
            description = "Récupère les détails d'une compagnie par son identifiant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compagnie trouvée",
                    content = @Content(schema = @Schema(implementation = CompanyDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Compagnie non trouvée",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<CompanyDTO.Response> getCompany(
            @Parameter(description = "Identifiant de la compagnie", required = true)
            @PathVariable Long id) {

        log.debug("Demande de récupération de la compagnie ID: {}", id);

        CompanyDTO.Response response = companyService.getCompanyById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer toutes les compagnies avec pagination et recherche
     */
    @GetMapping
    @Operation(
            summary = "Lister toutes les compagnies",
            description = "Récupère la liste paginée de toutes les compagnies avec possibilité de recherche"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des compagnies récupérée",
                    content = @Content(schema = @Schema(implementation = CompanyDTO.PageResponse.class)))
    })
    public ResponseEntity<CompanyDTO.PageResponse> getAllCompanies(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Champ de tri", example = "nom")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direction du tri", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDirection,
            @Parameter(description = "Recherche textuelle (nom, raison sociale, email, ville)", example = "Maritime")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filtre par pays", example = "Sénégal")
            @RequestParam(required = false) String pays,
            @Parameter(description = "Filtre par statut actif", example = "true")
            @RequestParam(required = false) Boolean active) {

        log.debug("Demande de liste des compagnies - Page: {}, Taille: {}, Tri: {} {}, Recherche: '{}', Pays: '{}', Actif: {}",
                page, size, sortBy, sortDirection, search, pays, active);

        // Créer le filtre avec tous les paramètres
        CompanyDTO.SearchFilter filter = CompanyDTO.SearchFilter.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .search(search)
                .pays(pays)
                .active(active)
                .build();

        CompanyDTO.PageResponse response = companyService.searchCompanies(filter);

        log.debug("Recherche terminée - {} résultats trouvés sur {} total",
                response.getCompanies().size(), response.getTotal());

        return ResponseEntity.ok(response);
    }

    /**
     * Rechercher des compagnies avec filtres
     */
    @PostMapping("/search")
    @Operation(
            summary = "Rechercher des compagnies",
            description = "Recherche des compagnies avec filtres avancés"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résultats de recherche",
                    content = @Content(schema = @Schema(implementation = CompanyDTO.PageResponse.class)))
    })
    public ResponseEntity<CompanyDTO.PageResponse> searchCompanies(
            @Valid @RequestBody CompanyDTO.SearchFilter filter) {

        log.debug("Demande de recherche avec filtres: {}", filter);

        CompanyDTO.PageResponse response = companyService.searchCompanies(filter);

        log.debug("Recherche terminée - {} résultats trouvés", response.getTotal());

        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer une compagnie (suppression logique)
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer une compagnie",
            description = "Supprime logiquement une compagnie (la marque comme inactive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Compagnie supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Compagnie non trouvée",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteCompany(
            @Parameter(description = "Identifiant de la compagnie", required = true)
            @PathVariable Long id) {

        log.info("Demande de suppression de la compagnie ID: {}", id);

        companyService.deleteCompany(id);

        log.info("Compagnie supprimée avec succès - ID: {}", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Activer une compagnie
     */
    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "Activer une compagnie",
            description = "Active une compagnie précédemment désactivée"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compagnie activée avec succès",
                    content = @Content(schema = @Schema(implementation = CompanyDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Compagnie non trouvée",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<CompanyDTO.Response> activateCompany(
            @Parameter(description = "Identifiant de la compagnie", required = true)
            @PathVariable Long id) {

        log.info("Demande d'activation de la compagnie ID: {}", id);

        CompanyDTO.Response response = companyService.activateCompany(id);

        log.info("Compagnie activée avec succès - ID: {}", id);

        return ResponseEntity.ok(response);
    }

    /**
     * Désactiver une compagnie
     */
    @PatchMapping("/{id}/deactivate")
    @Operation(
            summary = "Désactiver une compagnie",
            description = "Désactive une compagnie sans la supprimer"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compagnie désactivée avec succès",
                    content = @Content(schema = @Schema(implementation = CompanyDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Compagnie non trouvée",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<CompanyDTO.Response> deactivateCompany(
            @Parameter(description = "Identifiant de la compagnie", required = true)
            @PathVariable Long id) {

        log.info("Demande de désactivation de la compagnie ID: {}", id);

        CompanyDTO.Response response = companyService.deactivateCompany(id);

        log.info("Compagnie désactivée avec succès - ID: {}", id);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer toutes les compagnies actives (pour les listes déroulantes)
     */
    @GetMapping("/active")
    @Operation(
            summary = "Lister les compagnies actives",
            description = "Récupère la liste de toutes les compagnies actives"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des compagnies actives",
                    content = @Content(schema = @Schema(implementation = CompanyDTO.Response.class)))
    })
    public ResponseEntity<List<CompanyDTO.Response>> getActiveCompanies() {

        log.debug("Demande de liste des compagnies actives");

        List<CompanyDTO.Response> response = companyService.getActiveCompanies();

        log.debug("Trouvé {} compagnies actives", response.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Rechercher une compagnie par email
     */
    @GetMapping("/search/email/{email}")
    @Operation(
            summary = "Rechercher par email",
            description = "Recherche une compagnie par son adresse email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compagnie trouvée",
                    content = @Content(schema = @Schema(implementation = CompanyDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Compagnie non trouvée")
    })
    public ResponseEntity<CompanyDTO.Response> findByEmail(
            @Parameter(description = "Adresse email", required = true)
            @PathVariable String email) {

        log.debug("Recherche de compagnie par email: {}", email);

        Optional<CompanyDTO.Response> response = companyService.findByEmail(email);

        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Rechercher une compagnie par RCCM
     */
    @GetMapping("/search/rccm/{rccm}")
    @Operation(
            summary = "Rechercher par RCCM",
            description = "Recherche une compagnie par son numéro RCCM"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compagnie trouvée",
                    content = @Content(schema = @Schema(implementation = CompanyDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Compagnie non trouvée")
    })
    public ResponseEntity<CompanyDTO.Response> findByRccm(
            @Parameter(description = "Numéro RCCM", required = true)
            @PathVariable String rccm) {

        log.debug("Recherche de compagnie par RCCM: {}", rccm);

        Optional<CompanyDTO.Response> response = companyService.findByRccm(rccm);

        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtenir les statistiques des compagnies par pays
     */
    @GetMapping("/statistics/countries")
    @Operation(
            summary = "Statistiques par pays",
            description = "Récupère les statistiques du nombre de compagnies par pays"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées")
    })
    public ResponseEntity<List<Object[]>> getStatisticsByCountry() {

        log.debug("Demande de statistiques par pays");

        List<Object[]> statistics = companyService.getCompanyStatisticsByCountry();

        return ResponseEntity.ok(statistics);
    }

    /**
     * Obtenir les statistiques des compagnies par ville pour un pays
     */
    @GetMapping("/statistics/cities/{pays}")
    @Operation(
            summary = "Statistiques par ville",
            description = "Récupère les statistiques du nombre de compagnies par ville pour un pays donné"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées")
    })
    public ResponseEntity<List<Object[]>> getStatisticsByCity(
            @Parameter(description = "Nom du pays", required = true)
            @PathVariable String pays) {

        log.debug("Demande de statistiques par ville pour le pays: {}", pays);

        List<Object[]> statistics = companyService.getCompanyStatisticsByCity(pays);

        return ResponseEntity.ok(statistics);
    }

    /**
     * Vérifier si une compagnie existe
     */
    @GetMapping("/{id}/exists")
    @Operation(
            summary = "Vérifier l'existence",
            description = "Vérifie si une compagnie existe avec l'identifiant donné"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vérification effectuée")
    })
    public ResponseEntity<Boolean> existsById(
            @Parameter(description = "Identifiant de la compagnie", required = true)
            @PathVariable Long id) {

        log.debug("Vérification de l'existence de la compagnie ID: {}", id);

        boolean exists = companyService.existsById(id);

        return ResponseEntity.ok(exists);
    }
}
