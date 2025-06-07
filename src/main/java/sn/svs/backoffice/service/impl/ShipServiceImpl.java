package sn.svs.backoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sn.svs.backoffice.domain.Company;
import sn.svs.backoffice.domain.Ship;
import sn.svs.backoffice.domain.ennumeration.ShipClassification;
import sn.svs.backoffice.domain.ennumeration.ShipFlag;
import sn.svs.backoffice.domain.ennumeration.ShipType;
import sn.svs.backoffice.dto.ShipDTO;
import sn.svs.backoffice.exceptions.BusinessException;
import sn.svs.backoffice.exceptions.DuplicateResourceException;
import sn.svs.backoffice.exceptions.ResourceNotFoundException;
import sn.svs.backoffice.mapper.ShipMapper;
import sn.svs.backoffice.repository.CompanyRepository;
import sn.svs.backoffice.repository.ShipRepository;
import sn.svs.backoffice.service.ShipService;

import java.util.List;
import java.util.Optional;

/**
 * Implémentation du service Ship
 * SVS - Dakar, Sénégal
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipServiceImpl implements ShipService {

    private final ShipRepository shipRepository;
    private final CompanyRepository companyRepository;
    private final ShipMapper shipMapper;

    @Override
    @Transactional
    public ShipDTO.Response createShip(ShipDTO.CreateRequest createRequest) {
        // Validation de l'existence de la compagnie
        if (!companyRepository.existsById(createRequest.getCompagnieId())) {
            throw new ResourceNotFoundException("Compagnie", createRequest.getCompagnieId());
        }

        // Validation des champs uniques
        validateUniqueFields(createRequest.getNumeroIMO(), createRequest.getNumeroMMSI(),
                createRequest.getNumeroAppel(), null);

        // Mapping direct - le mapper gère maintenant correctement la compagnie
        Ship ship = shipMapper.toEntity(createRequest);
        Ship savedShip = shipRepository.save(ship);

        log.info("Navire créé avec succès - ID: {}, Nom: {}, Compagnie ID: {}",
                savedShip.getId(), savedShip.getNom(), savedShip.getCompagnie().getId());

        return shipMapper.toResponse(savedShip);
    }

    @Override
    @Transactional
    public ShipDTO.Response updateShip(Long id, ShipDTO.UpdateRequest updateRequest) {
        log.info("Mise à jour du navire ID: {}", id);

        Ship existingShip = findShipById(id);

        // Validation de la nouvelle compagnie si fournie
        if (updateRequest.getCompagnieId() != null &&
                !companyRepository.existsById(updateRequest.getCompagnieId())) {
            throw new ResourceNotFoundException("Compagnie", updateRequest.getCompagnieId());
        }

        // Validation des champs uniques
        if (StringUtils.hasText(updateRequest.getNumeroMMSI())){
//            validateMMSIUnique(updateRequest.getNumeroMMSI(), id);
            validateUniqueFields(null, updateRequest.getNumeroMMSI(),
                    updateRequest.getNumeroAppel(), id);
        }
        if (StringUtils.hasText(updateRequest.getNumeroAppel())) {
//            validateCallSignUnique(updateRequest.getNumeroAppel(), id);
            validateUniqueFields(null, updateRequest.getNumeroMMSI(),
                    updateRequest.getNumeroAppel(), id);
        }

        // Mise à jour
        shipMapper.updateEntityFromDto(updateRequest, existingShip);
        Ship updatedShip = shipRepository.save(existingShip);

        return shipMapper.toResponse(updatedShip);
    }

    @Override
    public ShipDTO.Response getShipById(Long id) {
        log.debug("Recherche du navire ID: {}", id);

        Ship ship = findShipById(id);
        return shipMapper.toResponse(ship);
    }

    @Override
    public ShipDTO.PageResponse getAllShips(Pageable pageable) {
        log.debug("Récupération de tous les navires - Page: {}, Taille: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Ship> ships = shipRepository.findAll(pageable);

        log.debug("Trouvé {} navires sur {} au total",
                ships.getNumberOfElements(), ships.getTotalElements());

        return shipMapper.toPageResponse(ships);
    }

    @Override
    public ShipDTO.PageResponse searchShips(ShipDTO.SearchFilter filter) {
        log.debug("Recherche de navires avec filtres: {}", filter);

        // Création du Pageable avec tri
        Sort sort = createSort(filter.getSortBy(), filter.getSortDirection());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        Page<Ship> ships;

        // Recherche avec ou sans compagnieId
        if (filter.getCompagnieId() != null) {
            ships = shipRepository.findWithFiltersAndCompany(
                    filter.getSearch(),
                    filter.getCompagnieId(),
                    filter.getTypeNavire(),
                    filter.getPavillon(),
                    filter.getActive(),
                    pageable
            );
        } else {
            ships = shipRepository.findWithFilters(
                    filter.getSearch(),
                    filter.getTypeNavire(),
                    filter.getPavillon(),
                    filter.getActive(),
                    pageable
            );
        }

        log.debug("Recherche terminée - {} résultats trouvés", ships.getTotalElements());

        return shipMapper.toPageResponse(ships);
    }

    @Override
    @Transactional
    public void deleteShip(Long id) {
        log.info("Suppression logique du navire ID: {}", id);

        Ship ship = findShipById(id);

        // Vérification des contraintes métier
        // TODO: Vérifier s'il y a des factures liées à ce navire

        ship.softDelete();
        shipRepository.save(ship);

        log.info("Navire supprimé logiquement - ID: {}, Nom: {}",
                ship.getId(), ship.getNom());
    }

    @Override
    @Transactional
    public ShipDTO.Response activateShip(Long id) {
        log.info("Activation du navire ID: {}", id);

        Ship ship = findShipById(id);
        ship.activate();
        Ship activatedShip = shipRepository.save(ship);

        log.info("Navire activé - ID: {}, Nom: {}",
                activatedShip.getId(), activatedShip.getNom());

        return shipMapper.toResponse(activatedShip);
    }

    @Override
    @Transactional
    public ShipDTO.Response deactivateShip(Long id) {
        log.info("Désactivation du navire ID: {}", id);

        Ship ship = findShipById(id);
        ship.deactivate();
        Ship deactivatedShip = shipRepository.save(ship);

        log.info("Navire désactivé - ID: {}, Nom: {}",
                deactivatedShip.getId(), deactivatedShip.getNom());

        return shipMapper.toResponse(deactivatedShip);
    }

    @Override
    public List<ShipDTO.Response> getActiveShips() {
        log.debug("Récupération de tous les navires actifs");

        List<Ship> activeShips = shipRepository.findAllActiveShips();

        log.debug("Trouvé {} navires actifs", activeShips.size());

        return shipMapper.toResponseList(activeShips);
    }

    @Override
    public List<ShipDTO.Summary> getActiveShipsSummary() {
        log.debug("Récupération de tous les navires actifs (résumé)");

        List<Ship> activeShips = shipRepository.findAllActiveShips();

        log.debug("Trouvé {} navires actifs", activeShips.size());

        return shipMapper.toSummaryList(activeShips);
    }

    @Override
    public List<ShipDTO.Response> getShipsByCompany(Long compagnieId, Boolean activeOnly) {
        log.debug("Récupération des navires de la compagnie ID: {}, actifs seulement: {}",
                compagnieId, activeOnly);

        validateCompanyExists(compagnieId);

        Boolean active = activeOnly != null ? activeOnly : true;
        List<Ship> ships = shipRepository.findByCompagnie_IdAndActiveOrderByNomAsc(compagnieId, active);

        log.debug("Trouvé {} navires pour la compagnie ID: {}", ships.size(), compagnieId);

        return shipMapper.toResponseList(ships);
    }

    @Override
    public ShipDTO.PageResponse getShipsByCompanyPaginated(Long compagnieId, Boolean activeOnly, Pageable pageable) {
        log.debug("Récupération paginée des navires de la compagnie ID: {}", compagnieId);

        validateCompanyExists(compagnieId);

        Boolean active = activeOnly != null ? activeOnly : true;
        Page<Ship> ships = shipRepository.findByCompagnie_IdAndActive(compagnieId, active, pageable);

        log.debug("Trouvé {} navires pour la compagnie ID: {}", ships.getTotalElements(), compagnieId);

        return shipMapper.toPageResponse(ships);
    }

    @Override
    public boolean existsById(Long id) {
        log.debug("Vérification de l'existence du navire ID: {}", id);

        boolean exists = shipRepository.existsById(id);
        log.debug("Navire ID: {} existe: {}", id, exists);

        return exists;
    }

    @Override
    public Optional<ShipDTO.Response> findByNumeroIMO(String numeroIMO) {
        log.debug("Recherche de navire par IMO: {}", numeroIMO);

        Optional<Ship> ship = shipRepository.findByNumeroIMOIgnoreCase(numeroIMO);
        return ship.map(shipMapper::toResponse);
    }

    @Override
    public Optional<ShipDTO.Response> findByNumeroMMSI(String numeroMMSI) {
        log.debug("Recherche de navire par MMSI: {}", numeroMMSI);

        Optional<Ship> ship = shipRepository.findByNumeroMMSIIgnoreCase(numeroMMSI);
        return ship.map(shipMapper::toResponse);
    }

    @Override
    public Optional<ShipDTO.Response> findByNumeroAppel(String numeroAppel) {
        log.debug("Recherche de navire par numéro d'appel: {}", numeroAppel);

        Optional<Ship> ship = shipRepository.findByNumeroAppelIgnoreCase(numeroAppel);
        return ship.map(shipMapper::toResponse);
    }

    @Override
    public List<ShipDTO.Response> getShipsByType(ShipType typeNavire, Boolean activeOnly) {
        log.debug("Récupération des navires de type: {}", typeNavire);

        Boolean active = activeOnly != null ? activeOnly : true;
        Page<Ship> ships = shipRepository.findByTypeNavireAndActive(typeNavire, active, Pageable.unpaged());

        return shipMapper.toResponseList(ships.getContent());
    }

    @Override
    public List<ShipDTO.Response> getShipsByFlag(ShipFlag pavillon, Boolean activeOnly) {
        log.debug("Récupération des navires sous pavillon: {}", pavillon);

        Boolean active = activeOnly != null ? activeOnly : true;
        Page<Ship> ships = shipRepository.findByPavillonAndActive(pavillon, active, Pageable.unpaged());

        return shipMapper.toResponseList(ships.getContent());
    }

    @Override
    public List<ShipDTO.Response> getShipsByClassification(ShipClassification classification, Boolean activeOnly) {
        log.debug("Récupération des navires avec classification: {}", classification);

        Boolean active = activeOnly != null ? activeOnly : true;
        List<Ship> ships = shipRepository.findByClassificationAndActiveOrderByNomAsc(classification, active);

        return shipMapper.toResponseList(ships);
    }

    @Override
    public List<ShipDTO.Response> getPassengerShips() {
        log.debug("Récupération des navires passagers");

        List<Ship> ships = shipRepository.findPassengerShips();

        log.debug("Trouvé {} navires passagers", ships.size());

        return shipMapper.toResponseList(ships);
    }

    @Override
    public List<Object[]> getShipStatisticsByType() {
        log.debug("Récupération des statistiques par type de navire");

        return shipRepository.getShipStatisticsByType();
    }

    @Override
    public List<Object[]> getShipStatisticsByFlag() {
        log.debug("Récupération des statistiques par pavillon");

        return shipRepository.getShipStatisticsByFlag();
    }

    @Override
    public List<Object[]> getShipStatisticsByCompany() {
        log.debug("Récupération des statistiques par compagnie");

        return shipRepository.getShipStatisticsByCompany();
    }

    @Override
    public boolean hasShips(Long compagnieId) {
        log.debug("Vérification si la compagnie ID: {} a des navires", compagnieId);

        boolean hasShips = shipRepository.existsByCompagnie_IdAndActive(compagnieId, true);
        log.debug("Compagnie ID: {} a des navires: {}", compagnieId, hasShips);

        return hasShips;
    }

    @Override
    public Long countShipsByCompany(Long compagnieId) {
        log.debug("Comptage des navires de la compagnie ID: {}", compagnieId);

        return shipRepository.countByCompagnie_IdAndActive(compagnieId);
    }

    @Override
    public void validateUniqueFields(String numeroIMO, String numeroMMSI, String numeroAppel, Long excludeId) {
        log.debug("Validation des champs uniques - IMO: {}, MMSI: {}, Appel: {}, ExcludeId: {}",
                numeroIMO, numeroMMSI, numeroAppel, excludeId);

        // Validation du numéro IMO
        if (StringUtils.hasText(numeroIMO)) {
            boolean imoExists = excludeId != null ?
                    shipRepository.existsByNumeroIMOIgnoreCaseAndIdNot(numeroIMO, excludeId) :
                    shipRepository.findByNumeroIMOIgnoreCase(numeroIMO).isPresent();

            if (imoExists) {
                log.warn("Tentative de création/modification avec un IMO déjà existant: {}", numeroIMO);
                throw new DuplicateResourceException("Navire", "numéro IMO", numeroIMO);
            }
        }

        // Validation du numéro MMSI
        if (StringUtils.hasText(numeroMMSI)) {
            boolean mmsiExists = excludeId != null ?
                    shipRepository.existsByNumeroMMSIIgnoreCaseAndIdNot(numeroMMSI, excludeId) :
                    shipRepository.findByNumeroMMSIIgnoreCase(numeroMMSI).isPresent();

            if (mmsiExists) {
                log.warn("Tentative de création/modification avec un MMSI déjà existant: {}", numeroMMSI);
                throw new DuplicateResourceException("Navire", "numéro MMSI", numeroMMSI);
            }
        }

        // Validation du numéro d'appel
        if (StringUtils.hasText(numeroAppel)) {
            boolean appelExists = excludeId != null ?
                    shipRepository.existsByNumeroAppelIgnoreCaseAndIdNot(numeroAppel, excludeId) :
                    shipRepository.findByNumeroAppelIgnoreCase(numeroAppel).isPresent();

            if (appelExists) {
                log.warn("Tentative de création/modification avec un numéro d'appel déjà existant: {}", numeroAppel);
                throw new DuplicateResourceException("Navire", "numéro d'appel", numeroAppel);
            }
        }

        log.debug("Validation des champs uniques réussie");
    }

    @Override
    public void validateCompanyExists(Long compagnieId) {
        log.debug("Validation de l'existence de la compagnie ID: {}", compagnieId);

        if (!companyRepository.existsById(compagnieId)) {
            log.warn("Tentative d'association avec une compagnie inexistante: {}", compagnieId);
            throw new ResourceNotFoundException("Compagnie", compagnieId);
        }

        log.debug("Compagnie ID: {} existe", compagnieId);
    }

    /**
     * Méthode utilitaire pour trouver un navire par ID
     */
    private Ship findShipById(Long id) {
        return shipRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Navire non trouvé avec l'ID: {}", id);
                    return new ResourceNotFoundException("Navire", id);
                });
    }

    /**
     * Créer un objet Sort basé sur les paramètres de tri
     */
    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        // Validation du champ de tri
        String validSortBy = validateSortField(sortBy);

        return Sort.by(direction, validSortBy);
    }

    /**
     * Valider le champ de tri
     */
    private String validateSortField(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "id";
        }

        // Liste des champs autorisés pour le tri
        List<String> allowedSortFields = List.of(
                "id", "nom", "numeroIMO", "numeroMMSI", "typeNavire", "pavillon",
                "portAttache", "numeroAppel", "classification", "createdAt", "updatedAt", "active"
        );

        if (allowedSortFields.contains(sortBy)) {
            return sortBy;
        }

        log.warn("Champ de tri non autorisé: {}, utilisation de 'id' par défaut", sortBy);
        return "id";
    }

//    private void validateTheUniqueFields(String imo, String mmsi, String callSign, Long excludeId) {
//        if (imo != null && shipRepository.existsByNumeroIMOAndIdNot(imo, excludeId)) {
//            throw new BusinessException("Un navire avec ce numéro IMO existe déjà");
//        }
//        if (mmsi != null && shipRepository.existsByNumeroMMSIAndIdNot(mmsi, excludeId)) {
//            throw new BusinessException("Un navire avec ce numéro MMSI existe déjà");
//        }
//        if (callSign != null && shipRepository.existsByNumeroAppelAndIdNot(callSign, excludeId)) {
//            throw new BusinessException("Un navire avec ce numéro d'appel existe déjà");
//        }
//    }
}
