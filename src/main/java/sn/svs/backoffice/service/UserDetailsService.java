package sn.svs.backoffice.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Interface personnalisée pour le service UserDetails
 * Étend l'interface Spring Security avec des méthodes métier spécifiques
 */
public interface UserDetailsService extends org.springframework.security.core.userdetails.UserDetailsService {

    /**
     * Charge un utilisateur par son nom d'utilisateur (username ou email)
     * Héritée de l'interface Spring Security
     */
    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    /**
     * Charge un utilisateur par son ID
     */
    UserDetails loadUserById(Long userId) throws UsernameNotFoundException;

    /**
     * Vérifie si un utilisateur existe par username ou email
     */
    boolean existsByUsernameOrEmail(String identifier);

    /**
     * Met à jour la dernière connexion de l'utilisateur
     */
    void updateLastLogin(String username);

    /**
     * Incrémente le compteur de tentatives de connexion échouées
     */
    void incrementFailedAttempts(String username);

    /**
     * Réinitialise les tentatives de connexion échouées
     */
    void resetFailedAttempts(String username);

    /**
     * Vérifie si un compte utilisateur est valide (actif, email vérifié, non verrouillé)
     */
    boolean isAccountValid(String username);
}
