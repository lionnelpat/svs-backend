package sn.svs.backoffice.service;

import org.springframework.data.domain.Pageable;
import sn.svs.backoffice.domain.ennumeration.ShipClassification;
import sn.svs.backoffice.domain.ennumeration.ShipFlag;
import sn.svs.backoffice.domain.ennumeration.ShipType;
import sn.svs.backoffice.dto.ShipDTO;

import java.util.List;
import java.util.Optional;

/**
 * Interface du service Ship
 * SVS - Dakar, Sénégal
 */
public interface ShipService {

    /**
     * Créer un nouveau navire
     *
     * @param createRequest Les données de création
     * @return Le navire créé
     */
    ShipDTO.Response createShip(ShipDTO.CreateRequest createRequest);

    /**
     * Mettre à jour un navire existant
     *
     * @param id L'identifiant du navire
     * @param updateRequest Les données de mise à jour
     * @return Le navire mis à jour
     */
    ShipDTO.Response updateShip(Long id, ShipDTO.UpdateRequest updateRequest);

    /**
     * Récupérer un navire par son identifiant
     *
     * @param id L'identifiant du navire
     * @return Le navire trouvé
     */
    ShipDTO.Response getShipById(Long id);

    /**
     * Récupérer tous les navires avec pagination
     *
     * @param pageable Les paramètres de pagination
     * @return Page des navires
     */
    ShipDTO.PageResponse getAllShips(Pageable pageable);

    /**
     * Rechercher des navires avec filtres
     *
     * @param filter Les filtres de recherche
     * @return Page des navires filtrés
     */
    ShipDTO.PageResponse searchShips(ShipDTO.SearchFilter filter);

    /**
     * Supprimer un navire (suppression logique)
     *
     * @param id L'identifiant du navire
     */
    void deleteShip(Long id);

    /**
     * Activer un navire
     *
     * @param id L'identifiant du navire
     * @return Le navire activé
     */
    ShipDTO.Response activateShip(Long id);

    /**
     * Désactiver un navire
     *
     * @param id L'identifiant du navire
     * @return Le navire désactivé
     */
    ShipDTO.Response deactivateShip(Long id);

    /**
     * Récupérer tous les navires actifs
     *
     * @return Liste des navires actifs
     */
    List<ShipDTO.Response> getActiveShips();

    /**
     * Récupérer tous les navires actifs (version résumée)
     *
     * @return Liste des navires actifs en version résumée
     */
    List<ShipDTO.Summary> getActiveShipsSummary();

    /**
     * Récupérer les navires d'une compagnie
     *
     * @param compagnieId L'identifiant de la compagnie
     * @param activeOnly Filtrer uniquement les navires actifs
     * @return Liste des navires de la compagnie
     */
    List<ShipDTO.Response> getShipsByCompany(Long compagnieId, Boolean activeOnly);

    /**
     * Récupérer les navires d'une compagnie avec pagination
     *
     * @param compagnieId L'identifiant de la compagnie
     * @param activeOnly Filtrer uniquement les navires actifs
     * @param pageable Paramètres de pagination
     * @return Page des navires de la compagnie
     */
    ShipDTO.PageResponse getShipsByCompanyPaginated(Long compagnieId, Boolean activeOnly, Pageable pageable);

    /**
     * Vérifier si un navire existe
     *
     * @param id L'identifiant du navire
     * @return true si le navire existe
     */
    boolean existsById(Long id);

    /**
     * Rechercher un navire par numéro IMO
     *
     * @param numeroIMO Le numéro IMO
     * @return Le navire trouvé
     */
    Optional<ShipDTO.Response> findByNumeroIMO(String numeroIMO);

    /**
     * Rechercher un navire par numéro MMSI
     *
     * @param numeroMMSI Le numéro MMSI
     * @return Le navire trouvé
     */
    Optional<ShipDTO.Response> findByNumeroMMSI(String numeroMMSI);

    /**
     * Rechercher un navire par numéro d'appel
     *
     * @param numeroAppel Le numéro d'appel
     * @return Le navire trouvé
     */
    Optional<ShipDTO.Response> findByNumeroAppel(String numeroAppel);

    /**
     * Récupérer les navires par type
     *
     * @param typeNavire Le type de navire
     * @param activeOnly Filtrer uniquement les navires actifs
     * @return Liste des navires du type spécifié
     */
    List<ShipDTO.Response> getShipsByType(ShipType typeNavire, Boolean activeOnly);

    /**
     * Récupérer les navires par pavillon
     *
     * @param pavillon Le pavillon
     * @param activeOnly Filtrer uniquement les navires actifs
     * @return Liste des navires sous le pavillon spécifié
     */
    List<ShipDTO.Response> getShipsByFlag(ShipFlag pavillon, Boolean activeOnly);

    /**
     * Récupérer les navires par classification
     *
     * @param classification La classification
     * @param activeOnly Filtrer uniquement les navires actifs
     * @return Liste des navires avec la classification spécifiée
     */
    List<ShipDTO.Response> getShipsByClassification(ShipClassification classification, Boolean activeOnly);

    /**
     * Récupérer les navires pouvant transporter des passagers
     *
     * @return Liste des navires passagers
     */
    List<ShipDTO.Response> getPassengerShips();

    /**
     * Obtenir les statistiques des navires par type
     *
     * @return Map avec type et nombre de navires
     */
    List<Object[]> getShipStatisticsByType();

    /**
     * Obtenir les statistiques des navires par pavillon
     *
     * @return Map avec pavillon et nombre de navires
     */
    List<Object[]> getShipStatisticsByFlag();

    /**
     * Obtenir les statistiques des navires par compagnie
     *
     * @return Map avec compagnie et nombre de navires
     */
    List<Object[]> getShipStatisticsByCompany();

    /**
     * Vérifier si une compagnie a des navires
     *
     * @param compagnieId L'identifiant de la compagnie
     * @return true si la compagnie a des navires actifs
     */
    boolean hasShips(Long compagnieId);

    /**
     * Compter le nombre de navires d'une compagnie
     *
     * @param compagnieId L'identifiant de la compagnie
     * @return Nombre de navires actifs de la compagnie
     */
    Long countShipsByCompany(Long compagnieId);

    /**
     * Valider l'unicité des champs uniques
     *
     * @param numeroIMO Le numéro IMO à valider
     * @param numeroMMSI Le numéro MMSI à valider
     * @param numeroAppel Le numéro d'appel à valider
     * @param excludeId L'ID à exclure de la validation (pour les mises à jour)
     */
    void validateUniqueFields(String numeroIMO, String numeroMMSI, String numeroAppel, Long excludeId);

    /**
     * Valider l'existence de la compagnie
     *
     * @param compagnieId L'identifiant de la compagnie
     */
    void validateCompanyExists(Long compagnieId);
}
