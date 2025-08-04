package sn.svs.backoffice.domain.ennumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Énumération pour les organismes de classification
 */
@Getter
public enum ShipClassification {
    BUREAU_VERITAS("Bureau Veritas"),
    LLOYDS_REGISTER("Lloyd's Register"),
    DNV_GL("DNV GL"),
    ABS("American Bureau of Shipping"),
    CLASS_NK("ClassNK"),
    RINA("RINA"),
    CCS("CCS"),
    RS("RS"),
    KR("KR"),
    IRS("IRS");

    private final String displayName;

    ShipClassification(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ShipClassification fromDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }

        for (ShipClassification classification : ShipClassification.values()) {
            if (classification.displayName.equalsIgnoreCase(displayName) ||
                    classification.name().equalsIgnoreCase(displayName)) {
                return classification;
            }
        }

        throw new IllegalArgumentException("Classification inconnue: " + displayName +
                ". Classifications acceptées: " + java.util.Arrays.toString(ShipClassification.values()));
    }
}
