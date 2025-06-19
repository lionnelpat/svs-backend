// ========== USER SERVICE INTERFACE ==========
package sn.svs.backoffice.service;

import org.springframework.data.domain.Pageable;
import sn.svs.backoffice.dto.UserDTO;

import java.util.List;

/**
 * Service pour la gestion des utilisateurs
 * SVS - Dakar, Sénégal
 */
public interface UserService {

    /**
     * Liste tous les utilisateurs avec pagination
     */
    UserDTO.PageResponse findAll(Pageable pageable);

    /**
     * Liste tous les utilisateurs actifs (pour les sélecteurs)
     */
    List<UserDTO.Summary> findAllActive();

    /**
     * Recherche des utilisateurs
     */
    UserDTO.PageResponse search(UserDTO.SearchFilter filter, Pageable pageable);

    /**
     * Trouve un utilisateur par ID
     */
    UserDTO.Response findById(Long id);

    /**
     * Crée un nouvel utilisateur
     */
    UserDTO.Response create(UserDTO.CreateRequest request);

    /**
     * Met à jour un utilisateur
     */
    UserDTO.Response update(Long id, UserDTO.UpdateRequest request);

    /**
     * Active un utilisateur
     */
    UserDTO.Response activate(Long id);

    /**
     * Désactive un utilisateur
     */
    UserDTO.Response deactivate(Long id);

    /**
     * Déverrouille un compte utilisateur
     */
    UserDTO.Response unlock(Long id);

    /**
     * Supprime un utilisateur
     */
    void delete(Long id);

    /**
     * Vérifie si un utilisateur peut être supprimé
     */
    boolean canDelete(Long id);
}

