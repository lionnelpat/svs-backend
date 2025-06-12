package sn.svs.backoffice.exceptions;

/**
 * Exception pour les erreurs métier
 */
public class BusinessException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
