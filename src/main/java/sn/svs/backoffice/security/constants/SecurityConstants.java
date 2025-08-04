package sn.svs.backoffice.security.constants;

/**
 * Constantes de sécurité pour centraliser les rôles et permissions
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Classe utilitaire
    }

    // Rôles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_USER = "USER";

    // Rôles pour annotations (sans préfixe ROLE_)
    public static final String ADMIN = "ADMIN";
    public static final String MANAGER = "MANAGER";
    public static final String USER = "USER";

    // Expressions SpEL courantes
    public static final String HAS_ROLE_ADMIN = "hasRole('ADMIN')";
    public static final String HAS_ROLE_MANAGER = "hasRole('MANAGER')";
    public static final String HAS_ROLE_USER = "hasRole('USER')";
    public static final String HAS_ROLE_ADMIN_OR_MANAGER = "hasRole('ADMIN') or hasRole('MANAGER')";
    public static final String IS_AUTHENTICATED = "isAuthenticated()";

    // Permissions métier spécifiques
    public static final String CAN_READ_FACTURES = "hasAnyRole('ADMIN', 'MANAGER', 'USER')";
    public static final String CAN_WRITE_FACTURES = "hasAnyRole('ADMIN', 'MANAGER')";
    public static final String CAN_DELETE_FACTURES = "hasRole('ADMIN')";
    public static final String CAN_MANAGE_USERS = "hasRole('ADMIN')";
    public static final String CAN_VIEW_REPORTS = "hasAnyRole('ADMIN', 'MANAGER')";
}

