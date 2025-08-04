package sn.svs.backoffice.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import sn.svs.backoffice.dto.ErrorDTO;

import java.time.LocalDateTime;

@Slf4j
@Component
public class AuthExceptionHandler {

    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException e, HttpServletRequest request, String username) {
        log.warn("Échec d'authentification pour {}: {}", username, e.getClass().getSimpleName());

        String code;
        String message;
        String suggestionMessage;
        String suggestionEndpoint;
        String suggestionAction;
        HttpStatus status;

        if (e instanceof BadCredentialsException) {
            code = "INVALID_CREDENTIALS";
            message = "Identifiants incorrects. Veuillez vérifier votre nom d'utilisateur et mot de passe.";
            suggestionMessage = "Vérifiez vos identifiants et réessayez.";
            suggestionEndpoint = "/api/v1/auth/login";
            suggestionAction = "retry";
            status = HttpStatus.UNAUTHORIZED;

        } else if (e instanceof DisabledException) {
            code = "ACCOUNT_DISABLED";
            message = "Votre compte est désactivé. Veuillez contacter l'administrateur.";
            suggestionMessage = "Contactez l'administrateur pour réactiver votre compte.";
            suggestionEndpoint = "/api/v1/auth/contact-support";
            suggestionAction = "contact_support";
            status = HttpStatus.FORBIDDEN;

        } else if (e instanceof LockedException) {
            code = "ACCOUNT_LOCKED";
            message = "Votre compte est temporairement verrouillé en raison de tentatives de connexion multiples.";
            suggestionMessage = "Attendez avant de réessayer ou contactez l'administrateur.";
            suggestionEndpoint = "/api/v1/auth/unlock-account";
            suggestionAction = "unlock_account";
            status = HttpStatus.LOCKED;

        } else if (e instanceof AccountExpiredException) {
            code = "ACCOUNT_EXPIRED";
            message = "Votre compte a expiré. Veuillez le renouveler.";
            suggestionMessage = "Renouvelez votre compte ou contactez l'administrateur.";
            suggestionEndpoint = "/api/v1/auth/renew-account";
            suggestionAction = "renew_account";
            status = HttpStatus.UNAUTHORIZED;

        } else if (e instanceof CredentialsExpiredException) {
            code = "CREDENTIALS_EXPIRED";
            message = "Votre mot de passe a expiré. Veuillez le réinitialiser.";
            suggestionMessage = "Réinitialisez votre mot de passe pour continuer.";
            suggestionEndpoint = "/api/v1/auth/reset-password";
            suggestionAction = "reset_password";
            status = HttpStatus.UNAUTHORIZED;

        } else {
            code = "AUTHENTICATION_FAILED";
            message = "Échec de l'authentification. Veuillez réessayer.";
            suggestionMessage = "Réessayez dans quelques instants ou contactez le support.";
            suggestionEndpoint = "/api/v1/auth/login";
            suggestionAction = "retry";
            status = HttpStatus.UNAUTHORIZED;
        }

        ErrorDTO.Response errorResponse = ErrorDTO.Response.builder()
                .path(request.getRequestURI())
                .code(code)
                .method(request.getMethod())
                .success(false)
                .suggestions(ErrorDTO.Suggestion.builder()
                        .endpoint(suggestionEndpoint)
                        .action(suggestionAction)
                        .message(suggestionMessage)
                        .build())
                .error(true)
                .message(message)
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}

