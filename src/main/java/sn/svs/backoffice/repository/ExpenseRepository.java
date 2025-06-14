package sn.svs.backoffice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.svs.backoffice.domain.Expense;
import sn.svs.backoffice.domain.ennumeration.ExpenseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository pour l'entité Expense
 * SVS - Dakar, Sénégal
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    /**
     * Recherche par numéro unique
     */
    Optional<Expense> findByNumeroAndActiveTrue(String numero);

    /**
     * Vérification d'unicité du numéro (pour un autre ID)
     */
    boolean existsByNumeroAndIdNotAndActiveTrue(String numero, Long id);

    /**
     * Recherche textuelle dans titre, numéro et description
     */
    @Query("SELECT e FROM Expense e " +
            "LEFT JOIN FETCH e.categorie c " +
            "LEFT JOIN FETCH e.fournisseur f " +
            "LEFT JOIN FETCH e.paymentMethod pm " +
            "WHERE e.active = true " +
            "AND (LOWER(e.titre) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.numero) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY e.dateDepense DESC, e.id DESC")
    Page<Expense> findBySearchTermWithFetch(@Param("search") String search, Pageable pageable);

    /**
     * Recherche par catégorie avec pagination
     */
    @Query("SELECT e FROM Expense e " +
            "LEFT JOIN FETCH e.categorie c " +
            "LEFT JOIN FETCH e.fournisseur f " +
            "LEFT JOIN FETCH e.paymentMethod pm " +
            "WHERE e.active = true AND e.categorieId = :categorieId " +
            "ORDER BY e.dateDepense DESC")
    Page<Expense> findByCategorieIdAndActiveTrueWithFetch(@Param("categorieId") Long categorieId, Pageable pageable);

    /**
     * Recherche par fournisseur avec pagination
     */
    @Query("SELECT e FROM Expense e " +
            "LEFT JOIN FETCH e.categorie c " +
            "LEFT JOIN FETCH e.fournisseur f " +
            "LEFT JOIN FETCH e.paymentMethod pm " +
            "WHERE e.active = true AND e.fournisseurId = :fournisseurId " +
            "ORDER BY e.dateDepense DESC")
    Page<Expense> findByFournisseurIdAndActiveTrueWithFetch(@Param("fournisseurId") Long fournisseurId, Pageable pageable);

    /**
     * Recherche par statut avec pagination
     */
    @Query("SELECT e FROM Expense e " +
            "LEFT JOIN FETCH e.categorie c " +
            "LEFT JOIN FETCH e.fournisseur f " +
            "LEFT JOIN FETCH e.paymentMethod pm " +
            "WHERE e.active = true AND e.statut = :statut " +
            "ORDER BY e.dateDepense DESC")
    Page<Expense> findByStatutAndActiveTrueWithFetch(@Param("statut") ExpenseStatus statut, Pageable pageable);

    /**
     * Recherche par période (année/mois/jour)
     */
    @Query("SELECT e FROM Expense e " +
            "LEFT JOIN FETCH e.categorie c " +
            "LEFT JOIN FETCH e.fournisseur f " +
            "LEFT JOIN FETCH e.paymentMethod pm " +
            "WHERE e.active = true " +
            "AND (:year IS NULL OR YEAR(e.dateDepense) = :year) " +
            "AND (:month IS NULL OR MONTH(e.dateDepense) = :month) " +
            "AND (:day IS NULL OR DAY(e.dateDepense) = :day) " +
            "ORDER BY e.dateDepense DESC")
    Page<Expense> findByDatePeriodWithFetch(
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("day") Integer day,
            Pageable pageable);

    /**
     * Statistiques générales
     */
    @Query("SELECT new map(COUNT(e) as total, SUM(e.montantXOF) as sumXOF, SUM(e.montantEURO) as sumEUR) " +
            "FROM Expense e WHERE e.active = true")
    Map<String, Object> getGeneralStats();

    /**
     * Répartition par statut avec comptage et montants
     */
    @Query("SELECT e.statut, COUNT(e), SUM(e.montantXOF), SUM(e.montantEURO) " +
            "FROM Expense e " +
            "WHERE e.active = true " +
            "GROUP BY e.statut " +
            "ORDER BY COUNT(e) DESC")
    List<Object[]> getStatsByStatus();

    /**
     * Répartition par catégorie avec comptage et montants
     */
    @Query("SELECT e.categorieId, c.nom, COUNT(e), SUM(e.montantXOF), SUM(e.montantEURO) " +
            "FROM Expense e " +
            "LEFT JOIN e.categorie c " +
            "WHERE e.active = true " +
            "GROUP BY e.categorieId, c.nom " +
            "ORDER BY SUM(e.montantXOF) DESC")
    List<Object[]> getStatsByCategory();

    /**
     * Évolution mensuelle des dépenses
     */
    @Query("SELECT YEAR(e.dateDepense), MONTH(e.dateDepense), COUNT(e), SUM(e.montantXOF), SUM(e.montantEURO) " +
            "FROM Expense e " +
            "WHERE e.active = true " +
            "AND e.dateDepense >= :startDate " +
            "GROUP BY YEAR(e.dateDepense), MONTH(e.dateDepense) " +
            "ORDER BY YEAR(e.dateDepense), MONTH(e.dateDepense)")
    List<Object[]> getMonthlyEvolution(@Param("startDate") LocalDate startDate);

    /**
     * Top catégories par montant (pour dashboard)
     */
    @Query("SELECT c.nom, SUM(e.montantXOF) " +
            "FROM Expense e " +
            "LEFT JOIN e.categorie c " +
            "WHERE e.active = true " +
            "AND e.dateDepense >= :startDate " +
            "GROUP BY c.nom " +
            "ORDER BY SUM(e.montantXOF) DESC")
    List<Object[]> getTopCategoriesByAmount(@Param("startDate") LocalDate startDate, Pageable pageable);

    /**
     * Dépenses récentes (pour dashboard)
     */
    @Query("SELECT e FROM Expense e " +
            "LEFT JOIN FETCH e.categorie c " +
            "LEFT JOIN FETCH e.fournisseur f " +
            "LEFT JOIN FETCH e.paymentMethod pm " +
            "WHERE e.active = true " +
            "ORDER BY e.createdAt DESC")
    List<Expense> findRecentExpensesWithFetch(Pageable pageable);

    /**
     * Dépenses en attente d'approbation
     */
    @Query("SELECT e FROM Expense e " +
            "LEFT JOIN FETCH e.categorie c " +
            "LEFT JOIN FETCH e.fournisseur f " +
            "LEFT JOIN FETCH e.paymentMethod pm " +
            "WHERE e.active = true AND e.statut = 'EN_ATTENTE' " +
            "ORDER BY e.dateDepense DESC")
    List<Expense> findPendingExpensesWithFetch();

    /**
     * Montant total par devise pour une période
     */
    @Query("SELECT e.devise, SUM(CASE WHEN e.devise = 'XOF' THEN e.montantXOF ELSE e.montantEURO END) " +
            "FROM Expense e " +
            "WHERE e.active = true " +
            "AND e.dateDepense BETWEEN :startDate AND :endDate " +
            "GROUP BY e.devise")
    List<Object[]> getTotalAmountsByDevise(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Recherche de dépenses par plage de montants
     */
    @Query("SELECT e FROM Expense e " +
            "LEFT JOIN FETCH e.categorie c " +
            "LEFT JOIN FETCH e.fournisseur f " +
            "LEFT JOIN FETCH e.paymentMethod pm " +
            "WHERE e.active = true " +
            "AND e.montantXOF BETWEEN :minAmount AND :maxAmount " +
            "ORDER BY e.montantXOF DESC")
    Page<Expense> findByAmountRangeWithFetch(@Param("minAmount") BigDecimal minAmount, @Param("maxAmount") BigDecimal maxAmount, Pageable pageable);

    /**
     * Dépenses d'un fournisseur spécifique
     */
    List<Expense> findByFournisseurIdAndActiveTrueOrderByDateDepenseDesc(Long fournisseurId);

    /**
     * Dépenses par mode de paiement
     */
    @Query("SELECT pm.nom, COUNT(e), SUM(e.montantXOF) " +
            "FROM Expense e " +
            "LEFT JOIN e.paymentMethod pm " +
            "WHERE e.active = true " +
            "GROUP BY pm.nom " +
            "ORDER BY SUM(e.montantXOF) DESC")
    List<Object[]> getStatsByPaymentMethod();

    /**
     * Vérification d'existence avec relations
     */
    @Query("SELECT COUNT(e) > 0 FROM Expense e WHERE e.categorieId = :categorieId AND e.active = true")
    boolean existsByCategorieIdAndActiveTrue(@Param("categorieId") Long categorieId);

    @Query("SELECT COUNT(e) > 0 FROM Expense e WHERE e.fournisseurId = :fournisseurId AND e.active = true")
    boolean existsByFournisseurIdAndActiveTrue(@Param("fournisseurId") Long fournisseurId);

    @Query("SELECT COUNT(e) > 0 FROM Expense e WHERE e.paymentMethodId = :paymentMethodId AND e.active = true")
    boolean existsByPaymentMethodIdAndActiveTrue(@Param("paymentMethodId") Long paymentMethodId);

    /**
     * Suppression logique en lot par statut
     */
    @Query("UPDATE Expense e SET e.active = false WHERE e.statut = :statut")
    int deleteByStatus(@Param("statut") ExpenseStatus statut);

    /**
     * Changement de statut en lot
     */
    @Query("UPDATE Expense e SET e.statut = :newStatut WHERE e.statut = :oldStatut AND e.active = true")
    int updateStatusBatch(@Param("oldStatut") ExpenseStatus oldStatut, @Param("newStatut") ExpenseStatus newStatut);

    /**
     * Recherche avec fetch optimisé pour un ID spécifique
     */
    @Query("SELECT e FROM Expense e " +
            "LEFT JOIN FETCH e.categorie c " +
            "LEFT JOIN FETCH e.fournisseur f " +
            "LEFT JOIN FETCH e.paymentMethod pm " +
            "WHERE e.id = :id AND e.active = true")
    Optional<Expense> findByIdWithFetch(@Param("id") Long id);

    /**
     * Liste de tous les numéros existants (pour validation d'unicité côté client)
     */
    @Query("SELECT e.numero FROM Expense e WHERE e.active = true")
    List<String> findAllActiveNumeros();
}