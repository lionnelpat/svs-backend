package sn.svs.backoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.svs.backoffice.domain.Operation;
import sn.svs.backoffice.domain.PaymentMethod;
import sn.svs.backoffice.dto.OperationDTO;
import sn.svs.backoffice.dto.PaymentMethodDTO;
import sn.svs.backoffice.exceptions.BusinessException;
import sn.svs.backoffice.exceptions.ResourceNotFoundException;
import sn.svs.backoffice.mapper.PaymentMethodMapper;
import sn.svs.backoffice.repository.PaymentMethodRepository;
import sn.svs.backoffice.service.PaymentMethodService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation du service pour la gestion des modes de paiement
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodMapper paymentMethodMapper;

    @Override
    public PaymentMethodDTO.Response create(PaymentMethodDTO.CreateRequest createDTO) {
        log.info("Création d'un nouveau mode de paiement: {}", createDTO.getNom());

        if (paymentMethodRepository.existsByNomIgnoreCase(createDTO.getNom())) {
            throw new BusinessException("Un mode de paiement avec ce nom existe déjà: " + createDTO.getNom());
        }

        if (paymentMethodRepository.existsByCodeIgnoreCase(createDTO.getCode())) {
            throw new BusinessException("Un mode de paiement avec ce code existe déjà: " + createDTO.getCode());
        }

        PaymentMethod paymentMethod = paymentMethodMapper.toEntity(createDTO);
        paymentMethod.setCreatedBy(getCurrentUsername());

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
        log.info("Mode de paiement créé avec succès avec l'ID: {}", savedPaymentMethod.getId());
        return paymentMethodMapper.toDTO(savedPaymentMethod);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentMethodDTO.Response> findAll(Pageable pageable) {
        log.debug("Récupération de tous les modes de paiement - Page: {}, Taille: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return paymentMethodRepository.findAll(pageable)
                .map(paymentMethodMapper::toDTO);
    }


    @Override
    @Transactional(readOnly = true)
    public PaymentMethodDTO.PageResponse findAll(PaymentMethodDTO.SearchFilter filter) {
        log.debug("Recherche des methodes de paiement avec filtre: {}", filter);

        // Construire le Pageable
        Pageable pageable = buildPageable(filter);

        // Rechercher avec filtres
        String searchCriteria = paymentMethodMapper.buildSearchCriteria(filter);

        Page<PaymentMethod> paymentMethodPage = paymentMethodRepository.findWithFilters(
                searchCriteria,
                pageable
        );

        log.debug("Trouvé {} opérations sur {} au total",
                paymentMethodPage.getNumberOfElements(), paymentMethodPage.getTotalElements());

        return paymentMethodMapper.toPageResponse(paymentMethodPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodDTO.Response> findAllActive() {
        log.debug("Récupération de tous les modes de paiement actifs");
        return paymentMethodMapper.toDTOList(paymentMethodRepository.findActivePaymentMethod());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentMethodDTO.Response> findAllActive(Pageable pageable) {
        log.debug("Récupération de tous les modes de paiement actifs avec pagination");
        return paymentMethodRepository.findAllActive(pageable)
                .map(paymentMethodMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodDTO.Response findById(Long id) {
        log.debug("Récupération du mode de paiement avec l'ID: {}", id);
        return paymentMethodMapper.toDTO(
                paymentMethodRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Mode de paiement non trouvé avec l'ID: " + id))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodDTO.Response findByCode(String code) {
        log.debug("Récupération du mode de paiement avec le code: {}", code);
        return paymentMethodMapper.toDTO(
                paymentMethodRepository.findByCodeIgnoreCase(code)
                        .orElseThrow(() -> new ResourceNotFoundException("Mode de paiement non trouvé avec le code: " + code))
        );
    }

    @Override
    public PaymentMethodDTO.Response update(Long id, PaymentMethodDTO.UpdateRequest updateDTO) {
        log.info("Mise à jour du mode de paiement avec l'ID: {}", id);

        PaymentMethod existingPaymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mode de paiement non trouvé avec l'ID: " + id));

        if (!existingPaymentMethod.getNom().equalsIgnoreCase(updateDTO.getNom()) &&
                paymentMethodRepository.existsByNomIgnoreCaseAndIdNot(updateDTO.getNom(), id)) {
            throw new BusinessException("Un autre mode de paiement avec ce nom existe déjà: " + updateDTO.getNom());
        }

        paymentMethodMapper.updateEntityFromDTO(updateDTO, existingPaymentMethod);
        existingPaymentMethod.setUpdatedBy(getCurrentUsername());

        PaymentMethod updatedPaymentMethod = paymentMethodRepository.save(existingPaymentMethod);
        log.info("Mode de paiement mis à jour avec succès: {}", updatedPaymentMethod.getId());
        return paymentMethodMapper.toDTO(updatedPaymentMethod);
    }

    @Override
    public void delete(Long id) {
        log.info("Suppression logique du mode de paiement avec l'ID: {}", id);
        paymentMethodRepository.updateActifStatus(id, false, LocalDateTime.now(), getCurrentUsername());
        paymentMethodRepository.updateActiveStatus(id, false, LocalDateTime.now(), getCurrentUsername());
        log.info("Mode de paiement supprimé logiquement avec succès: {}", id);
    }

    @Override
    public void hardDelete(Long id) {
        log.info("Suppression définitive du mode de paiement avec l'ID: {}", id);
        paymentMethodRepository.deleteById(id);
        log.info("Mode de paiement supprimé définitivement avec succès: {}", id);
    }

    @Override
    public PaymentMethodDTO.Response restore(Long id) {
        log.info("Restauration du mode de paiement avec l'ID: {}", id);
        paymentMethodRepository.updateActifStatus(id, true, LocalDateTime.now(), getCurrentUsername());

        return paymentMethodMapper.toDTO(
                paymentMethodRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Erreur lors de la restauration du mode de paiement"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentMethodDTO.Response> search(String query, Pageable pageable) {
        log.debug("Recherche de modes de paiement avec le terme: '{}'", query);

        if (query == null || query.trim().isEmpty()) {
            return findAll(pageable);
        }

        return paymentMethodRepository.findByQuery(query.trim(), pageable)
                .map(paymentMethodMapper::toDTO);
    }

    @Override
    public PaymentMethodDTO.Response toggleActiveStatus(Long id) {
        log.info("Basculement du statut du mode de paiement avec l'ID: {}", id);

        boolean currentStatus = paymentMethodRepository.findById(id)
                .map(PaymentMethod::isActif)
                .orElseThrow(() -> new ResourceNotFoundException("Mode de paiement non trouvé avec l'ID: " + id));

        paymentMethodRepository.updateActifStatus(id, !currentStatus, LocalDateTime.now(), getCurrentUsername());

        return paymentMethodMapper.toDTO(
                paymentMethodRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Erreur lors de la mise à jour du statut"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNomOrCode(String nom, String code) {
        log.debug("Vérification de l'existence d'un mode de paiement - Nom: '{}', Code: '{}'", nom, code);
        boolean existsByNom = nom != null && paymentMethodRepository.existsByNomIgnoreCase(nom);
        boolean existsByCode = code != null && paymentMethodRepository.existsByCodeIgnoreCase(code);
        return existsByNom || existsByCode;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodDTO.Stats getStats() {
        log.debug("Récupération des statistiques des modes de paiement");
        return PaymentMethodDTO.Stats.builder()
                .totalCount(paymentMethodRepository.count())
                .activeCount(paymentMethodRepository.countActive())
                .inactiveCount(paymentMethodRepository.countInactive())
                .build();
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Méthode utilitaire pour construire un objet Pageable
     */
    private Pageable buildPageable(PaymentMethodDTO.SearchFilter filter) {
        Sort sort = Sort.by(
                filter.getSortDirection().equalsIgnoreCase("desc") ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSortBy()
        );

        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }


}