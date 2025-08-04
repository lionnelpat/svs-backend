package sn.svs.backoffice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.svs.backoffice.domain.Operation;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Operation
 * SVS - Dakar, Sénégal
 */
@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {

    /**
     * Trouve une opération par son code
     */
    Optional<Operation> findByCode(String code);

    /**
     * Vérifie si une opération avec ce code existe déjà
     */
    boolean existsByCode(String code);

    /**
     * Vérifie si une opération avec ce code existe (en excluant un ID spécifique)
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * Trouve toutes les opérations actives
     */
    List<Operation> findByActiveTrue();

    /**
     * Trouve toutes les opérations par statut avec pagination
     */
    Page<Operation> findByActive(Boolean active, Pageable pageable);

    /**
     * Recherche avancée avec filtres
     */
    @Query("""
        SELECT o FROM Operation o
        WHERE (:search IS NULL OR 
               LOWER(o.nom) LIKE :search OR 
               LOWER(o.code) LIKE :search OR 
               LOWER(o.description) LIKE :search)
        AND (:active IS NULL OR o.active = :active)
        """)
    Page<Operation> findWithFilters(
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable
    );

    /**
     * Compte le nombre d'opérations par statut
     */
    long countByActive(Boolean active);

    /**
     * Trouve les opérations par nom (recherche partielle, insensible à la casse)
     */
    @Query("SELECT o FROM Operation o WHERE LOWER(o.nom) LIKE LOWER(CONCAT('%', :nom, '%'))")
    List<Operation> findByNomContainingIgnoreCase(@Param("nom") String nom);

    /**
     * Trouve les opérations dans une plage de prix XOF
     */
    @Query("SELECT o FROM Operation o WHERE o.prixXOF BETWEEN :minPrice AND :maxPrice ORDER BY o.prixXOF ASC")
    List<Operation> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice,
                                     @Param("maxPrice") java.math.BigDecimal maxPrice);

    /**
     * Trouve les opérations les plus récentes
     */
    @Query("SELECT o FROM Operation o ORDER BY o.createdAt DESC")
    Page<Operation> findMostRecent(Pageable pageable);

    /**
     * Statistiques : compte total par statut
     */
    @Query("""
        SELECT 
            CASE WHEN o.active = true THEN 'ACTIVE' ELSE 'INACTIVE' END as status,
            COUNT(o) as count
        FROM Operation o
        GROUP BY o.active
        """)
    List<Object[]> getOperationStats();

    @Query("SELECT o.code FROM Operation o WHERE o.code LIKE 'OPE-%' ORDER BY o.code DESC LIMIT 1")
    String findLastCode();

    /**
     * Prestations les plus facturées
     */
    @Query("""
        SELECT 
            o.nom as nomOperation,
            o.code as codeOperation,
            COUNT(ili) as nombreFois,
            SUM(ili.montantXOF) as montantTotal,
            AVG(ili.montantXOF) as montantMoyen
        FROM Operation o
        LEFT JOIN InvoiceLineItem ili ON ili.operationId = o.id
        LEFT JOIN ili.invoice i ON i.active = true
        WHERE o.active = true 
            AND (:annee IS NULL OR YEAR(i.dateFacture) = :annee)
        GROUP BY o.id, o.nom, o.code
        HAVING COUNT(ili) > 0
        ORDER BY montantTotal DESC
        """)
    List<Object[]> findTopOperationsByRevenue(@Param("annee") Integer annee);

    /**
     * Statistiques par opération
     */
    @Query("""
        SELECT 
            o.nom as nomOperation,
            COUNT(ili) as utilisations,
            SUM(ili.quantite) as quantiteTotale,
            SUM(ili.montantXOF) as montantTotal
        FROM Operation o
        LEFT JOIN InvoiceLineItem ili ON ili.operationId = o.id
        LEFT JOIN ili.invoice i ON i.active = true
        WHERE o.active = true
        GROUP BY o.id, o.nom
        ORDER BY montantTotal DESC
        """)
    List<Object[]> findOperationStatistics();

}
