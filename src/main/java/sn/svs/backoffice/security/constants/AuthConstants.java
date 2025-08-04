package sn.svs.backoffice.security.constants;

/**
 * Constantes pour les DTOs d'authentification
 */
public final class AuthConstants {

    private AuthConstants() {
        // Classe utilitaire
    }

    // Messages de validation
    public static final String USERNAME_REQUIRED = "Le nom d'utilisateur est obligatoire";
    public static final String PASSWORD_REQUIRED = "Le mot de passe est obligatoire";
    public static final String EMAIL_REQUIRED = "L'email est obligatoire";
    public static final String EMAIL_INVALID = "Format d'email invalide";
    public static final String PASSWORD_TOO_SHORT = "Le mot de passe doit contenir au moins 8 caractères";
    public static final String PASSWORD_PATTERN = "Le mot de passe doit contenir au moins une minuscule, une majuscule, un chiffre et un caractère spécial";
    public static final String PASSWORDS_NOT_MATCHING = "Les mots de passe ne correspondent pas";

    // Messages de réponse
    public static final String LOGIN_SUCCESS = "Connexion réussie";
    public static final String LOGIN_FAILED = "Identifiants incorrects";
    public static final String ACCOUNT_DISABLED = "Compte désactivé";
    public static final String ACCOUNT_LOCKED = "Compte temporairement verrouillé";
    public static final String EMAIL_NOT_VERIFIED = "Email non vérifié";
    public static final String TOKEN_EXPIRED = "Token expiré";
    public static final String TOKEN_INVALID = "Token invalide";
    public static final String PASSWORD_CHANGED = "Mot de passe modifié avec succès";
    public static final String EMAIL_VERIFIED = "Email vérifié avec succès";
    public static final String RESET_EMAIL_SENT = "Email de réinitialisation envoyé";

    // Patterns de validation
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9._-]+$";
    public static final String PHONE_PATTERN = "^[+]?[0-9\\s.-()]+$";
    public static final String PASSWORD_STRONG_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$";

    // Limites
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 50;
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int EMAIL_MAX_LENGTH = 100;
    public static final int NAME_MAX_LENGTH = 50;
    public static final int PHONE_MAX_LENGTH = 20;
}
