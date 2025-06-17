// ========== ROLE SERVICE INTERFACE ==========
package sn.svs.backoffice.service;

import org.springframework.data.domain.Pageable;
import sn.svs.backoffice.dto.RoleDTO;

import java.util.List;

/**
 * Service pour la gestion des rôles
 * SVS - Dakar, Sénégal
 */
public interface RoleService {

    /**
     * Liste tous les rôles avec pagination
     */
    RoleDTO.PageResponse findAll(Pageable pageable);

    /**
     * Liste tous les rôles actifs (pour les sélecteurs)
     */
    List<RoleDTO.Summary> findAllActive();

    /**
     * Recherche des rôles
     */
    RoleDTO.PageResponse search(RoleDTO.SearchFilter filter, Pageable pageable);

    /**
     * Trouve un rôle par ID
     */
    RoleDTO.Response findById(Long id);

    /**
     * Crée un nouveau rôle
     */
    RoleDTO.Response create(RoleDTO.CreateRequest request);

    /**
     * Met à jour un rôle
     */
    RoleDTO.Response update(Long id, RoleDTO.UpdateRequest request);

    /**
     * Active un rôle
     */
    RoleDTO.Response activate(Long id);

    /**
     * Désactive un rôle
     */
    RoleDTO.Response deactivate(Long id);

    /**
     * Supprime un rôle
     */
    void delete(Long id);

    /**
     * Vérifie si un rôle peut être supprimé
     */
    boolean canDelete(Long id);
}
