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
import sn.svs.backoffice.config.GlobalExceptionHandler;
import sn.svs.backoffice.dto.ApiResponseDTO;
import sn.svs.backoffice.dto.OperationDTO;
import sn.svs.backoffice.dto.PaymentMethodDTO;
import sn.svs.backoffice.service.PaymentMethodService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment-methods")
@RequiredArgsConstructor
@Tag(name = "Payment Methods", description = "API de gestion des modes de paiement")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    /**
     * Créer un mode de paiement
     */
    @PostMapping
    @Operation(summary = "Créer un mode de paiement", description = "Crée un nouveau mode de paiement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mode de paiement créé avec succès",
                    content = @Content(schema = @Schema(implementation = PaymentMethodDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflit - Mode de paiement déjà existant",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<PaymentMethodDTO.Response> createPaymentMethod(
            @Valid @RequestBody PaymentMethodDTO.CreateRequest request) {

        log.info("Création d'un mode de paiement: {}", request.getNom());
        PaymentMethodDTO.Response response = paymentMethodService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Mettre à jour un mode de paiement
     */
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un mode de paiement", description = "Met à jour les informations d'un mode de paiement existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mode de paiement mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = PaymentMethodDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Mode de paiement non trouvé",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<PaymentMethodDTO.Response> updatePaymentMethod(
            @Parameter(description = "Identifiant du mode de paiement", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PaymentMethodDTO.UpdateRequest request) {

        log.info("Mise à jour du mode de paiement ID: {}", id);
        PaymentMethodDTO.Response response = paymentMethodService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer un mode de paiement par ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un mode de paiement", description = "Récupère les détails d'un mode de paiement par son identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mode de paiement trouvé",
                    content = @Content(schema = @Schema(implementation = PaymentMethodDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Mode de paiement non trouvé",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<PaymentMethodDTO.Response> getPaymentMethodById(
            @Parameter(description = "Identifiant du mode de paiement") @PathVariable Long id) {

        log.debug("Récupération du mode de paiement ID: {}", id);
        PaymentMethodDTO.Response response = paymentMethodService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer un mode de paiement (logique)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un mode de paiement", description = "Supprime un mode de paiement de manière logique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Mode de paiement supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Mode de paiement non trouvé",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletePaymentMethod(
            @Parameter(description = "Identifiant du mode de paiement", required = true)
            @PathVariable Long id) {

        log.info("Suppression logique du mode de paiement ID: {}", id);
        paymentMethodService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Lister les methodes de paiement avec pagination et filtres",
            description = "Récupère une liste paginée de methodes de paiement avec possibilité de filtrer"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
            content = @Content(schema = @Schema(implementation = PaymentMethodDTO.PageResponse.class)))
    @GetMapping
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<PaymentMethodDTO.PageResponse>> getAllPaymentMethods(
            @Parameter(description = "Terme de recherche") @RequestParam(required = false) String search,
            @Parameter(description = "Filtre par statut actif") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Champ de tri") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direction du tri") @RequestParam(defaultValue = "asc") String sortDirection) {

        log.debug("Recherche de methodes de paiement avec filtres - search: {}, active: {}, page: {}", search, active, page);

        PaymentMethodDTO.SearchFilter filter = PaymentMethodDTO.SearchFilter.builder()
                .search(search)
                .active(true)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        PaymentMethodDTO.PageResponse response = paymentMethodService.findAll(filter);

        return ResponseEntity.ok(ApiResponseDTO.<PaymentMethodDTO.PageResponse>builder()
                .success(true)
                .message("Opérations récupérées avec succès")
                .data(response)
                .build());
    }

    /**
     * Récupérer tous les modes de paiement actifs (liste déroulante)
     */
    @GetMapping("/active")
    @Operation(summary = "Lister les modes de paiement actifs", description = "Récupère la liste des modes de paiement actifs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des modes de paiement actifs",
                    content = @Content(schema = @Schema(implementation = PaymentMethodDTO.Summary.class)))
    })
    public ResponseEntity<List<PaymentMethodDTO.Response>> getActivePaymentMethods() {

        log.debug("Liste des modes de paiement actifs demandée");
        List<PaymentMethodDTO.Response> response = paymentMethodService.findAllActive();
        return ResponseEntity.ok(response);
    }
}