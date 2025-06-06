package sn.svs.backoffice.exceptions;

/**
 * Exception pour les ressources non trouvées
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s avec %s '%s' non trouvé(e)", resource, field, value));
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s avec l'identifiant '%d' non trouvé(e)", resource, id));
    }
}
