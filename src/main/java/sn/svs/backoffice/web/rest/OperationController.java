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
import org.springframework.web.bind.annotation.*;
import sn.svs.backoffice.dto.ApiResponseDTO;
import sn.svs.backoffice.dto.OperationDTO;
import sn.svs.backoffice.service.OperationService;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des opérations maritimes
 * SVS - Dakar, Sénégal
 */
@RestController
@RequestMapping("/api/v1/operations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operations", description = "API de gestion des opérations maritimes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OperationController {

    private final OperationService operationService;

    @Operation(
            summary = "Créer une nouvelle opération",
            description = "Crée une nouvelle opération maritime avec les informations fournies"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Opération créée avec succès",
                    content = @Content(schema = @Schema(implementation = OperationDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Une opération avec ce code existe déjà",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PostMapping
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<OperationDTO.Response>> createOperation(
            @Valid @RequestBody OperationDTO.CreateRequest request) {

        log.info("Demande de création d'opération reçue: {}", request.getNom());

        OperationDTO.Response response = operationService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.<OperationDTO.Response>builder()
                        .success(true)
                        .message("Opération créée avec succès")
                        .data(response)
                        .build());
    }

    @Operation(
            summary = "Mettre à jour une opération",
            description = "Met à jour les informations d'une opération existante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = OperationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Opération non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Code déjà utilisé par une autre opération",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<OperationDTO.Response>> updateOperation(
            @Parameter(description = "ID de l'opération") @PathVariable Long id,
            @Valid @RequestBody OperationDTO.UpdateRequest request) {

        log.info("Demande de mise à jour d'opération reçue pour ID: {}", id);

        OperationDTO.Response response = operationService.update(id, request);

        return ResponseEntity.ok(ApiResponseDTO.<OperationDTO.Response>builder()
                .success(true)
                .message("Opération mise à jour avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Obtenir une opération par ID",
            description = "Récupère les détails d'une opération spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération trouvée",
                    content = @Content(schema = @Schema(implementation = OperationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Opération non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<OperationDTO.Response>> getOperationById(
            @Parameter(description = "ID de l'opération") @PathVariable Long id) {

        log.debug("Recherche d'opération par ID: {}", id);

        OperationDTO.Response response = operationService.findById(id);

        return ResponseEntity.ok(ApiResponseDTO.<OperationDTO.Response>builder()
                .success(true)
                .message("Opération récupérée avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Obtenir une opération par code",
            description = "Récupère les détails d'une opération par son code unique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération trouvée",
                    content = @Content(schema = @Schema(implementation = OperationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Opération non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @GetMapping("/code/{code}")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<OperationDTO.Response>> getOperationByCode(
            @Parameter(description = "Code de l'opération") @PathVariable String code) {

        log.debug("Recherche d'opération par code: {}", code);

        OperationDTO.Response response = operationService.findByCode(code);

        return ResponseEntity.ok(ApiResponseDTO.<OperationDTO.Response>builder()
                .success(true)
                .message("Opération récupérée avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Lister les opérations avec pagination et filtres",
            description = "Récupère une liste paginée d'opérations avec possibilité de filtrer"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
            content = @Content(schema = @Schema(implementation = OperationDTO.PageResponse.class)))
    @GetMapping
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<OperationDTO.PageResponse>> getAllOperations(
            @Parameter(description = "Terme de recherche") @RequestParam(required = false) String search,
            @Parameter(description = "Filtre par statut actif") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Champ de tri") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direction du tri") @RequestParam(defaultValue = "asc") String sortDirection) {

        log.debug("Recherche d'opérations avec filtres - search: {}, active: {}, page: {}", search, active, page);

        OperationDTO.SearchFilter filter = OperationDTO.SearchFilter.builder()
                .search(search)
                .active(active)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        OperationDTO.PageResponse response = operationService.findAll(filter);

        return ResponseEntity.ok(ApiResponseDTO.<OperationDTO.PageResponse>builder()
                .success(true)
                .message("Opérations récupérées avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Lister les opérations actives",
            description = "Récupère toutes les opérations actives sous forme simplifiée (pour les listes déroulantes)"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @GetMapping("/active")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<List<OperationDTO.Summary>>> getActiveOperations() {

        log.debug("Recherche de toutes les opérations actives");

        List<OperationDTO.Summary> response = operationService.findAllActive();

        return ResponseEntity.ok(ApiResponseDTO.<List<OperationDTO.Summary>>builder()
                .success(true)
                .message("Opérations actives récupérées avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Activer/Désactiver une opération",
            description = "Bascule le statut actif/inactif d'une opération"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut modifié avec succès",
                    content = @Content(schema = @Schema(implementation = OperationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Opération non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PatchMapping("/{id}/toggle-active")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<OperationDTO.Response>> toggleActiveStatus(
            @Parameter(description = "ID de l'opération") @PathVariable Long id) {

        log.info("Basculement du statut actif pour l'opération ID: {}", id);

        OperationDTO.Response response = operationService.toggleActive(id);

        return ResponseEntity.ok(ApiResponseDTO.<OperationDTO.Response>builder()
                .success(true)
                .message("Statut de l'opération modifié avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Supprimer une opération (logique)",
            description = "Désactive une opération (suppression logique)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Opération non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteOperation(
            @Parameter(description = "ID de l'opération") @PathVariable Long id) {

        log.info("Suppression logique de l'opération ID: {}", id);

        operationService.delete(id);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(true)
                .message("Opération supprimée avec succès")
                .build());
    }

    @Operation(
            summary = "Supprimer définitivement une opération",
            description = "Supprime définitivement une opération de la base de données"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération supprimée définitivement"),
            @ApiResponse(responseCode = "404", description = "Opération non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @DeleteMapping("/{id}/hard")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> hardDeleteOperation(
            @Parameter(description = "ID de l'opération") @PathVariable Long id) {

        log.info("Suppression définitive de l'opération ID: {}", id);

        operationService.hardDelete(id);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(true)
                .message("Opération supprimée définitivement")
                .build());
    }

    @Operation(
            summary = "Vérifier l'existence d'un code",
            description = "Vérifie si un code d'opération existe déjà"
    )
    @ApiResponse(responseCode = "200", description = "Vérification effectuée")
    @GetMapping("/exists/code/{code}")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<Boolean>> checkCodeExists(
            @Parameter(description = "Code à vérifier") @PathVariable String code) {

        log.debug("Vérification de l'existence du code: {}", code);

        boolean exists = operationService.existsByCode(code);

        return ResponseEntity.ok(ApiResponseDTO.<Boolean>builder()
                .success(true)
                .message("Vérification effectuée")
                .data(exists)
                .build());
    }

    @Operation(
            summary = "Obtenir les statistiques des opérations",
            description = "Récupère les statistiques globales des opérations"
    )
    @ApiResponse(responseCode = "200", description = "Statistiques récupérées")
    @GetMapping("/stats")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<OperationService.OperationStatsDTO>> getOperationStats() {

        log.debug("Récupération des statistiques des opérations");

        OperationService.OperationStatsDTO stats = operationService.getStats();

        return ResponseEntity.ok(ApiResponseDTO.<OperationService.OperationStatsDTO>builder()
                .success(true)
                .message("Statistiques récupérées avec succès")
                .data(stats)
                .build());
    }
}
