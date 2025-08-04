package sn.svs.backoffice.domain.ennumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Énumération pour les types de navires
 */
@Getter
public enum ShipType {
    CARGO("Cargo"),
    CONTENEUR("Conteneur"),
    PETROLIER("Pétrolier"),
    VRAQUEUR("Vraqueur"),
    PASSAGERS("Passagers"),
    RO_RO("Ro-Ro"),
    FRIGORIFIQUE("Frigorifique"),
    CHIMIQUIER("Chimiquier"),
    GAZIER("Gazier"),
    REMORQUEUR("Remorqueur"),
    PILOTE("Pilote");

    private final String displayName;

    ShipType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ShipType fromDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }

        for (ShipType type : ShipType.values()) {
            if (type.displayName.equalsIgnoreCase(displayName) ||
                    type.name().equalsIgnoreCase(displayName)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Type de navire inconnu: " + displayName +
                ". Types acceptés: " + java.util.Arrays.toString(ShipType.values()));
    }
}
