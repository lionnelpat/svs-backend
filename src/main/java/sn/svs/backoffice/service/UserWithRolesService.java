package sn.svs.backoffice.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sn.svs.backoffice.domain.Role;
import sn.svs.backoffice.domain.User;
import sn.svs.backoffice.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserWithRolesService {

    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Transactional
    public User loadUserWithRoles(String username) {
        log.debug("Chargement de l'utilisateur {} avec ses rôles", username);

        // Chargement de l'utilisateur seul (sans les rôles pour éviter les problèmes Hibernate)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));

        // Création d'un nouvel objet User détaché pour éviter les problèmes de persistance
        User detachedUser = createDetachedUser(user);

        // Chargement des rôles avec requête native
        List<Role> roles = loadRolesForUser(user.getId());

        // Attribution des rôles au user détaché
        detachedUser.setRoles(new HashSet<>(roles));

        log.debug("Utilisateur {} chargé avec {} rôles", username, roles.size());

        return detachedUser;
    }

    /**
     * Crée une copie détachée de l'utilisateur pour éviter les problèmes Hibernate
     */
    private User createDetachedUser(User originalUser) {
        return User.builder()
                .id(originalUser.getId())
                .username(originalUser.getUsername())
                .email(originalUser.getEmail())
                .password(originalUser.getPassword())
                .firstName(originalUser.getFirstName())
                .lastName(originalUser.getLastName())
                .phone(originalUser.getPhone())
                .isActive(originalUser.getIsActive())
                .isEmailVerified(originalUser.getIsEmailVerified())
                .emailVerificationToken(originalUser.getEmailVerificationToken())
                .passwordResetToken(originalUser.getPasswordResetToken())
                .passwordResetTokenExpiry(originalUser.getPasswordResetTokenExpiry())
                .lastLogin(originalUser.getLastLogin())
                .loginAttempts(originalUser.getLoginAttempts())
                .accountLockedUntil(originalUser.getAccountLockedUntil())
                .createdAt(originalUser.getCreatedAt())
                .updatedAt(originalUser.getUpdatedAt())
                .createdBy(originalUser.getCreatedBy())
                .updatedBy(originalUser.getUpdatedBy())
                .roles(new HashSet<>()) // Sera rempli après
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<Role> loadRolesForUser(Long userId) {
        return entityManager.createNativeQuery(
                        "SELECT r.* FROM roles r " +
                                "INNER JOIN user_roles ur ON r.id = ur.role_id " +
                                "WHERE ur.user_id = ?1", Role.class)
                .setParameter(1, userId)
                .getResultList();
    }
}
