// ========== USER SERVICE IMPLEMENTATION ==========
package sn.svs.backoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.svs.backoffice.domain.Role;
import sn.svs.backoffice.domain.User;
import sn.svs.backoffice.domain.ennumeration.RoleName;
import sn.svs.backoffice.dto.UserDTO;
import sn.svs.backoffice.mapper.UserMapper;
import sn.svs.backoffice.repository.RoleRepository;
import sn.svs.backoffice.repository.UserRepository;
import sn.svs.backoffice.service.UserService;
import sn.svs.backoffice.service.UserWithRolesService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implémentation du service User
 * SVS - Dakar, Sénégal
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserWithRolesService userWithRolesService;

    @Override
    @Transactional(readOnly = true)
    public UserDTO.PageResponse findAll(Pageable pageable) {
        log.debug("Récupération de tous les utilisateurs - Page: {}", pageable.getPageNumber());

        Page<User> usersPage = userWithRolesService.findAllUsersWithRoles(pageable);

        return userMapper.toPageResponse(usersPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO.Summary> findAllActive() {
        log.debug("Récupération de tous les utilisateurs actifs");

        List<User> activeUsers = userRepository.findByIsActiveTrueAndIsEmailVerifiedTrue();
        return userMapper.toSummaryList(activeUsers);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO.PageResponse search(UserDTO.SearchFilter filter, Pageable pageable) {
        log.debug("Recherche d'utilisateurs avec filtre: {}", filter);

        Page<User> usersPage = userWithRolesService.searchUsersWithRoles(filter, pageable);

        return userMapper.toPageResponse(usersPage);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO.Response findById(Long id) {
        log.debug("Récupération de l'utilisateur ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserDTO.Response create(UserDTO.CreateRequest request) {
        log.info("Création d'un nouvel utilisateur: {}", request.getUsername());

        // Vérifier l'unicité du nom d'utilisateur
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé");
        }

        // Vérifier l'unicité de l'email
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Cette adresse email est déjà utilisée");
        }

        // Récupérer les rôles
        Set<Role> roles = roleRepository.findAllById(request.getRoleIds())
                .stream()
                .collect(Collectors.toSet());

        if (roles.size() != request.getRoleIds().size()) {
            throw new IllegalArgumentException("Un ou plusieurs rôles spécifiés n'existent pas");
        }

        // Créer l'utilisateur
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail().toLowerCase());
        user.setRoles(roles);
        user.setCreatedBy("ADMIN");

        User savedUser = userRepository.save(user);

        log.info("Utilisateur créé avec succès: {}", savedUser.getUsername());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserDTO.Response update(Long id, UserDTO.UpdateRequest request) {
        log.info("Mise à jour de l'utilisateur ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        // Vérifier l'unicité du nom d'utilisateur (si modifié)
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
                throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé");
            }
        }

        // Vérifier l'unicité de l'email (si modifié)
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new IllegalArgumentException("Cette adresse email est déjà utilisée");
            }
            user.setEmail(request.getEmail().toLowerCase());
        }

        // Mettre à jour les rôles si fournis
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> newRoles = roleRepository.findAllById(request.getRoleIds())
                    .stream()
                    .collect(Collectors.toSet());

            if (newRoles.size() != request.getRoleIds().size()) {
                throw new IllegalArgumentException("Un ou plusieurs rôles spécifiés n'existent pas");
            }

            user.setRoles(newRoles);
        }

        // Mettre à jour les autres champs
        userMapper.updateEntity(request, user);
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy("ADMIN");

        User updatedUser = userRepository.save(user);

        log.info("Utilisateur mis à jour avec succès: {}", updatedUser.getUsername());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserDTO.Response activate(Long id) {
        log.info("Activation de l'utilisateur ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy("ADMIN");

        User updatedUser = userRepository.save(user);

        log.info("Utilisateur activé avec succès: {}", updatedUser.getUsername());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserDTO.Response deactivate(Long id) {
        log.info("Désactivation de l'utilisateur ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy("ADMIN");

        User updatedUser = userRepository.save(user);

        log.info("Utilisateur désactivé avec succès: {}", updatedUser.getUsername());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserDTO.Response unlock(Long id) {
        log.info("Déverrouillage du compte utilisateur ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        user.resetLoginAttempts(); // Cette méthode reset aussi accountLockedUntil
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy("ADMIN");

        User updatedUser = userRepository.save(user);

        log.info("Compte déverrouillé avec succès pour l'utilisateur: {}", updatedUser.getUsername());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Suppression de l'utilisateur ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        if (!canDelete(id)) {
            throw new IllegalArgumentException("Cet utilisateur ne peut pas être supprimé");
        }

        userRepository.delete(user);

        log.info("Utilisateur supprimé avec succès: {}", user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDelete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        // Ne peut pas supprimer les administrateurs système
        boolean isSystemAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ADMIN);

        // Pour l'instant, on permet la suppression de tous les utilisateurs sauf les admins système
        // Tu peux ajouter d'autres règles métier ici
        return !isSystemAdmin;
    }
}
