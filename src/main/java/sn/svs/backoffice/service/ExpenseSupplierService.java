package sn.svs.backoffice.service;

import sn.svs.backoffice.dto.ExpenseSupplierDTO;

import java.util.List;

/**
 * Service pour la gestion des fournisseurs de dépenses
 * SVS - Dakar, Sénégal
 */
public interface ExpenseSupplierService {

    /**
     * Crée un nouveau fournisseur
     */
    ExpenseSupplierDTO.Response create(ExpenseSupplierDTO.CreateRequest request);

    /**
     * Met à jour un fournisseur existant
     */
    ExpenseSupplierDTO.Response update(Long id, ExpenseSupplierDTO.UpdateRequest request);

    /**
     * Trouve un fournisseur par son ID
     */
    ExpenseSupplierDTO.Response findById(Long id);

    /**
     * Trouve un fournisseur par son email
     */
    ExpenseSupplierDTO.Response findByEmail(String email);

    /**
     * Trouve un fournisseur par son numéro NINEA
     */
    ExpenseSupplierDTO.Response findByNinea(String numeroNinea);

    /**
     * Trouve tous les fournisseurs avec pagination et filtres
     */
    ExpenseSupplierDTO.PageResponse findAll(ExpenseSupplierDTO.SearchFilter filter);

    /**
     * Trouve tous les fournisseurs actifs (pour les listes déroulantes)
     */
    List<ExpenseSupplierDTO.Summary> findAllActive();

    /**
     * Active/désactive un fournisseur
     */
    ExpenseSupplierDTO.Response toggleActive(Long id);

    /**
     * Supprime un fournisseur (suppression logique)
     */
    void delete(Long id);

    /**
     * Supprime définitivement un fournisseur
     */
    void hardDelete(Long id);

    /**
     * Vérifie si un fournisseur existe par son email
     */
    boolean existsByEmail(String email);

    /**
     * Vérifie si un fournisseur existe par son email (en excluant un ID)
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Vérifie si un fournisseur existe par son numéro NINEA
     */
    boolean existsByNinea(String numeroNinea);

    /**
     * Vérifie si un fournisseur existe par son numéro NINEA (en excluant un ID)
     */
    boolean existsByNineaAndIdNot(String numeroNinea, Long id);

    /**
     * Vérifie si un fournisseur existe par son nom (insensible à la casse)
     */
    boolean existsByNom(String nom);

    /**
     * Vérifie si un fournisseur existe par son nom (en excluant un ID)
     */
    boolean existsByNomAndIdNot(String nom, Long id);

    /**
     * Obtient les statistiques des fournisseurs
     */
    SupplierStatsDTO getStats();

    ExpenseSupplierDTO.Response restore(Long id);

    /**
     * Classe interne pour les statistiques
     */
    record SupplierStatsDTO(
            long totalSuppliers,
            long activeSuppliers,
            long inactiveSuppliers,
            double activePercentage,
            long suppliersWithEmail,
            long suppliersWithNinea
    ){}
}