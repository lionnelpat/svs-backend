package sn.svs.backoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sn.svs.backoffice.domain.Company;
import sn.svs.backoffice.dto.CompanyDTO;
import sn.svs.backoffice.exceptions.DuplicateResourceException;
import sn.svs.backoffice.exceptions.ResourceNotFoundException;
import sn.svs.backoffice.exceptions.ValidationException;
import sn.svs.backoffice.mapper.CompanyMapper;
import sn.svs.backoffice.repository.CompanyRepository;
import sn.svs.backoffice.service.CompanyService;

import java.util.List;
import java.util.Optional;

/**
 * Implémentation du service Company
 * SVS - Dakar, Sénégal
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @Override
    @Transactional
    public CompanyDTO.Response createCompany(CompanyDTO.CreateRequest createRequest) {
        log.info("Création d'une nouvelle compagnie: {}", createRequest.getNom());

        // Validation des champs uniques
        validateUniqueFields(createRequest.getEmail(), createRequest.getRccm(),
                createRequest.getNinea(), null);

        // Mapping et sauvegarde
        Company company = companyMapper.toEntity(createRequest);
        Company savedCompany = companyRepository.save(company);

        log.info("Compagnie créée avec succès - ID: {}, Nom: {}",
                savedCompany.getId(), savedCompany.getNom());

        return companyMapper.toResponse(savedCompany);
    }

    @Override
    @Transactional
    public CompanyDTO.Response updateCompany(Long id, CompanyDTO.UpdateRequest updateRequest) {
        log.info("Mise à jour de la compagnie ID: {}", id);

        Company existingCompany = findCompanyById(id);

        // Validation des champs uniques si modifiés
        if (StringUtils.hasText(updateRequest.getEmail()) ||
                StringUtils.hasText(updateRequest.getRccm()) ||
                StringUtils.hasText(updateRequest.getNinea())) {

            validateUniqueFields(updateRequest.getEmail(), updateRequest.getRccm(),
                    updateRequest.getNinea(), id);
        }

        // Mapping des modifications
        companyMapper.updateEntityFromDto(updateRequest, existingCompany);

        Company updatedCompany = companyRepository.save(existingCompany);

        log.info("Compagnie mise à jour avec succès - ID: {}, Nom: {}",
                updatedCompany.getId(), updatedCompany.getNom());

        return companyMapper.toResponse(updatedCompany);
    }

    @Override
    public CompanyDTO.Response getCompanyById(Long id) {
        log.debug("Recherche de la compagnie ID: {}", id);

        Company company = findCompanyById(id);
        return companyMapper.toResponse(company);
    }

    @Override
    public CompanyDTO.PageResponse getAllCompanies(Pageable pageable) {
        log.debug("Récupération de toutes les compagnies - Page: {}, Taille: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Company> companies = companyRepository.findAll(pageable);

        log.debug("Trouvé {} compagnies sur {} au total",
                companies.getNumberOfElements(), companies.getTotalElements());

        return companyMapper.toPageResponse(companies);
    }

    @Override
    public CompanyDTO.PageResponse searchCompanies(CompanyDTO.SearchFilter filter) {
        log.debug("Recherche de compagnies avec filtres: {}", filter);

        // Création du Pageable avec tri
        Sort sort = createSort(filter.getSortBy(), filter.getSortDirection());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // Recherche avec filtres
        Page<Company> companies = companyRepository.findWithFilters(
                filter.getSearch(),
                filter.getPays(),
                filter.getActive(),
                pageable
        );

        log.debug("Recherche terminée - {} résultats trouvés", companies.getTotalElements());

        return companyMapper.toPageResponse(companies);
    }

    @Override
    @Transactional
    public void deleteCompany(Long id) {
        log.info("Suppression logique de la compagnie ID: {}", id);

        Company company = findCompanyById(id);

        // Vérification des contraintes métier
        // TODO: Vérifier s'il y a des navires ou factures liées

        company.softDelete();
        companyRepository.save(company);

        log.info("Compagnie supprimée logiquement - ID: {}, Nom: {}",
                company.getId(), company.getNom());
    }

    @Override
    @Transactional
    public CompanyDTO.Response activateCompany(Long id) {
        log.info("Activation de la compagnie ID: {}", id);

        Company company = findCompanyById(id);
        company.activate();
        Company activatedCompany = companyRepository.save(company);

        log.info("Compagnie activée - ID: {}, Nom: {}",
                activatedCompany.getId(), activatedCompany.getNom());

        return companyMapper.toResponse(activatedCompany);
    }

    @Override
    @Transactional
    public CompanyDTO.Response deactivateCompany(Long id) {
        log.info("Désactivation de la compagnie ID: {}", id);

        Company company = findCompanyById(id);
        company.deactivate();
        Company deactivatedCompany = companyRepository.save(company);

        log.info("Compagnie désactivée - ID: {}, Nom: {}",
                deactivatedCompany.getId(), deactivatedCompany.getNom());

        return companyMapper.toResponse(deactivatedCompany);
    }

    @Override
    public List<CompanyDTO.Response> getActiveCompanies() {
        log.debug("Récupération de toutes les compagnies actives");

        List<Company> activeCompanies = companyRepository.findAllActiveCompanies();

        log.debug("Trouvé {} compagnies actives", activeCompanies.size());

        return companyMapper.toResponseList(activeCompanies);
    }

    @Override
    public boolean existsById(Long id) {
        log.debug("Vérification de l'existence de la compagnie ID: {}", id);

        boolean exists = companyRepository.existsById(id);
        log.debug("Compagnie ID: {} existe: {}", id, exists);

        return exists;
    }

    @Override
    public Optional<CompanyDTO.Response> findByEmail(String email) {
        log.debug("Recherche de compagnie par email: {}", email);

        Optional<Company> company = companyRepository.findByEmailIgnoreCase(email);
        return company.map(companyMapper::toResponse);
    }

    @Override
    public Optional<CompanyDTO.Response> findByRccm(String rccm) {
        log.debug("Recherche de compagnie par RCCM: {}", rccm);

        Optional<Company> company = companyRepository.findByRccmIgnoreCase(rccm);
        return company.map(companyMapper::toResponse);
    }

    @Override
    public Optional<CompanyDTO.Response> findByNinea(String ninea) {
        log.debug("Recherche de compagnie par NINEA: {}", ninea);

        Optional<Company> company = companyRepository.findByNineaIgnoreCase(ninea);
        return company.map(companyMapper::toResponse);
    }

    @Override
    public List<Object[]> getCompanyStatisticsByCountry() {
        log.debug("Récupération des statistiques par pays");

        return companyRepository.getCompanyStatisticsByCountry();
    }

    @Override
    public List<Object[]> getCompanyStatisticsByCity(String pays) {
        log.debug("Récupération des statistiques par ville pour le pays: {}", pays);

        return companyRepository.getCompanyStatisticsByCity(pays);
    }

    @Override
    public void validateUniqueFields(String email, String rccm, String ninea, Long excludeId) {
        log.debug("Validation des champs uniques - Email: {}, RCCM: {}, NINEA: {}, ExcludeId: {}",
                email, rccm, ninea, excludeId);

        // Validation de l'email
        if (StringUtils.hasText(email)) {
            boolean emailExists = excludeId != null ?
                    companyRepository.existsByEmailIgnoreCaseAndIdNot(email, excludeId) :
                    companyRepository.findByEmailIgnoreCase(email).isPresent();

            if (emailExists) {
                log.warn("Tentative de création/modification avec un email déjà existant: {}", email);
                throw new DuplicateResourceException("Compagnie", "email", email);
            }
        }

        // Validation du RCCM
        if (StringUtils.hasText(rccm)) {
            boolean rccmExists = excludeId != null ?
                    companyRepository.existsByRccmIgnoreCaseAndIdNot(rccm, excludeId) :
                    companyRepository.findByRccmIgnoreCase(rccm).isPresent();

            if (rccmExists) {
                log.warn("Tentative de création/modification avec un RCCM déjà existant: {}", rccm);
                throw new DuplicateResourceException("Compagnie", "RCCM", rccm);
            }
        }

        // Validation du NINEA
        if (StringUtils.hasText(ninea)) {
            boolean nineaExists = excludeId != null ?
                    companyRepository.existsByNineaIgnoreCaseAndIdNot(ninea, excludeId) :
                    companyRepository.findByNineaIgnoreCase(ninea).isPresent();

            if (nineaExists) {
                log.warn("Tentative de création/modification avec un NINEA déjà existant: {}", ninea);
                throw new DuplicateResourceException("Compagnie", "NINEA", ninea);
            }
        }

        log.debug("Validation des champs uniques réussie");
    }

    /**
     * Méthode utilitaire pour trouver une compagnie par ID
     */
    private Company findCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Compagnie non trouvée avec l'ID: {}", id);
                    return new ResourceNotFoundException("Compagnie", id);
                });
    }

    /**
     * Créer un objet Sort basé sur les paramètres de tri
     */
    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        // Validation du champ de tri
        String validSortBy = validateSortField(sortBy);

        return Sort.by(direction, validSortBy);
    }

    /**
     * Valider le champ de tri
     */
    private String validateSortField(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "id";
        }

        // Liste des champs autorisés pour le tri
        List<String> allowedSortFields = List.of(
                "id", "nom", "raisonSociale", "ville", "pays",
                "email", "telephone", "createdAt", "updatedAt", "active"
        );

        if (allowedSortFields.contains(sortBy)) {
            return sortBy;
        }

        log.warn("Champ de tri non autorisé: {}, utilisation de 'id' par défaut", sortBy);
        return "id";
    }
}
