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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.svs.backoffice.dto.ApiResponseDTO;
import sn.svs.backoffice.dto.ExpenseCategoryDTO;
import sn.svs.backoffice.service.ExpenseCategoryService;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des catégories de dépenses
 * SVS - Dakar, Sénégal
 */
@RestController
@RequestMapping("/api/v1/expense-categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Expense Categories", description = "API de gestion des catégories de dépenses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ExpenseCategoryController {

    private final ExpenseCategoryService categoryService;

    @Operation(
            summary = "Créer une nouvelle catégorie",
            description = "Crée une nouvelle catégorie de dépense avec les informations fournies"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Catégorie créée avec succès",
                    content = @Content(schema = @Schema(implementation = ExpenseCategoryDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Une catégorie avec ce code/nom existe déjà",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PostMapping
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<ExpenseCategoryDTO.Response>> createCategory(
            @Valid @RequestBody ExpenseCategoryDTO.CreateRequest request) {

        log.info("Demande de création de catégorie reçue: {}", request.getCode());

        ExpenseCategoryDTO.Response response = categoryService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.<ExpenseCategoryDTO.Response>builder()
                        .success(true)
                        .message("Catégorie créée avec succès")
                        .data(response)
                        .build());
    }

    @Operation(
            summary = "Mettre à jour une catégorie",
            description = "Met à jour les informations d'une catégorie existante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catégorie mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = ExpenseCategoryDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Catégorie non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Code/nom déjà utilisé par une autre catégorie",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<ExpenseCategoryDTO.Response>> updateCategory(
            @Parameter(description = "ID de la catégorie") @PathVariable Long id,
            @Valid @RequestBody ExpenseCategoryDTO.UpdateRequest request) {

        log.info("Demande de mise à jour de catégorie reçue pour ID: {}", id);

        ExpenseCategoryDTO.Response response = categoryService.update(id, request);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseCategoryDTO.Response>builder()
                .success(true)
                .message("Catégorie mise à jour avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Obtenir une catégorie par ID",
            description = "Récupère les détails d'une catégorie spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catégorie trouvée",
                    content = @Content(schema = @Schema(implementation = ExpenseCategoryDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Catégorie non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<ExpenseCategoryDTO.Response>> getCategoryById(
            @Parameter(description = "ID de la catégorie") @PathVariable Long id) {

        log.debug("Recherche de catégorie par ID: {}", id);

        ExpenseCategoryDTO.Response response = categoryService.findById(id);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseCategoryDTO.Response>builder()
                .success(true)
                .message("Catégorie récupérée avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Obtenir une catégorie par code",
            description = "Récupère les détails d'une catégorie par son code unique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catégorie trouvée",
                    content = @Content(schema = @Schema(implementation = ExpenseCategoryDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Catégorie non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @GetMapping("/code/{code}")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<ExpenseCategoryDTO.Response>> getCategoryByCode(
            @Parameter(description = "Code de la catégorie") @PathVariable String code) {

        log.debug("Recherche de catégorie par code: {}", code);

        ExpenseCategoryDTO.Response response = categoryService.findByCode(code);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseCategoryDTO.Response>builder()
                .success(true)
                .message("Catégorie récupérée avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Lister les catégories avec pagination et filtres",
            description = "Récupère une liste paginée de catégories avec possibilité de filtrer"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
            content = @Content(schema = @Schema(implementation = ExpenseCategoryDTO.PageResponse.class)))
    @GetMapping
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<ExpenseCategoryDTO.PageResponse>> getAllCategories(
            @Parameter(description = "Terme de recherche") @RequestParam(required = false) String search,
            @Parameter(description = "Filtre par statut actif") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Champ de tri") @RequestParam(defaultValue = "nom") String sortBy,
            @Parameter(description = "Direction du tri") @RequestParam(defaultValue = "asc") String sortDirection) {

        log.debug("Recherche de catégories avec filtres - search: {}, active: {}, page: {}", search, active, page);

        ExpenseCategoryDTO.SearchFilter filter = ExpenseCategoryDTO.SearchFilter.builder()
                .search(search)
                .active(active)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        ExpenseCategoryDTO.PageResponse response = categoryService.findAll(filter);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseCategoryDTO.PageResponse>builder()
                .success(true)
                .message("Catégories récupérées avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Lister les catégories actives",
            description = "Récupère toutes les catégories actives sous forme simplifiée (pour les listes déroulantes)"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @GetMapping("/active")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<List<ExpenseCategoryDTO.Summary>>> getActiveCategories() {

        log.debug("Recherche de toutes les catégories actives");

        List<ExpenseCategoryDTO.Summary> response = categoryService.findAllActive();

        return ResponseEntity.ok(ApiResponseDTO.<List<ExpenseCategoryDTO.Summary>>builder()
                .success(true)
                .message("Catégories actives récupérées avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Activer/Désactiver une catégorie",
            description = "Bascule le statut actif/inactif d'une catégorie"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut modifié avec succès",
                    content = @Content(schema = @Schema(implementation = ExpenseCategoryDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Catégorie non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PatchMapping("/{id}/toggle-active")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<ExpenseCategoryDTO.Response>> toggleActiveStatus(
            @Parameter(description = "ID de la catégorie") @PathVariable Long id) {

        log.info("Basculement du statut actif pour la catégorie ID: {}", id);

        ExpenseCategoryDTO.Response response = categoryService.toggleActive(id);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseCategoryDTO.Response>builder()
                .success(true)
                .message("Statut de la catégorie modifié avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Supprimer une catégorie (logique)",
            description = "Désactive une catégorie (suppression logique)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catégorie supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Catégorie non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteCategory(
            @Parameter(description = "ID de la catégorie") @PathVariable Long id) {

        log.info("Suppression logique de la catégorie ID: {}", id);

        categoryService.delete(id);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(true)
                .message("Catégorie supprimée avec succès")
                .build());
    }

    @Operation(
            summary = "Supprimer définitivement une catégorie",
            description = "Supprime définitivement une catégorie de la base de données"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catégorie supprimée définitivement"),
            @ApiResponse(responseCode = "404", description = "Catégorie non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @DeleteMapping("/{id}/hard")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> hardDeleteCategory(
            @Parameter(description = "ID de la catégorie") @PathVariable Long id) {

        log.info("Suppression définitive de la catégorie ID: {}", id);

        categoryService.hardDelete(id);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(true)
                .message("Catégorie supprimée définitivement")
                .build());
    }

    @Operation(
            summary = "Vérifier l'existence d'un code",
            description = "Vérifie si un code de catégorie existe déjà"
    )
    @ApiResponse(responseCode = "200", description = "Vérification effectuée")
    @GetMapping("/exists/code/{code}")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<Boolean>> checkCodeExists(
            @Parameter(description = "Code à vérifier") @PathVariable String code) {

        log.debug("Vérification de l'existence du code: {}", code);

        boolean exists = categoryService.existsByCode(code);

        return ResponseEntity.ok(ApiResponseDTO.<Boolean>builder()
                .success(true)
                .message("Vérification effectuée")
                .data(exists)
                .build());
    }

    @Operation(
            summary = "Vérifier l'existence d'un nom",
            description = "Vérifie si un nom de catégorie existe déjà"
    )
    @ApiResponse(responseCode = "200", description = "Vérification effectuée")
    @GetMapping("/exists/nom/{nom}")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<Boolean>> checkNomExists(
            @Parameter(description = "Nom à vérifier") @PathVariable String nom) {

        log.debug("Vérification de l'existence du nom: {}", nom);

        boolean exists = categoryService.existsByNom(nom);

        return ResponseEntity.ok(ApiResponseDTO.<Boolean>builder()
                .success(true)
                .message("Vérification effectuée")
                .data(exists)
                .build());
    }

    @Operation(
            summary = "Obtenir les statistiques des catégories",
            description = "Récupère les statistiques globales des catégories"
    )
    @ApiResponse(responseCode = "200", description = "Statistiques récupérées")
    @GetMapping("/stats")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<ExpenseCategoryService.CategoryStatsDTO>> getCategoryStats() {

        log.debug("Récupération des statistiques des catégories");

        ExpenseCategoryService.CategoryStatsDTO stats = categoryService.getStats();

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseCategoryService.CategoryStatsDTO>builder()
                .success(true)
                .message("Statistiques récupérées avec succès")
                .data(stats)
                .build());
    }
}
