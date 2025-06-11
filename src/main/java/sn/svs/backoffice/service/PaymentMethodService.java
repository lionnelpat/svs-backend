package sn.svs.backoffice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.svs.backoffice.dto.PaymentMethodDTO;
import sn.svs.backoffice.exceptions.BusinessException;
import sn.svs.backoffice.exceptions.ResourceNotFoundException;
import sn.svs.backoffice.service.impl.PaymentMethodServiceImpl;

import java.util.List;

/**
 * Service interface pour la gestion des modes de paiement
 */
public interface PaymentMethodService {

    /**
     * Crée un nouveau mode de paiement
     */
    PaymentMethodDTO.Response create(PaymentMethodDTO.CreateRequest createDTO) throws BusinessException;

    /**
     * Récupère tous les modes de paiement avec pagination
     */
    Page<PaymentMethodDTO.Response> findAll(Pageable pageable);

    /**
     * Récupère tous les modes de paiement avec pagination
     */
    PaymentMethodDTO.PageResponse findAll(PaymentMethodDTO.SearchFilter filter);

    /**
     * Récupère tous les modes de paiement actifs
     */
    List<PaymentMethodDTO.Response> findAllActive();

    /**
     * Récupère tous les modes de paiement actifs avec pagination
     */
    Page<PaymentMethodDTO.Response> findAllActive(Pageable pageable);

    /**
     * Récupère un mode de paiement par son ID
     */
    PaymentMethodDTO.Response findById(Long id) throws ResourceNotFoundException;

    /**
     * Récupère un mode de paiement par son code
     */
    PaymentMethodDTO.Response findByCode(String code) throws ResourceNotFoundException;

    /**
     * Met à jour un mode de paiement existant
     */
    PaymentMethodDTO.Response update(Long id, PaymentMethodDTO.UpdateRequest updateDTO)
            throws ResourceNotFoundException, BusinessException;

    /**
     * Supprime logiquement un mode de paiement
     */
    void delete(Long id) throws ResourceNotFoundException;

    /**
     * Supprime définitivement un mode de paiement
     */
    void hardDelete(Long id) throws ResourceNotFoundException;

    /**
     * Restaure un mode de paiement supprimé logiquement
     */
    PaymentMethodDTO.Response restore(Long id) throws ResourceNotFoundException;

    /**
     * Recherche des modes de paiement par terme de recherche
     */
    Page<PaymentMethodDTO.Response> search(String query, Pageable pageable);

    /**
     * Active ou désactive un mode de paiement
     */
    PaymentMethodDTO.Response toggleActiveStatus(Long id) throws ResourceNotFoundException;

    /**
     * Vérifie si un mode de paiement existe par nom ou code
     */
    boolean existsByNomOrCode(String nom, String code);

    /**
     * Récupère les statistiques des modes de paiement
     */
    PaymentMethodDTO.Stats getStats();
}