package sn.svs.backoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.svs.backoffice.domain.Operation;
import sn.svs.backoffice.dto.OperationDTO;
import sn.svs.backoffice.exceptions.DuplicateResourceException;
import sn.svs.backoffice.exceptions.ResourceNotFoundException;
import sn.svs.backoffice.mapper.OperationMapper;
import sn.svs.backoffice.repository.OperationRepository;
import sn.svs.backoffice.service.OperationService;

import java.util.List;

/**
 * Implémentation du service pour la gestion des opérations maritimes
 * SVS - Dakar, Sénégal
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OperationServiceImpl implements OperationService {

    private final OperationRepository operationRepository;
    private final OperationMapper operationMapper;

    @Override
    public OperationDTO.Response create(OperationDTO.CreateRequest request) {
        log.info("Création d'une nouvelle opération avec le code: {}", request.getCode());

        // Vérifier l'unicité du code
        if (operationRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Une opération avec le code '" + request.getCode() + "' existe déjà");
        }

        // Convertir et sauvegarder
        Operation operation = operationMapper.toEntity(request);
        Operation savedOperation = operationRepository.save(operation);

        log.info("Opération créée avec succès - ID: {}, Code: {}", savedOperation.getId(), savedOperation.getCode());
        return operationMapper.toResponse(savedOperation);
    }

    @Override
    public OperationDTO.Response update(Long id, OperationDTO.UpdateRequest request) {
        log.info("Mise à jour de l'opération ID: {}", id);

        Operation existingOperation = findOperationById(id);

        // Vérifier l'unicité du code si modifié
        if (request.getCode() != null && !request.getCode().equals(existingOperation.getCode())) {
            if (operationRepository.existsByCodeAndIdNot(request.getCode(), id)) {
                throw new DuplicateResourceException("Une opération avec le code '" + request.getCode() + "' existe déjà");
            }
        }

        // Mettre à jour les champs
        operationMapper.updateEntity(request, existingOperation);
        Operation updatedOperation = operationRepository.save(existingOperation);

        log.info("Opération mise à jour avec succès - ID: {}", updatedOperation.getId());
        return operationMapper.toResponse(updatedOperation);
    }

    @Override
    @Transactional(readOnly = true)
    public OperationDTO.Response findById(Long id) {
        log.debug("Recherche de l'opération par ID: {}", id);
        Operation operation = findOperationById(id);
        return operationMapper.toResponse(operation);
    }

    @Override
    @Transactional(readOnly = true)
    public OperationDTO.Response findByCode(String code) {
        log.debug("Recherche de l'opération par code: {}", code);
        Operation operation = operationRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Operation", "code", code));
        return operationMapper.toResponse(operation);
    }

    @Override
    @Transactional(readOnly = true)
    public OperationDTO.PageResponse findAll(OperationDTO.SearchFilter filter) {
        log.debug("Recherche des opérations avec filtre: {}", filter);

        // Construire le Pageable
        Pageable pageable = buildPageable(filter);

        // Rechercher avec filtres
        String searchCriteria = operationMapper.buildSearchCriteria(filter);
        Page<Operation> operationsPage = operationRepository.findWithFilters(
                searchCriteria,
                filter.getActive(),
                pageable
        );

        log.debug("Trouvé {} opérations sur {} au total",
                operationsPage.getNumberOfElements(), operationsPage.getTotalElements());

        return operationMapper.toPageResponse(operationsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationDTO.Summary> findAllActive() {
        log.debug("Recherche de toutes les opérations actives");
        List<Operation> activeOperations = operationRepository.findByActiveTrue();
        return operationMapper.toSummaryList(activeOperations);
    }

    @Override
    public OperationDTO.Response toggleActive(Long id) {
        log.info("Basculement du statut actif pour l'opération ID: {}", id);

        Operation operation = findOperationById(id);
        operation.setActive(!operation.getActive());
        Operation updatedOperation = operationRepository.save(operation);

        log.info("Statut de l'opération {} changé à: {}", id, updatedOperation.getActive());
        return operationMapper.toResponse(updatedOperation);
    }

    @Override
    public void delete(Long id) {
        log.info("Suppression logique de l'opération ID: {}", id);

        Operation operation = findOperationById(id);
        operation.setActive(false);
        operationRepository.save(operation);

        log.info("Opération {} désactivée avec succès", id);
    }

    @Override
    public void hardDelete(Long id) {
        log.info("Suppression définitive de l'opération ID: {}", id);

        if (!operationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Operation", "id", id);
        }

        operationRepository.deleteById(id);
        log.info("Opération {} supprimée définitivement", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return operationRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return operationRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    @Transactional(readOnly = true)
    public OperationStatsDTO getStats() {
        log.debug("Calcul des statistiques des opérations");

        long totalOperations = operationRepository.count();
        long activeOperations = operationRepository.countByActive(true);
        long inactiveOperations = operationRepository.countByActive(false);
        double activePercentage = totalOperations > 0 ? (double) activeOperations / totalOperations * 100 : 0;

        return new OperationStatsDTO(totalOperations, activeOperations, inactiveOperations, activePercentage);
    }

    /**
     * Méthode utilitaire pour trouver une opération par ID avec gestion d'exception
     */
    private Operation findOperationById(Long id) {
        return operationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operation", "id", id));
    }

    /**
     * Méthode utilitaire pour construire un objet Pageable
     */
    private Pageable buildPageable(OperationDTO.SearchFilter filter) {
        Sort sort = Sort.by(
                filter.getSortDirection().equalsIgnoreCase("desc") ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSortBy()
        );

        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }
}
