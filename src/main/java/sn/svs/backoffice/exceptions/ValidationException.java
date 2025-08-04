package sn.svs.backoffice.exceptions;

/**
 * Exception pour les erreurs de validation métier
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String field, Object value, String reason) {
        super(String.format("Validation échouée pour le champ '%s' avec la valeur '%s': %s", field, value, reason));
    }
}
