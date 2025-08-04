package sn.svs.backoffice.domain.ennumeration;

import lombok.Getter;

/**
 * Énumération pour les statuts des factures
 */
@Getter
public enum InvoiceStatus {
    BROUILLON("Brouillon", "Facture en cours de rédaction"),
    EMISE("Émise", "Facture émise et envoyée au client"),
    PAYEE("Payée", "Facture payée intégralement"),
    ANNULEE("Annulée", "Facture annulée"),
    EN_RETARD("En retard", "Facture échue non payée");

    private final String label;
    private final String description;

    InvoiceStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return this.label;
    }

}
