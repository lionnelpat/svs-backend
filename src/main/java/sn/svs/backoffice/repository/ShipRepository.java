package sn.svs.backoffice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.svs.backoffice.domain.Ship;
import sn.svs.backoffice.domain.ennumeration.ShipClassification;
import sn.svs.backoffice.domain.ennumeration.ShipFlag;
import sn.svs.backoffice.domain.ennumeration.ShipType;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Ship
 * SVS - Dakar, Sénégal
 */
@Repository
public interface ShipRepository extends JpaRepository<Ship, Long>, JpaSpecificationExecutor<Ship> {

    /**
     * Recherche un navire par numéro IMO
     */
    Optional<Ship> findByNumeroIMOIgnoreCase(String numeroIMO);

    /**
     * Recherche un navire par numéro MMSI
     */
    Optional<Ship> findByNumeroMMSIIgnoreCase(String numeroMMSI);

    /**
     * Recherche un navire par numéro d'appel
     */
    Optional<Ship> findByNumeroAppelIgnoreCase(String numeroAppel);

    /**
     * Vérifie si un numéro IMO existe déjà (pour la validation)
     */
    boolean existsByNumeroIMOIgnoreCaseAndIdNot(String numeroIMO, Long id);

    /**
     * Vérifie si un numéro MMSI existe déjà (pour la validation)
     */
    boolean existsByNumeroMMSIIgnoreCaseAndIdNot(String numeroMMSI, Long id);

    /**
     * Vérifie si un numéro d'appel existe déjà (pour la validation)
     */
    boolean existsByNumeroAppelIgnoreCaseAndIdNot(String numeroAppel, Long id);

    /**
     * Recherche des navires par compagnie
     */
    List<Ship> findByCompagnie_IdAndActiveOrderByNomAsc(Long compagnieId, Boolean active);

    /**
     * Recherche des navires par type
     */
    Page<Ship> findByTypeNavireAndActive(ShipType typeNavire, Boolean active, Pageable pageable);

    /**
     * Recherche des navires par pavillon
     */
    Page<Ship> findByPavillonAndActive(ShipFlag pavillon, Boolean active, Pageable pageable);

    /**
     * Recherche textuelle avec filtres avancés (SANS compagnieId)
     */
    @Query("SELECT s FROM Ship s LEFT JOIN FETCH s.compagnie c WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(s.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.numeroIMO) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.numeroMMSI) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.portAttache) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.numeroAppel) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:typeNavire IS NULL OR s.typeNavire = :typeNavire) " +
            "AND (:pavillon IS NULL OR s.pavillon = :pavillon) " +
            "AND (:active IS NULL OR s.active = :active)")
    Page<Ship> findWithFilters(@Param("search") String search,
                               @Param("typeNavire") ShipType typeNavire,
                               @Param("pavillon") ShipFlag pavillon,
                               @Param("active") Boolean active,
                               Pageable pageable);

    /**
     * Recherche textuelle avec filtres avancés (AVEC compagnieId)
     */
    @Query("SELECT s FROM Ship s LEFT JOIN FETCH s.compagnie c WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(s.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.numeroIMO) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.numeroMMSI) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.portAttache) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.numeroAppel) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (s.compagnie.id = :compagnieId) " +
            "AND (:typeNavire IS NULL OR s.typeNavire = :typeNavire) " +
            "AND (:pavillon IS NULL OR s.pavillon = :pavillon) " +
            "AND (:active IS NULL OR s.active = :active)")
    Page<Ship> findWithFiltersAndCompany(@Param("search") String search,
                                         @Param("compagnieId") Long compagnieId,
                                         @Param("typeNavire") ShipType typeNavire,
                                         @Param("pavillon") ShipFlag pavillon,
                                         @Param("active") Boolean active,
                                         Pageable pageable);

    /**
     * Compte le nombre total de navires avec filtres (SANS compagnieId)
     */
    @Query("SELECT COUNT(s) FROM Ship s WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(s.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.numeroIMO) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.numeroMMSI) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.portAttache) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.numeroAppel) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:typeNavire IS NULL OR s.typeNavire = :typeNavire) " +
            "AND (:pavillon IS NULL OR s.pavillon = :pavillon) " +
            "AND (:active IS NULL OR s.active = :active)")
    Long countWithFilters(@Param("search") String search,
                          @Param("typeNavire") ShipType typeNavire,
                          @Param("pavillon") ShipFlag pavillon,
                          @Param("active") Boolean active);

    /**
     * Recherche tous les navires actifs triés par nom
     */
    @Query("SELECT s FROM Ship s LEFT JOIN FETCH s.compagnie WHERE s.active = true ORDER BY s.nom ASC")
    List<Ship> findAllActiveShips();

    /**
     * Recherche des navires par port d'attache
     */
    List<Ship> findByPortAttacheIgnoreCaseContainingAndActiveOrderByNomAsc(String portAttache, Boolean active);

    /**
     * Statistiques : nombre de navires par type
     */
    @Query("SELECT s.typeNavire, COUNT(s) FROM Ship s WHERE s.active = true GROUP BY s.typeNavire ORDER BY COUNT(s) DESC")
    List<Object[]> getShipStatisticsByType();

    /**
     * Statistiques : nombre de navires par pavillon
     */
    @Query("SELECT s.pavillon, COUNT(s) FROM Ship s WHERE s.active = true GROUP BY s.pavillon ORDER BY COUNT(s) DESC")
    List<Object[]> getShipStatisticsByFlag();

    /**
     * Statistiques : nombre de navires par compagnie
     */
    @Query("SELECT c.nom, COUNT(s) FROM Ship s JOIN s.compagnie c WHERE s.active = true GROUP BY c.nom ORDER BY COUNT(s) DESC")
    List<Object[]> getShipStatisticsByCompany();

    /**
     * Recherche des navires d'une compagnie avec pagination
     */
    @Query("SELECT s FROM Ship s LEFT JOIN FETCH s.compagnie WHERE s.compagnie.id = :compagnieId AND s.active = :active")
    Page<Ship> findByCompagnie_IdAndActive(@Param("compagnieId") Long compagnieId,
                                           @Param("active") Boolean active,
                                           Pageable pageable);

    /**
     * Navires pouvant transporter des passagers
     */
    @Query("SELECT s FROM Ship s LEFT JOIN FETCH s.compagnie WHERE " +
            "(s.typeNavire = 'PASSAGERS' OR s.typeNavire = 'RO_RO' OR s.nombrePassagers > 0) " +
            "AND s.active = true ORDER BY s.nom ASC")
    List<Ship> findPassengerShips();

    /**
     * Recherche de navires par classification
     */
    List<Ship> findByClassificationAndActiveOrderByNomAsc(ShipClassification classification, Boolean active);

    /**
     * Vérifier si une compagnie a des navires
     */
    boolean existsByCompagnie_IdAndActive(Long compagnieId, Boolean active);

    /**
     * Nombre de navires par compagnie
     */
    @Query("SELECT COUNT(s) FROM Ship s WHERE s.compagnie.id = :compagnieId AND s.active = true")
    Long countByCompagnie_IdAndActive(@Param("compagnieId") Long compagnieId);
}