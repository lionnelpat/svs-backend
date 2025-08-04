package sn.svs.backoffice.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.svs.backoffice.domain.User;
import sn.svs.backoffice.repository.UserRepository;
import sn.svs.backoffice.service.UserReloadService;
import sn.svs.backoffice.service.UserWithRolesService;

/**
 * Service utilitaire pour recharger les utilisateurs avec leurs rôles
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserReloadServiceImpl implements UserReloadService {

    private final UserWithRolesService userWithRolesService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User reloadUserWithRoles(User user) {
        if (user == null || user.getUsername() == null) {
            log.warn("Tentative de rechargement d'un utilisateur null ou sans username");
            return user;
        }

        log.debug("Rechargement de l'utilisateur {} avec ses rôles", user.getUsername());
        return userWithRolesService.loadUserWithRoles(user.getUsername());
    }

    @Transactional(readOnly = true)
    public User reloadUserWithRoles(String username) {
        log.debug("Rechargement de l'utilisateur {} avec ses rôles", username);
        return userWithRolesService.loadUserWithRoles(username);
    }

    /**
     * Recharge un utilisateur avec ses rôles par ID
     */
    @Transactional(readOnly = true)
    public User reloadUserWithRoles(Long userId) {
        log.debug("Rechargement de l'utilisateur ID {} avec ses rôles", userId);

        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé lors du rechargement: ID {}", userId);
                    return new RuntimeException("Utilisateur non trouvé: " + userId);
                });
    }
}
