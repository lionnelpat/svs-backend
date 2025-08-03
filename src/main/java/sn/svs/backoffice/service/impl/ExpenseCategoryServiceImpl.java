package sn.svs.backoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.svs.backoffice.domain.ExpenseCategory;
import sn.svs.backoffice.dto.ExpenseCategoryDTO;
import sn.svs.backoffice.exceptions.DuplicateResourceException;
import sn.svs.backoffice.exceptions.ResourceNotFoundException;
import sn.svs.backoffice.mapper.ExpenseCategoryMapper;
import sn.svs.backoffice.repository.ExpenseCategoryRepository;
import sn.svs.backoffice.service.ExpenseCategoryService;
import sn.svs.backoffice.utils.CodeGenerator;

import java.util.List;

/**
 * Implémentation du service pour la gestion des catégories de dépenses
 * SVS - Dakar, Sénégal
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExpenseCategoryServiceImpl implements ExpenseCategoryService {

    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseCategoryMapper categoryMapper;
    private final ExpenseCategoryRepository expenseCategoryRepository;

    @Override
    public ExpenseCategoryDTO.Response create(ExpenseCategoryDTO.CreateRequest request) {
        log.info("Création d'une nouvelle catégorie avec le nom: {}", request.getNom());

        String code = CodeGenerator.generate(
                "CAT-DEP",
                expenseCategoryRepository::existsByCode,
                expenseCategoryRepository::findLastCode
        );


        // Convertir et sauvegarder
        ExpenseCategory category = categoryMapper.toEntity(request);
        category.setCode(code);
        ExpenseCategory savedCategory = categoryRepository.save(category);

        log.info("Catégorie créée avec succès - ID: {}, Code: {}", savedCategory.getId(), savedCategory.getCode());
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public ExpenseCategoryDTO.Response update(Long id, ExpenseCategoryDTO.UpdateRequest request) {
        log.info("Mise à jour de la catégorie ID: {}", id);

        ExpenseCategory existingCategory = findCategoryById(id);


        // Vérifier l'unicité du nom si modifié (insensible à la casse)
        if (request.getNom() != null && !request.getNom().equalsIgnoreCase(existingCategory.getNom())) {
            if (categoryRepository.existsByNomIgnoreCaseAndIdNot(request.getNom(), id)) {
                throw new DuplicateResourceException("Une catégorie avec le nom '" + request.getNom() + "' existe déjà");
            }
        }

        // Mettre à jour les champs
        categoryMapper.updateEntity(request, existingCategory);
        ExpenseCategory updatedCategory = categoryRepository.save(existingCategory);

        log.info("Catégorie mise à jour avec succès - ID: {}", updatedCategory.getId());
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseCategoryDTO.Response findById(Long id) {
        log.debug("Recherche de la catégorie par ID: {}", id);
        ExpenseCategory category = findCategoryById(id);
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseCategoryDTO.Response findByCode(String code) {
        log.debug("Recherche de la catégorie par code: {}", code);
        ExpenseCategory category = categoryRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseCategory", "code", code));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseCategoryDTO.PageResponse findAll(ExpenseCategoryDTO.SearchFilter filter) {
        log.debug("Recherche des catégories avec filtre: {}", filter);

        // Construire le Pageable
        Pageable pageable = buildPageable(filter);

        // Rechercher avec filtres
        String searchCriteria = categoryMapper.buildSearchCriteria(filter);
        Page<ExpenseCategory> categoriesPage = categoryRepository.findWithFilters(
                searchCriteria,
                true,
                pageable
        );

        log.debug("Trouvé {} catégories sur {} au total",
                categoriesPage.getNumberOfElements(), categoriesPage.getTotalElements());

        return categoryMapper.toPageResponse(categoriesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseCategoryDTO.Summary> findAllActive() {
        log.debug("Recherche de toutes les catégories actives");
        List<ExpenseCategory> activeCategories = categoryRepository.findByActiveTrueOrderByNomAsc();
        return categoryMapper.toSummaryList(activeCategories);
    }

    @Override
    public ExpenseCategoryDTO.Response toggleActive(Long id) {
        log.info("Basculement du statut actif pour la catégorie ID: {}", id);

        ExpenseCategory category = findCategoryById(id);
        category.setActive(!category.getActive());
        ExpenseCategory updatedCategory = categoryRepository.save(category);

        log.info("Statut de la catégorie {} changé à: {}", id, updatedCategory.getActive());
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    public void delete(Long id) {
        log.info("Suppression logique de la catégorie ID: {}", id);

        ExpenseCategory category = findCategoryById(id);
        category.setActive(false);
        categoryRepository.save(category);

        log.info("Catégorie {} désactivée avec succès", id);
    }

    @Override
    public void hardDelete(Long id) {
        log.info("Suppression définitive de la catégorie ID: {}", id);

        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("ExpenseCategory", "id", id);
        }

        categoryRepository.deleteById(id);
        log.info("Catégorie {} supprimée définitivement", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return categoryRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return categoryRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNom(String nom) {
        return categoryRepository.existsByNomIgnoreCase(nom);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNomAndIdNot(String nom, Long id) {
        return categoryRepository.existsByNomIgnoreCaseAndIdNot(nom, id);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryStatsDTO getStats() {
        log.debug("Calcul des statistiques des catégories");

        long totalCategories = categoryRepository.count();
        long activeCategories = categoryRepository.countByActive(true);
        long inactiveCategories = categoryRepository.countByActive(false);
        double activePercentage = totalCategories > 0 ? (double) activeCategories / totalCategories * 100 : 0;

        return new CategoryStatsDTO(totalCategories, activeCategories, inactiveCategories, activePercentage);
    }

    /**
     * Méthode utilitaire pour trouver une catégorie par ID avec gestion d'exception
     */
    private ExpenseCategory findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseCategory", "id", id));
    }

    /**
     * Méthode utilitaire pour construire un objet Pageable
     */
    private Pageable buildPageable(ExpenseCategoryDTO.SearchFilter filter) {
        Sort sort = Sort.by(
                filter.getSortDirection().equalsIgnoreCase("desc") ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSortBy()
        );

        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }
}
