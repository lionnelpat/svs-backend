package sn.svs.backoffice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.svs.backoffice.domain.ExpenseSupplier;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité ExpenseSupplier
 * SVS - Dakar, Sénégal
 */
@Repository
public interface ExpenseSupplierRepository extends JpaRepository<ExpenseSupplier, Long> {

    /**
     * Trouve un fournisseur par son email
     */
    Optional<ExpenseSupplier> findByEmail(String email);

    /**
     * Trouve un fournisseur par son numéro NINEA
     */
    Optional<ExpenseSupplier> findByNinea(String numeroNinea);

    /**
     * Vérifie si un fournisseur avec cet email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Vérifie si un fournisseur avec cet email existe (en excluant un ID spécifique)
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Vérifie si un fournisseur avec ce numéro NINEA existe déjà
     */
    boolean existsByNinea(String numeroNinea);

    /**
     * Vérifie si un fournisseur avec ce numéro NINEA existe (en excluant un ID spécifique)
     */
    boolean existsByNineaAndIdNot(String numeroNinea, Long id);

    /**
     * Trouve tous les fournisseurs actifs
     */
    List<ExpenseSupplier> findByActiveTrueOrderByNomAsc();

    /**
     * Trouve tous les fournisseurs par statut avec pagination
     */
    Page<ExpenseSupplier> findByActive(Boolean active, Pageable pageable);

    /**
     * Recherche avancée avec filtres
     */
    /**
     * Recherche avancée avec filtres simplifiés
     */
    @Query("""
    SELECT es FROM ExpenseSupplier es
    WHERE (:search IS NULL OR 
           LOWER(es.nom) LIKE :search OR 
           LOWER(es.email) LIKE :search OR 
           LOWER(es.telephone) LIKE :search)
    AND (:active IS NULL OR es.active = :active)
    """)
    Page<ExpenseSupplier> findWithFilters(
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable
    );




    /**
     * Compte le nombre de fournisseurs par statut
     */
    long countByActive(Boolean active);

    /**
     * Trouve les fournisseurs par nom (recherche partielle, insensible à la casse)
     */
    @Query("SELECT es FROM ExpenseSupplier es WHERE LOWER(es.nom) LIKE LOWER(CONCAT('%', :nom, '%'))")
    List<ExpenseSupplier> findByNomContainingIgnoreCase(@Param("nom") String nom);

    /**
     * Trouve les fournisseurs les plus récents
     */
    @Query("SELECT es FROM ExpenseSupplier es ORDER BY es.createdAt DESC")
    Page<ExpenseSupplier> findMostRecent(Pageable pageable);

    /**
     * Vérifie si un fournisseur avec ce nom existe déjà (insensible à la casse)
     */
    @Query("SELECT COUNT(es) > 0 FROM ExpenseSupplier es WHERE LOWER(es.nom) = LOWER(:nom)")
    boolean existsByNomIgnoreCase(@Param("nom") String nom);

    /**
     * Vérifie si un fournisseur avec ce nom existe déjà (insensible à la casse, en excluant un ID)
     */
    @Query("SELECT COUNT(es) > 0 FROM ExpenseSupplier es WHERE LOWER(es.nom) = LOWER(:nom) AND es.id != :id")
    boolean existsByNomIgnoreCaseAndIdNot(@Param("nom") String nom, @Param("id") Long id);

    /**
     * Trouve les fournisseurs par numéro de téléphone (recherche partielle)
     */
    @Query("SELECT es FROM ExpenseSupplier es WHERE es.telephone LIKE CONCAT('%', :telephone, '%')")
    List<ExpenseSupplier> findByTelephoneContaining(@Param("telephone") String telephone);

    /**
     * Statistiques : compte total par statut
     */
    @Query("""
        SELECT 
            CASE WHEN es.active = true THEN 'ACTIVE' ELSE 'INACTIVE' END as status,
            COUNT(es) as count
        FROM ExpenseSupplier es
        GROUP BY es.active
        """)
    List<Object[]> getSupplierStats();
}
