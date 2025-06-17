// ========== ROLE SERVICE IMPLEMENTATION ==========
package sn.svs.backoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.svs.backoffice.domain.Role;
import sn.svs.backoffice.dto.RoleDTO;
import sn.svs.backoffice.mapper.RoleMapper;
import sn.svs.backoffice.repository.RoleRepository;
import sn.svs.backoffice.service.RoleService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation du service Role
 * SVS - Dakar, Sénégal
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public RoleDTO.PageResponse findAll(Pageable pageable) {
        log.debug("Récupération de tous les rôles - Page: {}", pageable.getPageNumber());

        Page<Role> rolesPage = roleRepository.findAll(pageable);
        RoleDTO.PageResponse response = roleMapper.toPageResponse(rolesPage);

        // Ajouter le compteur d'utilisateurs pour chaque rôle
        response.getRoles().forEach(role -> {
            Long userCount = roleRepository.countUsersWithRole(role.getName());
            role.setUserCount(userCount);
        });

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO.Summary> findAllActive() {
        log.debug("Récupération de tous les rôles actifs");

        List<Role> activeRoles = roleRepository.findByIsActiveTrue();
        return roleMapper.toSummaryList(activeRoles);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO.PageResponse search(RoleDTO.SearchFilter filter, Pageable pageable) {
        log.debug("Recherche de rôles avec filtre: {}", filter);

        Page<Role> rolesPage;

        if (filter.getIsActive() != null) {
            rolesPage = roleRepository.findByIsActive(filter.getIsActive(), pageable);
        } else {
            rolesPage = roleRepository.findAll(pageable);
        }

        RoleDTO.PageResponse response = roleMapper.toPageResponse(rolesPage);

        // Filtrer par recherche textuelle si nécessaire
        if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
            String searchTerm = filter.getSearch().toLowerCase();
            response.getRoles().removeIf(role ->
                    !role.getName().name().toLowerCase().contains(searchTerm) &&
                            (role.getDescription() == null || !role.getDescription().toLowerCase().contains(searchTerm))
            );
        }

        // Ajouter le compteur d'utilisateurs
        response.getRoles().forEach(role -> {
            Long userCount = roleRepository.countUsersWithRole(role.getName());
            role.setUserCount(userCount);
        });

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO.Response findById(Long id) {
        log.debug("Récupération du rôle ID: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + id));

        RoleDTO.Response response = roleMapper.toResponse(role);

        // Ajouter le compteur d'utilisateurs
        Long userCount = roleRepository.countUsersWithRole(role.getName());
        response.setUserCount(userCount);

        return response;
    }

    @Override
    @Transactional
    public RoleDTO.Response create(RoleDTO.CreateRequest request) {
        log.info("Création d'un nouveau rôle: {}", request.getName());

        // Vérifier l'unicité du nom de rôle
        if (roleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Ce nom de rôle existe déjà");
        }

        Role role = roleMapper.toEntity(request);
        Role savedRole = roleRepository.save(role);

        log.info("Rôle créé avec succès: {}", savedRole.getName());

        RoleDTO.Response response = roleMapper.toResponse(savedRole);
        response.setUserCount(0L);

        return response;
    }

    @Override
    @Transactional
    public RoleDTO.Response update(Long id, RoleDTO.UpdateRequest request) {
        log.info("Mise à jour du rôle ID: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + id));

        // Protection des rôles système
        if (isSystemRole(role)) {
            throw new IllegalArgumentException("Les rôles système ne peuvent pas être modifiés");
        }

        roleMapper.updateEntity(request, role);
        role.setUpdatedAt(LocalDateTime.now());

        Role updatedRole = roleRepository.save(role);

        log.info("Rôle mis à jour avec succès: {}", updatedRole.getName());

        RoleDTO.Response response = roleMapper.toResponse(updatedRole);
        Long userCount = roleRepository.countUsersWithRole(updatedRole.getName());
        response.setUserCount(userCount);

        return response;
    }

    @Override
    @Transactional
    public RoleDTO.Response activate(Long id) {
        log.info("Activation du rôle ID: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + id));

        role.setIsActive(true);
        role.setUpdatedAt(LocalDateTime.now());

        Role updatedRole = roleRepository.save(role);

        log.info("Rôle activé avec succès: {}", updatedRole.getName());

        RoleDTO.Response response = roleMapper.toResponse(updatedRole);
        Long userCount = roleRepository.countUsersWithRole(updatedRole.getName());
        response.setUserCount(userCount);

        return response;
    }

    @Override
    @Transactional
    public RoleDTO.Response deactivate(Long id) {
        log.info("Désactivation du rôle ID: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + id));

        // Protection des rôles système
        if (isSystemRole(role)) {
            throw new IllegalArgumentException("Les rôles système ne peuvent pas être désactivés");
        }

        role.setIsActive(false);
        role.setUpdatedAt(LocalDateTime.now());

        Role updatedRole = roleRepository.save(role);

        log.info("Rôle désactivé avec succès: {}", updatedRole.getName());

        RoleDTO.Response response = roleMapper.toResponse(updatedRole);
        Long userCount = roleRepository.countUsersWithRole(updatedRole.getName());
        response.setUserCount(userCount);

        return response;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Suppression du rôle ID: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + id));

        if (!canDelete(id)) {
            throw new IllegalArgumentException("Ce rôle ne peut pas être supprimé");
        }

        roleRepository.delete(role);

        log.info("Rôle supprimé avec succès: {}", role.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDelete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID: " + id));

        // Ne peut pas supprimer les rôles système
        if (isSystemRole(role)) {
            return false;
        }

        // Ne peut pas supprimer un rôle qui a des utilisateurs
        Long userCount = roleRepository.countUsersWithRole(role.getName());
        return userCount == 0;
    }

    // ========== MÉTHODES PRIVÉES ==========

    /**
     * Vérifie si un rôle est un rôle système
     */
    private boolean isSystemRole(Role role) {
        return role.getName() == Role.RoleName.ROLE_ADMIN ||
                role.getName() == Role.RoleName.ROLE_MANAGER ||
                role.getName() == Role.RoleName.ROLE_USER;
    }
}

