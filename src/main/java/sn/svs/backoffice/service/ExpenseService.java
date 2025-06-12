package sn.svs.backoffice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.svs.backoffice.dto.ExpenseDTO;
import sn.svs.backoffice.domain.ennumeration.ExpenseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface du service pour la gestion des dépenses
 * SVS - Dakar, Sénégal
 */
public interface ExpenseService {

    // ========== CRUD de base ==========

    /**
     * Créer une nouvelle dépense
     * @param createRequest données de création
     * @return dépense créée
     */
    ExpenseDTO.Response create(ExpenseDTO.CreateRequest createRequest);

    /**
     * Mettre à jour une dépense existante
     * @param id identifiant de la dépense
     * @param updateRequest données de mise à jour
     * @return dépense mise à jour
     */
    ExpenseDTO.Response update(Long id, ExpenseDTO.UpdateRequest updateRequest);

    /**
     * Récupérer une dépense par son ID
     * @param id identifiant de la dépense
     * @return dépense trouvée
     */
    Optional<ExpenseDTO.Response> findById(Long id);

    /**
     * Récupérer toutes les dépenses avec pagination
     * @param pageable paramètres de pagination
     * @return page de dépenses
     */
    ExpenseDTO.PageResponse findAll(Pageable pageable);

    /**
     * Supprimer une dépense (suppression logique)
     * @param id identifiant de la dépense
     */
    void delete(Long id);

    /**
     * Activer/Désactiver une dépense
     * @param id identifiant de la dépense
     * @param active nouveau statut actif
     * @return dépense mise à jour
     */
    ExpenseDTO.Response toggleActive(Long id, Boolean active);

    // ========== Recherche et filtres ==========

    /**
     * Recherche textuelle dans les dépenses
     * @param searchTerm terme de recherche
     * @param pageable paramètres de pagination
     * @return page de dépenses correspondantes
     */
    ExpenseDTO.PageResponse search(String searchTerm, Pageable pageable);

    /**
     * Recherche avec filtres multiples
     * @param filter critères de filtrage
     * @return page de dépenses filtrées
     */
    ExpenseDTO.PageResponse findWithFilters(ExpenseDTO.SearchFilter filter);

    /**
     * Recherche par catégorie
     * @param categorieId identifiant de la catégorie
     * @param pageable paramètres de pagination
     * @return page de dépenses de la catégorie
     */
    ExpenseDTO.PageResponse findByCategorie(Long categorieId, Pageable pageable);

    /**
     * Recherche par fournisseur
     * @param fournisseurId identifiant du fournisseur
     * @param pageable paramètres de pagination
     * @return page de dépenses du fournisseur
     */
    ExpenseDTO.PageResponse findByFournisseur(Long fournisseurId, Pageable pageable);

    /**
     * Recherche par statut
     * @param statut statut des dépenses
     * @param pageable paramètres de pagination
     * @return page de dépenses avec le statut donné
     */
    ExpenseDTO.PageResponse findByStatut(ExpenseStatus statut, Pageable pageable);

    /**
     * Recherche par période (année/mois/jour)
     * @param year année (optionnelle)
     * @param month mois (optionnel)
     * @param day jour (optionnel)
     * @param pageable paramètres de pagination
     * @return page de dépenses de la période
     */
    ExpenseDTO.PageResponse findByDatePeriod(Integer year, Integer month, Integer day, Pageable pageable);

    /**
     * Recherche par plage de montants
     * @param minAmount montant minimum
     * @param maxAmount montant maximum
     * @param pageable paramètres de pagination
     * @return page de dépenses dans la plage
     */
    ExpenseDTO.PageResponse findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * Réinitialiser les filtres de recherche
     * @param pageable paramètres de pagination
     * @return page de toutes les dépenses actives
     */
    ExpenseDTO.PageResponse resetSearch(Pageable pageable);

    // ========== Gestion des statuts ==========

    /**
     * Changer le statut d'une dépense
     * @param id identifiant de la dépense
     * @param statusRequest nouvelle demande de statut
     * @return dépense avec statut mis à jour
     */
    ExpenseDTO.Response changeStatus(Long id, ExpenseDTO.StatusChangeRequest statusRequest);

    /**
     * Approuver une dépense
     * @param id identifiant de la dépense
     * @param commentaire commentaire optionnel
     * @return dépense approuvée
     */
    ExpenseDTO.Response approve(Long id, String commentaire);

    /**
     * Rejeter une dépense
     * @param id identifiant de la dépense
     * @param commentaire commentaire obligatoire
     * @return dépense rejetée
     */
    ExpenseDTO.Response reject(Long id, String commentaire);

    /**
     * Marquer une dépense comme payée
     * @param id identifiant de la dépense
     * @param commentaire commentaire optionnel
     * @return dépense marquée payée
     */
    ExpenseDTO.Response markAsPaid(Long id, String commentaire);

