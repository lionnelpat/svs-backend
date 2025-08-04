package sn.svs.backoffice.exceptions;

/**
 * Exception pour les ressources déjà existantes
 */
public class DuplicateResourceException extends  RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resource, String field, Object value) {
        super(String.format("%s avec %s '%s' existe déjà", resource, field, value));
    }
}
