package sn.svs.backoffice.service;

import sn.svs.backoffice.dto.ExpenseCategoryDTO;

import java.util.List;

/**
 * Service pour la gestion des catégories de dépenses
 * SVS - Dakar, Sénégal
 */
public interface ExpenseCategoryService {

    /**
     * Crée une nouvelle catégorie de dépense
     */
    ExpenseCategoryDTO.Response create(ExpenseCategoryDTO.CreateRequest request);

    /**
     * Met à jour une catégorie existante
     */
    ExpenseCategoryDTO.Response update(Long id, ExpenseCategoryDTO.UpdateRequest request);

    /**
     * Trouve une catégorie par son ID
     */
    ExpenseCategoryDTO.Response findById(Long id);

    /**
     * Trouve une catégorie par son code
     */
    ExpenseCategoryDTO.Response findByCode(String code);

    /**
     * Trouve toutes les catégories avec pagination et filtres
     */
    ExpenseCategoryDTO.PageResponse findAll(ExpenseCategoryDTO.SearchFilter filter);

    /**
     * Trouve toutes les catégories actives (pour les listes déroulantes)
     */
    List<ExpenseCategoryDTO.Summary> findAllActive();

    /**
     * Active/désactive une catégorie
     */
    ExpenseCategoryDTO.Response toggleActive(Long id);

    /**
     * Supprime une catégorie (suppression logique)
     */
    void delete(Long id);

    /**
     * Supprime définitivement une catégorie
     */
    void hardDelete(Long id);

    /**
     * Vérifie si une catégorie existe par son code
     */
    boolean existsByCode(String code);

    /**
     * Vérifie si une catégorie existe par son code (en excluant un ID)
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * Vérifie si une catégorie existe par son nom (insensible à la casse)
     */
    boolean existsByNom(String nom);

    /**
     * Vérifie si une catégorie existe par son nom (en excluant un ID)
     */
    boolean existsByNomAndIdNot(String nom, Long id);

    /**
     * Obtient les statistiques des catégories
     */
    CategoryStatsDTO getStats();

    /**
     * Classe interne pour les statistiques
     */
    record CategoryStatsDTO(
            long totalCategories,
            long activeCategories,
            long inactiveCategories,
            double activePercentage
    ) {}
}
