package sn.svs.backoffice.domain.ennumeration;

import lombok.Getter;

/**
 * Énumération pour les rôles utilisateurs
 */

@Getter
public enum RoleName {
    ADMIN("Administrateur"),
    MANAGER("Gestionnaire"),
    OPERATOR("Opérateur"),
    VIEWER("Lecteur"),
    USER("Utilisateur");

    private final String displayName;

    RoleName(String displayName) {
        this.displayName = displayName;
    }
}

