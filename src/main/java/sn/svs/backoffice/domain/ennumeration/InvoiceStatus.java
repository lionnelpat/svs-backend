package sn.svs.backoffice.domain.ennumeration;

import lombok.Getter;

/**
 * Énumération pour les statuts des factures
 */
@Getter
public enum InvoiceStatus {
    BROUILLON("Brouillon"),
    ENVOYEE("Envoyée"),
    PARTIELLEMENT_PAYEE("Partiellement payée"),
    PAYEE("Payée"),
    EN_RETARD("En retard"),
    ANNULEE("Annulée");

    private final String displayName;

    InvoiceStatus(String displayName) {
        this.displayName = displayName;
    }

}
