package sn.svs.backoffice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.svs.backoffice.domain.Expense;
import sn.svs.backoffice.domain.ennumeration.ExpenseStatus;
import sn.svs.backoffice.dto.ExpenseDTO;
import sn.svs.backoffice.exceptions.BusinessException;
import sn.svs.backoffice.mapper.ExpenseMapper;
import sn.svs.backoffice.repository.ExpenseCategoryRepository;
import sn.svs.backoffice.repository.ExpenseRepository;
import sn.svs.backoffice.repository.ExpenseSupplierRepository;
import sn.svs.backoffice.repository.PaymentMethodRepository;
import sn.svs.backoffice.service.ExpenseExportService;
import sn.svs.backoffice.service.ExpenseService;
import sn.svs.backoffice.specification.ExpenseSpecification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implémentation du service pour la gestion des dépenses
 * SVS - Dakar, Sénégal
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseSupplierRepository supplierRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ExpenseMapper expenseMapper;
    private final ExpenseExportService exportService;

    // ========== CRUD de base ==========

    @Override
    public ExpenseDTO.Response create(ExpenseDTO.CreateRequest createRequest) {
        log.info("Création d'une nouvelle dépense: {}", createRequest.getTitre());

        // Validation des relations
        validateRelations(createRequest.getCategorieId(), createRequest.getFournisseurId(), createRequest.getPaymentMethodId());

        // Conversion en entité
        Expense expense = expenseMapper.toEntity(createRequest);

        // Génération du numéro si absent
        if (expense.getNumero() == null || expense.getNumero().trim().isEmpty()) {
            expense.setNumero(generateNumero());
        }

        // Validation unicité du numéro
        if (!isNumeroUnique(expense.getNumero(), null)) {
            throw new BusinessException("Le numéro de dépense '" + expense.getNumero() + "' existe déjà");
        }

        // Calculs automatiques
        calculateAmounts(expense);

        // Sauvegarde
        Expense savedExpense = expenseRepository.save(expense);
        log.info("Dépense créée avec succès: ID={}, Numéro={}", savedExpense.getId(), savedExpense.getNumero());

        return findById(savedExpense.getId()).orElseThrow();
    }

    @Override
    public ExpenseDTO.Response update(Long id, ExpenseDTO.UpdateRequest updateRequest) {
        log.info("Mise à jour de la dépense ID: {}", id);

        Expense existingExpense = getExpenseEntity(id);

        // Vérification des droits de modification
        if (!isEditable(id)) {
            throw new BusinessException("Cette dépense ne peut plus être modifiée (statut: " + existingExpense.getStatut() + ")");
        }

        // Validation des relations si modifiées
        Long categorieId = updateRequest.getCategorieId() != null ? updateRequest.getCategorieId() : existingExpense.getCategorieId();
        Long fournisseurId = updateRequest.getFournisseurId() != null ? updateRequest.getFournisseurId() : existingExpense.getFournisseurId();
        Long paymentMethodId = updateRequest.getPaymentMethodId() != null ? updateRequest.getPaymentMethodId() : existingExpense.getPaymentMethodId();

        validateRelations(categorieId, fournisseurId, paymentMethodId);

        // Validation unicité du numéro si modifié
        if (updateRequest.getNumero() != null && !isNumeroUnique(updateRequest.getNumero(), id)) {
            throw new BusinessException("Le numéro de dépense '" + updateRequest.getNumero() + "' existe déjà");
        }

        // Mise à jour avec mapper
        expenseMapper.updateEntityFromDto(updateRequest, existingExpense);

        // Recalcul des montants si nécessaire
        calculateAmounts(existingExpense);

        // Sauvegarde
        Expense updatedExpense = expenseRepository.save(existingExpense);
        log.info("Dépense mise à jour avec succès: ID={}", updatedExpense.getId());

        return findById(updatedExpense.getId()).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExpenseDTO.Response> findById(Long id) {
        log.debug("Recherche de la dépense ID: {}", id);

        return expenseRepository.findByIdWithFetch(id)
                .map(expenseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO.PageResponse findAll(Pageable pageable) {
        log.debug("Récupération de toutes les dépenses - Page: {}, Taille: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Expense> expensePage = expenseRepository.findAll(pageable);
        return expenseMapper.toPageResponse(expensePage);
    }

    @Override
    public void delete(Long id) {
        log.info("Suppression de la dépense ID: {}", id);

        Expense expense = getExpenseEntity(id);

        // Vérification des droits de suppression
        if (!isDeletable(id)) {
            throw new BusinessException("Cette dépense ne peut pas être supprimée (statut: " + expense.getStatut() + ")");
        }

        expense.setActive(false);
        expenseRepository.save(expense);

        log.info("Dépense supprimée avec succès: ID={}", id);
    }

    @Override
    public ExpenseDTO.Response toggleActive(Long id, Boolean active) {
        log.info("Changement du statut actif de la dépense ID: {} vers {}", id, active);

        Expense expense = getExpenseEntity(id);
        expense.setActive(active);
        expenseRepository.save(expense);

        return findById(id).orElseThrow();
    }

    // ========== Recherche et filtres ==========

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO.PageResponse search(String searchTerm, Pageable pageable) {
        log.debug("Recherche textuelle: '{}'", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll(pageable);
        }

        Page<Expense> expensePage = expenseRepository.findBySearchTermWithFetch(searchTerm.trim(), pageable);
        return expenseMapper.toPageResponse(expensePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO.PageResponse findWithFilters(ExpenseDTO.SearchFilter filter) {
        log.debug("Recherche avec filtres: {}", filter);

        // Construction du Pageable
        Sort sort = Sort.by(Sort.Direction.fromString(filter.getSortDirection()), filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // Si recherche textuelle uniquement
        if (isOnlyTextSearch(filter)) {
            return search(filter.getSearch(), pageable);
        }

        // Utilisation des Specifications pour requête dynamique
        Specification<Expense> specification = ExpenseSpecification.withFilters(filter);


        log.debug("Exécution de la requête avec specification");
        Page<Expense> expensePage = expenseRepository.findAll(specification, pageable);

        log.debug("Résultats trouvés: {} dépenses sur {} pages",
                expensePage.getTotalElements(), expensePage.getTotalPages());

        return expenseMapper.toPageResponse(expensePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO.PageResponse findByCategorie(Long categorieId, Pageable pageable) {
        log.debug("Recherche par catégorie ID: {}", categorieId);

        Page<Expense> expensePage = expenseRepository.findByCategorieIdAndActiveTrueWithFetch(categorieId, pageable);
        return expenseMapper.toPageResponse(expensePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO.PageResponse findByFournisseur(Long fournisseurId, Pageable pageable) {
        log.debug("Recherche par fournisseur ID: {}", fournisseurId);

        Page<Expense> expensePage = expenseRepository.findByFournisseurIdAndActiveTrueWithFetch(fournisseurId, pageable);
        return expenseMapper.toPageResponse(expensePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO.PageResponse findByStatut(ExpenseStatus statut, Pageable pageable) {
        log.debug("Recherche par statut: {}", statut);

        Page<Expense> expensePage = expenseRepository.findByStatutAndActiveTrueWithFetch(statut, pageable);
        return expenseMapper.toPageResponse(expensePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO.PageResponse findByDatePeriod(Integer year, Integer month, Integer day, Pageable pageable) {
        log.debug("Recherche par période: année={}, mois={}, jour={}", year, month, day);

        Page<Expense> expensePage = expenseRepository.findByDatePeriodWithFetch(year, month, day, pageable);
        return expenseMapper.toPageResponse(expensePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO.PageResponse findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        log.debug("Recherche par plage de montants: {} - {}", minAmount, maxAmount);

        Page<Expense> expensePage = expenseRepository.findByAmountRangeWithFetch(minAmount, maxAmount, pageable);
        return expenseMapper.toPageResponse(expensePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO.PageResponse resetSearch(Pageable pageable) {
        log.debug("Réinitialisation de la recherche");
        return findAll(pageable);
    }

    // ========== Gestion des statuts ==========

    @Override
    public ExpenseDTO.Response changeStatus(Long id, ExpenseDTO.StatusChangeRequest statusRequest) {
        log.info("Changement de statut de la dépense ID: {} vers {}", id, statusRequest.getStatut());

        Expense expense = getExpenseEntity(id);
        ExpenseStatus oldStatus = expense.getStatut();
        ExpenseStatus newStatus = statusRequest.getStatut();

        // Validation du changement de statut
        validateStatusChange(oldStatus, newStatus);

        expense.setStatut(newStatus);
        expenseRepository.save(expense);

        log.info("Statut changé avec succès: ID={}, {} -> {}", id, oldStatus, newStatus);
        return findById(id).orElseThrow();
    }

    @Override
    public ExpenseDTO.Response approve(Long id, String commentaire) {
        log.info("Approbation de la dépense ID: {}", id);

        ExpenseDTO.StatusChangeRequest request = ExpenseDTO.StatusChangeRequest.builder()
                .statut(ExpenseStatus.APPROUVEE)
                .commentaire(commentaire)
                .build();

        return changeStatus(id, request);
    }

    @Override
    public ExpenseDTO.Response reject(Long id, String commentaire) {
        log.info("Rejet de la dépense ID: {}", id);

        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new BusinessException("Un commentaire est obligatoire pour rejeter une dépense");
        }

        ExpenseDTO.StatusChangeRequest request = ExpenseDTO.StatusChangeRequest.builder()
                .statut(ExpenseStatus.REJETEE)
                .commentaire(commentaire)
                .build();

        return changeStatus(id, request);
    }

    @Override
    public ExpenseDTO.Response markAsPaid(Long id, String commentaire) {
        log.info("Marquage comme payée de la dépense ID: {}", id);

        ExpenseDTO.StatusChangeRequest request = ExpenseDTO.StatusChangeRequest.builder()
                .statut(ExpenseStatus.PAYEE)
                .commentaire(commentaire)
                .build();

        return changeStatus(id, request);
    }

    @Override
    public ExpenseDTO.Response markAsPending(Long id, String commentaire) {
        log.info("Remise en attente de la dépense ID: {}", id);

        ExpenseDTO.StatusChangeRequest request = ExpenseDTO.StatusChangeRequest.builder()
                .statut(ExpenseStatus.EN_ATTENTE)
                .commentaire(commentaire)
                .build();

        return changeStatus(id, request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDTO.Response> findPendingExpenses() {
        log.debug("Récupération des dépenses en attente");

        List<Expense> pendingExpenses = expenseRepository.findPendingExpensesWithFetch();
        return expenseMapper.toResponseList(pendingExpenses);
    }

    // ========== Statistiques ==========

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO.StatsResponse getStatistics() {
        log.debug("Génération des statistiques générales");

        // Stats générales
        Map<String, Object> generalStats = expenseRepository.getGeneralStats();
        Long totalExpenses = (Long) generalStats.get("total");

        BigDecimal totalAmountXOF = (BigDecimal) generalStats.get("sumXOF");
        BigDecimal totalAmountEUR = (BigDecimal) generalStats.get("sumEUR");

        List<Expense> pendingExpenses = expenseRepository.findPendingExpensesWithFetch();

        log.info("Total des dépenses: {}, Montant total XOF: {}, Montant total EUR: {}, Nombre de dépenses en attente: {}",
                totalExpenses, totalAmountXOF, totalAmountEUR, pendingExpenses.size());

        BigDecimal totalPendingExpenses = BigDecimal.valueOf(pendingExpenses.size());

        // Répartition par statut
        List<Object[]> statusStats = expenseRepository.getStatsByStatus();
        List<ExpenseDTO.StatsResponse.StatutCount> statutRepartition = statusStats.stream()
                .map(stat -> expenseMapper.toStatutCount(
                        (ExpenseStatus) stat[0],
                        (Long) stat[1],
                        (BigDecimal) stat[2]
                ))
                .collect(Collectors.toList());

        // Répartition par catégorie
        List<Object[]> categoryStats = expenseRepository.getStatsByCategory();
        List<ExpenseDTO.StatsResponse.CategorieCount> categorieRepartition = categoryStats.stream()
                .map(stat -> expenseMapper.toCategorieCount(
                        (Long) stat[0],
                        (String) stat[1],
                        (Long) stat[2],
                        (BigDecimal) stat[3]
                ))
                .collect(Collectors.toList());

        // Évolution mensuelle (12 derniers mois)
        LocalDate startDate = LocalDate.now().minusMonths(12);
        List<Object[]> monthlyStats = expenseRepository.getMonthlyEvolution(startDate);
        List<ExpenseDTO.StatsResponse.MonthlyExpense> evolutionMensuelle = monthlyStats.stream()
                .map(stat -> expenseMapper.toMonthlyExpense(
                        (Integer) stat[0],
                        (Integer) stat[1],
                        (Long) stat[2],
                        (BigDecimal) stat[3]
                ))
                .collect(Collectors.toList());

        return ExpenseDTO.StatsResponse.builder()
                .totalExpenses(totalExpenses)
                .totalPending(totalPendingExpenses)
                .totalAmountXOF(totalAmountXOF)
                .totalAmountEUR(totalAmountEUR)
                .statutRepartition(statutRepartition)
                .categorieRepartition(categorieRepartition)
                .evolutionMensuelle(evolutionMensuelle)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO.StatsResponse getStatisticsForPeriod(LocalDate startDate, LocalDate endDate) {
        log.debug("Génération des statistiques pour la période: {} - {}", startDate, endDate);

        // TODO: Implémenter les statistiques pour une période spécifique
        // Similar to getStatistics() but with date filters
        return getStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDTO.StatsResponse.CategorieCount> getTopCategoriesByAmount(int limit, LocalDate startDate) {
        log.debug("Récupération du top {} catégories depuis {}", limit, startDate);

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> topCategories = expenseRepository.getTopCategoriesByAmount(startDate, pageable);

        return topCategories.stream()
                .map(stat -> ExpenseDTO.StatsResponse.CategorieCount.builder()
                        .categorieNom((String) stat[0])
                        .totalAmount((BigDecimal) stat[1])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDTO.Response> getRecentExpenses(int limit) {
        log.debug("Récupération des {} dépenses récentes", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Expense> recentExpenses = expenseRepository.findRecentExpensesWithFetch(pageable);

        return expenseMapper.toResponseList(recentExpenses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDTO.StatsResponse.MonthlyExpense> getMonthlyEvolution(int months) {
        log.debug("Récupération de l'évolution sur {} mois", months);

        LocalDate startDate = LocalDate.now().minusMonths(months);
        List<Object[]> monthlyStats = expenseRepository.getMonthlyEvolution(startDate);

        return monthlyStats.stream()
                .map(stat -> expenseMapper.toMonthlyExpense(
                        (Integer) stat[0],
                        (Integer) stat[1],
                        (Long) stat[2],
                        (BigDecimal) stat[3]
                ))
                .collect(Collectors.toList());
    }

    // ========== Validation et utilitaires ==========

    @Override
    @Transactional(readOnly = true)
    public boolean isNumeroUnique(String numero, Long excludeId) {
        if (numero == null || numero.trim().isEmpty()) {
            return false;
        }

        if (excludeId == null) {
            return !expenseRepository.findByNumeroAndActiveTrue(numero).isPresent();
        } else {
            return !expenseRepository.existsByNumeroAndIdNotAndActiveTrue(numero, excludeId);
        }
    }

    @Override
    public String generateNumero() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseNumero = "DEP-" + dateStr + "-";

        // Trouver le prochain numéro disponible
        int counter = 1;
        String numero;
        do {
            numero = baseNumero + String.format("%03d", counter);
            counter++;
        } while (!isNumeroUnique(numero, null) && counter <= 999);

        if (counter > 999) {
            // Fallback avec timestamp
            numero = baseNumero + System.currentTimeMillis() % 10000;
        }

        log.debug("Numéro généré: {}", numero);
        return numero;
    }

    @Override
    public void validateRelations(Long categorieId, Long fournisseurId, Long paymentMethodId) {
        // Validation catégorie (obligatoire)
        if (categorieId == null) {
            throw new BusinessException("La catégorie est obligatoire");
        }
        if (!categoryRepository.existsByIdAndActiveTrue(categorieId)) {
            throw new EntityNotFoundException("Catégorie non trouvée avec l'ID: " + categorieId);
        }

        // Validation fournisseur (optionnel)
        if (fournisseurId != null && !supplierRepository.existsByIdAndActiveTrue(fournisseurId)) {
            throw new EntityNotFoundException("Fournisseur non trouvé avec l'ID: " + fournisseurId);
        }

        // Validation mode de paiement (obligatoire)
        if (paymentMethodId == null) {
            throw new BusinessException("Le mode de paiement est obligatoire");
        }
        if (!paymentMethodRepository.existsByIdAndActifTrue(paymentMethodId)) {
            throw new EntityNotFoundException("Mode de paiement non trouvé avec l'ID: " + paymentMethodId);
        }
    }

    @Override
    public BigDecimal calculateExchangeRate(BigDecimal montantXOF, BigDecimal montantEUR) {
        if (montantXOF == null || montantEUR == null || montantEUR.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return montantXOF.divide(montantEUR, 6, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEditable(Long id) {
        Optional<Expense> expenseOpt = expenseRepository.findById(id);
        if (expenseOpt.isEmpty()) {
            return false;
        }

        ExpenseStatus status = expenseOpt.get().getStatut();
        // Seules les dépenses EN_ATTENTE et REJETEE peuvent être modifiées
        return status == ExpenseStatus.EN_ATTENTE || status == ExpenseStatus.REJETEE;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDeletable(Long id) {
        Optional<Expense> expenseOpt = expenseRepository.findById(id);
        if (expenseOpt.isEmpty()) {
            return false;
        }

        ExpenseStatus status = expenseOpt.get().getStatut();
        // Seules les dépenses EN_ATTENTE et REJETEE peuvent être supprimées
        return status == ExpenseStatus.EN_ATTENTE || status == ExpenseStatus.REJETEE;
    }

    // ========== Export et rapports ==========

    @Override
    @Transactional(readOnly = true)
    public byte[] exportToPdf(ExpenseDTO.SearchFilter filter) {
        log.info("Export PDF des dépenses avec filtres: {}", filter);
        return exportService.exportToPdf(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportToExcel(ExpenseDTO.SearchFilter filter) {
        log.info("Export Excel des dépenses avec filtres: {}", filter);
        return exportService.exportToExcel(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateMonthlyReport(int year, int month) {
        log.info("Génération du rapport mensuel: {}/{}", month, year);

        // Création d'un filtre pour le mois spécifique
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        ExpenseDTO.SearchFilter filter = ExpenseDTO.SearchFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .sortBy("dateDepense")
                .sortDirection("desc")
                .build();

        return exportService.exportToPdf(filter);
    }

    // ========== Opérations en lot ==========

    @Override
    public int deleteBatch(List<Long> ids) {
        log.info("Suppression en lot de {} dépenses", ids.size());

        int deletedCount = 0;
        for (Long id : ids) {
            try {
                if (isDeletable(id)) {
                    delete(id);
                    deletedCount++;
                }
            } catch (Exception e) {
                log.warn("Impossible de supprimer la dépense ID: {}", id, e);
            }
        }

        log.info("{} dépenses supprimées sur {}", deletedCount, ids.size());
        return deletedCount;
    }

    @Override
    public int changeStatusBatch(List<Long> ids, ExpenseStatus newStatus) {
        log.info("Changement de statut en lot vers {} pour {} dépenses", newStatus, ids.size());

        int updatedCount = 0;
        for (Long id : ids) {
            try {
                ExpenseDTO.StatusChangeRequest request = ExpenseDTO.StatusChangeRequest.builder()
                        .statut(newStatus)
                        .commentaire("Changement en lot")
                        .build();
                changeStatus(id, request);
                updatedCount++;
            } catch (Exception e) {
                log.warn("Impossible de changer le statut de la dépense ID: {}", id, e);
            }
        }

        log.info("{} dépenses mises à jour sur {}", updatedCount, ids.size());
        return updatedCount;
    }

    @Override
    public int approveBatch(List<Long> ids, String commentaire) {
        log.info("Approbation en lot de {} dépenses", ids.size());
        return changeStatusBatch(ids, ExpenseStatus.APPROUVEE);
    }

    // ========== Méthodes privées utilitaires ==========

    private Expense getExpenseEntity(Long id) {
        return expenseRepository.findByIdWithFetch(id)
                .orElseThrow(() -> new EntityNotFoundException("Dépense non trouvée avec l'ID: " + id));
    }

    private void calculateAmounts(Expense expense) {
        if (expense.getTauxChange() != null) {
            if (expense.isDeviseXOF() && expense.getMontantXOF() != null && expense.getMontantEURO() == null) {
                expense.calculerMontantEURO();
            } else if (expense.isDeviseEUR() && expense.getMontantEURO() != null && expense.getMontantXOF() == null) {
                expense.calculerMontantXOF();
            }
        }
    }

    private void validateStatusChange(ExpenseStatus oldStatus, ExpenseStatus newStatus) {
        // Règles de transition des statuts
        switch (oldStatus) {
            case EN_ATTENTE:
                // EN_ATTENTE peut aller vers APPROUVEE ou REJETEE
                if (newStatus != ExpenseStatus.APPROUVEE && newStatus != ExpenseStatus.REJETEE) {
                    throw new BusinessException("Une dépense en attente ne peut être que approuvée ou rejetée");
                }
                break;
            case APPROUVEE:
                // APPROUVEE peut aller vers PAYEE ou revenir EN_ATTENTE
                if (newStatus != ExpenseStatus.PAYEE && newStatus != ExpenseStatus.EN_ATTENTE) {
                    throw new BusinessException("Une dépense approuvée ne peut être que payée ou remise en attente");
                }
                break;
            case REJETEE:
                // REJETEE peut revenir EN_ATTENTE
                if (newStatus != ExpenseStatus.EN_ATTENTE) {
                    throw new BusinessException("Une dépense rejetée ne peut être que remise en attente");
                }
                break;
            case PAYEE:
                // PAYEE ne peut plus changer (sauf cas exceptionnel vers EN_ATTENTE)
                if (newStatus != ExpenseStatus.EN_ATTENTE) {
                    throw new BusinessException("Une dépense payée ne peut plus changer de statut");
                }
                break;
        }
    }

    private boolean isOnlyTextSearch(ExpenseDTO.SearchFilter filter) {
        return filter.getSearch() != null && !filter.getSearch().trim().isEmpty() &&
                filter.getCategorieId() == null &&
                filter.getFournisseurId() == null &&
                filter.getStatut() == null &&
                filter.getPaymentMethodId() == null &&
                filter.getDevise() == null &&
                filter.getMinAmount() == null &&
                filter.getMaxAmount() == null &&
                filter.getStartDate() == null &&
                filter.getEndDate() == null &&
                filter.getYear() == null &&
                filter.getMonth() == null &&
                filter.getDay() == null;
    }
}