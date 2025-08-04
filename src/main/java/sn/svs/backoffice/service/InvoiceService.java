package sn.svs.backoffice.service;

import org.springframework.data.domain.Pageable;
import sn.svs.backoffice.domain.ennumeration.InvoiceStatus;
import sn.svs.backoffice.dto.InvoiceDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface du service pour la gestion des factures
 * SVS - Dakar, Sénégal
 */
public interface InvoiceService {

    // ========== CRUD de base ==========

    /**
     * Créer une nouvelle facture
     * @param createRequest données de création
     * @return facture créée
     */
    InvoiceDTO.Response create(InvoiceDTO.CreateRequest createRequest);

    /**
     * Mettre à jour une facture existante
     * @param id identifiant de la facture
     * @param updateRequest données de mise à jour
     * @return facture mise à jour
     */
    InvoiceDTO.Response update(Long id, InvoiceDTO.UpdateRequest updateRequest);

    /**
     * Récupérer une facture par son ID avec toutes les relations
     * @param id identifiant de la facture
     * @return facture trouvée avec compagnie, navire et prestations
     */
    Optional<InvoiceDTO.Response> findById(Long id);

    /**
     * Récupérer toutes les factures avec pagination
     * @param pageable paramètres de pagination
     * @return page de factures
     */
    InvoiceDTO.PageResponse findAll(Pageable pageable);

    /**
     * Supprimer une facture (suppression logique)
     * @param id identifiant de la facture
     */
    void delete(Long id);

    /**
     * Activer/Désactiver une facture
     * @param id identifiant de la facture
     * @param active nouveau statut actif
     * @return facture mise à jour
     */
    InvoiceDTO.Response toggleActive(Long id, Boolean active);

    // ========== Recherche et filtres ==========

    /**
     * Recherche textuelle dans les factures (numéro, notes, compagnie, navire)
     * @param searchTerm terme de recherche
     * @param pageable paramètres de pagination
     * @return page de factures correspondantes
     */
    InvoiceDTO.PageResponse search(String searchTerm, Pageable pageable);

    /**
     * Recherche avec filtres multiples
     * @param filter critères de filtrage
     * @return page de factures filtrées
     */
    InvoiceDTO.PageResponse findWithFilters(InvoiceDTO.SearchFilter filter);

    /**
     * Recherche par compagnie
     * @param compagnieId identifiant de la compagnie
     * @param pageable paramètres de pagination
     * @return page de factures de la compagnie
     */
    InvoiceDTO.PageResponse findByCompagnie(Long compagnieId, Pageable pageable);

    /**
     * Recherche par navire
     * @param navireId identifiant du navire
     * @param pageable paramètres de pagination
     * @return page de factures du navire
     */
    InvoiceDTO.PageResponse findByNavire(Long navireId, Pageable pageable);

    /**
     * Recherche par statut
     * @param statut statut des factures
     * @param pageable paramètres de pagination
     * @return page de factures avec le statut donné
     */
    InvoiceDTO.PageResponse findByStatut(InvoiceStatus statut, Pageable pageable);

    /**
     * Recherche par période (année/mois)
     * @param year année (optionnelle)
     * @param month mois (optionnel)
     * @param pageable paramètres de pagination
     * @return page de factures de la période
     */
    InvoiceDTO.PageResponse findByDatePeriod(Integer year, Integer month, Pageable pageable);

    /**
     * Recherche par plage de dates
     * @param startDate date de début
     * @param endDate date de fin
     * @param pageable paramètres de pagination
     * @return page de factures dans la plage
     */
    InvoiceDTO.PageResponse findByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Recherche par plage de montants
     * @param minAmount montant minimum
     * @param maxAmount montant maximum
     * @param pageable paramètres de pagination
     * @return page de factures dans la plage
     */
    InvoiceDTO.PageResponse findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * Réinitialiser les filtres de recherche
     * @param pageable paramètres de pagination
     * @return page de toutes les factures actives
     */
    InvoiceDTO.PageResponse resetSearch(Pageable pageable);

    // ========== Gestion des statuts ==========

