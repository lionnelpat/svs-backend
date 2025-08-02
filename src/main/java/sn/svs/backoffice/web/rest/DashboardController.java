package sn.svs.backoffice.web.rest;

// Contrôleur REST pour le Dashboard

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.svs.backoffice.dto.DashboardDtos;
import sn.svs.backoffice.service.DashboardService;
import sn.svs.backoffice.dto.ApiResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "API pour les données du tableau de bord")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary = "Récupère les statistiques générales du dashboard",
            description = "Retourne les statistiques principales : nombre de factures, montants, dépenses, etc."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("/stats")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<ApiResponseDTO<DashboardDtos.DashboardStatsDto>> getDashboardStats(
            @Parameter(description = "Année de filtrage (optionnel)")
            @RequestParam(required = false) Integer annee,
            @Parameter(description = "Mois de filtrage (optionnel)")
            @RequestParam(required = false) Integer mois,
            @Parameter(description = "ID de la compagnie pour filtrage (optionnel)")
            @RequestParam(required = false) Long compagnieId) {

        log.info("Récupération des statistiques du dashboard - Année: {}, Mois: {}, Compagnie: {}",
                annee, mois, compagnieId);

        try {
            DashboardDtos.DashboardFilterDto filter = DashboardDtos.DashboardFilterDto.builder()
                    .annee(annee)
                    .mois(mois)
                    .compagnieId(compagnieId)
                    .build();

            DashboardDtos.DashboardStatsDto stats = dashboardService.getDashboardStats(filter);

            return ResponseEntity.ok(ApiResponseDTO.<DashboardDtos.DashboardStatsDto>builder()
                    .success(true)
                    .message("Statistiques récupérées avec succès")
                    .data(stats)
                    .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                    .build());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques du dashboard", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.<DashboardDtos.DashboardStatsDto>builder()
                            .success(false)
                            .message("Erreur lors de la récupération des statistiques: " + e.getMessage())
                            .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                            .build());
        }
    }

    /**
     * Récupère les données de graphique pour l'évolution mensuelle des MONTANTS
     */
    @Operation(
            summary = "Récupère les données de graphique pour l'évolution des montants",
            description = "Retourne les données formatées pour un graphique linéaire des montants"
    )
    @GetMapping("/evolution-montants-chart")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<ApiResponseDTO<DashboardDtos.ChartDataDto>> getEvolutionMontantsChart(
            @RequestParam(defaultValue = "12") Integer nombreMois) {

        log.info("Génération du graphique d'évolution des montants mensuels");

        try {
            DashboardDtos.DashboardFilterDto filter = DashboardDtos.DashboardFilterDto.builder()
                    .nombreMoisEvolution(nombreMois)
                    .build();

            DashboardDtos.ChartDataDto chartData = dashboardService.getEvolutionMontantsChart(filter);

            return ResponseEntity.ok(ApiResponseDTO.<DashboardDtos.ChartDataDto>builder()
                    .success(true)
                    .message("Données de graphique des montants récupérées avec succès")
                    .data(chartData)
                    .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                    .build());

        } catch (Exception e) {
            log.error("Erreur lors de la génération du graphique d'évolution des montants", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.<DashboardDtos.ChartDataDto>builder()
                            .success(false)
                            .message("Erreur lors de la génération du graphique: " + e.getMessage())
                            .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                            .build());
        }
    }

    @Operation(
            summary = "Récupère l'évolution mensuelle des factures et dépenses",
            description = "Retourne les données d'évolution sur les 12 derniers mois par défaut"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Évolution mensuelle récupérée avec succès"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("/evolution-mensuelle")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<ApiResponseDTO<List<DashboardDtos.EvolutionMensuelleDto>>> getEvolutionMensuelle(
            @Parameter(description = "Nombre de mois à afficher (défaut: 12)")
            @RequestParam(defaultValue = "12") Integer nombreMois) {

        log.info("Récupération de l'évolution mensuelle - Nombre de mois: {}", nombreMois);

        try {
            DashboardDtos.DashboardFilterDto filter = DashboardDtos.DashboardFilterDto.builder()
                    .nombreMoisEvolution(nombreMois)
                    .build();

            List<DashboardDtos.EvolutionMensuelleDto> evolution = dashboardService.getEvolutionMensuelle(filter);

            return ResponseEntity.ok(ApiResponseDTO.<List<DashboardDtos.EvolutionMensuelleDto>>builder()
                    .success(true)
                    .message("Évolution mensuelle récupérée avec succès")
                    .data(evolution)
                    .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                    .build());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'évolution mensuelle", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.<List<DashboardDtos.EvolutionMensuelleDto>>builder()
                            .success(false)
                            .message("Erreur lors de la récupération de l'évolution: " + e.getMessage())
                            .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                            .build());
        }
    }

    @Operation(
            summary = "Récupère les données de graphique pour l'évolution mensuelle",
            description = "Retourne les données formatées pour Chart.js"
    )
    @GetMapping("/evolution-chart")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<ApiResponseDTO<DashboardDtos.ChartDataDto>> getEvolutionChart(
            @RequestParam(defaultValue = "12") Integer nombreMois) {

        log.info("Génération du graphique d'évolution mensuelle");

        try {
            DashboardDtos.DashboardFilterDto filter = DashboardDtos.DashboardFilterDto.builder()
                    .nombreMoisEvolution(nombreMois)
                    .build();

            DashboardDtos.ChartDataDto chartData = dashboardService.getEvolutionChart(filter);

            return ResponseEntity.ok(ApiResponseDTO.<DashboardDtos.ChartDataDto>builder()
                    .success(true)
                    .message("Données de graphique récupérées avec succès")
                    .data(chartData)
                    .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                    .build());

        } catch (Exception e) {
            log.error("Erreur lors de la génération du graphique d'évolution", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.<DashboardDtos.ChartDataDto>builder()
                            .success(false)
                            .message("Erreur lors de la génération du graphique: " + e.getMessage())
                            .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                            .build());
        }
    }

    @Operation(
            summary = "Récupère la répartition par société",
            description = "Retourne le top des sociétés par chiffre d'affaires"
    )
    @GetMapping("/repartition-societes")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<ApiResponseDTO<List<DashboardDtos.RepartitionSocieteDto>>> getRepartitionSocietes(
            @RequestParam(required = false) Integer annee,
            @RequestParam(defaultValue = "6") Integer limite) {

        log.info("Récupération de la répartition par société - Année: {}, Limite: {}", annee, limite);

        try {
            DashboardDtos.DashboardFilterDto filter = DashboardDtos.DashboardFilterDto.builder()
                    .annee(annee)
                    .limiteRepartition(limite)
                    .build();

            List<DashboardDtos.RepartitionSocieteDto> repartition = dashboardService.getRepartitionParSociete(filter);

            return ResponseEntity.ok(ApiResponseDTO.<List<DashboardDtos.RepartitionSocieteDto>>builder()
                    .success(true)
                    .message("Répartition par société récupérée avec succès")
                    .data(repartition)
                    .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                    .build());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la répartition par société", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.<List<DashboardDtos.RepartitionSocieteDto>>builder()
                            .success(false)
                            .message("Erreur lors de la récupération de la répartition: " + e.getMessage())
                            .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                            .build());
        }
    }

    @Operation(
            summary = "Récupère les données de graphique pour la répartition par société",
            description = "Retourne les données formatées pour un graphique en secteurs"
    )
    @GetMapping("/repartition-societes-chart")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<ApiResponseDTO<DashboardDtos.PieChartDataDto>> getRepartitionSocietesChart(
            @RequestParam(required = false) Integer annee,
            @RequestParam(defaultValue = "6") Integer limite) {

        log.info("Génération du graphique de répartition par société");

        try {
            DashboardDtos.DashboardFilterDto filter = DashboardDtos.DashboardFilterDto.builder()
                    .annee(annee)
                    .limiteRepartition(limite)
                    .build();

            DashboardDtos.PieChartDataDto chartData = dashboardService.getRepartitionSocietesChart(filter);

            return ResponseEntity.ok(ApiResponseDTO.<DashboardDtos.PieChartDataDto>builder()
                    .success(true)
                    .message("Données de graphique récupérées avec succès")
                    .data(chartData)
                    .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                    .build());

        } catch (Exception e) {
            log.error("Erreur lors de la génération du graphique de répartition par société", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.<DashboardDtos.PieChartDataDto>builder()
                            .success(false)
                            .message("Erreur lors de la génération du graphique: " + e.getMessage())
                            .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                            .build());
        }
    }

    @Operation(
            summary = "Récupère la répartition par prestations",
            description = "Retourne le top des prestations par montant facturé"
    )
    @GetMapping("/repartition-prestations")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<ApiResponseDTO<List<DashboardDtos.RepartitionPrestationDto>>> getRepartitionPrestations(
            @RequestParam(required = false) Integer annee,
            @RequestParam(defaultValue = "6") Integer limite) {

        log.info("Récupération de la répartition par prestations");

        try {
            DashboardDtos.DashboardFilterDto filter = DashboardDtos.DashboardFilterDto.builder()
                    .annee(annee)
                    .limiteRepartition(limite)
                    .build();

            List<DashboardDtos.RepartitionPrestationDto> repartition = dashboardService.getRepartitionParPrestation(filter);

            return ResponseEntity.ok(ApiResponseDTO.<List<DashboardDtos.RepartitionPrestationDto>>builder()
                    .success(true)
                    .message("Répartition par prestations récupérée avec succès")
                    .data(repartition)
                    .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                    .build());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la répartition par prestations", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.<List<DashboardDtos.RepartitionPrestationDto>>builder()
                            .success(false)
                            .message("Erreur lors de la récupération de la répartition: " + e.getMessage())
                            .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                            .build());
        }
    }

    @Operation(
            summary = "Récupère les données de graphique pour la répartition par prestations"
    )
    @GetMapping("/repartition-prestations-chart")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity< ApiResponseDTO<DashboardDtos.PieChartDataDto>> getRepartitionPrestationsChart(
            @RequestParam(required = false) Integer annee,
            @RequestParam(defaultValue = "6") Integer limite) {

        log.info("Génération du graphique de répartition par prestations");

        try {
            DashboardDtos.DashboardFilterDto filter = DashboardDtos.DashboardFilterDto.builder()
                    .annee(annee)
                    .limiteRepartition(limite)
                    .build();

            DashboardDtos.PieChartDataDto chartData = dashboardService.getRepartitionPrestationsChart(filter);

            return ResponseEntity.ok( ApiResponseDTO.<DashboardDtos.PieChartDataDto>builder()
                    .success(true)
                    .message("Données de graphique récupérées avec succès")
                    .data(chartData)
                    .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                    .build());

        } catch (Exception e) {
            log.error("Erreur lors de la génération du graphique de répartition par prestations", e);
            return ResponseEntity.internalServerError()
                    .body( ApiResponseDTO.<DashboardDtos.PieChartDataDto>builder()
                            .success(false)
                            .message("Erreur lors de la génération du graphique: " + e.getMessage())
                            .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                            .build());
        }
    }

    @Operation(
            summary = "Récupère la répartition des dépenses par catégorie"
    )
    @GetMapping("/repartition-depenses")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity< ApiResponseDTO<List<DashboardDtos.RepartitionDepenseDto>>> getRepartitionDepenses(
            @RequestParam(required = false) Integer annee,
            @RequestParam(defaultValue = "6") Integer limite) {

        log.info("Récupération de la répartition des dépenses");

        try {
            DashboardDtos.DashboardFilterDto filter = DashboardDtos.DashboardFilterDto.builder()
                    .annee(annee)
                    .limiteRepartition(limite)
                    .build();

            List<DashboardDtos.RepartitionDepenseDto> repartition = dashboardService.getRepartitionDepenses(filter);

            return ResponseEntity.ok( ApiResponseDTO.<List<DashboardDtos.RepartitionDepenseDto>>builder()
                    .success(true)
                    .message("Répartition des dépenses récupérée avec succès")
                    .data(repartition)
                    .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                    .build());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la répartition des dépenses", e);
            return ResponseEntity.internalServerError()
                    .body( ApiResponseDTO.<List<DashboardDtos.RepartitionDepenseDto>>builder()
                            .success(false)
                            .message("Erreur lors de la récupération de la répartition: " + e.getMessage())
                            .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                            .build());
        }
    }

    @Operation(
            summary = "Récupère les données de graphique pour la répartition des dépenses"
    )
    @GetMapping("/repartition-depenses-chart")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity< ApiResponseDTO<DashboardDtos.PieChartDataDto>> getRepartitionDepensesChart(
            @RequestParam(required = false) Integer annee,
            @RequestParam(defaultValue = "6") Integer limite) {

        log.info("Génération du graphique de répartition des dépenses");

        try {
            DashboardDtos.DashboardFilterDto filter = DashboardDtos.DashboardFilterDto.builder()
                    .annee(annee)
                    .limiteRepartition(limite)
                    .build();

            DashboardDtos.PieChartDataDto chartData = dashboardService.getRepartitionDepensesChart(filter);

            return ResponseEntity.ok(ApiResponseDTO.<DashboardDtos.PieChartDataDto>builder()
                    .success(true)
                    .message("Données de graphique récupérées avec succès")
                    .data(chartData)
                    .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                    .build());

        } catch (Exception e) {
            log.error("Erreur lors de la génération du graphique de répartition des dépenses", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.<DashboardDtos.PieChartDataDto>builder()
                            .success(false)
                            .message("Erreur lors de la génération du graphique: " + e.getMessage())
                            .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                            .build());
        }
    }

    @Operation(
            summary = "Récupère les KPIs additionnels du dashboard",
            description = "Retourne des métriques complémentaires pour le tableau de bord"
    )
    @GetMapping("/kpis")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<ApiResponseDTO<DashboardDtos.DashboardKPIsDto>> getKPIsAdditionnels(
            @RequestParam(required = false) Integer annee) {

        log.info("Récupération des KPIs additionnels");

        try {
            DashboardDtos.DashboardFilterDto filter = DashboardDtos.DashboardFilterDto.builder()
                    .annee(annee)
                    .build();

            DashboardDtos.DashboardKPIsDto kpis = dashboardService.getKPIsAdditionnels(filter);

            return ResponseEntity.ok(ApiResponseDTO.<DashboardDtos.DashboardKPIsDto>builder()
                    .success(true)
                    .message("KPIs additionnels récupérés avec succès")
                    .data(kpis)
                    .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                    .build());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des KPIs additionnels", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.<DashboardDtos.DashboardKPIsDto>builder()
                            .success(false)
                            .message("Erreur lors de la récupération des KPIs: " + e.getMessage())
                            .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                            .build());
        }
    }

    @Operation(
            summary = "Récupère toutes les données du dashboard en une seule requête",
            description = "Endpoint optimisé pour charger toutes les données du dashboard"
    )
    @PostMapping("/complete")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<ApiResponseDTO<DashboardDtos.DashboardCompleteDto>> getDashboardComplete(
            @Valid @RequestBody DashboardDtos.DashboardFilterDto filter) {

        log.info("Récupération complète des données du dashboard avec filtre: {}", filter);

        try {
            // Mise à jour des valeurs par défaut si nécessaires
            if (filter.getNombreMoisEvolution() == null) {
                filter.setNombreMoisEvolution(12);
            }
            if (filter.getLimiteRepartition() == null) {
                filter.setLimiteRepartition(6);
            }

            DashboardDtos.DashboardCompleteDto result = DashboardDtos.DashboardCompleteDto.builder()
                    .stats(dashboardService.getDashboardStats(filter))
                    .evolutionMensuelle(dashboardService.getEvolutionMensuelle(filter))
                    .evolutionChart(dashboardService.getEvolutionChart(filter))
                    .repartitionSocietes(dashboardService.getRepartitionParSociete(filter))
                    .repartitionSocietesChart(dashboardService.getRepartitionSocietesChart(filter))
                    .repartitionPrestations(dashboardService.getRepartitionParPrestation(filter))
                    .repartitionPrestationsChart(dashboardService.getRepartitionPrestationsChart(filter))
                    .repartitionDepenses(dashboardService.getRepartitionDepenses(filter))
                    .repartitionDepensesChart(dashboardService.getRepartitionDepensesChart(filter))
                    .kpis(dashboardService.getKPIsAdditionnels(filter))
                    .build();

            return ResponseEntity.ok(ApiResponseDTO.<DashboardDtos.DashboardCompleteDto>builder()
                    .success(true)
                    .message("Données complètes du dashboard récupérées avec succès")
                    .data(result)
                    .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                    .build());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération complète des données du dashboard", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.<DashboardDtos.DashboardCompleteDto>builder()
                            .success(false)
                            .message("Erreur lors de la récupération des données: " + e.getMessage())
                            .timestamp(LocalDateTime.parse(LocalDateTime.now().toString()))
                            .build());
        }
    }
}


