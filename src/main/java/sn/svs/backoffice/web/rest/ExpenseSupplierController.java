package sn.svs.backoffice.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.svs.backoffice.dto.ApiResponseDTO;
import sn.svs.backoffice.dto.ExpenseSupplierDTO;
import sn.svs.backoffice.service.ExpenseSupplierService;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des fournisseurs de dépenses
 */
@RestController
@RequestMapping("/api/v1/expense-suppliers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Expense Suppliers", description = "API de gestion des fournisseurs de dépenses")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ExpenseSupplierController {

    private final ExpenseSupplierService supplierService;

    @Operation(
            summary = "Créer un nouveau fournisseur",
            description = "Crée un nouveau fournisseur de dépenses avec les informations fournies"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fournisseur créé avec succès",
                    content = @Content(schema = @Schema(implementation = ExpenseSupplierDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Un fournisseur avec ce nom/raison sociale existe déjà",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PostMapping
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<ExpenseSupplierDTO.Response>> createSupplier(
            @Valid @RequestBody ExpenseSupplierDTO.CreateRequest request) {

        log.info("Demande de création de fournisseur reçue: {}", request.getNom());

        ExpenseSupplierDTO.Response response = supplierService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.<ExpenseSupplierDTO.Response>builder()
                        .success(true)
                        .message("Fournisseur créé avec succès")
                        .data(response)
                        .build());
    }

    @Operation(
            summary = "Mettre à jour un fournisseur",
            description = "Met à jour les informations d'un fournisseur existant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fournisseur mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = ExpenseSupplierDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Fournisseur non trouvé",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Nom/raison sociale déjà utilisé par un autre fournisseur",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<ExpenseSupplierDTO.Response>> updateSupplier(
            @Parameter(description = "ID du fournisseur") @PathVariable Long id,
            @Valid @RequestBody ExpenseSupplierDTO.UpdateRequest request) {

        log.info("Demande de mise à jour de fournisseur reçue pour ID: {}", id);

        ExpenseSupplierDTO.Response response = supplierService.update(id, request);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseSupplierDTO.Response>builder()
                .success(true)
                .message("Fournisseur mis à jour avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Obtenir un fournisseur par ID",
            description = "Récupère les détails d'un fournisseur spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fournisseur trouvé",
                    content = @Content(schema = @Schema(implementation = ExpenseSupplierDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Fournisseur non trouvé",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<ExpenseSupplierDTO.Response>> getSupplierById(
            @Parameter(description = "ID du fournisseur") @PathVariable Long id) {

        log.debug("Recherche de fournisseur par ID: {}", id);

        ExpenseSupplierDTO.Response response = supplierService.findById(id);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseSupplierDTO.Response>builder()
                .success(true)
                .message("Fournisseur récupéré avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Lister les fournisseurs avec pagination et filtres",
            description = "Récupère une liste paginée de fournisseurs avec possibilité de filtrer"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
            content = @Content(schema = @Schema(implementation = ExpenseSupplierDTO.PageResponse.class)))
    @GetMapping
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<ExpenseSupplierDTO.PageResponse>> getAllSuppliers(
            @Parameter(description = "Terme de recherche") @RequestParam(required = false) String search,
            @Parameter(description = "Filtre par statut actif") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Champ de tri") @RequestParam(defaultValue = "nom") String sortBy,
            @Parameter(description = "Direction du tri") @RequestParam(defaultValue = "asc") String sortDirection) {

        log.debug("Recherche de fournisseurs avec filtres - search: {}, page: {}", search, page);

        ExpenseSupplierDTO.SearchFilter filter = ExpenseSupplierDTO.SearchFilter.builder()
                .search(search)
                .active(true)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        ExpenseSupplierDTO.PageResponse response = supplierService.findAll(filter);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseSupplierDTO.PageResponse>builder()
                .success(true)
                .message("Fournisseurs récupérés avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Lister les fournisseurs actifs",
            description = "Récupère tous les fournisseurs actifs sous forme simplifiée"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @GetMapping("/active")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<List<ExpenseSupplierDTO.Summary>>> getActiveSuppliers() {

        log.debug("Recherche de tous les fournisseurs actifs");

        List<ExpenseSupplierDTO.Summary> response = supplierService.findAllActive();

        return ResponseEntity.ok(ApiResponseDTO.<List<ExpenseSupplierDTO.Summary>>builder()
                .success(true)
                .message("Fournisseurs actifs récupérés avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Supprimer un fournisseur (logique)",
            description = "Désactive un fournisseur (suppression logique)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fournisseur supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Fournisseur non trouvé",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Impossible de supprimer - Fournisseur utilisé dans des dépenses",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteSupplier(
            @Parameter(description = "ID du fournisseur") @PathVariable Long id) {

        log.info("Suppression logique du fournisseur ID: {}", id);

        supplierService.delete(id);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(true)
                .message("Fournisseur supprimé avec succès")
                .build());
    }

    @Operation(
            summary = "Supprimer définitivement un fournisseur",
            description = "Supprime définitivement un fournisseur de la base de données"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fournisseur supprimé définitivement"),
            @ApiResponse(responseCode = "404", description = "Fournisseur non trouvé",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Impossible de supprimer - Fournisseur utilisé dans des dépenses",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @DeleteMapping("/{id}/hard")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> hardDeleteSupplier(
            @Parameter(description = "ID du fournisseur") @PathVariable Long id) {

        log.info("Suppression définitive du fournisseur ID: {}", id);

        supplierService.hardDelete(id);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(true)
                .message("Fournisseur supprimé définitivement")
                .build());
    }

    @Operation(
            summary = "Restaurer un fournisseur",
            description = "Restaure un fournisseur supprimé logiquement"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fournisseur restauré avec succès",
                    content = @Content(schema = @Schema(implementation = ExpenseSupplierDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Fournisseur non trouvé",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PatchMapping("/{id}/restore")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExpenseSupplierDTO.Response>> restoreSupplier(
            @Parameter(description = "ID du fournisseur") @PathVariable Long id) {

        log.info("Restauration du fournisseur ID: {}", id);

        ExpenseSupplierDTO.Response response = supplierService.restore(id);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseSupplierDTO.Response>builder()
                .success(true)
                .message("Fournisseur restauré avec succès")
                .data(response)
                .build());
    }


    @Operation(
            summary = "Vérifier l'existence d'un fournisseur",
            description = "Vérifie si un fournisseur existe par nom ou raison sociale"
    )
    @ApiResponse(responseCode = "200", description = "Vérification effectuée")
    @GetMapping("/exists")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<Boolean>> checkSupplierExists(
            @Parameter(description = "NINEA du fournisseur") @RequestParam(required = false) String ninea){

        log.debug("Vérification de l'existence d'un fournisseur - Ninea: {}", ninea);

        boolean exists = supplierService.existsByNinea(ninea);

        return ResponseEntity.ok(ApiResponseDTO.<Boolean>builder()
                .success(true)
                .message("Vérification effectuée")
                .data(exists)
                .build());
    }
}