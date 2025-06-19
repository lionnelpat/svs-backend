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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.svs.backoffice.dto.ApiResponseDTO;
import sn.svs.backoffice.dto.ExpenseDTO;
import sn.svs.backoffice.domain.ennumeration.ExpenseStatus;
import sn.svs.backoffice.service.ExpenseService;
import sn.svs.backoffice.service.ExpenseExportService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des dépenses maritimes
 * SVS - Dakar, Sénégal
 */
@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Expenses", description = "API de gestion des dépenses maritimes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseExportService exportService;

    // ========== CRUD de base ==========

    @Operation(
            summary = "Créer une nouvelle dépense",
            description = "Crée une nouvelle dépense maritime avec les informations fournies"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dépense créée avec succès",
                    content = @Content(schema = @Schema(implementation = ExpenseDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Une dépense avec ce numéro existe déjà",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PostMapping
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExpenseDTO.Response>> createExpense(
            @Valid @RequestBody ExpenseDTO.CreateRequest request) {

        log.info("Demande de création de dépense reçue: {}", request.getTitre());

        ExpenseDTO.Response response = expenseService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.<ExpenseDTO.Response>builder()
                        .success(true)
                        .message("Dépense créée avec succès")
                        .data(response)
                        .build());
    }

    @Operation(
            summary = "Mettre à jour une dépense",
            description = "Met à jour les informations d'une dépense existante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dépense mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = ExpenseDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Dépense non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Modification non autorisée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExpenseDTO.Response>> updateExpense(
            @Parameter(description = "ID de la dépense") @PathVariable Long id,
            @Valid @RequestBody ExpenseDTO.UpdateRequest request) {

        log.info("Demande de mise à jour de dépense reçue pour ID: {}", id);

        ExpenseDTO.Response response = expenseService.update(id, request);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseDTO.Response>builder()
                .success(true)
                .message("Dépense mise à jour avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Obtenir une dépense par ID",
            description = "Récupère les détails d'une dépense spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dépense trouvée",
                    content = @Content(schema = @Schema(implementation = ExpenseDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Dépense non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExpenseDTO.Response>> getExpenseById(
            @Parameter(description = "ID de la dépense") @PathVariable Long id) {

        log.debug("Recherche de dépense par ID: {}", id);

        return expenseService.findById(id)
                .map(expense -> ResponseEntity.ok(ApiResponseDTO.<ExpenseDTO.Response>builder()
                        .success(true)
                        .message("Dépense récupérée avec succès")
                        .data(expense)
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Obtenir une dépense par numéro",
            description = "Récupère les détails d'une dépense par son numéro unique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dépense trouvée",
                    content = @Content(schema = @Schema(implementation = ExpenseDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Dépense non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @GetMapping("/numero/{numero}")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExpenseDTO.Response>> getExpenseByNumero(
            @Parameter(description = "Numéro de la dépense") @PathVariable String numero) {

        log.debug("Recherche de dépense par numéro: {}", numero);

        // Note: Cette méthode nécessite d'être ajoutée au service
        return expenseService.findById(1L) // Placeholder - à implémenter dans le service
                .map(expense -> ResponseEntity.ok(ApiResponseDTO.<ExpenseDTO.Response>builder()
                        .success(true)
                        .message("Dépense récupérée avec succès")
                        .data(expense)
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Lister les dépenses avec pagination et filtres",
            description = "Récupère une liste paginée de dépenses avec possibilité de filtrer"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
            content = @Content(schema = @Schema(implementation = ExpenseDTO.PageResponse.class)))
    @GetMapping
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExpenseDTO.PageResponse>> getAllExpenses(
            @Parameter(description = "Terme de recherche") @RequestParam(required = false) String search,
            @Parameter(description = "Filtre par catégorie") @RequestParam(required = false) Long categorieId,
            @Parameter(description = "Filtre par fournisseur") @RequestParam(required = false) Long fournisseurId,
            @Parameter(description = "Filtre par statut") @RequestParam(required = false) ExpenseStatus statut,
            @Parameter(description = "Filtre par mode de paiement") @RequestParam(required = false) Long paymentMethodId,
            @Parameter(description = "Montant minimum") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Montant maximum") @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "Date de début") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam(required = false) LocalDate endDate,
            @Parameter(description = "Filtre par statut actif") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Champ de tri") @RequestParam(defaultValue = "dateDepense") String sortBy,
            @Parameter(description = "Direction du tri") @RequestParam(defaultValue = "desc") String sortDirection) {


        ExpenseDTO.SearchFilter filter = ExpenseDTO.SearchFilter.builder()
                .search(search)
                .categorieId(categorieId)
                .fournisseurId(fournisseurId)
                .statut(statut)
                .paymentMethodId(paymentMethodId)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .startDate(startDate)
                .endDate(endDate)
                .active(active)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        ExpenseDTO.PageResponse response = expenseService.findWithFilters(filter);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseDTO.PageResponse>builder()
                .success(true)
                .message("Dépenses récupérées avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Lister les dépenses récentes",
            description = "Récupère les dépenses récentes (pour le dashboard)"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @GetMapping("/recent")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<ExpenseDTO.Response>>> getRecentExpenses(
            @Parameter(description = "Nombre de dépenses") @RequestParam(defaultValue = "10") Integer limit) {

        log.debug("Recherche des {} dépenses récentes", limit);

        List<ExpenseDTO.Response> response = expenseService.getRecentExpenses(limit);

        return ResponseEntity.ok(ApiResponseDTO.<List<ExpenseDTO.Response>>builder()
                .success(true)
                .message("Dépenses récentes récupérées avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Lister les dépenses en attente",
            description = "Récupère toutes les dépenses en attente d'approbation"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @GetMapping("/pending")
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<ExpenseDTO.Response>>> getPendingExpenses() {

        log.debug("Recherche de toutes les dépenses en attente");

        List<ExpenseDTO.Response> response = expenseService.findPendingExpenses();

        return ResponseEntity.ok(ApiResponseDTO.<List<ExpenseDTO.Response>>builder()
                .success(true)
                .message("Dépenses en attente récupérées avec succès")
                .data(response)
                .build());
    }

    // ========== Gestion des statuts ==========

    @Operation(
            summary = "Changer le statut d'une dépense",
            description = "Modifie le statut d'une dépense selon le workflow d'approbation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut modifié avec succès",
                    content = @Content(schema = @Schema(implementation = ExpenseDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Dépense non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Changement de statut non autorisé",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PutMapping("/{id}/status")
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExpenseDTO.Response>> changeExpenseStatus(
            @Parameter(description = "ID de la dépense") @PathVariable Long id,
            @Valid @RequestBody ExpenseDTO.StatusChangeRequest request) {

        log.info("Changement de statut pour la dépense ID: {} vers {}", id, request.getStatut());

        ExpenseDTO.Response response = expenseService.changeStatus(id, request);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseDTO.Response>builder()
                .success(true)
                .message("Statut de la dépense modifié avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Approuver une dépense",
            description = "Approuve une dépense en attente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dépense approuvée avec succès",
                    content = @Content(schema = @Schema(implementation = ExpenseDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Dépense non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PatchMapping("/{id}/approve")
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExpenseDTO.Response>> approveExpense(
            @Parameter(description = "ID de la dépense") @PathVariable Long id,
            @Parameter(description = "Commentaire d'approbation") @RequestParam(required = false) String commentaire) {

        log.info("Approbation de la dépense ID: {}", id);

        ExpenseDTO.Response response = expenseService.approve(id, commentaire);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseDTO.Response>builder()
                .success(true)
                .message("Dépense approuvée avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Rejeter une dépense",
            description = "Rejette une dépense avec commentaire obligatoire"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dépense rejetée avec succès",
                    content = @Content(schema = @Schema(implementation = ExpenseDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Dépense non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PatchMapping("/{id}/reject")
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExpenseDTO.Response>> rejectExpense(
            @Parameter(description = "ID de la dépense") @PathVariable Long id,
            @Parameter(description = "Commentaire de rejet (obligatoire)") @RequestParam String commentaire) {

        log.info("Rejet de la dépense ID: {}", id);

        ExpenseDTO.Response response = expenseService.reject(id, commentaire);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseDTO.Response>builder()
                .success(true)
                .message("Dépense rejetée avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Marquer comme payée",
            description = "Marque une dépense approuvée comme payée"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dépense marquée comme payée",
                    content = @Content(schema = @Schema(implementation = ExpenseDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Dépense non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PatchMapping("/{id}/mark-paid")
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExpenseDTO.Response>> markExpenseAsPaid(
            @Parameter(description = "ID de la dépense") @PathVariable Long id,
            @Parameter(description = "Commentaire de paiement") @RequestParam(required = false) String commentaire) {

        log.info("Marquage comme payée de la dépense ID: {}", id);

        ExpenseDTO.Response response = expenseService.markAsPaid(id, commentaire);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseDTO.Response>builder()
                .success(true)
                .message("Dépense marquée comme payée")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Activer/Désactiver une dépense",
            description = "Bascule le statut actif/inactif d'une dépense"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut modifié avec succès",
                    content = @Content(schema = @Schema(implementation = ExpenseDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Dépense non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PatchMapping("/{id}/toggle-active")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExpenseDTO.Response>> toggleActiveStatus(
            @Parameter(description = "ID de la dépense") @PathVariable Long id,
            @Parameter(description = "Nouveau statut actif") @RequestParam Boolean active) {

        log.info("Basculement du statut actif pour la dépense ID: {} vers {}", id, active);

        ExpenseDTO.Response response = expenseService.toggleActive(id, active);

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseDTO.Response>builder()
                .success(true)
                .message("Statut de la dépense modifié avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Supprimer une dépense (logique)",
            description = "Désactive une dépense (suppression logique)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dépense supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Dépense non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteExpense(
            @Parameter(description = "ID de la dépense") @PathVariable Long id) {

        log.info("Suppression logique de la dépense ID: {}", id);

        expenseService.delete(id);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(true)
                .message("Dépense supprimée avec succès")
                .build());
    }

    // ========== Statistiques ==========

    @Operation(
            summary = "Obtenir les statistiques des dépenses",
            description = "Récupère les statistiques globales des dépenses"
    )
    @ApiResponse(responseCode = "200", description = "Statistiques récupérées")
    @GetMapping("/stats")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExpenseDTO.StatsResponse>> getExpenseStats() {

        log.debug("Récupération des statistiques des dépenses");

        ExpenseDTO.StatsResponse stats = expenseService.getStatistics();

        return ResponseEntity.ok(ApiResponseDTO.<ExpenseDTO.StatsResponse>builder()
                .success(true)
                .message("Statistiques récupérées avec succès")
                .data(stats)
                .build());
    }

    @Operation(
            summary = "Données du dashboard",
            description = "Récupère toutes les données pour le dashboard des dépenses"
    )
    @ApiResponse(responseCode = "200", description = "Données du dashboard récupérées")
    @GetMapping("/dashboard")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<DashboardData>> getDashboardData() {

        log.debug("Récupération des données du dashboard des dépenses");

        ExpenseDTO.StatsResponse stats = expenseService.getStatistics();
        List<ExpenseDTO.Response> recentExpenses = expenseService.getRecentExpenses(5);
        List<ExpenseDTO.Response> pendingExpenses = expenseService.findPendingExpenses();
        List<ExpenseDTO.StatsResponse.MonthlyExpense> monthlyEvolution = expenseService.getMonthlyEvolution(6);
        List<ExpenseDTO.StatsResponse.CategorieCount> topCategories = expenseService.getTopCategoriesByAmount(5, LocalDate.now().minusMonths(3));

        DashboardData dashboardData = DashboardData.builder()
                .statistics(stats)
                .recentExpenses(recentExpenses)
                .pendingExpenses(pendingExpenses)
                .monthlyEvolution(monthlyEvolution)
                .topCategories(topCategories)
                .build();

        return ResponseEntity.ok(ApiResponseDTO.<DashboardData>builder()
                .success(true)
                .message("Données du dashboard récupérées avec succès")
                .data(dashboardData)
                .build());
    }

    // ========== Export et rapports ==========

    @Operation(
            summary = "Export PDF des dépenses",
            description = "Exporte les dépenses au format PDF avec filtres"
    )
    @PostMapping("/export/pdf")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportToPdf(@Valid @RequestBody ExpenseDTO.SearchFilter filter) {

        log.info("Export PDF des dépenses");

        byte[] pdfBytes = expenseService.exportToPdf(filter);
        String filename = exportService.generateFileName("pdf");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @Operation(
            summary = "Export Excel des dépenses",
            description = "Exporte les dépenses au format Excel avec filtres"
    )
    @PostMapping("/export/excel")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportToExcel(@Valid @RequestBody ExpenseDTO.SearchFilter filter) {

        log.info("Export Excel des dépenses");

        byte[] excelBytes = expenseService.exportToExcel(filter);
        String filename = exportService.generateFileName("xlsx");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excelBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    // ========== Utilitaires ==========

    @Operation(
            summary = "Vérifier l'existence d'un numéro",
            description = "Vérifie si un numéro de dépense existe déjà"
    )
    @ApiResponse(responseCode = "200", description = "Vérification effectuée")
    @GetMapping("/exists/numero/{numero}")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Boolean>> checkNumeroExists(
            @Parameter(description = "Numéro à vérifier") @PathVariable String numero,
            @Parameter(description = "ID à exclure (pour mise à jour)") @RequestParam(required = false) Long excludeId) {

        log.debug("Vérification de l'existence du numéro: {}", numero);

        boolean exists = !expenseService.isNumeroUnique(numero, excludeId);

        return ResponseEntity.ok(ApiResponseDTO.<Boolean>builder()
                .success(true)
                .message("Vérification effectuée")
                .data(exists)
                .build());
    }

    @Operation(
            summary = "Générer un numéro automatique",
            description = "Génère un nouveau numéro de dépense unique"
    )
    @ApiResponse(responseCode = "200", description = "Numéro généré")
    @GetMapping("/generate-numero")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<String>> generateNumero() {

        log.debug("Génération d'un nouveau numéro de dépense");

        String numero = expenseService.generateNumero();

        return ResponseEntity.ok(ApiResponseDTO.<String>builder()
                .success(true)
                .message("Numéro généré avec succès")
                .data(numero)
                .build());
    }

    /**
     * DTO pour regrouper les données du dashboard
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Données complètes du dashboard des dépenses")
    public static class DashboardData {

        @Schema(description = "Statistiques générales")
        private ExpenseDTO.StatsResponse statistics;

        @Schema(description = "Dépenses récentes")
        private List<ExpenseDTO.Response> recentExpenses;

        @Schema(description = "Dépenses en attente")
        private List<ExpenseDTO.Response> pendingExpenses;

        @Schema(description = "Évolution mensuelle")
        private List<ExpenseDTO.StatsResponse.MonthlyExpense> monthlyEvolution;

        @Schema(description = "Top catégories")
        private List<ExpenseDTO.StatsResponse.CategorieCount> topCategories;
    }
}