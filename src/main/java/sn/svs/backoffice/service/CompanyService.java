package sn.svs.backoffice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.svs.backoffice.domain.Company;
import sn.svs.backoffice.dto.CompanyDTO;

import java.util.List;
import java.util.Optional;

/**
 * Interface du service Company
 * SVS - Dakar, Sénégal
 */
public interface CompanyService {

    /**
     * Créer une nouvelle compagnie
     *
     * @param createRequest Les données de création
     * @return La compagnie créée
     */
    CompanyDTO.Response createCompany(CompanyDTO.CreateRequest createRequest);

    /**
     * Mettre à jour une compagnie existante
     *
     * @param id L'identifiant de la compagnie
     * @param updateRequest Les données de mise à jour
     * @return La compagnie mise à jour
     */
    CompanyDTO.Response updateCompany(Long id, CompanyDTO.UpdateRequest updateRequest);

    /**
     * Récupérer une compagnie par son identifiant
     *
     * @param id L'identifiant de la compagnie
     * @return La compagnie trouvée
     */
    CompanyDTO.Response getCompanyById(Long id);

    /**
     * Récupérer toutes les compagnies avec pagination
     *
     * @param pageable Les paramètres de pagination
     * @return Page des compagnies
     */
    CompanyDTO.PageResponse getAllCompanies(Pageable pageable);

    /**
     * Rechercher des compagnies avec filtres
     *
     * @param filter Les filtres de recherche
     * @return Page des compagnies filtrées
     */
    CompanyDTO.PageResponse searchCompanies(CompanyDTO.SearchFilter filter);

    /**
     * Supprimer une compagnie (suppression logique)
     *
     * @param id L'identifiant de la compagnie
     */
    void deleteCompany(Long id);

    /**
     * Activer une compagnie
     *
     * @param id L'identifiant de la compagnie
     * @return La compagnie activée
     */
    CompanyDTO.Response activateCompany(Long id);

    /**
     * Désactiver une compagnie
     *
     * @param id L'identifiant de la compagnie
     * @return La compagnie désactivée
     */
    CompanyDTO.Response deactivateCompany(Long id);

    /**
     * Récupérer toutes les compagnies actives
     *
     * @return Liste des compagnies actives
     */
    List<CompanyDTO.Response> getActiveCompanies();

    /**
     * Vérifier si une compagnie existe
     *
     * @param id L'identifiant de la compagnie
     * @return true si la compagnie existe
     */
    boolean existsById(Long id);

    /**
     * Rechercher une compagnie par email
     *
     * @param email L'email de la compagnie
     * @return La compagnie trouvée
     */
    Optional<CompanyDTO.Response> findByEmail(String email);

    /**
     * Rechercher une compagnie par RCCM
     *
     * @param rccm Le numéro RCCM
     * @return La compagnie trouvée
     */
    Optional<CompanyDTO.Response> findByRccm(String rccm);

    /**
     * Rechercher une compagnie par NINEA
     *
     * @param ninea Le numéro NINEA
     * @return La compagnie trouvée
     */
    Optional<CompanyDTO.Response> findByNinea(String ninea);

    /**
     * Obtenir les statistiques des compagnies par pays
     *
     * @return Map avec pays et nombre de compagnies
     */
    List<Object[]> getCompanyStatisticsByCountry();

    /**
     * Obtenir les statistiques des compagnies par ville pour un pays donné
     *
     * @param pays Le pays
     * @return Map avec ville et nombre de compagnies
     */
    List<Object[]> getCompanyStatisticsByCity(String pays);

    /**
     * Valider l'unicité des champs uniques
     *
     * @param email L'email à valider
     * @param rccm Le RCCM à valider
     * @param ninea Le NINEA à valider
     * @param excludeId L'ID à exclure de la validation (pour les mises à jour)
     */
    void validateUniqueFields(String email, String rccm, String ninea, Long excludeId);
}
