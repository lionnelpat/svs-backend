package sn.svs.backoffice.mapper;

import org.mapstruct.*;
import sn.svs.backoffice.dto.InvoiceDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mapper MapStruct pour les statistiques des factures
 * Gère les conversions des projections de requêtes vers les DTOs de statistiques
 * SVS - Dakar, Sénégal
 */
@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface InvoiceStatisticsMapper {

    // ========================================
    // INTERFACES POUR LES PROJECTIONS JPA
    // ========================================

    /**
     * Interface de projection pour les statistiques mensuelles
     * Utilisée avec les requêtes JPA natives ou JPQL
     */
    interface MonthlyStatsProjection {
        Integer getMois();
        Integer getAnnee();
        Long getNombreFactures();
        BigDecimal getMontantTotalXOF();
        BigDecimal getMontantTotalEURO();
    }

    /**
     * Interface de projection pour les statistiques par compagnie
     * Utilisée avec les requêtes JPA natives ou JPQL
     */
    interface CompanyStatsProjection {
        Long getCompagnieId();
        String getCompagnieNom();
        Long getNombreFactures();
        BigDecimal getMontantTotalXOF();
        BigDecimal getMontantTotalEURO();
    }

    /**
     * Interface de projection pour les statistiques générales
     * Utilisée avec les requêtes JPA natives ou JPQL
     */
    interface GeneralStatsProjection {
        Long getTotalFactures();
        BigDecimal getTotalMontantXOF();
        BigDecimal getTotalMontantEURO();
        Long getFacturesEnAttente();
        Long getFacturesPayees();
        Long getFacturesEnRetard();
    }

    // ========================================
    // CONVERSIONS POUR STATISTIQUES MENSUELLES
    // ========================================

    /**
     * Convertit une projection de statistiques mensuelles en DTO
     */
    InvoiceDTO.MonthlyInvoiceStatsResponse toMonthlyStatsDto(MonthlyStatsProjection projection);

    /**
     * Convertit une liste de projections mensuelles en DTOs
     */
    List<InvoiceDTO.MonthlyInvoiceStatsResponse> toMonthlyStatsDtoList(List<MonthlyStatsProjection> projections);

    // ========================================
    // CONVERSIONS POUR STATISTIQUES PAR COMPAGNIE
    // ========================================

    /**
     * Convertit une projection de statistiques par compagnie en DTO
     */
    InvoiceDTO.CompanyInvoiceStatsResponse toCompanyStatsDto(CompanyStatsProjection projection);

    /**
     * Convertit une liste de projections par compagnie en DTOs
     */
    List<InvoiceDTO.CompanyInvoiceStatsResponse> toCompanyStatsDtoList(List<CompanyStatsProjection> projections);

    // ========================================
    // CONVERSION POUR STATISTIQUES GÉNÉRALES
    // ========================================

    /**
     * Construit un DTO de statistiques générales à partir de données séparées
     * Cette méthode combine les différentes sources de données statistiques
     */
    @Mapping(target = "facturesParMois", source = "monthlyStats")
    @Mapping(target = "topCompagnies", source = "companyStats")
    InvoiceDTO.StatisticsResponse toStatisticsResponse(
            GeneralStatsProjection generalStats,
            List<MonthlyStatsProjection> monthlyStats,
            List<CompanyStatsProjection> companyStats
    );

    // ========================================
    // MÉTHODES UTILITAIRES POUR BUILDER PATTERN
    // ========================================

    /**
     * Crée un DTO de statistiques générales avec builder pattern
     * Utile quand les données viennent de sources multiples
     */
    default InvoiceDTO.StatisticsResponse buildStatisticsResponse(
            Long totalFactures,
            BigDecimal totalMontantXOF,
            BigDecimal totalMontantEURO,
            Long facturesEnAttente,
            Long facturesPayees,
            Long facturesEnRetard,
            List<InvoiceDTO.MonthlyInvoiceStatsResponse> facturesParMois,
            List<InvoiceDTO.CompanyInvoiceStatsResponse> topCompagnies) {

        return InvoiceDTO.StatisticsResponse.builder()
                .totalFactures(totalFactures)
                .totalMontantXOF(totalMontantXOF)
                .totalMontantEURO(totalMontantEURO)
                .facturesEnAttente(facturesEnAttente)
                .facturesPayees(facturesPayees)
                .facturesEnRetard(facturesEnRetard)
                .facturesParMois(facturesParMois)
                .topCompagnies(topCompagnies)
                .build();
    }

    /**
     * Crée un DTO de statistiques mensuelles avec builder pattern
     */
    default InvoiceDTO.MonthlyInvoiceStatsResponse buildMonthlyStats(
            Integer mois,
            Integer annee,
            Long nombreFactures,
            BigDecimal montantTotalXOF,
            BigDecimal montantTotalEURO) {

        return InvoiceDTO.MonthlyInvoiceStatsResponse.builder()
                .mois(mois)
                .annee(annee)
                .nombreFactures(nombreFactures)
                .montantTotalXOF(montantTotalXOF != null ? montantTotalXOF : BigDecimal.ZERO)
                .montantTotalEURO(montantTotalEURO != null ? montantTotalEURO : BigDecimal.ZERO)
                .build();
    }

    /**
     * Crée un DTO de statistiques par compagnie avec builder pattern
     */
    default InvoiceDTO.CompanyInvoiceStatsResponse buildCompanyStats(
            Long compagnieId,
            String compagnieNom,
            Long nombreFactures,
            BigDecimal montantTotalXOF,
            BigDecimal montantTotalEURO) {

        return InvoiceDTO.CompanyInvoiceStatsResponse.builder()
                .compagnieId(compagnieId)
                .compagnieNom(compagnieNom)
                .nombreFactures(nombreFactures)
                .montantTotalXOF(montantTotalXOF != null ? montantTotalXOF : BigDecimal.ZERO)
                .montantTotalEURO(montantTotalEURO != null ? montantTotalEURO : BigDecimal.ZERO)
                .build();
    }

    // ========================================
    // MÉTHODES POUR GESTION DES VALEURS NULLES
    // ========================================

    /**
     * Sécurise les valeurs BigDecimal nulles en les remplaçant par zéro
     */
    default BigDecimal safeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /**
     * Sécurise les valeurs Long nulles en les remplaçant par zéro
     */
    default Long safeLong(Long value) {
        return value != null ? value : 0L;
    }

    /**
     * Sécurise les valeurs Integer nulles en les remplaçant par zéro
     */
    default Integer safeInteger(Integer value) {
        return value != null ? value : 0;
    }

    // ========================================
    // MÉTHODES APRÈS MAPPING POUR NETTOYAGE
    // ========================================

    /**
     * Nettoie les données après mapping des statistiques mensuelles
     */
    @AfterMapping
    default void cleanMonthlyStats(@MappingTarget InvoiceDTO.MonthlyInvoiceStatsResponse dto) {
        if (dto.getMontantTotalXOF() == null) {
            dto.setMontantTotalXOF(BigDecimal.ZERO);
        }
        if (dto.getMontantTotalEURO() == null) {
            dto.setMontantTotalEURO(BigDecimal.ZERO);
        }
        if (dto.getNombreFactures() == null) {
            dto.setNombreFactures(0L);
        }
    }

    /**
     * Nettoie les données après mapping des statistiques par compagnie
     */
    @AfterMapping
    default void cleanCompanyStats(@MappingTarget InvoiceDTO.CompanyInvoiceStatsResponse dto) {
        if (dto.getMontantTotalXOF() == null) {
            dto.setMontantTotalXOF(BigDecimal.ZERO);
        }
        if (dto.getMontantTotalEURO() == null) {
            dto.setMontantTotalEURO(BigDecimal.ZERO);
        }
        if (dto.getNombreFactures() == null) {
            dto.setNombreFactures(0L);
        }
    }

    /**
     * Nettoie les données après mapping des statistiques générales
     */
    @AfterMapping
    default void cleanGeneralStats(@MappingTarget InvoiceDTO.StatisticsResponse dto) {
        if (dto.getTotalMontantXOF() == null) {
            dto.setTotalMontantXOF(BigDecimal.ZERO);
        }
        if (dto.getTotalMontantEURO() == null) {
            dto.setTotalMontantEURO(BigDecimal.ZERO);
        }
        if (dto.getTotalFactures() == null) {
            dto.setTotalFactures(0L);
        }
        if (dto.getFacturesEnAttente() == null) {
            dto.setFacturesEnAttente(0L);
        }
        if (dto.getFacturesPayees() == null) {
            dto.setFacturesPayees(0L);
        }
        if (dto.getFacturesEnRetard() == null) {
            dto.setFacturesEnRetard(0L);
        }
    }
}
