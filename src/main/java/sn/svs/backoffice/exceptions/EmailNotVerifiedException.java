package sn.svs.backoffice.exceptions;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Exception lancée quand l'email d'un utilisateur n'est pas vérifié
 */
public class EmailNotVerifiedException extends UsernameNotFoundException {
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
