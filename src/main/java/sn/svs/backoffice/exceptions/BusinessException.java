package sn.svs.backoffice.exceptions;

/**
 * Exception pour les erreurs métier
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
