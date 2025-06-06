package sn.svs.backoffice.domain.ennumeration;

import lombok.Getter;

/**
 * Énumération pour les types de navires
 */
@Getter
public enum ShipType {
    CARGO("Cargo"),
    TANKER("Pétrolier"),
    CONTAINER("Porte-conteneurs"),
    PASSENGER("Passagers"),
    FERRY("Ferry"),
    FISHING("Pêche"),
    TUG("Remorqueur"),
    PILOT("Pilote"),
    OTHER("Autre");

    private final String displayName;

    ShipType(String displayName) {
        this.displayName = displayName;
    }

}
