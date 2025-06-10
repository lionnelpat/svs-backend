package sn.svs.backoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.svs.backoffice.domain.ExpenseSupplier;
import sn.svs.backoffice.dto.ExpenseSupplierDTO;
import sn.svs.backoffice.exceptions.DuplicateResourceException;
import sn.svs.backoffice.exceptions.ResourceNotFoundException;
import sn.svs.backoffice.mapper.ExpenseSupplierMapper;
import sn.svs.backoffice.repository.ExpenseSupplierRepository;
import sn.svs.backoffice.service.ExpenseSupplierService;

import java.util.List;

/**
 * Implémentation du service pour la gestion des fournisseurs de dépenses
 * SVS - Dakar, Sénégal
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExpenseSupplierServiceImpl implements ExpenseSupplierService {

    private final ExpenseSupplierRepository supplierRepository;
    private final ExpenseSupplierMapper supplierMapper;

    @Override
    public ExpenseSupplierDTO.Response create(ExpenseSupplierDTO.CreateRequest request) {
        log.info("Création d'un nouveau fournisseur: {}", request.getNom());

        // Vérifier l'unicité du nom (insensible à la casse)
        if (supplierRepository.existsByNomIgnoreCase(request.getNom())) {
            throw new DuplicateResourceException("Un fournisseur avec le nom '" + request.getNom() + "' existe déjà");
        }

        // Vérifier l'unicité de l'email si fourni
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (supplierRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Un fournisseur avec l'email '" + request.getEmail() + "' existe déjà");
            }
        }

        // Vérifier l'unicité du numéro NINEA si fourni
        if (request.getNinea() != null && !request.getNinea().trim().isEmpty()) {
            if (supplierRepository.existsByNinea(request.getNinea())) {
                throw new DuplicateResourceException("Un fournisseur avec le numéro NINEA '" + request.getNinea() + "' existe déjà");
            }
        }

        // Convertir et sauvegarder
        ExpenseSupplier supplier = supplierMapper.toEntity(request);
        ExpenseSupplier savedSupplier = supplierRepository.save(supplier);

        log.info("Fournisseur créé avec succès - ID: {}, Nom: {}", savedSupplier.getId(), savedSupplier.getNom());
        return supplierMapper.toResponse(savedSupplier);
    }

    @Override
    public ExpenseSupplierDTO.Response update(Long id, ExpenseSupplierDTO.UpdateRequest request) {
        log.info("Mise à jour du fournisseur ID: {}", id);

        ExpenseSupplier existingSupplier = findSupplierById(id);

        // Vérifier l'unicité du nom si modifié (insensible à la casse)
        if (request.getNom() != null && !request.getNom().equalsIgnoreCase(existingSupplier.getNom())) {
            if (supplierRepository.existsByNomIgnoreCaseAndIdNot(request.getNom(), id)) {
                throw new DuplicateResourceException("Un fournisseur avec le nom '" + request.getNom() + "' existe déjà");
            }
        }

        // Vérifier l'unicité de l'email si modifié
        if (request.getEmail() != null && !request.getEmail().equals(existingSupplier.getEmail())) {
            if (supplierRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new DuplicateResourceException("Un fournisseur avec l'email '" + request.getEmail() + "' existe déjà");
            }
        }

        // Vérifier l'unicité du numéro NINEA si modifié
        if (request.getNinea() != null && !request.getNinea().equals(existingSupplier.getNinea())) {
            if (supplierRepository.existsByNineaAndIdNot(request.getNinea(), id)) {
                throw new DuplicateResourceException("Un fournisseur avec le numéro NINEA '" + request.getNinea() + "' existe déjà");
            }
        }

        // Mettre à jour les champs
        supplierMapper.updateEntity(request, existingSupplier);
        ExpenseSupplier updatedSupplier = supplierRepository.save(existingSupplier);

        log.info("Fournisseur mis à jour avec succès - ID: {}", updatedSupplier.getId());
        return supplierMapper.toResponse(updatedSupplier);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseSupplierDTO.Response findById(Long id) {
        log.debug("Recherche du fournisseur par ID: {}", id);
        ExpenseSupplier supplier = findSupplierById(id);
        return supplierMapper.toResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseSupplierDTO.Response findByEmail(String email) {
        log.debug("Recherche du fournisseur par email: {}", email);
        ExpenseSupplier supplier = supplierRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseSupplier", "email", email));
        return supplierMapper.toResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseSupplierDTO.Response findByNinea(String numeroNinea) {
        log.debug("Recherche du fournisseur par numéro NINEA: {}", numeroNinea);
        ExpenseSupplier supplier = supplierRepository.findByNinea(numeroNinea)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseSupplier", "numeroNinea", numeroNinea));
        return supplierMapper.toResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseSupplierDTO.PageResponse findAll(ExpenseSupplierDTO.SearchFilter filter) {
        log.debug("Recherche des fournisseurs avec filtre: {}", filter);

        // Construire le Pageable
        Pageable pageable = buildPageable(filter);

        // Rechercher avec filtres
        String searchCriteria = supplierMapper.buildSearchCriteria(filter);

        Page<ExpenseSupplier> suppliersPage = supplierRepository.findWithFilters(
                searchCriteria,
                filter.getActive(),
                pageable
        );


        log.debug("Trouvé {} fournisseurs sur {} au total",
                suppliersPage.getNumberOfElements(), suppliersPage.getTotalElements());

        return supplierMapper.toPageResponse(suppliersPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseSupplierDTO.Summary> findAllActive() {
        log.debug("Recherche de tous les fournisseurs actifs");
        List<ExpenseSupplier> activeSuppliers = supplierRepository.findByActiveTrueOrderByNomAsc();
        return supplierMapper.toSummaryList(activeSuppliers);
    }

    @Override
    public ExpenseSupplierDTO.Response toggleActive(Long id) {
        log.info("Basculement du statut actif pour le fournisseur ID: {}", id);

        ExpenseSupplier supplier = findSupplierById(id);
        supplier.setActive(!supplier.getActive());
        ExpenseSupplier updatedSupplier = supplierRepository.save(supplier);

        log.info("Statut du fournisseur {} changé à: {}", id, updatedSupplier.getActive());
        return supplierMapper.toResponse(updatedSupplier);
    }

    @Override
    public ExpenseSupplierDTO.Response restore(Long id) {
        log.info("Restauration du statut actif pour le fournisseur ID: {}", id);

        ExpenseSupplier supplier = findSupplierById(id);
        supplier.setActive(true);
        ExpenseSupplier updatedSupplier = supplierRepository.save(supplier);

        log.info("Statut du fournisseur {} changé à: {}", id, updatedSupplier.getActive());
        return supplierMapper.toResponse(updatedSupplier);
    }

    @Override
    public void delete(Long id) {
        log.info("Suppression logique du fournisseur ID: {}", id);

        ExpenseSupplier supplier = findSupplierById(id);
        supplier.setActive(false);
        supplierRepository.save(supplier);

        log.info("Fournisseur {} désactivé avec succès", id);
    }

    @Override
    public void hardDelete(Long id) {
        log.info("Suppression définitive du fournisseur ID: {}", id);

        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("ExpenseSupplier", "id", id);
        }

        supplierRepository.deleteById(id);
        log.info("Fournisseur {} supprimé définitivement", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return supplierRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return supplierRepository.existsByEmailAndIdNot(email, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNinea(String numeroNinea) {
        return supplierRepository.existsByNinea(numeroNinea);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNineaAndIdNot(String numeroNinea, Long id) {
        return supplierRepository.existsByNineaAndIdNot(numeroNinea, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNom(String nom) {
        return supplierRepository.existsByNomIgnoreCase(nom);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNomAndIdNot(String nom, Long id) {
        return supplierRepository.existsByNomIgnoreCaseAndIdNot(nom, id);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierStatsDTO getStats() {
        log.debug("Calcul des statistiques des fournisseurs");

        long totalSuppliers = supplierRepository.count();
        long activeSuppliers = supplierRepository.countByActive(true);
        long inactiveSuppliers = supplierRepository.countByActive(false);
        double activePercentage = totalSuppliers > 0 ? (double) activeSuppliers / totalSuppliers * 100 : 0;

        // Statistiques supplémentaires
        long suppliersWithEmail = supplierRepository.findAll().stream()
                .filter(s -> s.getEmail() != null && !s.getEmail().trim().isEmpty())
                .count();

        long suppliersWithNinea = supplierRepository.findAll().stream()
                .filter(s -> s.getNinea() != null && !s.getNinea().trim().isEmpty())
                .count();

        return new SupplierStatsDTO(
                totalSuppliers,
                activeSuppliers,
                inactiveSuppliers,
                activePercentage,
                suppliersWithEmail,
                suppliersWithNinea
        );
    }

    /**
     * Méthode utilitaire pour trouver un fournisseur par ID avec gestion d'exception
     */
    private ExpenseSupplier findSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseSupplier", "id", id));
    }

    /**
     * Méthode utilitaire pour construire un objet Pageable
     */
    private Pageable buildPageable(ExpenseSupplierDTO.SearchFilter filter) {
        Sort sort = Sort.by(
                filter.getSortDirection().equalsIgnoreCase("desc") ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSortBy()
        );

        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }
}
