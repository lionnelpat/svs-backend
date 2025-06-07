package sn.svs.backoffice.service;

import sn.svs.backoffice.dto.OperationDTO;

import java.util.List;

/**
 * Service pour la gestion des opérations maritimes
 * SVS - Dakar, Sénégal
 */
public interface OperationService {

    /**
     * Crée une nouvelle opération
     */
    OperationDTO.Response create(OperationDTO.CreateRequest request);

    /**
     * Met à jour une opération existante
     */
    OperationDTO.Response update(Long id, OperationDTO.UpdateRequest request);

    /**
     * Trouve une opération par son ID
     */
    OperationDTO.Response findById(Long id);

    /**
     * Trouve une opération par son code
     */
    OperationDTO.Response findByCode(String code);

    /**
     * Trouve toutes les opérations avec pagination et filtres
     */
    OperationDTO.PageResponse findAll(OperationDTO.SearchFilter filter);

    /**
     * Trouve toutes les opérations actives (pour les listes déroulantes)
     */
    List<OperationDTO.Summary> findAllActive();

    /**
     * Active/désactive une opération
     */
    OperationDTO.Response toggleActive(Long id);

    /**
     * Supprime une opération (suppression logique)
     */
    void delete(Long id);

    /**
     * Supprime définitivement une opération
     */
    void hardDelete(Long id);

    /**
     * Vérifie si une opération existe par son code
     */
    boolean existsByCode(String code);

    /**
     * Vérifie si une opération existe par son code (en excluant un ID)
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * Obtient les statistiques des opérations
     */
    OperationStatsDTO getStats();

    /**
     * Classe interne pour les statistiques
     */
    record OperationStatsDTO(
            long totalOperations,
            long activeOperations,
            long inactiveOperations,
            double activePercentage
    ) {}
}
