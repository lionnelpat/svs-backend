package sn.svs.backoffice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.svs.backoffice.domain.Company;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Company
 * SVS - Dakar, Sénégal
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {

    /**
     * Recherche une compagnie par email
     */
    Optional<Company> findByEmailIgnoreCase(String email);

    /**
     * Recherche une compagnie par RCCM
     */
    Optional<Company> findByRccmIgnoreCase(String rccm);

    /**
     * Recherche une compagnie par NINEA
     */
    Optional<Company> findByNineaIgnoreCase(String ninea);

    /**
     * Vérifie si un email existe déjà (pour la validation)
     */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    /**
     * Vérifie si un RCCM existe déjà (pour la validation)
     */
    boolean existsByRccmIgnoreCaseAndIdNot(String rccm, Long id);

    /**
     * Vérifie si un NINEA existe déjà (pour la validation)
     */
    boolean existsByNineaIgnoreCaseAndIdNot(String ninea, Long id);

    /**
     * Recherche des compagnies par statut actif
     */
    List<Company> findByActiveOrderByNomAsc(Boolean active);

    /**
     * Recherche des compagnies par pays
     */
    Page<Company> findByPaysIgnoreCaseContaining(String pays, Pageable pageable);

    /**
     * Recherche des compagnies actives par pays
     */
    Page<Company> findByPaysIgnoreCaseContainingAndActive(String pays, Boolean active, Pageable pageable);

    /**
     * Recherche textuelle dans nom, raison sociale et email
     */
    @Query("SELECT c FROM Company c WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(c.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.raisonSociale) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.ville) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:pays IS NULL OR :pays = '' OR LOWER(c.pays) LIKE LOWER(CONCAT('%', :pays, '%'))) " +
            "AND (:active IS NULL OR c.active = :active)")
    Page<Company> findWithFilters(@Param("search") String search,
                                  @Param("pays") String pays,
                                  @Param("active") Boolean active,
                                  Pageable pageable);

    /**
     * Compte le nombre total de compagnies avec filtres
     */
    @Query("SELECT COUNT(c) FROM Company c WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(c.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.raisonSociale) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.ville) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:pays IS NULL OR :pays = '' OR LOWER(c.pays) LIKE LOWER(CONCAT('%', :pays, '%'))) " +
            "AND (:active IS NULL OR c.active = :active)")
    Long countWithFilters(@Param("search") String search,
                          @Param("pays") String pays,
                          @Param("active") Boolean active);

    /**
     * Recherche toutes les compagnies actives triées par nom
     */
    @Query("SELECT c FROM Company c WHERE c.active = true ORDER BY c.nom ASC")
    List<Company> findAllActiveCompanies();

    /**
     * Recherche des compagnies par ville
     */
    List<Company> findByVilleIgnoreCaseContainingAndActiveOrderByNomAsc(String ville, Boolean active);

    /**
     * Statistiques : nombre de compagnies par pays
     */
    @Query("SELECT c.pays, COUNT(c) FROM Company c WHERE c.active = true GROUP BY c.pays ORDER BY COUNT(c) DESC")
    List<Object[]> getCompanyStatisticsByCountry();

    /**
     * Statistiques : nombre de compagnies par ville
     */
    @Query("SELECT c.ville, COUNT(c) FROM Company c WHERE c.active = true AND c.pays = :pays GROUP BY c.ville ORDER BY COUNT(c) DESC")
    List<Object[]> getCompanyStatisticsByCity(@Param("pays") String pays);


    /**
     * Top compagnies par chiffre d'affaires
     */
    @Query("""
        SELECT 
            c.nom as nomCompagnie,
            COUNT(i) as nombreFactures,
            SUM(i.montantTotal) as chiffreAffaires
        FROM Company c
        LEFT JOIN Invoice i ON i.compagnieId = c.id AND i.active = true
        WHERE (:annee IS NULL OR YEAR(i.dateFacture) = :annee)
        GROUP BY c.id, c.nom
        HAVING SUM(i.montantTotal) > 0
        ORDER BY chiffreAffaires DESC
        """)
    List<Object[]> findTopCompaniesByRevenue(@Param("annee") Integer annee);
}