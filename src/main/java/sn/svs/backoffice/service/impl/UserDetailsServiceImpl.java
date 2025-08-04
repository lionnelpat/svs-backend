// ========== IMPLÉMENTATION USERDETAILSSERVICEIMPL ==========
package sn.svs.backoffice.service.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sn.svs.backoffice.domain.Role;
import sn.svs.backoffice.domain.User;
import sn.svs.backoffice.exceptions.AccountLockedException;
import sn.svs.backoffice.exceptions.DisabledUserException;
import sn.svs.backoffice.exceptions.EmailNotVerifiedException;
import sn.svs.backoffice.repository.UserRepository;
import sn.svs.backoffice.service.UserDetailsService;
import sn.svs.backoffice.service.UserWithRolesService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation du service UserDetails personnalisé
 * Charge les détails de l'utilisateur depuis la base de données pour Spring Security
 *
 * Cette classe est utilisée par :
 * - Le processus d'authentification JWT
 * - Spring Security pour valider les credentials
 * - Le filtre JWT pour récupérer les informations utilisateur
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final UserWithRolesService userWithRolesService;

    /**
     * Charge un utilisateur par son nom d'utilisateur (username ou email)
     * Méthode principale appelée par Spring Security
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Chargement de l'utilisateur: {}", username);

        // Utilisation du service dédié qui retourne un objet détaché
        User user = userWithRolesService.loadUserWithRoles(username);

        log.debug("Utilisateur trouvé: {} avec {} rôle(s)",
                user.getUsername(),
                user.getRoles() != null ? user.getRoles().size() : 0);

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            user.getRoles().forEach(role ->
                    log.debug("Role chargé: {}", role.getName()));
        }

        return user;
    }

    /**
     * Charge un utilisateur par son ID (méthode utilitaire)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("Chargement de l'utilisateur par ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé avec l'ID: {}", userId);
                    return new UsernameNotFoundException(
                            String.format("Utilisateur non trouvé avec l'ID: %s", userId)
                    );
                });

        validateUserAccount(user);

        log.debug("Utilisateur ID {} chargé avec succès", userId);
        return user;
    }

    /**
     * Vérifie si un utilisateur existe par username ou email
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsernameOrEmail(String identifier) {
        boolean exists = userRepository.findByUsernameIgnoreCase(identifier).isPresent() ||
                userRepository.findByEmailIgnoreCase(identifier).isPresent();

        log.debug("Vérification d'existence pour '{}': {}", identifier, exists);
        return exists;
    }

    /**
     * Met à jour la dernière connexion de l'utilisateur
     */
    @Override
    @Transactional
    public void updateLastLogin(String username) {
        try {
            // Requête directe pour éviter les problèmes de relations
            userRepository.updateLastLoginByUsername(username, LocalDateTime.now());
            log.debug("Dernière connexion mise à jour pour l'utilisateur: {}", username);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la dernière connexion pour {}: {}", username, e.getMessage());
            throw e;
        }
    }

    /**
     * Incrémente le compteur de tentatives de connexion échouées
     */
    @Override
    @Transactional
    public void incrementFailedAttempts(String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(username)
                    .or(() -> userRepository.findByEmailIgnoreCase(username));

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.incrementLoginAttempts();

                // Verrouiller le compte après 5 tentatives échouées
                if (user.getLoginAttempts() >= 5) {
                    user.lockAccount(30); // Verrouillage pendant 30 minutes
                    log.warn("Compte verrouillé pour 30 minutes après {} tentatives échouées: {}",
                            user.getLoginAttempts(), username);
                }

                userRepository.save(user);

                log.debug("Tentatives de connexion échouées incrémentées pour {}: {}",
                        username, user.getLoginAttempts());
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'incrémentation des tentatives échouées pour {}: {}",
                    username, e.getMessage());
        }
    }

    /**
     * Réinitialise les tentatives de connexion échouées
     */
    @Override
    @Transactional
    public void resetFailedAttempts(String username) {
        try {
            User user = findUserByUsernameOrEmail(username);
            user.resetLoginAttempts();
            userRepository.save(user);

            log.debug("Tentatives de connexion échouées réinitialisées pour: {}", username);
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation des tentatives échouées pour {}: {}",
                    username, e.getMessage());
        }
    }

    /**
     * Vérifie si un compte utilisateur est valide
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isAccountValid(String username) {
        try {
            User user = findUserByUsernameOrEmail(username);
            return user.getIsActive() &&
                    user.getIsEmailVerified() &&
                    (user.getAccountLockedUntil() == null ||
                            user.getAccountLockedUntil().isBefore(LocalDateTime.now()));
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    // ========== MÉTHODES PRIVÉES ==========

    /**
     * Recherche l'utilisateur par username ou email
     */
    private User findUserByUsernameOrEmail(String identifier) {
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + identifier));
    }



    /**
     * Valide l'état du compte utilisateur
     */
    private void validateUserAccount(User user) {
        // Vérifier si le compte est actif
        if (!user.getIsActive()) {
            log.warn("Tentative de connexion sur un compte désactivé: {}", user.getUsername());
            throw new DisabledUserException(
                    String.format("Le compte utilisateur '%s' est désactivé", user.getUsername())
            );
        }

        // Vérifier si l'email est vérifié
        if (!user.getIsEmailVerified()) {
            log.warn("Tentative de connexion avec email non vérifié: {}", user.getUsername());
            throw new EmailNotVerifiedException(
                    String.format("L'email de l'utilisateur '%s' n'est pas vérifié", user.getUsername())
            );
        }

        // Vérifier si le compte n'est pas verrouillé
        if (user.getAccountLockedUntil() != null &&
                user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {

            log.warn("Tentative de connexion sur un compte verrouillé: {} (verrouillé jusqu'à: {})",
                    user.getUsername(), user.getAccountLockedUntil());

            throw new AccountLockedException(
                    String.format("Le compte utilisateur '%s' est temporairement verrouillé jusqu'à %s",
                            user.getUsername(), user.getAccountLockedUntil())
            );
        }

        // Si le compte était verrouillé mais que la période est expirée, le déverrouiller
        if (user.getAccountLockedUntil() != null &&
                user.getAccountLockedUntil().isBefore(LocalDateTime.now())) {

            log.info("Déverrouillage automatique du compte: {}", user.getUsername());
            user.resetLoginAttempts(); // Cette méthode reset aussi accountLockedUntil
            userRepository.save(user);
        }
    }
}