    /**
     * Changer le statut d'une facture
     * @param id identifiant de la facture
     * @param newStatus nouveau statut
     * @param commentaire commentaire optionnel
     * @return facture avec statut mis à jour
     */
    InvoiceDTO.Response changeStatus(Long id, InvoiceStatus newStatus, String commentaire);

    /**
     * Émettre une facture (passer de BROUILLON à EMISE)
     * @param id identifiant de la facture
     * @param commentaire commentaire optionnel
     * @return facture émise
     */
    InvoiceDTO.Response emit(Long id, String commentaire);

    /**
     * Marquer une facture comme payée
     * @param id identifiant de la facture
     * @param commentaire commentaire optionnel
     * @return facture marquée payée
     */
    InvoiceDTO.Response markAsPaid(Long id, String commentaire);

    /**
     * Annuler une facture
     * @param id identifiant de la facture
     * @param commentaire commentaire obligatoire
     * @return facture annulée
     */
    InvoiceDTO.Response cancel(Long id, String commentaire);

    /**
     * Remettre une facture en brouillon
     * @param id identifiant de la facture
     * @param commentaire commentaire optionnel
     * @return facture remise en brouillon
     */
    InvoiceDTO.Response markAsDraft(Long id, String commentaire);

    /**
     * Mettre à jour automatiquement les factures en retard
     * @return nombre de factures mises à jour
     */
    int updateOverdueInvoices();

    /**
     * Obtenir les factures en attente (brouillon)
     * @return liste des factures en attente
     */
    List<InvoiceDTO.Response> findPendingInvoices();

    /**
     * Obtenir les factures échues (en retard)
     * @return liste des factures échues
     */
    List<InvoiceDTO.Response> findOverdueInvoices();

    // ========== Statistiques ==========

    /**
     * Obtenir les statistiques générales des factures
     * @return statistiques complètes
     */
    InvoiceDTO.StatisticsResponse getStatistics();

    /**
     * Obtenir les statistiques pour une période donnée
     * @param startDate date de début
     * @param endDate date de fin
     * @return statistiques de la période
     */
    InvoiceDTO.StatisticsResponse getStatisticsForPeriod(LocalDate startDate, LocalDate endDate);

    /**
     * Obtenir les top compagnies par chiffre d'affaires
     * @param limit nombre de compagnies à retourner
     * @param startDate date de début (optionnelle)
     * @return liste des top compagnies
     */
    List<InvoiceDTO.CompanyInvoiceStatsResponse> getTopCompaniesByRevenue(int limit, LocalDate startDate);

    /**
     * Obtenir les factures récentes
     * @param limit nombre de factures à retourner
     * @return liste des factures récentes
     */
    List<InvoiceDTO.Response> getRecentInvoices(int limit);

    /**
     * Obtenir l'évolution mensuelle des factures
     * @param months nombre de mois à retourner
     * @return évolution mensuelle
     */
    List<InvoiceDTO.MonthlyInvoiceStatsResponse> getMonthlyEvolution(int months);

    /**
     * Obtenir les compagnies avec factures impayées
     * @return liste des compagnies avec factures en attente/retard
     */
    List<InvoiceDTO.CompanyInvoiceStatsResponse> getCompaniesWithUnpaidInvoices();

    /**
     * Calculer le chiffre d'affaires pour une période
     * @param startDate date de début
     * @param endDate date de fin
     * @return montant total des factures payées
     */
    BigDecimal calculateRevenueForPeriod(LocalDate startDate, LocalDate endDate);

    /**
     * Compter les factures créées ce mois
     * @return nombre de factures du mois en cours
     */
    Long countInvoicesThisMonth();

    // ========== Validation et utilitaires ==========

    /**
     * Vérifier l'unicité du numéro de facture
     * @param numero numéro à vérifier
     * @param excludeId ID à exclure de la vérification (pour les mises à jour)
     * @return true si le numéro est unique
     */
    boolean isNumeroUnique(String numero, Long excludeId);

    /**
     * Générer un numéro de facture automatique
     * Format: FAC-YYYY-NNNNNN
     * @return nouveau numéro unique
     */
    String generateNumero();

