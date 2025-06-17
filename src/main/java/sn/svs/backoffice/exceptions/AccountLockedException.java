package sn.svs.backoffice.exceptions;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class AccountLockedException extends UsernameNotFoundException {
    public AccountLockedException(String message) {
        super(message);
    }
}