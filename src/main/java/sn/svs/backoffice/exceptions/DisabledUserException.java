package sn.svs.backoffice.exceptions;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Exception lancée quand un utilisateur est désactivé
 */
public class DisabledUserException extends UsernameNotFoundException {
    public DisabledUserException(String message) {
        super(message);
    }
}
