package sn.svs.backoffice.domain.ennumeration;


import lombok.Getter;

/**
 * Énumération pour les statuts des dépenses
 */
@Getter
public enum ExpenseStatus {
    EN_ATTENTE("En attente"),
    APPROUVEE("Approuvée"),
    REJETEE("Rejetée"),
    PAYEE("Payée");

    private final String label;

    ExpenseStatus(String label) {
        this.label = label;
    }

}
