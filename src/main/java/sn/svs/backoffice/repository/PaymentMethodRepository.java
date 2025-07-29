package sn.svs.backoffice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.svs.backoffice.domain.PaymentMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité PaymentMethod
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    /**
     * Vérifie si un mode de paiement existe par son nom (insensible à la casse)
     */
    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END " +
            "FROM PaymentMethod pm WHERE LOWER(pm.nom) = LOWER(:nom)")
    boolean existsByNomIgnoreCase(@Param("nom") String nom);

    /**
     * Vérifie si un mode de paiement existe par son code (insensible à la casse)
     */
    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END " +
            "FROM PaymentMethod pm WHERE LOWER(pm.code) = LOWER(:code)")
    boolean existsByCodeIgnoreCase(@Param("code") String code);

    /**
     * Vérifie si un mode de paiement existe par son nom, en excluant un ID spécifique
     */
    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END " +
            "FROM PaymentMethod pm WHERE LOWER(pm.nom) = LOWER(:nom) AND pm.id != :id")
    boolean existsByNomIgnoreCaseAndIdNot(@Param("nom") String nom, @Param("id") Long id);

    /**
     * Vérifie si un mode de paiement existe par son code, en excluant un ID spécifique
     */
    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END " +
            "FROM PaymentMethod pm WHERE LOWER(pm.code) = LOWER(:code) AND pm.id != :id")
    boolean existsByCodeIgnoreCaseAndIdNot(@Param("code") String code, @Param("id") Long id);

    /**
     * Trouve un mode de paiement par son code (insensible à la casse)
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE LOWER(pm.code) = LOWER(:code)")
    Optional<PaymentMethod> findByCodeIgnoreCase(@Param("code") String code);

    /**
     * Trouve un mode de paiement par son nom (insensible à la casse)
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE LOWER(pm.nom) = LOWER(:nom)")
    Optional<PaymentMethod> findByNomIgnoreCase(@Param("nom") String nom);

    /**
     * Recherche des modes de paiement par nom ou code (recherche textuelle)
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE " +
            "LOWER(pm.nom) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(pm.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(pm.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<PaymentMethod> findByQuery(@Param("query") String query, Pageable pageable);


    /**
     * Trouve tous les modes de paiement actifs avec pagination
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.actif = true")
    Page<PaymentMethod> findAllActive(Pageable pageable);

    /**
     * Trouve tous les modes de paiement actifs avec pagination
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.actif = true")
    List<PaymentMethod> findActivePaymentMethod();

    /**
     * Trouve tous les modes de paiement inactifs avec pagination
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.actif = false")
    Page<PaymentMethod> findAllInactive(Pageable pageable);

    /**
     * Compte le nombre de modes de paiement actifs
     */
    @Query("SELECT COUNT(pm) FROM PaymentMethod pm WHERE pm.actif = true")
    long countActive();

    /**
     * Compte le nombre de modes de paiement inactifs
     */
    @Query("SELECT COUNT(pm) FROM PaymentMethod pm WHERE pm.actif = false")
    long countInactive();

    /**
     * Met à jour le statut actif d'un mode de paiement
     */
    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.actif = :actif, pm.updatedAt = :updatedAt, pm.updatedBy = :updatedBy " +
            "WHERE pm.id = :id")
    void updateActifStatus(@Param("id") Long id, @Param("actif") Boolean actif,
                          @Param("updatedAt") LocalDateTime updatedAt, @Param("updatedBy") String updatedBy);


    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.active = :active, pm.updatedAt = :updatedAt, pm.updatedBy = :updatedBy " +
            "WHERE pm.id = :id")
    void updateActiveStatus(
            @Param("id") Long id,
            @Param("active") Boolean active,
            @Param("updatedAt") LocalDateTime updatedAt,
            @Param("updatedBy") String updatedBy
    );

    /**
     * Recherche avancée avec filtres
     */
    @Query("""
        SELECT o FROM PaymentMethod o
        WHERE (:search IS NULL OR 
               LOWER(o.nom) LIKE :search OR 
               LOWER(o.code) LIKE :search OR 
               LOWER(o.description) LIKE :search)
        """)
    Page<PaymentMethod> findWithFilters(
            @Param("search") String search,
            Pageable pageable
    );


    /**
     * Statistiques : compte total par statut
     */
    @Query("""
        SELECT 
            CASE WHEN o.active = true THEN 'ACTIVE' ELSE 'INACTIVE' END as status,
            COUNT(o) as count
        FROM PaymentMethod o
        GROUP BY o.active
        """)
    List<Object[]> getPaymentMethodStats();

    boolean existsByIdAndActifTrue(Long paymentMethodId);

//    /**
//     * Vérifie si un mode de paiement est utilisé dans des dépenses
//     */
//    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
//            "FROM Expense e WHERE e.paymentMethodId = :paymentMethodId")
//    boolean isUsedInExpenses(@Param("paymentMethodId") Long paymentMethodId);

//    /**
//     * Suppression logique personnalisée
//     */
//    @Modifying
//    @Query("UPDATE PaymentMethod pm SET pm.deletedAt = :deletedAt WHERE pm.id = :id")
//    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);
//
//    /**
//     * Restauration d'un mode de paiement supprimé logiquement
//     */
//    @Modifying
//    @Query("UPDATE PaymentMethod pm SET pm.deletedAt = null, pm.updatedAt = :updatedAt, pm.updatedBy = :updatedBy " +
//            "WHERE pm.id = :id")
//    int restore(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt, @Param("updatedBy") String updatedBy);
//
//    /**
//     * Trouve tous les modes de paiement supprimés logiquement
//     */
//    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.deletedAt IS NOT NULL")
//    Page<PaymentMethod> findAllDeleted(Pageable pageable);
//
//    /**
//     * Trouve un mode de paiement supprimé logiquement par son ID
//     */
//    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.id = :id AND pm.deletedAt IS NOT NULL")
//    Optional<PaymentMethod> findDeletedById(@Param("id") Long id);
}
