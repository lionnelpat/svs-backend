package sn.svs.backoffice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.svs.backoffice.domain.ExpenseCategory;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité ExpenseCategory
 * SVS - Dakar, Sénégal
 */
@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {

    /**
     * Trouve une catégorie par son code
     */
    Optional<ExpenseCategory> findByCode(String code);

    /**
     * Vérifie si une catégorie avec ce code existe déjà
     */
    boolean existsByCode(String code);

    /**
     * Vérifie si une catégorie avec ce code existe (en excluant un ID spécifique)
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * Trouve toutes les catégories actives
     */
    List<ExpenseCategory> findByActiveTrueOrderByNomAsc();

    /**
     * Trouve toutes les catégories par statut avec pagination
     */
    Page<ExpenseCategory> findByActive(Boolean active, Pageable pageable);

    /**
     * Recherche avancée avec filtres
     */
    @Query("""
        SELECT ec FROM ExpenseCategory ec
        WHERE (:search IS NULL OR 
               LOWER(ec.nom) LIKE :search OR 
               LOWER(ec.code) LIKE :search OR 
               LOWER(ec.description) LIKE :search)
        AND (:active IS NULL OR ec.active = :active)
        """)
    Page<ExpenseCategory> findWithFilters(
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable
    );

    /**
     * Compte le nombre de catégories par statut
     */
    long countByActive(Boolean active);

    /**
     * Trouve les catégories par nom (recherche partielle, insensible à la casse)
     */
    @Query("SELECT ec FROM ExpenseCategory ec WHERE LOWER(ec.nom) LIKE LOWER(CONCAT('%', :nom, '%'))")
    List<ExpenseCategory> findByNomContainingIgnoreCase(@Param("nom") String nom);

    /**
     * Trouve les catégories les plus récentes
     */
    @Query("SELECT ec FROM ExpenseCategory ec ORDER BY ec.createdAt DESC")
    Page<ExpenseCategory> findMostRecent(Pageable pageable);

    /**
     * Vérifie si une catégorie avec ce nom existe déjà (insensible à la casse)
     */
    @Query("SELECT COUNT(ec) > 0 FROM ExpenseCategory ec WHERE LOWER(ec.nom) = LOWER(:nom)")
    boolean existsByNomIgnoreCase(@Param("nom") String nom);

    /**
     * Vérifie si une catégorie avec ce nom existe déjà (insensible à la casse, en excluant un ID)
     */
    @Query("SELECT COUNT(ec) > 0 FROM ExpenseCategory ec WHERE LOWER(ec.nom) = LOWER(:nom) AND ec.id != :id")
    boolean existsByNomIgnoreCaseAndIdNot(@Param("nom") String nom, @Param("id") Long id);

    /**
     * Statistiques : compte total par statut
     */
    @Query("""
        SELECT 
            CASE WHEN ec.active = true THEN 'ACTIVE' ELSE 'INACTIVE' END as status,
            COUNT(ec) as count
        FROM ExpenseCategory ec
        GROUP BY ec.active
        """)
    List<Object[]> getCategoryStats();

    boolean existsByIdAndActiveTrue(Long categorieId);
}