    /**
     * Remettre une dépense en attente
     * @param id identifiant de la dépense
     * @param commentaire commentaire optionnel
     * @return dépense remise en attente
     */
    ExpenseDTO.Response markAsPending(Long id, String commentaire);

    /**
     * Obtenir les dépenses en attente d'approbation
     * @return liste des dépenses en attente
     */
    List<ExpenseDTO.Response> findPendingExpenses();

    // ========== Statistiques ==========

    /**
     * Obtenir les statistiques générales des dépenses
     * @return statistiques complètes
     */
    ExpenseDTO.StatsResponse getStatistics();

    /**
     * Obtenir les statistiques pour une période donnée
     * @param startDate date de début
     * @param endDate date de fin
     * @return statistiques de la période
     */
    ExpenseDTO.StatsResponse getStatisticsForPeriod(LocalDate startDate, LocalDate endDate);

    /**
     * Obtenir les top catégories par montant
     * @param limit nombre de catégories à retourner
     * @param startDate date de début (optionnelle)
     * @return liste des top catégories
     */
    List<ExpenseDTO.StatsResponse.CategorieCount> getTopCategoriesByAmount(int limit, LocalDate startDate);

    /**
     * Obtenir les dépenses récentes
     * @param limit nombre de dépenses à retourner
     * @return liste des dépenses récentes
     */
    List<ExpenseDTO.Response> getRecentExpenses(int limit);

    /**
     * Obtenir l'évolution mensuelle des dépenses
     * @param months nombre de mois à retourner
     * @return évolution mensuelle
     */
    List<ExpenseDTO.StatsResponse.MonthlyExpense> getMonthlyEvolution(int months);

    // ========== Validation et utilitaires ==========

    /**
     * Vérifier l'unicité du numéro de dépense
     * @param numero numéro à vérifier
     * @param excludeId ID à exclure de la vérification (pour les mises à jour)
     * @return true si le numéro est unique
     */
    boolean isNumeroUnique(String numero, Long excludeId);

    /**
     * Générer un numéro de dépense automatique
     * @return nouveau numéro unique
     */
    String generateNumero();

    /**
     * Valider l'existence des relations (catégorie, fournisseur, mode de paiement)
     * @param categorieId ID de la catégorie
     * @param fournisseurId ID du fournisseur (optionnel)
     * @param paymentMethodId ID du mode de paiement
     * @throws IllegalArgumentException si une relation n'existe pas
     */
    void validateRelations(Long categorieId, Long fournisseurId, Long paymentMethodId);

    /**
     * Calculer le taux de change automatique si nécessaire
     * @param montantXOF montant en XOF
     * @param montantEUR montant en EUR
     * @return taux de change calculé ou null
     */
    BigDecimal calculateExchangeRate(BigDecimal montantXOF, BigDecimal montantEUR);

    /**
     * Vérifier si une dépense peut être modifiée selon son statut
     * @param id identifiant de la dépense
     * @return true si modifiable
     */
    boolean isEditable(Long id);

    /**
     * Vérifier si une dépense peut être supprimée selon son statut
     * @param id identifiant de la dépense
     * @return true si supprimable
     */
    boolean isDeletable(Long id);

    // ========== Export et rapports ==========

    /**
     * Exporter les dépenses au format PDF
     * @param filter filtres d'export
     * @return contenu du PDF en bytes
     */
    byte[] exportToPdf(ExpenseDTO.SearchFilter filter);

    /**
     * Exporter les dépenses au format Excel
     * @param filter filtres d'export
     * @return contenu du fichier Excel en bytes
     */
    byte[] exportToExcel(ExpenseDTO.SearchFilter filter);

    /**
     * Générer un rapport mensuel des dépenses
     * @param year année du rapport
     * @param month mois du rapport
     * @return contenu du rapport en bytes
     */
    byte[] generateMonthlyReport(int year, int month);

    // ========== Opérations en lot ==========

    /**
     * Supprimer plusieurs dépenses en une fois
     * @param ids liste des identifiants
     * @return nombre de dépenses supprimées
     */
    int deleteBatch(List<Long> ids);

    /**
     * Changer le statut de plusieurs dépenses
     * @param ids liste des identifiants
     * @param newStatus nouveau statut
     * @return nombre de dépenses mises à jour
     */
    int changeStatusBatch(List<Long> ids, ExpenseStatus newStatus);

    /**
     * Approuver plusieurs dépenses en une fois
     * @param ids liste des identifiants
     * @param commentaire commentaire pour toutes les dépenses
     * @return nombre de dépenses approuvées
     */
    int approveBatch(List<Long> ids, String commentaire);
}
