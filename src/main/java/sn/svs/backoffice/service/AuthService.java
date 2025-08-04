package sn.svs.backoffice.service;

// ========== INTERFACE AUTHSERVICE ==========

import sn.svs.backoffice.dto.AuthDTO;

/**
 * Interface du service d'authentification
 * Définit les contrats pour toutes les opérations d'authentification et de gestion des comptes
 */
public interface AuthService {

    /**
     * Inscription d'un nouvel utilisateur
     */
    AuthDTO.RegisterResponse register(AuthDTO.RegisterRequest registerRequest);

    /**
     * Vérification de l'email avec un token
     */
    boolean verifyEmail(String token);

    /**
     * Renvoie un email de vérification
     */
    boolean resendVerificationEmail(String email);

    /**
     * Initie une réinitialisation de mot de passe
     */
    boolean initiatePasswordReset(String email);

    /**
     * Réinitialise le mot de passe avec un token
     */
    boolean resetPassword(String token, String newPassword);

    /**
     * Change le mot de passe d'un utilisateur connecté
     */
    boolean changePassword(Long userId, String currentPassword, String newPassword);

    /**
     * Vérifie le statut d'un compte utilisateur
     */
    AuthDTO.AccountStatusResponse getAccountStatus(String username);

    /**
     * Active ou désactive un compte utilisateur (Admin uniquement)
     */
    boolean toggleUserStatus(Long userId, boolean isActive);

    /**
     * Déverrouille un compte utilisateur (Admin uniquement)
     */
    boolean unlockUserAccount(Long userId);

    /**
     * Vérifie si un nom d'utilisateur est disponible
     */
    boolean isUsernameAvailable(String username);

    /**
     * Vérifie si un email est disponible
     */
    boolean isEmailAvailable(String email);

    /**
     * Nettoie les tokens expirés (tâche de maintenance)
     */
    void cleanupExpiredTokens();
}
