package sn.svs.backoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import sn.svs.backoffice.service.UserDetailsService;

/**
 * Écouteur d'événements d'authentification
 * Gère automatiquement les tentatives de connexion réussies et échouées
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationEventListener {

    private final UserDetailsService userDetailsService;

    /**
     * Gère les événements de connexion réussie
     */
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        log.info("Connexion réussie pour l'utilisateur: {}", username);

        // Mettre à jour la dernière connexion et réinitialiser les tentatives échouées
        userDetailsService.updateLastLogin(username);
    }

    /**
     * Gère les événements de connexion échouée (mauvais credentials)
     */
    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        log.warn("Tentative de connexion échouée pour l'utilisateur: {} - Raison: {}",
                username, event.getException().getMessage());

        // Incrémenter le compteur de tentatives échouées
        userDetailsService.incrementFailedAttempts(username);
    }
}
