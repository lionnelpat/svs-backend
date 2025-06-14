package sn.svs.backoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sn.svs.backoffice.domain.Invoice;
import sn.svs.backoffice.domain.InvoiceLineItem;
import sn.svs.backoffice.domain.ennumeration.InvoiceStatus;
import sn.svs.backoffice.dto.InvoiceDTO;
import sn.svs.backoffice.exceptions.BusinessException;
import sn.svs.backoffice.exceptions.EntityNotFoundException;
import sn.svs.backoffice.mapper.InvoiceMapper;
import sn.svs.backoffice.mapper.InvoiceStatisticsMapper;
import sn.svs.backoffice.repository.InvoiceRepository;
import sn.svs.backoffice.repository.CompanyRepository;
import sn.svs.backoffice.repository.ShipRepository;
import sn.svs.backoffice.repository.OperationRepository;
import sn.svs.backoffice.service.InvoiceService;
import sn.svs.backoffice.service.InvoiceExportService;
import sn.svs.backoffice.specification.InvoiceSpecification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des factures
 * SVS - Dakar, Sénégal
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;
    private final ShipRepository shipRepository;
    private final OperationRepository operationRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceStatisticsMapper statisticsMapper;
    private final InvoiceExportService exportService;

    // ========== CRUD DE BASE ==========

    @Override
    public InvoiceDTO.Response create(InvoiceDTO.CreateRequest createRequest) {
        log.info("Création d'une nouvelle facture pour compagnie ID: {}", createRequest.getCompagnieId());

        // Validation des données d'entrée
        validateCreateRequest(createRequest);

        // Validation des relations
        List<Long> operationIds = createRequest.getPrestations().stream()
                .map(InvoiceDTO.CreateInvoiceLineItemRequest::getOperationId)
                .collect(Collectors.toList());
        validateRelations(createRequest.getCompagnieId(), createRequest.getNavireId(), operationIds);

        // Validation des dates
        validateDates(createRequest.getDateFacture(), createRequest.getDateEcheance());

        try {
            // Conversion en entité (sans les prestations d'abord)
            Invoice invoice = invoiceMapper.toEntity(createRequest);

            // Génération du numéro automatique
            invoice.setNumero(generateNumero());

            // Vider les prestations temporairement
            invoice.setPrestations(new ArrayList<>());

            // Calcul des montants manuellement
            InvoiceDTO.CalculatedAmounts amounts = calculateAmounts(createRequest.getPrestations(), createRequest.getTauxTva());
            invoice.setSousTotal(amounts.getSousTotal());
            invoice.setTva(amounts.getTva());
            invoice.setTauxTva(createRequest.getTauxTva());
            invoice.setMontantTotal(amounts.getMontantTotal());

            // Sauvegarde de la facture d'abord pour obtenir l'ID
            Invoice savedInvoice = invoiceRepository.save(invoice);

            // Maintenant créer les prestations avec l'ID de la facture
            List<InvoiceLineItem> prestations = new ArrayList<>();
            for (InvoiceDTO.CreateInvoiceLineItemRequest prestationRequest : createRequest.getPrestations()) {
                InvoiceLineItem lineItem = invoiceMapper.toLineItemEntity(prestationRequest);
                lineItem.setInvoiceId(savedInvoice.getId());
                lineItem.setInvoice(savedInvoice);

                // Calcul des montants de la ligne
                lineItem.setMontantXOF(prestationRequest.getQuantite().multiply(prestationRequest.getPrixUnitaireXOF()));
                if (prestationRequest.getPrixUnitaireEURO() != null) {
                    lineItem.setMontantEURO(prestationRequest.getQuantite().multiply(prestationRequest.getPrixUnitaireEURO()));
                }

                prestations.add(lineItem);
            }

            // Assigner les prestations à la facture
            savedInvoice.setPrestations(prestations);

            // Sauvegarde finale avec les prestations
            Invoice finalInvoice = invoiceRepository.save(savedInvoice);

            // Récupération avec relations pour la réponse
            Invoice invoiceWithRelations = invoiceRepository.findByIdWithFetch(finalInvoice.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Facture non trouvée après création"));

            log.info("Facture créée avec succès - ID: {}, Numéro: {}",
                    finalInvoice.getId(), finalInvoice.getNumero());

            return invoiceMapper.toDto(invoiceWithRelations);

        } catch (Exception e) {
            log.error("Erreur lors de la création de la facture", e);
            throw new BusinessException("Erreur lors de la création de la facture: " + e.getMessage());
        }
    }

    @Override
    public InvoiceDTO.Response update(Long id, InvoiceDTO.UpdateRequest updateRequest) {
        log.info("Mise à jour de la facture ID: {}", id);

        Invoice existingInvoice = invoiceRepository.findByIdWithFetch(id)
                .orElseThrow(() -> new EntityNotFoundException("Facture non trouvée: " + id));

        // Vérification si modifiable
        if (!isEditable(id)) {
            throw new BusinessException("Cette facture ne peut plus être modifiée (statut: " +
                    existingInvoice.getStatut().getLabel() + ")");
        }

        try {
            // Validation des nouvelles relations si fournies
            if (updateRequest.getCompagnieId() != null || updateRequest.getNavireId() != null ||
                    updateRequest.getPrestations() != null) {

                Long compagnieId = updateRequest.getCompagnieId() != null ?
                        updateRequest.getCompagnieId() : existingInvoice.getCompagnieId();
                Long navireId = updateRequest.getNavireId() != null ?
                        updateRequest.getNavireId() : existingInvoice.getNavireId();

                if (updateRequest.getPrestations() != null) {
                    List<Long> operationIds = updateRequest.getPrestations().stream()
                            .map(InvoiceDTO.CreateInvoiceLineItemRequest::getOperationId)
                            .collect(Collectors.toList());
                    validateRelations(compagnieId, navireId, operationIds);
                }
            }

            // Validation des dates si fournies
            if (updateRequest.getDateFacture() != null && updateRequest.getDateEcheance() != null) {
                validateDates(updateRequest.getDateFacture(), updateRequest.getDateEcheance());
            }

            // Mise à jour des champs
            invoiceMapper.updateEntityFromDto(updateRequest, existingInvoice);

            // Recalcul des montants si prestations modifiées
            if (updateRequest.getPrestations() != null || updateRequest.getTauxTva() != null) {
                BigDecimal tauxTva = updateRequest.getTauxTva() != null ?
                        updateRequest.getTauxTva() : existingInvoice.getTauxTva();
                calculateAndSetAmounts(existingInvoice, updateRequest.getPrestations(), tauxTva);
            }

            Invoice updatedInvoice = invoiceRepository.save(existingInvoice);

            log.info("Facture mise à jour avec succès - ID: {}", id);

            return invoiceMapper.toDto(updatedInvoice);

        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la facture ID: {}", id, e);
            throw new BusinessException("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InvoiceDTO.Response> findById(Long id) {
        log.debug("Recherche facture par ID: {}", id);

        return invoiceRepository.findByIdWithFetch(id)
                .map(invoiceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO.PageResponse findAll(Pageable pageable) {
        log.debug("Recherche toutes les factures avec pagination");

        // Tri par défaut par date de facture décroissante
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "dateFacture"));
        }

        Page<Invoice> invoicePage = invoiceRepository.findAll(
                InvoiceSpecification.withFilters(InvoiceDTO.SearchFilter.builder().build()),
                pageable);

        return invoiceMapper.toPageResponse(invoicePage);
    }

    @Override
    public void delete(Long id) {
        log.info("Suppression logique de la facture ID: {}", id);

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Facture non trouvée: " + id));

        if (!isDeletable(id)) {
            throw new BusinessException("Cette facture ne peut pas être supprimée (statut: " +
                    invoice.getStatut().getLabel() + ")");
        }

        invoice.setActive(false);
        invoiceRepository.save(invoice);

        log.info("Facture supprimée logiquement - ID: {}", id);
    }

    @Override
    public InvoiceDTO.Response toggleActive(Long id, Boolean active) {
        log.info("Changement statut actif facture ID: {} -> {}", id, active);

        Invoice invoice = invoiceRepository.findByIdWithFetch(id)
                .orElseThrow(() -> new EntityNotFoundException("Facture non trouvée: " + id));

        invoice.setActive(active);
        Invoice updatedInvoice = invoiceRepository.save(invoice);

        return invoiceMapper.toDto(updatedInvoice);
    }

    // ========== RECHERCHE ET FILTRES ==========

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO.PageResponse search(String searchTerm, Pageable pageable) {
        log.debug("Recherche textuelle factures: '{}'", searchTerm);

        if (!StringUtils.hasText(searchTerm)) {
            return findAll(pageable);
        }

        Page<Invoice> invoicePage = invoiceRepository.findAll(
                InvoiceSpecification.withTextSearch(searchTerm), pageable);

        return invoiceMapper.toPageResponse(invoicePage);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO.PageResponse findWithFilters(InvoiceDTO.SearchFilter filter) {
        log.debug("Recherche factures avec filtres: {}", filter);

        // Construction de la pagination avec tri
        Pageable pageable = PageRequest.of(
                filter.getPage() != null ? filter.getPage() : 0,
                filter.getSize() != null ? filter.getSize() : 20,
                Sort.by(Sort.Direction.fromString(filter.getSortDirection()), filter.getSortBy())
        );

        Page<Invoice> invoicePage = invoiceRepository.findAll(
                InvoiceSpecification.withFilters(filter), pageable);

        return invoiceMapper.toPageResponse(invoicePage);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO.PageResponse findByCompagnie(Long compagnieId, Pageable pageable) {
        log.debug("Recherche factures par compagnie ID: {}", compagnieId);

        Page<Invoice> invoicePage = invoiceRepository.findAll(
                InvoiceSpecification.withCompagnie(compagnieId), pageable);

        return invoiceMapper.toPageResponse(invoicePage);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO.PageResponse findByNavire(Long navireId, Pageable pageable) {
        log.debug("Recherche factures par navire ID: {}", navireId);

        Page<Invoice> invoicePage = invoiceRepository.findAll(
                InvoiceSpecification.withNavire(navireId), pageable);

        return invoiceMapper.toPageResponse(invoicePage);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO.PageResponse findByStatut(InvoiceStatus statut, Pageable pageable) {
        log.debug("Recherche factures par statut: {}", statut);

        Page<Invoice> invoicePage = invoiceRepository.findAll(
                InvoiceSpecification.withStatus(statut), pageable);

        return invoiceMapper.toPageResponse(invoicePage);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO.PageResponse findByDatePeriod(Integer year, Integer month, Pageable pageable) {
        log.debug("Recherche factures par période: {}/{}", month, year);

        Page<Invoice> invoicePage = invoiceRepository.findAll(
                InvoiceSpecification.withYearAndMonth(year, month), pageable);

        return invoiceMapper.toPageResponse(invoicePage);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO.PageResponse findByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Recherche factures par plage de dates: {} à {}", startDate, endDate);

        Page<Invoice> invoicePage = invoiceRepository.findAll(
                InvoiceSpecification.withDateRange(startDate, endDate), pageable);

        return invoiceMapper.toPageResponse(invoicePage);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO.PageResponse findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        log.debug("Recherche factures par plage de montants: {} à {}", minAmount, maxAmount);

        Page<Invoice> invoicePage = invoiceRepository.findAll(
                InvoiceSpecification.withAmountRange(minAmount, maxAmount), pageable);

        return invoiceMapper.toPageResponse(invoicePage);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO.PageResponse resetSearch(Pageable pageable) {
        log.debug("Réinitialisation recherche factures");
        return findAll(pageable);
    }

    // ========== GESTION DES STATUTS ==========

    @Override
    public InvoiceDTO.Response changeStatus(Long id, InvoiceStatus newStatus, String commentaire) {
        log.info("Changement statut facture ID: {} vers {}", id, newStatus);

        Invoice invoice = invoiceRepository.findByIdWithFetch(id)
                .orElseThrow(() -> new EntityNotFoundException("Facture non trouvée: " + id));

        if (!canTransitionToStatus(invoice.getStatut(), newStatus)) {
            throw new BusinessException(String.format(
                    "Transition de statut non autorisée: %s -> %s",
                    invoice.getStatut().getLabel(), newStatus.getLabel()));
        }

        invoice.setStatut(newStatus);

        if (StringUtils.hasText(commentaire)) {
            String currentNotes = invoice.getNotes() != null ? invoice.getNotes() : "";
            invoice.setNotes(currentNotes + "\n[" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    "] " + newStatus.getLabel() + ": " + commentaire);
        }

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        log.info("Statut facture changé avec succès - ID: {}, Nouveau statut: {}", id, newStatus);

        return invoiceMapper.toDto(updatedInvoice);
    }

    @Override
    public InvoiceDTO.Response emit(Long id, String commentaire) {
        log.info("Émission de la facture ID: {}", id);
        return changeStatus(id, InvoiceStatus.EMISE, commentaire != null ? commentaire : "Facture émise");
    }

    @Override
    public InvoiceDTO.Response markAsPaid(Long id, String commentaire) {
        log.info("Marquage facture payée ID: {}", id);
        return changeStatus(id, InvoiceStatus.PAYEE, commentaire != null ? commentaire : "Paiement reçu");
    }

    @Override
    public InvoiceDTO.Response cancel(Long id, String commentaire) {
        log.info("Annulation de la facture ID: {}", id);

        if (!StringUtils.hasText(commentaire)) {
            throw new BusinessException("Un commentaire est obligatoire pour l'annulation d'une facture");
        }

        return changeStatus(id, InvoiceStatus.ANNULEE, commentaire);
    }

    @Override
    public InvoiceDTO.Response markAsDraft(Long id, String commentaire) {
        log.info("Remise en brouillon de la facture ID: {}", id);
        return changeStatus(id, InvoiceStatus.BROUILLON, commentaire != null ? commentaire : "Remise en brouillon");
    }

    @Override
    public int updateOverdueInvoices() {
        log.info("Mise à jour des factures en retard");

        int updatedCount = invoiceRepository.updateOverdueInvoicesStatus();

        log.info("{} factures mises à jour en statut 'EN_RETARD'", updatedCount);
        return updatedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO.Response> findPendingInvoices() {
        log.debug("Recherche factures en attente");

        List<Invoice> pendingInvoices = invoiceRepository.findPendingInvoicesWithFetch();
        return invoiceMapper.toDtoList(pendingInvoices);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO.Response> findOverdueInvoices() {
        log.debug("Recherche factures échues");

        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoicesWithFetch();
        return invoiceMapper.toDtoList(overdueInvoices);
    }

    // ========== STATISTIQUES ==========

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO.StatisticsResponse getStatistics() {
        log.debug("Calcul des statistiques générales");

        try {
            // Statistiques générales
            Long totalFactures = invoiceRepository.count();
            BigDecimal totalMontantXOF = BigDecimal.ZERO;
            BigDecimal totalMontantEURO = BigDecimal.ZERO;

            // Calcul via requête optimisée
            var generalStats = invoiceRepository.getGeneralStats();
            if (generalStats.containsKey("totalMontantXOF")) {
                totalMontantXOF = (BigDecimal) generalStats.get("totalMontantXOF");
            }
            if (generalStats.containsKey("totalMontantEURO")) {
                totalMontantEURO = (BigDecimal) generalStats.get("totalMontantEURO");
            }

            // Compteurs par statut
            Long facturesEnAttente = invoiceRepository.countFacturesEnAttente();
            Long facturesPayees = invoiceRepository.countFacturesPayees();
            Long facturesEnRetard = invoiceRepository.countFacturesEnRetard();

            // Évolution mensuelle (12 derniers mois)
            LocalDate startDate = LocalDate.now().minusMonths(12);
            List<Object[]> monthlyData = invoiceRepository.getMonthlyEvolution(startDate);
            List<InvoiceDTO.MonthlyInvoiceStatsResponse> monthlyStats = monthlyData.stream()
                    .map(row -> statisticsMapper.buildMonthlyStats(
                            (Integer) row[1], // mois
                            (Integer) row[0], // année
                            (Long) row[2],    // nombre
                            (BigDecimal) row[3], // montant XOF
                            (BigDecimal) row[4]  // montant EUR
                    ))
                    .collect(Collectors.toList());

            // Top compagnies
            Pageable topLimit = PageRequest.of(0, 10);
            List<Object[]> companyData = invoiceRepository.getStatsByCompany(topLimit);
            List<InvoiceDTO.CompanyInvoiceStatsResponse> topCompagnies = companyData.stream()
                    .map(row -> statisticsMapper.buildCompanyStats(
                            (Long) row[0],    // compagnieId
                            (String) row[1],  // nom
                            (Long) row[2],    // nombre
                            (BigDecimal) row[3], // montant XOF
                            (BigDecimal) row[4]  // montant EUR
                    ))
                    .collect(Collectors.toList());

            return statisticsMapper.buildStatisticsResponse(
                    totalFactures, totalMontantXOF, totalMontantEURO,
                    facturesEnAttente, facturesPayees, facturesEnRetard,
                    monthlyStats, topCompagnies
            );

        } catch (Exception e) {
            log.error("Erreur lors du calcul des statistiques", e);
            throw new BusinessException("Erreur lors du calcul des statistiques: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO.StatisticsResponse getStatisticsForPeriod(LocalDate startDate, LocalDate endDate) {
        log.debug("Calcul des statistiques pour la période: {} à {}", startDate, endDate);

        InvoiceDTO.SearchFilter filter = InvoiceDTO.SearchFilter.builder()
                .dateDebut(startDate)
                .dateFin(endDate)
                .build();

        List<Invoice> invoices = invoiceRepository.findAll(InvoiceSpecification.withFilters(filter));

        // Calculs sur la période
        Long totalFactures = (long) invoices.size();
        BigDecimal totalMontantXOF = invoices.stream()
                .map(Invoice::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMontantEURO = invoices.stream()
                .map(this::calculateTotalEuro)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Compteurs par statut pour la période
        Long facturesEnAttente = invoices.stream()
                .mapToLong(i -> i.getStatut() == InvoiceStatus.BROUILLON ? 1 : 0)
                .sum();
        Long facturesPayees = invoices.stream()
                .mapToLong(i -> i.getStatut() == InvoiceStatus.PAYEE ? 1 : 0)
                .sum();
        Long facturesEnRetard = invoices.stream()
                .mapToLong(i -> i.getStatut() == InvoiceStatus.EN_RETARD ? 1 : 0)
                .sum();

        return statisticsMapper.buildStatisticsResponse(
                totalFactures, totalMontantXOF, totalMontantEURO,
                facturesEnAttente, facturesPayees, facturesEnRetard,
                List.of(), List.of() // Pas d'évolution mensuelle ni top compagnies pour période spécifique
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO.CompanyInvoiceStatsResponse> getTopCompaniesByRevenue(int limit, LocalDate startDate) {
        log.debug("Calcul top compagnies par CA - Limite: {}, Depuis: {}", limit, startDate);

        LocalDate fromDate = startDate != null ? startDate : LocalDate.now().minusYears(1);
        Pageable pageable = PageRequest.of(0, limit);

        List<Object[]> companyData = invoiceRepository.getTopCompaniesByRevenue(fromDate, pageable);

        return companyData.stream()
                .map(row -> statisticsMapper.buildCompanyStats(
                        null, // ID non disponible dans cette requête
                        (String) row[0],  // nom
                        null, // nombre non calculé
                        (BigDecimal) row[1], // montant
                        null  // EUR non calculé
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO.Response> getRecentInvoices(int limit) {
        log.debug("Recherche des {} dernières factures", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Invoice> recentInvoices = invoiceRepository.findRecentInvoicesWithFetch(pageable);

        return invoiceMapper.toDtoList(recentInvoices);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO.MonthlyInvoiceStatsResponse> getMonthlyEvolution(int months) {
        log.debug("Calcul évolution mensuelle sur {} mois", months);

        LocalDate startDate = LocalDate.now().minusMonths(months);
        List<Object[]> monthlyData = invoiceRepository.getMonthlyEvolution(startDate);

        return monthlyData.stream()
                .map(row -> statisticsMapper.buildMonthlyStats(
                        (Integer) row[1], // mois
                        (Integer) row[0], // année
                        (Long) row[2],    // nombre
                        (BigDecimal) row[3], // montant XOF
                        (BigDecimal) row[4]  // montant EUR
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO.CompanyInvoiceStatsResponse> getCompaniesWithUnpaidInvoices() {
        log.debug("Recherche compagnies avec factures impayées");

        List<Object[]> companiesData = invoiceRepository.findCompaniesWithUnpaidInvoices();

        return companiesData.stream()
                .map(row -> statisticsMapper.buildCompanyStats(
                        (Long) row[0],    // ID
                        (String) row[1],  // nom
                        null, null, null  // Autres champs non calculés ici
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateRevenueForPeriod(LocalDate startDate, LocalDate endDate) {
        log.debug("Calcul CA pour période: {} à {}", startDate, endDate);

        BigDecimal revenue = invoiceRepository.getTotalRevenueForPeriod(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countInvoicesThisMonth() {
        log.debug("Comptage factures du mois");
        return invoiceRepository.countInvoicesThisMonth();
    }

    // ========== VALIDATION ET UTILITAIRES ==========

    @Override
    @Transactional(readOnly = true)
    public boolean isNumeroUnique(String numero, Long excludeId) {
        if (!StringUtils.hasText(numero)) {
            return false;
        }

        if (excludeId != null) {
            return !invoiceRepository.existsByNumeroAndIdNotAndActiveTrue(numero, excludeId);
        }

        return !invoiceRepository.findByNumeroAndActiveTrue(numero).isPresent();
    }

    @Override
    public String generateNumero() {
        log.debug("Génération nouveau numéro de facture");

        try {
            // Format: FAC-YYYY-NNNNNN
            String year = String.valueOf(LocalDate.now().getYear());

            // Recherche du dernier numéro de l'année
            String prefix = "FAC-" + year + "-";
            Pageable topOne = PageRequest.of(0, 1);
            List<Invoice> lastInvoices = invoiceRepository.findLastCreatedInvoice(topOne);

            int nextNumber = 1;
            if (!lastInvoices.isEmpty()) {
                Invoice lastInvoice = lastInvoices.get(0);
                String lastNumero = lastInvoice.getNumero();
                if (lastNumero.startsWith(prefix)) {
                    try {
                        String numberPart = lastNumero.substring(prefix.length());
                        nextNumber = Integer.parseInt(numberPart) + 1;
                    } catch (NumberFormatException e) {
                        log.warn("Impossible de parser le numéro: {}", lastNumero);
                    }
                }
            }

            String newNumero = prefix + String.format("%06d", nextNumber);

            // Vérification unicité
            while (!isNumeroUnique(newNumero, null)) {
                nextNumber++;
                newNumero = prefix + String.format("%06d", nextNumber);
            }

            log.debug("Nouveau numéro généré: {}", newNumero);
            return newNumero;

        } catch (Exception e) {
            log.error("Erreur lors de la génération du numéro", e);
            // Fallback avec timestamp
            return "FAC-" + LocalDate.now().getYear() + "-" + System.currentTimeMillis() % 1000000;
        }
    }

    @Override
    public void validateRelations(Long compagnieId, Long navireId, List<Long> operationIds) {
        log.debug("Validation des relations - Compagnie: {}, Navire: {}, Opérations: {}",
                compagnieId, navireId, operationIds);

        // Validation compagnie
        if (!companyRepository.existsById(compagnieId)) {
            throw new EntityNotFoundException("Compagnie non trouvée: " + compagnieId);
        }

        // Validation navire
        if (!shipRepository.existsById(navireId)) {
            throw new EntityNotFoundException("Navire non trouvé: " + navireId);
        }

        // Validation que le navire appartient à la compagnie
        // Note: Cette validation dépend de votre modèle Ship
        // shipRepository.findByIdAndCompagnieId(navireId, compagnieId)...

        // Validation opérations
        if (operationIds != null && !operationIds.isEmpty()) {
            for (Long operationId : operationIds) {
                if (!operationRepository.existsById(operationId)) {
                    throw new EntityNotFoundException("Opération non trouvée: " + operationId);
                }
            }
        }
    }

    @Override
    public InvoiceDTO.CalculatedAmounts calculateAmounts(List<InvoiceDTO.CreateInvoiceLineItemRequest> prestations, BigDecimal tauxTva) {
        log.debug("Calcul des montants pour {} prestations, TVA: {}%", prestations.size(), tauxTva);

        if (prestations == null || prestations.isEmpty()) {
            return new InvoiceDTO.CalculatedAmounts(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // Calcul sous-total XOF
        BigDecimal sousTotal = prestations.stream()
                .map(p -> p.getQuantite().multiply(p.getPrixUnitaireXOF()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcul TVA
        BigDecimal tva = sousTotal.multiply(tauxTva)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Calcul total XOF
        BigDecimal montantTotal = sousTotal.add(tva);

        // Calcul total EUR (si prix EUR disponibles)
        BigDecimal montantTotalEuro = prestations.stream()
                .filter(p -> p.getPrixUnitaireEURO() != null)
                .map(p -> p.getQuantite().multiply(p.getPrixUnitaireEURO()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ajout TVA sur montant EUR
        if (montantTotalEuro.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tvaEuro = montantTotalEuro.multiply(tauxTva)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            montantTotalEuro = montantTotalEuro.add(tvaEuro);
        }

        log.debug("Montants calculés - Sous-total: {}, TVA: {}, Total: {}, Total EUR: {}",
                sousTotal, tva, montantTotal, montantTotalEuro);

        return new InvoiceDTO.CalculatedAmounts(sousTotal, tva, montantTotal, montantTotalEuro);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEditable(Long id) {
        return invoiceRepository.findById(id)
                .map(Invoice::isModifiable)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDeletable(Long id) {
        return invoiceRepository.findById(id)
                .map(Invoice::isSupprimable)
                .orElse(false);
    }

    @Override
    public boolean canTransitionToStatus(InvoiceStatus currentStatus, InvoiceStatus newStatus) {
        return InvoiceDTO.StatusTransition.isAllowed(currentStatus, newStatus);
    }

    @Override
    public void validateDates(LocalDate dateFacture, LocalDate dateEcheance) {
        if (dateFacture == null || dateEcheance == null) {
            throw new BusinessException("Les dates de facture et d'échéance sont obligatoires");
        }

        if (dateEcheance.isBefore(dateFacture)) {
            throw new BusinessException("La date d'échéance ne peut pas être antérieure à la date de facture");
        }

        // Validation: date de facture ne peut pas être dans le futur (sauf pour les brouillons)
        if (dateFacture.isAfter(LocalDate.now().plusDays(1))) {
            throw new BusinessException("La date de facture ne peut pas être dans le futur");
        }
    }

    // ========== EXPORT ET RAPPORTS ==========

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO.PrintDataResponse getPrintData(Long id) {
        log.debug("Préparation données impression facture ID: {}", id);

        Invoice invoice = invoiceRepository.findByIdWithFetch(id)
                .orElseThrow(() -> new EntityNotFoundException("Facture non trouvée: " + id));

        return invoiceMapper.toPrintData(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportToPdf(InvoiceDTO.SearchFilter filter) {
        log.info("Export PDF factures avec filtres");
        return exportService.exportListToPdf(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportToExcel(InvoiceDTO.SearchFilter filter) {
        log.info("Export Excel factures avec filtres");
        return exportService.exportListToExcel(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateMonthlyReport(int year, int month) {
        log.info("Génération rapport mensuel: {}/{}", month, year);
        return exportService.generateMonthlyReport(year, month);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportInvoiceToPdf(Long id) {
        log.info("Export PDF facture individuelle ID: {}", id);
        return exportService.exportInvoiceToPdf(id);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateAccountStatement(Long compagnieId, LocalDate asOfDate) {
        log.info("Génération état des comptes - Compagnie: {}, Date: {}", compagnieId, asOfDate);
        return exportService.generateAccountStatement(compagnieId, asOfDate);
    }

    // ========== OPÉRATIONS EN LOT ==========

    @Override
    public int deleteBatch(List<Long> ids) {
        log.info("Suppression en lot de {} factures", ids.size());

        int deletedCount = 0;
        for (Long id : ids) {
            try {
                if (isDeletable(id)) {
                    delete(id);
                    deletedCount++;
                } else {
                    log.warn("Facture ID {} non supprimable - ignorée", id);
                }
            } catch (Exception e) {
                log.error("Erreur suppression facture ID {}: {}", id, e.getMessage());
            }
        }

        log.info("{} factures supprimées sur {} demandées", deletedCount, ids.size());
        return deletedCount;
    }

    @Override
    public int changeStatusBatch(List<Long> ids, InvoiceStatus newStatus, String commentaire) {
        log.info("Changement statut en lot de {} factures vers {}", ids.size(), newStatus);

        int updatedCount = 0;
        for (Long id : ids) {
            try {
                changeStatus(id, newStatus, commentaire);
                updatedCount++;
            } catch (Exception e) {
                log.error("Erreur changement statut facture ID {}: {}", id, e.getMessage());
            }
        }

        log.info("{} factures mises à jour sur {} demandées", updatedCount, ids.size());
        return updatedCount;
    }

    @Override
    public int emitBatch(List<Long> ids, String commentaire) {
        log.info("Émission en lot de {} factures", ids.size());
        return changeStatusBatch(ids, InvoiceStatus.EMISE, commentaire);
    }

    @Override
    public int markAsPaidBatch(List<Long> ids, String commentaire) {
        log.info("Marquage payé en lot de {} factures", ids.size());
        return changeStatusBatch(ids, InvoiceStatus.PAYEE, commentaire);
    }

    // ========== MÉTHODES PRIVÉES ==========

    private void validateCreateRequest(InvoiceDTO.CreateRequest request) {
        if (request.getCompagnieId() == null) {
            throw new BusinessException("La compagnie est obligatoire");
        }
        if (request.getNavireId() == null) {
            throw new BusinessException("Le navire est obligatoire");
        }
        if (request.getDateFacture() == null) {
            throw new BusinessException("La date de facture est obligatoire");
        }
        if (request.getDateEcheance() == null) {
            throw new BusinessException("La date d'échéance est obligatoire");
        }
        if (request.getTauxTva() == null) {
            throw new BusinessException("Le taux de TVA est obligatoire");
        }
        if (request.getPrestations() == null || request.getPrestations().isEmpty()) {
            throw new BusinessException("Au moins une prestation est obligatoire");
        }

        // Validation des prestations
        for (InvoiceDTO.CreateInvoiceLineItemRequest prestation : request.getPrestations()) {
            if (prestation.getOperationId() == null) {
                throw new BusinessException("L'opération est obligatoire pour chaque prestation");
            }
            if (prestation.getQuantite() == null || prestation.getQuantite().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("La quantité doit être supérieure à zéro");
            }
            if (prestation.getPrixUnitaireXOF() == null || prestation.getPrixUnitaireXOF().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Le prix unitaire XOF doit être supérieur à zéro");
            }
        }
    }

    private void calculateAndSetAmounts(Invoice invoice, List<InvoiceDTO.CreateInvoiceLineItemRequest> prestations, BigDecimal tauxTva) {
        // Calcul des montants totaux
        InvoiceDTO.CalculatedAmounts amounts = calculateAmounts(prestations, tauxTva);

        // Mise à jour de la facture
        invoice.setSousTotal(amounts.getSousTotal());
        invoice.setTva(amounts.getTva());
        invoice.setTauxTva(tauxTva);
        invoice.setMontantTotal(amounts.getMontantTotal());

        // Création/mise à jour des lignes de prestations
        if (invoice.getPrestations() != null) {
            invoice.getPrestations().clear();
        }

        for (InvoiceDTO.CreateInvoiceLineItemRequest prestationRequest : prestations) {
            InvoiceLineItem lineItem = invoiceMapper.toLineItemEntity(prestationRequest);
            lineItem.setInvoiceId(invoice.getId());
            lineItem.setInvoice(invoice);

            // Calcul des montants de la ligne
            lineItem.setMontantXOF(prestationRequest.getQuantite().multiply(prestationRequest.getPrixUnitaireXOF()));
            if (prestationRequest.getPrixUnitaireEURO() != null) {
                lineItem.setMontantEURO(prestationRequest.getQuantite().multiply(prestationRequest.getPrixUnitaireEURO()));
            }

            if (invoice.getPrestations() == null) {
                invoice.setPrestations(new java.util.ArrayList<>());
            }
            invoice.getPrestations().add(lineItem);
        }
    }

    private BigDecimal calculateTotalEuro(Invoice invoice) {
        if (invoice.getPrestations() == null || invoice.getPrestations().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return invoice.getPrestations().stream()
                .filter(item -> item.getMontantEURO() != null)
                .map(InvoiceLineItem::getMontantEURO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
