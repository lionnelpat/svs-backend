package sn.svs.backoffice.domain.ennumeration;

import lombok.Getter;

/**
 * Énumération pour les devises
 */
@Getter
public enum Currency {
    XOF("Franc CFA", "₣"),
    EUR("Euro", "€"),
    USD("Dollar américain", "$");

    private final String displayName;
    private final String symbol;

    Currency(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

}
