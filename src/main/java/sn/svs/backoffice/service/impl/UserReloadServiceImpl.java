package sn.svs.backoffice.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.svs.backoffice.domain.User;
import sn.svs.backoffice.repository.UserRepository;
import sn.svs.backoffice.service.UserReloadService;

/**
 * Service utilitaire pour recharger les utilisateurs avec leurs rôles
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserReloadServiceImpl implements UserReloadService {

    private final UserRepository userRepository;

    /**
     * Recharge un utilisateur avec ses rôles depuis la base de données
     */
    @Transactional(readOnly = true)
    public User reloadUserWithRoles(User user) {
        if (user == null || user.getId() == null) {
            log.warn("Tentative de rechargement d'un utilisateur null ou sans ID");
            return user;
        }

        log.debug("Rechargement de l'utilisateur {} avec ses rôles", user.getUsername());

        return userRepository.findByIdWithRoles(user.getId())
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé lors du rechargement: ID {}", user.getId());
                    return new RuntimeException("Utilisateur non trouvé: " + user.getId());
                });
    }

    /**
     * Recharge un utilisateur avec ses rôles par username
     */
    @Transactional(readOnly = true)
    public User reloadUserWithRoles(String username) {
        log.debug("Rechargement de l'utilisateur {} avec ses rôles", username);

        return userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé lors du rechargement: {}", username);
                    return new RuntimeException("Utilisateur non trouvé: " + username);
                });
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
