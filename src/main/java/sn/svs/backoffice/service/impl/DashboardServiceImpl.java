package sn.svs.backoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.svs.backoffice.dto.DashboardDtos;
import sn.svs.backoffice.repository.CompanyRepository;
import sn.svs.backoffice.repository.ExpenseRepository;
import sn.svs.backoffice.repository.InvoiceRepository;
import sn.svs.backoffice.repository.OperationRepository;
import sn.svs.backoffice.service.DashboardService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final ExpenseRepository expenseRepository;
    private final CompanyRepository companyRepository;
    private final OperationRepository operationRepository;

    // Couleurs pour les graphiques
    private static final List<String> CHART_COLORS = Arrays.asList(
            "#3b82f6", "#06b6d4", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6",
            "#ec4899", "#14b8a6", "#f97316", "#84cc16", "#6366f1", "#64748b"
    );

    private static final List<String> CHART_HOVER_COLORS = Arrays.asList(
            "#2563eb", "#0891b2", "#059669", "#d97706", "#dc2626", "#7c3aed",
            "#db2777", "#0d9488", "#ea580c", "#65a30d", "#4f46e5", "#475569"
    );

    @Override
    public DashboardDtos.DashboardStatsDto getDashboardStats(DashboardDtos.DashboardFilterDto filter) {
        log.debug("Récupération des statistiques du dashboard avec filtre: {}", filter);

        try {
            Long totalFactures = invoiceRepository.countActiveInvoices();
            BigDecimal montantTotalFactures = Optional.ofNullable(
                    invoiceRepository.sumTotalAmountActiveInvoices()
            ).orElse(BigDecimal.ZERO);

            Long totalDepenses = expenseRepository.countTotalExpenses();
            BigDecimal montantTotalDepenses = Optional.ofNullable(
                    expenseRepository.sumTotalAmountExpenses()
            ).orElse(BigDecimal.ZERO);

            return DashboardDtos.DashboardStatsDto.builder()
                    .totalFactures(totalFactures)
                    .montantTotalFactures(montantTotalFactures)
                    .totalDepenses(totalDepenses)
                    .montantTotalDepenses(montantTotalDepenses)
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques du dashboard", e);
            throw new RuntimeException("Erreur lors de la récupération des statistiques", e);
        }
    }

    @Override
    public List<DashboardDtos.EvolutionMensuelleDto> getEvolutionMensuelle(DashboardDtos.DashboardFilterDto filter) {
        log.debug("Récupération de l'évolution mensuelle avec filtre: {}", filter);

        try {
            LocalDate dateFin = LocalDate.now();
            LocalDate dateDebut = dateFin.minusMonths(filter.getNombreMoisEvolution() - 1)
                    .withDayOfMonth(1);

            // Récupération des données de factures
            List<Object[]> facturesData = invoiceRepository.findEvolutionMensuelleFactures(dateDebut, dateFin);
            Map<String, DashboardDtos.EvolutionMensuelleDto> evolutionMap = new HashMap<>();

            // Traitement des factures
            facturesData.forEach(row -> {
                Integer annee = (Integer) row[0];
                Integer mois = (Integer) row[1];
                Long nombre = (Long) row[2];
                BigDecimal montant = (BigDecimal) row[3];

                String cle = String.format("%d-%02d", annee, mois);
                String libelleMois = YearMonth.of(annee, mois)
                        .format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH));

                evolutionMap.put(cle, DashboardDtos.EvolutionMensuelleDto.builder()
                        .mois(libelleMois)
                        .factures(nombre)
                        .montantFactures(montant)
                        .depenses(0L)
                        .montantDepenses(BigDecimal.ZERO)
                        .build());
            });

            // Récupération des données de dépenses
            List<Object[]> depensesData = expenseRepository.findEvolutionMensuelleDepenses(dateDebut, dateFin);
            depensesData.forEach(row -> {
                Integer annee = (Integer) row[0];
                Integer mois = (Integer) row[1];
                Long nombre = (Long) row[2];
                BigDecimal montant = (BigDecimal) row[3];

                String cle = String.format("%d-%02d", annee, mois);
                String libelleMois = YearMonth.of(annee, mois)
                        .format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH));

                DashboardDtos.EvolutionMensuelleDto existing = evolutionMap.get(cle);
                if (existing != null) {
                    existing.setDepenses(nombre);
                    existing.setMontantDepenses(montant);
                } else {
                    evolutionMap.put(cle, DashboardDtos.EvolutionMensuelleDto.builder()
                            .mois(libelleMois)
                            .factures(0L)
                            .montantFactures(BigDecimal.ZERO)
                            .depenses(nombre)
                            .montantDepenses(montant)
                            .build());
                }
            });

            // Créer la liste complète pour tous les mois
            List<DashboardDtos.EvolutionMensuelleDto> result = new ArrayList<>();
            for (int i = 0; i < filter.getNombreMoisEvolution(); i++) {
                YearMonth yearMonth = YearMonth.from(dateDebut).plusMonths(i);
                String cle = String.format("%d-%02d", yearMonth.getYear(), yearMonth.getMonthValue());
                String libelleMois = yearMonth.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH));

                DashboardDtos.EvolutionMensuelleDto dto = evolutionMap.getOrDefault(cle,
                        DashboardDtos.EvolutionMensuelleDto.builder()
                                .mois(libelleMois)
                                .factures(0L)
                                .montantFactures(BigDecimal.ZERO)
                                .depenses(0L)
                                .montantDepenses(BigDecimal.ZERO)
                                .build()
                );
                result.add(dto);
            }

            return result;

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'évolution mensuelle", e);
            throw new RuntimeException("Erreur lors de la récupération de l'évolution mensuelle", e);
        }
    }


    @Override
    public DashboardDtos.ChartDataDto getEvolutionChart(DashboardDtos.DashboardFilterDto filter) {
        log.debug("Génération du graphique d'évolution mensuelle");

        List<DashboardDtos.EvolutionMensuelleDto> evolution = getEvolutionMensuelle(filter);

        List<String> labels = evolution.stream()
                .map(DashboardDtos.EvolutionMensuelleDto::getMois)
                .collect(Collectors.toList());

        List<DashboardDtos.ChartDatasetDto> datasets = Arrays.asList(
                DashboardDtos.ChartDatasetDto.builder()
                        .label("Factures")
                        .data(evolution.stream()
                                .map(DashboardDtos.EvolutionMensuelleDto::getFactures)
                                .collect(Collectors.toList()))
                        .backgroundColor("rgba(59, 130, 246, 0.8)")
                        .borderColor("#3b82f6")
                        .borderWidth(2)
                        .fill(false)
                        .tension(0.4)
                        .build(),
                DashboardDtos.ChartDatasetDto.builder()
                        .label("Dépenses")
                        .data(evolution.stream()
                                .map(DashboardDtos.EvolutionMensuelleDto::getDepenses)
                                .collect(Collectors.toList()))
                        .backgroundColor("rgba(239, 68, 68, 0.8)")
                        .borderColor("#ef4444")
                        .borderWidth(2)
                        .fill(false)
                        .tension(0.4)
                        .build()
        );

        return DashboardDtos.ChartDataDto.builder()
                .labels(labels)
                .datasets(datasets)
                .build();
    }

    @Override
    public DashboardDtos.ChartDataDto getEvolutionMontantsChart(DashboardDtos.DashboardFilterDto filter) {
        log.debug("Génération du graphique d'évolution des montants mensuels");

        List<DashboardDtos.EvolutionMensuelleDto> evolution = getEvolutionMensuelle(filter);

        List<String> labels = evolution.stream()
                .map(DashboardDtos.EvolutionMensuelleDto::getMois)
                .collect(Collectors.toList());

        List<DashboardDtos.ChartDatasetDto> datasets = Arrays.asList(
                DashboardDtos.ChartDatasetDto.builder()
                        .label("Montant Factures (FCFA)")
                        .data(evolution.stream()
                                .map(item -> item.getMontantFactures() != null ? item.getMontantFactures() : BigDecimal.ZERO)
                                .collect(Collectors.toList()))
                        .backgroundColor("rgba(59, 130, 246, 0.1)")
                        .borderColor("#3b82f6")
                        .borderWidth(3)
                        .fill(true)
                        .tension(0.4)
                        .build(),
                DashboardDtos.ChartDatasetDto.builder()
                        .label("Montant Dépenses (FCFA)")
                        .data(evolution.stream()
                                .map(item -> item.getMontantDepenses() != null ? item.getMontantDepenses() : BigDecimal.ZERO)
                                .collect(Collectors.toList()))
                        .backgroundColor("rgba(239, 68, 68, 0.1)")
                        .borderColor("#ef4444")
                        .borderWidth(3)
                        .fill(true)
                        .tension(0.4)
                        .build()
        );

        return DashboardDtos.ChartDataDto.builder()
                .labels(labels)
                .datasets(datasets)
                .build();
    }

    @Override
    public List<DashboardDtos.RepartitionSocieteDto> getRepartitionParSociete(DashboardDtos.DashboardFilterDto filter) {
        log.debug("Récupération de la répartition par société");

        try {
            List<Object[]> data = invoiceRepository.findRepartitionParSociete(filter.getAnnee());

            return data.stream()
                    .limit(filter.getLimiteRepartition())
                    .map(row -> DashboardDtos.RepartitionSocieteDto.builder()
                            .nom((String) row[0])
                            .nombreFactures((Long) row[1])
                            .montant((BigDecimal) row[2])
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la répartition par société", e);
            throw new RuntimeException("Erreur lors de la récupération de la répartition par société", e);
        }
    }

    @Override
    public DashboardDtos.PieChartDataDto getRepartitionSocietesChart(DashboardDtos.DashboardFilterDto filter) {
        log.debug("Génération du graphique de répartition par société");

        List<DashboardDtos.RepartitionSocieteDto> repartition = getRepartitionParSociete(filter);

        List<String> labels = repartition.stream()
                .map(DashboardDtos.RepartitionSocieteDto::getNom)
                .collect(Collectors.toList());

        List<BigDecimal> data = repartition.stream()
                .map(DashboardDtos.RepartitionSocieteDto::getMontant)
                .collect(Collectors.toList());

        return DashboardDtos.PieChartDataDto.builder()
                .labels(labels)
                .datasets(Arrays.asList(
                        DashboardDtos.PieChartDatasetDto.builder()
                                .data(data)
                                .backgroundColor(CHART_COLORS.subList(0, Math.min(labels.size(), CHART_COLORS.size())))
                                .hoverBackgroundColor(CHART_HOVER_COLORS.subList(0, Math.min(labels.size(), CHART_HOVER_COLORS.size())))
                                .build()
                ))
                .build();
    }

    @Override
    public List<DashboardDtos.RepartitionPrestationDto> getRepartitionParPrestation(DashboardDtos.DashboardFilterDto filter) {
        log.debug("Récupération de la répartition par prestations");

        try {
            List<Object[]> data = invoiceRepository.findRepartitionParPrestation(filter.getAnnee());

            return data.stream()
                    .limit(filter.getLimiteRepartition())
                    .map(row -> DashboardDtos.RepartitionPrestationDto.builder()
                            .nom((String) row[0])
                            .nombreOperations((Long) row[1])
                            .montant((BigDecimal) row[2])
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la répartition par prestations", e);
            throw new RuntimeException("Erreur lors de la récupération de la répartition par prestations", e);
        }
    }

    @Override
    public DashboardDtos.PieChartDataDto getRepartitionPrestationsChart(DashboardDtos.DashboardFilterDto filter) {
        log.debug("Génération du graphique de répartition par prestations");

        List<DashboardDtos.RepartitionPrestationDto> repartition = getRepartitionParPrestation(filter);

        List<String> labels = repartition.stream()
                .map(DashboardDtos.RepartitionPrestationDto::getNom)
                .collect(Collectors.toList());

        List<BigDecimal> data = repartition.stream()
                .map(DashboardDtos.RepartitionPrestationDto::getMontant)
                .collect(Collectors.toList());

        return DashboardDtos.PieChartDataDto.builder()
                .labels(labels)
                .datasets(Arrays.asList(
                        DashboardDtos.PieChartDatasetDto.builder()
                                .data(data)
                                .backgroundColor(CHART_COLORS.subList(0, Math.min(labels.size(), CHART_COLORS.size())))
                                .hoverBackgroundColor(CHART_HOVER_COLORS.subList(0, Math.min(labels.size(), CHART_HOVER_COLORS.size())))
                                .build()
                ))
                .build();
    }

    @Override
    public List<DashboardDtos.RepartitionDepenseDto> getRepartitionDepenses(DashboardDtos.DashboardFilterDto filter) {
        log.debug("Récupération de la répartition des dépenses");

        try {
            List<Object[]> data = expenseRepository.findRepartitionParCategorie(filter.getAnnee());

            return data.stream()
                    .limit(filter.getLimiteRepartition())
                    .map(row -> DashboardDtos.RepartitionDepenseDto.builder()
                            .categorie((String) row[0])
                            .nombreDepenses((Long) row[1])
                            .montant((BigDecimal) row[2])
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la répartition des dépenses", e);
            throw new RuntimeException("Erreur lors de la récupération de la répartition des dépenses", e);
        }
    }

    @Override
    public DashboardDtos.PieChartDataDto getRepartitionDepensesChart(DashboardDtos.DashboardFilterDto filter) {
        log.debug("Génération du graphique de répartition des dépenses");

        List<DashboardDtos.RepartitionDepenseDto> repartition = getRepartitionDepenses(filter);

        List<String> labels = repartition.stream()
                .map(DashboardDtos.RepartitionDepenseDto::getCategorie)
                .collect(Collectors.toList());

        List<BigDecimal> data = repartition.stream()
                .map(DashboardDtos.RepartitionDepenseDto::getMontant)
                .collect(Collectors.toList());

        // Couleurs spécifiques pour les dépenses (tons rouges/oranges)
        List<String> depenseColors = Arrays.asList(
                "#ef4444", "#f59e0b", "#10b981", "#06b6d4", "#8b5cf6", "#ec4899"
        );
        List<String> depenseHoverColors = Arrays.asList(
                "#dc2626", "#d97706", "#059669", "#0891b2", "#7c3aed", "#db2777"
        );

        return DashboardDtos.PieChartDataDto.builder()
                .labels(labels)
                .datasets(Arrays.asList(
                        DashboardDtos.PieChartDatasetDto.builder()
                                .data(data)
                                .backgroundColor(depenseColors.subList(0, Math.min(labels.size(), depenseColors.size())))
                                .hoverBackgroundColor(depenseHoverColors.subList(0, Math.min(labels.size(), depenseHoverColors.size())))
                                .build()
                ))
                .build();
    }

    @Override
    public DashboardDtos.DashboardKPIsDto getKPIsAdditionnels(DashboardDtos.DashboardFilterDto filter) {
        log.debug("Récupération des KPIs additionnels");

        try {
            DashboardDtos.DashboardStatsDto stats = getDashboardStats(filter);

//            // Calcul du chiffre d'affaires moyen par facture
//            BigDecimal DashboardDtos.chiffreAffairesMoyenParFacture = BigDecimal.ZERO;
//            if (stats.getTotalFactures() > 0) {
//                DashboardDtos.chiffreAffairesMoyenParFacture = stats.getMontantTotalFactures()
//                        .divide(BigDecimal.valueOf(stats.getTotalFactures()), 2, RoundingMode.HALF_UP);
//            }

            // Calcul de la dépense moyenne par opération
            BigDecimal depenseMoyenneParOperation = BigDecimal.ZERO;
            if (stats.getTotalDepenses() > 0) {
                depenseMoyenneParOperation = stats.getMontantTotalDepenses()
                        .divide(BigDecimal.valueOf(stats.getTotalDepenses()), 2, RoundingMode.HALF_UP);
            }

            // Calcul du ratio dépenses/chiffre d'affaires
            BigDecimal ratioDepensesChiffreAffaires = BigDecimal.ZERO;
            if (stats.getMontantTotalFactures().compareTo(BigDecimal.ZERO) > 0) {
                ratioDepensesChiffreAffaires = stats.getMontantTotalDepenses()
                        .divide(stats.getMontantTotalFactures(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            // Autres KPIs (nécessitent des requêtes additionnelles)
            Long nombreSocietesClientes = companyRepository.count();
            Long nombreOperationsFacturees = operationRepository.count();

            return DashboardDtos.DashboardKPIsDto.builder()
                    .chiffreAffairesMoyenParFacture(BigDecimal.ZERO)
                    .depenseMoyenneParOperation(depenseMoyenneParOperation)
                    .nombreSocietesClientes(nombreSocietesClientes)
                    .nombreOperationsFacturees(nombreOperationsFacturees)
                    .ratioDepensesChiffreAffaires(ratioDepensesChiffreAffaires)
                    .derniereDateFacture(LocalDate.now()) // À remplacer par une vraie requête
                    .derniereDateDepense(LocalDate.now()) // À remplacer par une vraie requête
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des KPIs additionnels", e);
            throw new RuntimeException("Erreur lors de la récupération des KPIs additionnels", e);
        }
    }

    /**
     * Méthode utilitaire pour formater les montants en FCFA
     */
    public String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 FCFA";
        }

        return String.format("%,.0f FCFA", amount);
    }

    /**
     * Méthode utilitaire pour calculer les pourcentages
     */
    public BigDecimal calculatePercentage(BigDecimal part, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return part.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
