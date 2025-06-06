package sn.svs.backoffice.domain.ennumeration;


import lombok.Getter;

/**
 * Énumération pour les statuts des dépenses
 */
@Getter
public enum ExpenseStatus {
    BROUILLON("Brouillon"),
    EN_ATTENTE("En attente de validation"),
    VALIDEE("Validée"),
    PAYEE("Payée"),
    REJETEE("Rejetée"),
    ANNULEE("Annulée");

    private final String displayName;

    ExpenseStatus(String displayName) {
        this.displayName = displayName;
    }

}
