package sn.svs.backoffice.domain.ennumeration;

import lombok.Getter;

/**
 * Énumération pour les rôles utilisateurs
 */

@Getter
public enum Role {
    ADMIN("Administrateur"),
    MANAGER("Gestionnaire"),
    OPERATOR("Opérateur"),
    VIEWER("Lecteur");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

}
