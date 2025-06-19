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
import sn.svs.backoffice.dto.InvoiceDTO;
import sn.svs.backoffice.domain.ennumeration.InvoiceStatus;
import sn.svs.backoffice.service.InvoiceService;
import sn.svs.backoffice.service.InvoiceExportService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des factures de prestations maritimes
 * SVS - Dakar, Sénégal
 */
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoices", description = "API de gestion des factures de prestations maritimes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceExportService exportService;

    // ========== CRUD de base ==========

    @Operation(
            summary = "Créer une nouvelle facture",
            description = "Crée une nouvelle facture de prestations maritimes avec les informations fournies"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Facture créée avec succès",
                    content = @Content(schema = @Schema(implementation = InvoiceDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Une facture avec ce numéro existe déjà",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PostMapping
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<InvoiceDTO.Response>> createInvoice(
            @Valid @RequestBody InvoiceDTO.CreateRequest request) {

        log.info("Demande de création de facture reçue pour compagnie ID: {}", request.getCompagnieId());

        InvoiceDTO.Response response = invoiceService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.<InvoiceDTO.Response>builder()
                        .success(true)
                        .message("Facture créée avec succès")
                        .data(response)
                        .build());
    }

    @Operation(
            summary = "Mettre à jour une facture",
            description = "Met à jour les informations d'une facture existante (uniquement si statut BROUILLON)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facture mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = InvoiceDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Facture non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Modification non autorisée (facture déjà émise)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<InvoiceDTO.Response>> updateInvoice(
            @Parameter(description = "ID de la facture") @PathVariable Long id,
            @Valid @RequestBody InvoiceDTO.UpdateRequest request) {

        log.info("Demande de mise à jour de facture reçue pour ID: {}", id);

        InvoiceDTO.Response response = invoiceService.update(id, request);

        return ResponseEntity.ok(ApiResponseDTO.<InvoiceDTO.Response>builder()
                .success(true)
                .message("Facture mise à jour avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Obtenir une facture par ID",
            description = "Récupère les détails complets d'une facture avec ses prestations"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facture trouvée",
                    content = @Content(schema = @Schema(implementation = InvoiceDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Facture non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<InvoiceDTO.Response>> getInvoiceById(
            @Parameter(description = "ID de la facture") @PathVariable Long id) {

        log.debug("Recherche de facture par ID: {}", id);

        return invoiceService.findById(id)
                .map(invoice -> ResponseEntity.ok(ApiResponseDTO.<InvoiceDTO.Response>builder()
                        .success(true)
                        .message("Facture récupérée avec succès")
                        .data(invoice)
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Obtenir une facture par numéro",
            description = "Récupère les détails d'une facture par son numéro unique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facture trouvée",
                    content = @Content(schema = @Schema(implementation = InvoiceDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Facture non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @GetMapping("/numero/{numero}")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<InvoiceDTO.Response>> getInvoiceByNumero(
            @Parameter(description = "Numéro de la facture") @PathVariable String numero) {

        log.debug("Recherche de facture par numéro: {}", numero);

        // Recherche par numéro via specification
        InvoiceDTO.SearchFilter filter = InvoiceDTO.SearchFilter.builder()
                .search(numero)
                .size(1)
                .build();

        InvoiceDTO.PageResponse pageResponse = invoiceService.findWithFilters(filter);

        if (pageResponse.getInvoices().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ApiResponseDTO.<InvoiceDTO.Response>builder()
                .success(true)
                .message("Facture récupérée avec succès")
                .data(pageResponse.getInvoices().get(0))
                .build());
    }

    @Operation(
            summary = "Lister les factures avec pagination et filtres",
            description = "Récupère une liste paginée de factures avec possibilité de filtrer par compagnie, navire, statut, etc."
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
            content = @Content(schema = @Schema(implementation = InvoiceDTO.PageResponse.class)))
    @GetMapping
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<InvoiceDTO.PageResponse>> getAllInvoices(
            @Parameter(description = "Terme de recherche (numéro, compagnie, navire)") @RequestParam(required = false) String search,
            @Parameter(description = "Filtre par compagnie") @RequestParam(required = false) Long compagnieId,
            @Parameter(description = "Filtre par navire") @RequestParam(required = false) Long navireId,
            @Parameter(description = "Filtre par statut") @RequestParam(required = false) InvoiceStatus statut,
            @Parameter(description = "Date de début") @RequestParam(required = false) LocalDate dateDebut,
            @Parameter(description = "Date de fin") @RequestParam(required = false) LocalDate dateFin,
            @Parameter(description = "Filtre par mois") @RequestParam(required = false) Integer mois,
            @Parameter(description = "Filtre par année") @RequestParam(required = false) Integer annee,
            @Parameter(description = "Montant minimum") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Montant maximum") @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "Filtre par statut actif") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Champ de tri") @RequestParam(defaultValue = "dateFacture") String sortBy,
            @Parameter(description = "Direction du tri") @RequestParam(defaultValue = "desc") String sortDirection) {

        InvoiceDTO.SearchFilter filter = InvoiceDTO.SearchFilter.builder()
                .search(search)
                .compagnieId(compagnieId)
                .navireId(navireId)
                .statut(statut)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .mois(mois)
                .annee(annee)
                .active(active)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        InvoiceDTO.PageResponse response = invoiceService.findWithFilters(filter);

        return ResponseEntity.ok(ApiResponseDTO.<InvoiceDTO.PageResponse>builder()
                .success(true)
                .message("Factures récupérées avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Lister les factures récentes",
            description = "Récupère les factures récentes (pour le dashboard)"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @GetMapping("/recent")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<InvoiceDTO.Response>>> getRecentInvoices(
            @Parameter(description = "Nombre de factures") @RequestParam(defaultValue = "10") Integer limit) {

        log.debug("Recherche des {} factures récentes", limit);

        List<InvoiceDTO.Response> response = invoiceService.getRecentInvoices(limit);

        return ResponseEntity.ok(ApiResponseDTO.<List<InvoiceDTO.Response>>builder()
                .success(true)
                .message("Factures récentes récupérées avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Lister les factures en attente",
            description = "Récupère toutes les factures en statut BROUILLON"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<InvoiceDTO.Response>>> getPendingInvoices() {

        log.debug("Recherche de toutes les factures en attente");

        List<InvoiceDTO.Response> response = invoiceService.findPendingInvoices();

        return ResponseEntity.ok(ApiResponseDTO.<List<InvoiceDTO.Response>>builder()
                .success(true)
                .message("Factures en attente récupérées avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Lister les factures échues",
            description = "Récupère toutes les factures en retard de paiement"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @GetMapping("/overdue")
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<InvoiceDTO.Response>>> getOverdueInvoices() {

        log.debug("Recherche de toutes les factures échues");

        List<InvoiceDTO.Response> response = invoiceService.findOverdueInvoices();

        return ResponseEntity.ok(ApiResponseDTO.<List<InvoiceDTO.Response>>builder()
                .success(true)
                .message("Factures échues récupérées avec succès")
                .data(response)
                .build());
    }

    // ========== Gestion des statuts ==========

    @Operation(
            summary = "Changer le statut d'une facture",
            description = "Modifie le statut d'une facture selon le workflow d'approbation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut modifié avec succès",
                    content = @Content(schema = @Schema(implementation = InvoiceDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Facture non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Changement de statut non autorisé",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PutMapping("/{id}/status")
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<InvoiceDTO.Response>> changeInvoiceStatus(
            @Parameter(description = "ID de la facture") @PathVariable Long id,
            @Parameter(description = "Nouveau statut") @RequestParam InvoiceStatus statut,
            @Parameter(description = "Commentaire") @RequestParam(required = false) String commentaire) {

        log.info("Changement de statut pour la facture ID: {} vers {}", id, statut);

        InvoiceDTO.Response response = invoiceService.changeStatus(id, statut, commentaire);

        return ResponseEntity.ok(ApiResponseDTO.<InvoiceDTO.Response>builder()
                .success(true)
                .message("Statut de la facture modifié avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Émettre une facture",
            description = "Passe une facture de BROUILLON à EMISE"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facture émise avec succès",
                    content = @Content(schema = @Schema(implementation = InvoiceDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Facture non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Émission non autorisée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PatchMapping("/{id}/emit")
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<InvoiceDTO.Response>> emitInvoice(
            @Parameter(description = "ID de la facture") @PathVariable Long id,
            @Parameter(description = "Commentaire d'émission") @RequestParam(required = false) String commentaire) {

        log.info("Émission de la facture ID: {}", id);

        InvoiceDTO.Response response = invoiceService.emit(id, commentaire);

        return ResponseEntity.ok(ApiResponseDTO.<InvoiceDTO.Response>builder()
                .success(true)
                .message("Facture émise avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Marquer comme payée",
            description = "Marque une facture émise comme payée"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facture marquée comme payée",
                    content = @Content(schema = @Schema(implementation = InvoiceDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Facture non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Paiement non autorisé",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PatchMapping("/{id}/mark-paid")
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<InvoiceDTO.Response>> markInvoiceAsPaid(
            @Parameter(description = "ID de la facture") @PathVariable Long id,
            @Parameter(description = "Commentaire de paiement") @RequestParam(required = false) String commentaire) {

        log.info("Marquage comme payée de la facture ID: {}", id);

        InvoiceDTO.Response response = invoiceService.markAsPaid(id, commentaire);

        return ResponseEntity.ok(ApiResponseDTO.<InvoiceDTO.Response>builder()
                .success(true)
                .message("Facture marquée comme payée")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Annuler une facture",
            description = "Annule une facture avec commentaire obligatoire"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facture annulée avec succès",
                    content = @Content(schema = @Schema(implementation = InvoiceDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Facture non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Commentaire obligatoire",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PatchMapping("/{id}/cancel")
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<InvoiceDTO.Response>> cancelInvoice(
            @Parameter(description = "ID de la facture") @PathVariable Long id,
            @Parameter(description = "Commentaire d'annulation (obligatoire)") @RequestParam String commentaire) {

        log.info("Annulation de la facture ID: {}", id);

        InvoiceDTO.Response response = invoiceService.cancel(id, commentaire);

        return ResponseEntity.ok(ApiResponseDTO.<InvoiceDTO.Response>builder()
                .success(true)
                .message("Facture annulée avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Remettre en brouillon",
            description = "Remet une facture annulée en statut BROUILLON"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facture remise en brouillon",
                    content = @Content(schema = @Schema(implementation = InvoiceDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Facture non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PatchMapping("/{id}/mark-draft")
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<InvoiceDTO.Response>> markInvoiceAsDraft(
            @Parameter(description = "ID de la facture") @PathVariable Long id,
            @Parameter(description = "Commentaire") @RequestParam(required = false) String commentaire) {

        log.info("Remise en brouillon de la facture ID: {}", id);

        InvoiceDTO.Response response = invoiceService.markAsDraft(id, commentaire);

        return ResponseEntity.ok(ApiResponseDTO.<InvoiceDTO.Response>builder()
                .success(true)
                .message("Facture remise en brouillon")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Mettre à jour les factures en retard",
            description = "Met à jour automatiquement le statut des factures échues"
    )
    @ApiResponse(responseCode = "200", description = "Mise à jour effectuée")
    @PatchMapping("/update-overdue")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Integer>> updateOverdueInvoices() {

        log.info("Mise à jour des factures en retard");

        int updatedCount = invoiceService.updateOverdueInvoices();

        return ResponseEntity.ok(ApiResponseDTO.<Integer>builder()
                .success(true)
                .message("Factures en retard mises à jour")
                .data(updatedCount)
                .build());
    }

    @Operation(
            summary = "Activer/Désactiver une facture",
            description = "Bascule le statut actif/inactif d'une facture"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut modifié avec succès",
                    content = @Content(schema = @Schema(implementation = InvoiceDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Facture non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @PatchMapping("/{id}/toggle-active")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<InvoiceDTO.Response>> toggleActiveStatus(
            @Parameter(description = "ID de la facture") @PathVariable Long id,
            @Parameter(description = "Nouveau statut actif") @RequestParam Boolean active) {

        log.info("Basculement du statut actif pour la facture ID: {} vers {}", id, active);

        InvoiceDTO.Response response = invoiceService.toggleActive(id, active);

        return ResponseEntity.ok(ApiResponseDTO.<InvoiceDTO.Response>builder()
                .success(true)
                .message("Statut de la facture modifié avec succès")
                .data(response)
                .build());
    }

    @Operation(
            summary = "Supprimer une facture (logique)",
            description = "Désactive une facture (suppression logique)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facture supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Facture non trouvée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Suppression non autorisée",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteInvoice(
            @Parameter(description = "ID de la facture") @PathVariable Long id) {

        log.info("Suppression logique de la facture ID: {}", id);

        invoiceService.delete(id);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(true)
                .message("Facture supprimée avec succès")
                .build());
    }

    // ========== Export et rapports ==========

    @Operation(
            summary = "Export PDF des factures",
            description = "Exporte les factures au format PDF avec filtres"
    )
    @PostMapping("/export/pdf")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportToPdf(@Valid @RequestBody InvoiceDTO.SearchFilter filter) {

        log.info("Export PDF des factures");

        byte[] pdfBytes = invoiceService.exportToPdf(filter);
        String filename = exportService.generateFileName("pdf", "monthly");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @Operation(
            summary = "Export Excel des factures",
            description = "Exporte les factures au format Excel avec filtres"
    )
    @PostMapping("/export/excel")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportToExcel(@Valid @RequestBody InvoiceDTO.SearchFilter filter) {

        log.info("Export Excel des factures");

        byte[] excelBytes = invoiceService.exportToExcel(filter);
        String filename = exportService.generateFileName("xlsx", "monthly");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excelBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    // ========== Statistiques ==========

    @Operation(
            summary = "Obtenir les statistiques des factures",
            description = "Récupère les statistiques globales des factures"
    )
    @ApiResponse(responseCode = "200", description = "Statistiques récupérées")
    @GetMapping("/stats")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<InvoiceDTO.StatisticsResponse>> getInvoiceStats() {

        log.debug("Récupération des statistiques des factures");

        InvoiceDTO.StatisticsResponse stats = invoiceService.getStatistics();

        return ResponseEntity.ok(ApiResponseDTO.<InvoiceDTO.StatisticsResponse>builder()
                .success(true)
                .message("Statistiques récupérées avec succès")
                .data(stats)
                .build());
    }

    @Operation(
            summary = "Statistiques pour une période",
            description = "Récupère les statistiques des factures pour une période donnée"
    )
    @ApiResponse(responseCode = "200", description = "Statistiques récupérées")
    @GetMapping("/stats/period")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<InvoiceDTO.StatisticsResponse>> getInvoiceStatsForPeriod(
            @Parameter(description = "Date de début") @RequestParam LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam LocalDate endDate) {

        log.debug("Récupération des statistiques pour la période: {} à {}", startDate, endDate);

        InvoiceDTO.StatisticsResponse stats = invoiceService.getStatisticsForPeriod(startDate, endDate);

        return ResponseEntity.ok(ApiResponseDTO.<InvoiceDTO.StatisticsResponse>builder()
                .success(true)
                .message("Statistiques récupérées avec succès")
                .data(stats)
                .build());
    }

    @Operation(
            summary = "Top compagnies par chiffre d'affaires",
            description = "Récupère les meilleures compagnies par montant de factures"
    )
    @ApiResponse(responseCode = "200", description = "Top compagnies récupérées")
    @GetMapping("/stats/top-companies")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<InvoiceDTO.CompanyInvoiceStatsResponse>>> getTopCompanies(
            @Parameter(description = "Nombre de compagnies") @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "Date de début") @RequestParam(required = false) LocalDate startDate) {

        log.debug("Récupération du top {} compagnies", limit);

        List<InvoiceDTO.CompanyInvoiceStatsResponse> topCompanies =
                invoiceService.getTopCompaniesByRevenue(limit, startDate);

        return ResponseEntity.ok(ApiResponseDTO.<List<InvoiceDTO.CompanyInvoiceStatsResponse>>builder()
                .success(true)
                .message("Top compagnies récupérées avec succès")
                .data(topCompanies)
                .build());
    }

    @Operation(
            summary = "Évolution mensuelle des factures",
            description = "Récupère l'évolution mensuelle des factures"
    )
    @ApiResponse(responseCode = "200", description = "Évolution récupérée")
    @GetMapping("/stats/monthly-evolution")
//    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<InvoiceDTO.MonthlyInvoiceStatsResponse>>> getMonthlyEvolution(
            @Parameter(description = "Nombre de mois") @RequestParam(defaultValue = "12") Integer months) {

        log.debug("Récupération de l'évolution sur {} mois", months);

        List<InvoiceDTO.MonthlyInvoiceStatsResponse> evolution =
                invoiceService.getMonthlyEvolution(months);

        return ResponseEntity.ok(ApiResponseDTO.<List<InvoiceDTO.MonthlyInvoiceStatsResponse>>builder()
                .success(true)
                .message("Évolution mensuelle récupérée avec succès")
                .data(evolution)
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
        private InvoiceDTO.StatisticsResponse statistics;

        @Schema(description = "Dépenses récentes")
        private List<InvoiceDTO.Response> recentExpenses;

        @Schema(description = "Dépenses en attente")
        private List<InvoiceDTO.Response> pendingExpenses;

    }
}
