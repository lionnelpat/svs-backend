package sn.svs.backoffice.domain.ennumeration;

import lombok.Getter;

/**
 * Énumération pour les méthodes de paiement
 */
@Getter
public enum PaymentMethod {
    ESPECES("Espèces"),
    CHEQUE("Chèque"),
    VIREMENT("Virement bancaire"),
    CARTE_BANCAIRE("Carte bancaire"),
    MOBILE_MONEY("Mobile Money"),
    AUTRE("Autre");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

}
