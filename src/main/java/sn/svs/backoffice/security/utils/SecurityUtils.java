// ========== UTILITAIRE DE SÉCURITÉ ==========

package sn.svs.backoffice.security.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import sn.svs.backoffice.domain.Role;
import sn.svs.backoffice.domain.User;

import java.util.Optional;

/**
 * Utilitaire pour obtenir les informations de l'utilisateur connecté
 */
@Component
public class SecurityUtils {

    /**
     * Obtient l'utilisateur actuellement connecté
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return Optional.of((User) authentication.getPrincipal());
        }

        return Optional.empty();
    }

    /**
     * Obtient le nom d'utilisateur actuel
     */
    public Optional<String> getCurrentUsername() {
        return getCurrentUser().map(User::getUsername);
    }

    /**
     * Obtient l'ID de l'utilisateur actuel
     */
    public Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }

    /**
     * Vérifie si l'utilisateur actuel a un rôle spécifique
     */
    public boolean hasRole(String role) {
        return getCurrentUser()
                .map(user -> user.hasRole(Role.RoleName.valueOf("ROLE_" + role)))
                .orElse(false);
    }

    /**
     * Vérifie si l'utilisateur actuel est un administrateur
     */
    public boolean isCurrentUserAdmin() {
        return getCurrentUser()
                .map(User::isAdmin)
                .orElse(false);
    }
}
