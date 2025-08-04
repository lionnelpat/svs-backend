package sn.svs.backoffice.domain.ennumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Énumération pour les pavillons des navires
 */
@Getter
public enum ShipFlag {
    SENEGAL("Sénégal"),
    FRANCE("France"),
    LIBERIA("Liberia"),
    PANAMA("Panama"),
    MARSHALL_ISLANDS("Marshall Islands"),
    SINGAPORE("Singapour"),
    BAHAMAS("Bahamas"),
    MALTA("Malta"),
    CYPRUS("Chypre"),
    GREECE("Grèce");

    private final String displayName;

    ShipFlag(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ShipFlag fromDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }

        for (ShipFlag flag : ShipFlag.values()) {
            if (flag.displayName.equalsIgnoreCase(displayName) ||
                    flag.name().equalsIgnoreCase(displayName)) {
                return flag;
            }
        }

        throw new IllegalArgumentException("Pavillon inconnu: " + displayName +
                ". Pavillons acceptés: " + java.util.Arrays.toString(ShipFlag.values()));
    }
}
