package sn.svs.backoffice.service;

import sn.svs.backoffice.dto.DashboardDtos;

import java.util.List;
import java.time.LocalDate;

/**
 * Service interface pour les données du dashboard
 */
public interface DashboardService {

    /**
     * Récupère les statistiques générales du dashboard
     */
    DashboardDtos.DashboardStatsDto getDashboardStats(DashboardDtos.DashboardFilterDto filter);

    /**
     * Récupère l'évolution mensuelle des factures et dépenses
     */
    List<DashboardDtos.EvolutionMensuelleDto> getEvolutionMensuelle(DashboardDtos.DashboardFilterDto filter);

    /**
     * Récupère les données de graphique pour l'évolution mensuelle des factures et dépenses
     */
    DashboardDtos.ChartDataDto getEvolutionMontantsChart(DashboardDtos.DashboardFilterDto filter);

    /**
     * Récupère les données de graphique pour l'évolution mensuelle
     */
    DashboardDtos.ChartDataDto getEvolutionChart(DashboardDtos.DashboardFilterDto filter);

    /**
     * Récupère la répartition par société
     */
    List<DashboardDtos.RepartitionSocieteDto> getRepartitionParSociete(DashboardDtos.DashboardFilterDto filter);

    /**
     * Récupère les données de graphique pour la répartition par société
     */
    DashboardDtos.PieChartDataDto getRepartitionSocietesChart(DashboardDtos.DashboardFilterDto filter);

    /**
     * Récupère la répartition par prestations
     */
    List<DashboardDtos.RepartitionPrestationDto> getRepartitionParPrestation(DashboardDtos.DashboardFilterDto filter);

    /**
     * Récupère les données de graphique pour la répartition par prestations
     */
    DashboardDtos.PieChartDataDto getRepartitionPrestationsChart(DashboardDtos.DashboardFilterDto filter);

    /**
     * Récupère la répartition des dépenses par catégorie
     */
    List<DashboardDtos.RepartitionDepenseDto> getRepartitionDepenses(DashboardDtos.DashboardFilterDto filter);

    /**
     * Récupère les données de graphique pour la répartition des dépenses
     */
    DashboardDtos.PieChartDataDto getRepartitionDepensesChart(DashboardDtos.DashboardFilterDto filter);

    /**
     * Récupère les KPIs additionnels
     */
    DashboardDtos.DashboardKPIsDto getKPIsAdditionnels(DashboardDtos.DashboardFilterDto filter);
}

