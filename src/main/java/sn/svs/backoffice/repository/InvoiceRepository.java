package sn.svs.backoffice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.svs.backoffice.domain.Invoice;
import sn.svs.backoffice.domain.ennumeration.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository pour l'entité Invoice
 * SVS - Dakar, Sénégal
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

    /**
     * Recherche par numéro unique
     */
    Optional<Invoice> findByNumeroAndActiveTrue(String numero);

    /**
     * Vérification d'unicité du numéro (pour un autre ID)
     */
    boolean existsByNumeroAndIdNotAndActiveTrue(String numero, Long id);

    /**
     * Recherche textuelle dans numéro et notes avec relations complètes
     */
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.compagnie c " +
            "LEFT JOIN FETCH i.navire n " +
            "LEFT JOIN FETCH i.prestations p " +
            "LEFT JOIN FETCH p.operation o " +
            "WHERE i.active = true " +
            "AND (LOWER(i.numero) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(i.notes) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(n.nom) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY i.dateFacture DESC, i.id DESC")
    Page<Invoice> findBySearchTermWithFetch(@Param("search") String search, Pageable pageable);

    /**
     * Recherche par compagnie avec pagination
     */
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.compagnie c " +
            "LEFT JOIN FETCH i.navire n " +
            "LEFT JOIN FETCH i.prestations p " +
            "LEFT JOIN FETCH p.operation o " +
            "WHERE i.active = true AND i.compagnieId = :compagnieId " +
            "ORDER BY i.dateFacture DESC")
    Page<Invoice> findByCompagnieIdAndActiveTrueWithFetch(@Param("compagnieId") Long compagnieId, Pageable pageable);

    /**
     * Recherche par navire avec pagination
     */
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.compagnie c " +
            "LEFT JOIN FETCH i.navire n " +
            "LEFT JOIN FETCH i.prestations p " +
            "LEFT JOIN FETCH p.operation o " +
            "WHERE i.active = true AND i.navireId = :navireId " +
            "ORDER BY i.dateFacture DESC")
    Page<Invoice> findByNavireIdAndActiveTrueWithFetch(@Param("navireId") Long navireId, Pageable pageable);

    /**
     * Recherche par statut avec pagination
     */
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.compagnie c " +
            "LEFT JOIN FETCH i.navire n " +
            "LEFT JOIN FETCH i.prestations p " +
            "LEFT JOIN FETCH p.operation o " +
            "WHERE i.active = true AND i.statut = :statut " +
            "ORDER BY i.dateFacture DESC")
    Page<Invoice> findByStatutAndActiveTrueWithFetch(@Param("statut") InvoiceStatus statut, Pageable pageable);

    /**
     * Recherche par période (année/mois)
     */
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.compagnie c " +
            "LEFT JOIN FETCH i.navire n " +
            "LEFT JOIN FETCH i.prestations p " +
            "LEFT JOIN FETCH p.operation o " +
            "WHERE i.active = true " +
            "AND (:year IS NULL OR YEAR(i.dateFacture) = :year) " +
            "AND (:month IS NULL OR MONTH(i.dateFacture) = :month) " +
            "ORDER BY i.dateFacture DESC")
    Page<Invoice> findByDatePeriodWithFetch(
            @Param("year") Integer year,
            @Param("month") Integer month,
            Pageable pageable);

    /**
     * Recherche par plage de dates
     */
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.compagnie c " +
            "LEFT JOIN FETCH i.navire n " +
            "LEFT JOIN FETCH i.prestations p " +
            "LEFT JOIN FETCH p.operation o " +
            "WHERE i.active = true " +
            "AND i.dateFacture BETWEEN :startDate AND :endDate " +
            "ORDER BY i.dateFacture DESC")
    Page<Invoice> findByDateRangeWithFetch(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    // ========================================
    // REQUÊTES DE STATISTIQUES
    // ========================================

    /**
     * Statistiques générales
     */
    @Query("SELECT new map(" +
            "COUNT(i) as totalFactures, " +
            "SUM(i.montantTotal) as totalMontantXOF, " +
            "SUM(CASE WHEN p.montantEURO IS NOT NULL THEN p.montantEURO ELSE 0 END) as totalMontantEURO) " +
            "FROM Invoice i " +
            "LEFT JOIN i.prestations p " +
            "WHERE i.active = true")
    Map<String, Object> getGeneralStats();

    /**
     * Répartition par statut avec comptage et montants
     */
    @Query("SELECT i.statut, COUNT(i), SUM(i.montantTotal), " +
            "SUM(CASE WHEN p.montantEURO IS NOT NULL THEN p.montantEURO ELSE 0 END) " +
            "FROM Invoice i " +
            "LEFT JOIN i.prestations p " +
            "WHERE i.active = true " +
            "GROUP BY i.statut " +
            "ORDER BY COUNT(i) DESC")
    List<Object[]> getStatsByStatus();

    /**
     * Statistiques par compagnie (Top compagnies)
     */
    @Query("SELECT i.compagnieId, c.nom, COUNT(i), SUM(i.montantTotal), " +
            "SUM(CASE WHEN p.montantEURO IS NOT NULL THEN p.montantEURO ELSE 0 END) " +
            "FROM Invoice i " +
            "LEFT JOIN i.compagnie c " +
            "LEFT JOIN i.prestations p " +
            "WHERE i.active = true " +
            "GROUP BY i.compagnieId, c.nom " +
            "ORDER BY SUM(i.montantTotal) DESC")
    List<Object[]> getStatsByCompany(Pageable pageable);

    /**
     * Évolution mensuelle des factures
     */
    @Query("SELECT YEAR(i.dateFacture), MONTH(i.dateFacture), COUNT(i), SUM(i.montantTotal), " +
            "SUM(CASE WHEN p.montantEURO IS NOT NULL THEN p.montantEURO ELSE 0 END) " +
            "FROM Invoice i " +
            "LEFT JOIN i.prestations p " +
            "WHERE i.active = true " +
            "AND i.dateFacture >= :startDate " +
            "GROUP BY YEAR(i.dateFacture), MONTH(i.dateFacture) " +
            "ORDER BY YEAR(i.dateFacture), MONTH(i.dateFacture)")
    List<Object[]> getMonthlyEvolution(@Param("startDate") LocalDate startDate);

    /**
     * Comptage des factures par statut
     */
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.active = true AND i.statut = 'EN_ATTENTE'")
    Long countFacturesEnAttente();

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.active = true AND i.statut = 'PAYEE'")
    Long countFacturesPayees();

    /**
     * Comptage des factures en retard
     */
    @Query("SELECT COUNT(i) FROM Invoice i " +
            "WHERE i.active = true " +
            "AND i.statut IN ('EMISE', 'BROUILLON') " +
            "AND i.dateEcheance < CURRENT_DATE")
    Long countFacturesEnRetard();

    // ========================================
    // REQUÊTES POUR LE DASHBOARD
    // ========================================

    /**
     * Factures récentes (pour dashboard)
     */
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.compagnie c " +
            "LEFT JOIN FETCH i.navire n " +
            "WHERE i.active = true " +
            "ORDER BY i.createdAt DESC")
    List<Invoice> findRecentInvoicesWithFetch(Pageable pageable);

    /**
     * Factures en attente d'approbation
     */
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.compagnie c " +
            "LEFT JOIN FETCH i.navire n " +
            "WHERE i.active = true AND i.statut = 'BROUILLON' " +
            "ORDER BY i.dateFacture DESC")
    List<Invoice> findPendingInvoicesWithFetch();

    /**
     * Factures échues non payées
     */
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.compagnie c " +
            "LEFT JOIN FETCH i.navire n " +
            "WHERE i.active = true " +
            "AND i.statut IN ('EMISE', 'BROUILLON') " +
            "AND i.dateEcheance < CURRENT_DATE " +
            "ORDER BY i.dateEcheance ASC")
    List<Invoice> findOverdueInvoicesWithFetch();

    /**
     * Top compagnies par chiffre d'affaires
     */
    @Query("SELECT c.nom, SUM(i.montantTotal) " +
            "FROM Invoice i " +
            "LEFT JOIN i.compagnie c " +
            "WHERE i.active = true " +
            "AND i.statut = 'PAYEE' " +
            "AND i.dateFacture >= :startDate " +
            "GROUP BY c.nom " +
            "ORDER BY SUM(i.montantTotal) DESC")
    List<Object[]> getTopCompaniesByRevenue(@Param("startDate") LocalDate startDate, Pageable pageable);

    // ========================================
    // REQUÊTES POUR FILTRES AVANCÉS
    // ========================================

    /**
     * Recherche de factures par plage de montants
     */
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.compagnie c " +
            "LEFT JOIN FETCH i.navire n " +
            "LEFT JOIN FETCH i.prestations p " +
            "LEFT JOIN FETCH p.operation o " +
            "WHERE i.active = true " +
            "AND i.montantTotal BETWEEN :minAmount AND :maxAmount " +
            "ORDER BY i.montantTotal DESC")
    Page<Invoice> findByAmountRangeWithFetch(
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable);

    /**
     * Factures d'une compagnie spécifique (sans pagination)
     */
    List<Invoice> findByCompagnieIdAndActiveTrueOrderByDateFactureDesc(Long compagnieId);

    /**
     * Factures d'un navire spécifique (sans pagination)
     */
    List<Invoice> findByNavireIdAndActiveTrueOrderByDateFactureDesc(Long navireId);

    /**
     * Factures par statut (sans pagination)
     */
    List<Invoice> findByStatutAndActiveTrueOrderByDateFactureDesc(InvoiceStatus statut);

    // ========================================
    // REQUÊTES DE VALIDATION ET VÉRIFICATION
    // ========================================

    /**
     * Vérification d'existence avec relations
     */
    @Query("SELECT COUNT(i) > 0 FROM Invoice i WHERE i.compagnieId = :compagnieId AND i.active = true")
    boolean existsByCompagnieIdAndActiveTrue(@Param("compagnieId") Long compagnieId);

    @Query("SELECT COUNT(i) > 0 FROM Invoice i WHERE i.navireId = :navireId AND i.active = true")
    boolean existsByNavireIdAndActiveTrue(@Param("navireId") Long navireId);

    /**
     * Vérification d'existence des prestations avec opérations
     */
    @Query("SELECT COUNT(p) > 0 FROM InvoiceLineItem p WHERE p.operationId = :operationId")
    boolean existsLineItemByOperationId(@Param("operationId") Long operationId);

    // ========================================
    // REQUÊTES DE MISE À JOUR EN LOT
    // ========================================

    /**
     * Suppression logique en lot par statut
     */
    @Query("UPDATE Invoice i SET i.active = false WHERE i.statut = :statut")
    int deleteByStatus(@Param("statut") InvoiceStatus statut);

    /**
     * Changement de statut en lot
     */
    @Query("UPDATE Invoice i SET i.statut = :newStatut WHERE i.statut = :oldStatut AND i.active = true")
    int updateStatusBatch(@Param("oldStatut") InvoiceStatus oldStatut, @Param("newStatut") InvoiceStatus newStatut);

    /**
     * Mise à jour du statut des factures en retard
     */
    @Query("UPDATE Invoice i SET i.statut = 'EN_RETARD' " +
            "WHERE i.active = true " +
            "AND i.statut = 'EMISE' " +
            "AND i.dateEcheance < CURRENT_DATE")
    int updateOverdueInvoicesStatus();

    // ========================================
    // REQUÊTES OPTIMISÉES POUR L'EXPORT
    // ========================================

    /**
     * Recherche avec fetch optimisé pour un ID spécifique
     */
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.compagnie c " +
            "LEFT JOIN FETCH i.navire n " +
            "LEFT JOIN FETCH i.prestations p " +
            "LEFT JOIN FETCH p.operation o " +
            "WHERE i.id = :id AND i.active = true")
    Optional<Invoice> findByIdWithFetch(@Param("id") Long id);

    /**
     * Données pour export Excel (optimisé sans relations)
     */
    @Query("SELECT new map(" +
            "i.numero as numero, " +
            "c.nom as compagnie, " +
            "n.nom as navire, " +
            "i.dateFacture as dateFacture, " +
            "i.montantTotal as montantXOF, " +
            "i.statut as statut, " +
            "i.dateEcheance as dateEcheance) " +
            "FROM Invoice i " +
            "LEFT JOIN i.compagnie c " +
            "LEFT JOIN i.navire n " +
            "WHERE i.active = true " +
            "ORDER BY i.dateFacture DESC")
    List<Map<String, Object>> findAllForExport();

    /**
     * SUPPRIMÉ - Remplacé par InvoiceSpecification.withFilters()
     * Utiliser findAll(InvoiceSpecification.withFilters(filter)) à la place
     */

    // ========================================
    // REQUÊTES UTILITAIRES
    // ========================================

    /**
     * Liste de tous les numéros existants (pour validation d'unicité côté client)
     */
    @Query("SELECT i.numero FROM Invoice i WHERE i.active = true")
    List<String> findAllActiveNumeros();

    /**
     * Dernière facture créée (pour génération du prochain numéro)
     */
    @Query("SELECT i FROM Invoice i WHERE i.active = true ORDER BY i.id DESC")
    List<Invoice> findLastCreatedInvoice(Pageable pageable);

    /**
     * Montant total des factures pour une période donnée
     */
    @Query("SELECT SUM(i.montantTotal) FROM Invoice i " +
            "WHERE i.active = true " +
            "AND i.statut = 'PAYEE' " +
            "AND i.dateFacture BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueForPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Nombre de factures créées ce mois
     */
    @Query("SELECT COUNT(i) FROM Invoice i " +
            "WHERE i.active = true " +
            "AND YEAR(i.dateFacture) = YEAR(CURRENT_DATE) " +
            "AND MONTH(i.dateFacture) = MONTH(CURRENT_DATE)")
    Long countInvoicesThisMonth();

    /**
     * Compagnies avec factures impayées
     */
    @Query("SELECT DISTINCT c.id, c.nom " +
            "FROM Invoice i " +
            "LEFT JOIN i.compagnie c " +
            "WHERE i.active = true " +
            "AND i.statut IN ('EMISE', 'EN_RETARD') " +
            "ORDER BY c.nom")
    List<Object[]> findCompaniesWithUnpaidInvoices();
}