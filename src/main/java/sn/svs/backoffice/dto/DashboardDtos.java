package sn.svs.backoffice.dto;

// DTOs pour le Dashboard

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public class DashboardDtos {


    // DTO pour la réponse complète du dashboard
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardCompleteDto {
        private DashboardStatsDto stats;
        private List<EvolutionMensuelleDto> evolutionMensuelle;
        private ChartDataDto evolutionChart;
        private List<RepartitionSocieteDto> repartitionSocietes;
        private PieChartDataDto repartitionSocietesChart;
        private List<RepartitionPrestationDto> repartitionPrestations;
        private PieChartDataDto repartitionPrestationsChart;
        private List<RepartitionDepenseDto> repartitionDepenses;
        private PieChartDataDto repartitionDepensesChart;
        private DashboardKPIsDto kpis;
    }

    // DTO pour les KPIs additionnels
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardKPIsDto {
        private BigDecimal chiffreAffairesMoyenParFacture;
        private BigDecimal depenseMoyenneParOperation;
        private Long nombreSocietesClientes;
        private Long nombreOperationsFacturees;
        private BigDecimal ratioDepensesChiffreAffaires;
        private LocalDate derniereDateFacture;
        private LocalDate derniereDateDepense;
    }


    /**
     * DTO pour les statistiques générales du dashboard
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStatsDto {
        private Long totalFactures;
        private BigDecimal montantTotalFactures;
        private Long totalDepenses;
        private BigDecimal montantTotalDepenses;
    }

    /**
     * DTO pour l'évolution mensuelle
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvolutionMensuelleDto {
        private String mois;
        private Long factures;
        private Long depenses;
        private BigDecimal montantFactures;
        private BigDecimal montantDepenses;
    }

    /**
     * DTO pour la répartition par société
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepartitionSocieteDto {
        private String nom;
        private BigDecimal montant;
        private Long nombreFactures;
    }

    /**
     * DTO pour la répartition par prestations
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepartitionPrestationDto {
        private String nom;
        private BigDecimal montant;
        private Long nombreOperations;
    }

    /**
     * DTO pour la répartition des dépenses
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepartitionDepenseDto {
        private String categorie;
        private BigDecimal montant;
        private Long nombreDepenses;
    }

    /**
     * DTO pour les données de graphique
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDataDto {
        private List<String> labels;
        private List<ChartDatasetDto> datasets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDatasetDto {
        private String label;
        private List<Object> data; // Object pour supporter Number et String
        private String backgroundColor;
        private String borderColor;
        private Integer borderWidth;
        private Boolean fill;
        private Double tension;
    }

    /**
     * DTO pour les données de graphique en secteurs
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieChartDataDto {
        private List<String> labels;
        private List<PieChartDatasetDto> datasets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieChartDatasetDto {
        private List<BigDecimal> data;
        private List<String> backgroundColor;
        private List<String> hoverBackgroundColor;
    }

    /**
     * DTO pour les paramètres de filtre du dashboard
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardFilterDto {
        private Integer annee;
        private Integer mois;
        private Long compagnieId;
        private String devise;

        @Builder.Default
        private Integer nombreMoisEvolution = 12;

        @Builder.Default
        private Integer limiteRepartition = 6;
    }
}