    /**
     * Valider l'existence des relations (compagnie, navire, opérations)
     * @param compagnieId ID de la compagnie
     * @param navireId ID du navire
     * @param operationIds IDs des opérations dans les prestations
     * @throws IllegalArgumentException si une relation n'existe pas
     */
    void validateRelations(Long compagnieId, Long navireId, List<Long> operationIds);

    /**
     * Calculer automatiquement les montants d'une facture
     * @param prestations liste des prestations
     * @param tauxTva taux de TVA à appliquer
     * @return montants calculés (sous-total, TVA, total)
     */
    InvoiceDTO.CalculatedAmounts calculateAmounts(List<InvoiceDTO.CreateInvoiceLineItemRequest> prestations, BigDecimal tauxTva);

    /**
     * Vérifier si une facture peut être modifiée selon son statut
     * @param id identifiant de la facture
     * @return true si modifiable (statut BROUILLON)
     */
    boolean isEditable(Long id);

    /**
     * Vérifier si une facture peut être supprimée selon son statut
     * @param id identifiant de la facture
     * @return true si supprimable (statut BROUILLON ou ANNULEE)
     */
    boolean isDeletable(Long id);

    /**
     * Vérifier si une facture peut changer vers un nouveau statut
     * @param currentStatus statut actuel
     * @param newStatus nouveau statut désiré
     * @return true si la transition est autorisée
     */
    boolean canTransitionToStatus(InvoiceStatus currentStatus, InvoiceStatus newStatus);

    /**
     * Valider les dates d'une facture (échéance > émission)
     * @param dateFacture date d'émission
     * @param dateEcheance date d'échéance
     * @throws IllegalArgumentException si les dates sont invalides
     */
    void validateDates(LocalDate dateFacture, LocalDate dateEcheance);

    // ========== Export et rapports ==========

    /**
     * Obtenir les données pour impression d'une facture
     * @param id identifiant de la facture
     * @return données complètes pour impression PDF
     */
    InvoiceDTO.PrintDataResponse getPrintData(Long id);

    /**
     * Exporter les factures au format PDF
     * @param filter filtres d'export
     * @return contenu du PDF en bytes
     */
    byte[] exportToPdf(InvoiceDTO.SearchFilter filter);

    /**
     * Exporter les factures au format Excel
     * @param filter filtres d'export
     * @return contenu du fichier Excel en bytes
     */
    byte[] exportToExcel(InvoiceDTO.SearchFilter filter);

    /**
     * Générer un rapport mensuel des factures
     * @param year année du rapport
     * @param month mois du rapport
     * @return contenu du rapport en bytes
     */
    byte[] generateMonthlyReport(int year, int month);

    /**
     * Exporter une facture individuelle en PDF
     * @param id identifiant de la facture
     * @return contenu du PDF de la facture
     */
    byte[] exportInvoiceToPdf(Long id);

    /**
     * Générer un état des comptes clients
     * @param compagnieId ID de la compagnie (optionnel, toutes si null)
     * @param asOfDate date de l'état (optionnelle, aujourd'hui si null)
     * @return rapport des comptes clients en PDF
     */
    byte[] generateAccountStatement(Long compagnieId, LocalDate asOfDate);

    // ========== Opérations en lot ==========

    /**
     * Supprimer plusieurs factures en une fois
     * @param ids liste des identifiants
     * @return nombre de factures supprimées
     */
    int deleteBatch(List<Long> ids);

    /**
     * Changer le statut de plusieurs factures
     * @param ids liste des identifiants
     * @param newStatus nouveau statut
     * @param commentaire commentaire pour toutes les factures
     * @return nombre de factures mises à jour
     */
    int changeStatusBatch(List<Long> ids, InvoiceStatus newStatus, String commentaire);

    /**
     * Émettre plusieurs factures en une fois
     * @param ids liste des identifiants
     * @param commentaire commentaire pour toutes les factures
     * @return nombre de factures émises
     */
    int emitBatch(List<Long> ids, String commentaire);

    /**
     * Marquer plusieurs factures comme payées
     * @param ids liste des identifiants
     * @param commentaire commentaire pour toutes les factures
     * @return nombre de factures marquées payées
     */
    int markAsPaidBatch(List<Long> ids, String commentaire);

}
