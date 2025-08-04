package sn.svs.backoffice.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Point d'entrée pour gérer les erreurs d'authentification JWT
 * Retourne une réponse JSON standardisée pour les erreurs d'authentification
 *
 * Cette classe est appelée automatiquement par Spring Security quand :
 * - Un utilisateur non authentifié tente d'accéder à une ressource protégée
 * - Un token JWT est invalide, expiré ou malformé
 * - Une erreur d'authentification survient
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Méthode appelée lorsqu'un utilisateur non authentifié tente d'accéder à une ressource protégée
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Log de l'erreur d'authentification pour audit
        logAuthenticationError(request, authException);

        // Déterminer le type d'erreur et le message approprié
        ErrorDetails errorDetails = determineErrorDetails(request, authException);

        // Configurer la réponse HTTP
        response.setStatus(errorDetails.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Ajouter des headers de sécurité
        addSecurityHeaders(response);

        // Construire la réponse JSON d'erreur
        Map<String, Object> errorResponse = buildErrorResponse(request, errorDetails, authException);

        // Écrire la réponse JSON
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Détermine les détails de l'erreur selon le contexte de la requête
     */
    private ErrorDetails determineErrorDetails(HttpServletRequest request, AuthenticationException authException) {
        String authHeader = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();

        // Cas 1: Aucun token fourni
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ErrorDetails(
                    HttpStatus.UNAUTHORIZED,
                    "AUTH_TOKEN_MISSING",
                    "Token d'authentification manquant. Veuillez vous connecter.",
                    "Aucun token JWT trouvé dans le header Authorization."
            );
        }

        // Cas 2: Token présent mais invalide (plusieurs sous-cas possibles)
        String token = authHeader.substring(7);

        // Token vide
        if (token.trim().isEmpty()) {
            return new ErrorDetails(
                    HttpStatus.UNAUTHORIZED,
                    "AUTH_TOKEN_EMPTY",
                    "Token d'authentification vide.",
                    "Le token JWT fourni est vide."
            );
        }

        // Token malformé ou expiré (analyse du message d'exception)
        String exceptionMessage = authException.getMessage().toLowerCase();

        if (exceptionMessage.contains("expired")) {
            return new ErrorDetails(
                    HttpStatus.UNAUTHORIZED,
                    "AUTH_TOKEN_EXPIRED",
                    "Votre session a expiré. Veuillez vous reconnecter.",
                    "Le token JWT a expiré."
            );
        }

        if (exceptionMessage.contains("malformed") || exceptionMessage.contains("invalid")) {
            return new ErrorDetails(
                    HttpStatus.UNAUTHORIZED,
                    "AUTH_TOKEN_INVALID",
                    "Token d'authentification invalide.",
                    "Le token JWT fourni est invalide ou malformé."
            );
        }

        if (exceptionMessage.contains("signature")) {
            return new ErrorDetails(
                    HttpStatus.UNAUTHORIZED,
                    "AUTH_TOKEN_SIGNATURE_INVALID",
                    "Token d'authentification non valide.",
                    "La signature du token JWT est invalide."
            );
        }

        // Cas 3: Accès interdit (utilisateur authentifié mais pas autorisé)
        if (authException.getClass().getSimpleName().contains("Access")) {
            return new ErrorDetails(
                    HttpStatus.FORBIDDEN,
                    "AUTH_ACCESS_DENIED",
                    "Accès interdit. Vous n'avez pas les permissions nécessaires.",
                    "L'utilisateur n'a pas les autorisations requises pour accéder à cette ressource."
            );
        }

        // Cas par défaut
        return new ErrorDetails(
                HttpStatus.UNAUTHORIZED,
                "AUTH_ERROR",
                "Erreur d'authentification. Veuillez vous reconnecter.",
                "Erreur d'authentification générique."
        );
    }

    /**
     * Construit la réponse JSON d'erreur standardisée
     */
    private Map<String, Object> buildErrorResponse(HttpServletRequest request,
                                                   ErrorDetails errorDetails,
                                                   AuthenticationException authException) {
        Map<String, Object> errorResponse = new HashMap<>();

        // Informations principales de l'erreur
        errorResponse.put("success", false);
        errorResponse.put("error", true);
        errorResponse.put("status", errorDetails.getHttpStatus().value());
        errorResponse.put("code", errorDetails.getErrorCode());
        errorResponse.put("message", errorDetails.getUserMessage());
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Informations sur la requête
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("method", request.getMethod());

        // Informations détaillées (uniquement en développement)
        if (isDevelopmentMode()) {
            Map<String, Object> details = new HashMap<>();
            details.put("technicalMessage", errorDetails.getTechnicalMessage());
            details.put("exceptionType", authException.getClass().getSimpleName());
            details.put("exceptionMessage", authException.getMessage());
            details.put("userAgent", request.getHeader("User-Agent"));
            details.put("clientIp", getClientIpAddress(request));
            errorResponse.put("details", details);
        }

        // Suggestions d'action pour l'utilisateur
        errorResponse.put("suggestions", getSuggestions(errorDetails.getErrorCode()));

        return errorResponse;
    }

    /**
     * Fournit des suggestions d'action selon le type d'erreur
     */
    private Map<String, Object> getSuggestions(String errorCode) {
        Map<String, Object> suggestions = new HashMap<>();

        switch (errorCode) {
            case "AUTH_TOKEN_MISSING":
            case "AUTH_TOKEN_EMPTY":
                suggestions.put("action", "login");
                suggestions.put("message", "Veuillez vous connecter pour accéder à cette ressource.");
                suggestions.put("endpoint", "/api/v1/auth/login");
                break;

            case "AUTH_TOKEN_EXPIRED":
                suggestions.put("action", "refresh");
                suggestions.put("message", "Utilisez votre token de rafraîchissement ou reconnectez-vous.");
                suggestions.put("endpoint", "/api/auth/refresh-token");
                suggestions.put("fallback", "/api/v1/auth/login");
                break;

            case "AUTH_TOKEN_INVALID":
            case "AUTH_TOKEN_SIGNATURE_INVALID":
                suggestions.put("action", "login");
                suggestions.put("message", "Votre token est invalide. Veuillez vous reconnecter.");
                suggestions.put("endpoint", "/api/v1/auth/login");
                break;

            case "AUTH_ACCESS_DENIED":
                suggestions.put("action", "contact_admin");
                suggestions.put("message", "Contactez un administrateur pour obtenir les permissions nécessaires.");
                break;

            default:
                suggestions.put("action", "retry");
                suggestions.put("message", "Veuillez réessayer ou vous reconnecter.");
                suggestions.put("endpoint", "/api/v1/auth/login");
        }

        return suggestions;
    }

    /**
     * Ajoute des headers de sécurité à la réponse
     */
    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }

    /**
     * Log détaillé de l'erreur d'authentification pour audit et debugging
     */
    private void logAuthenticationError(HttpServletRequest request, AuthenticationException authException) {
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String authHeader = request.getHeader("Authorization");

        // Log de sécurité (sans exposer le token complet)
        log.warn("Erreur d'authentification JWT - IP: {}, URI: {}, Méthode: {}, " +
                        "Token présent: {}, Erreur: {}",
                clientIp,
                request.getRequestURI(),
                request.getMethod(),
                authHeader != null && authHeader.startsWith("Bearer "),
                authException.getMessage());

        // Log détaillé pour debugging (niveau DEBUG)
        if (log.isDebugEnabled()) {
            log.debug("Détails de l'erreur d'authentification:\n" +
                            "  - IP Client: {}\n" +
                            "  - User-Agent: {}\n" +
                            "  - URI: {}\n" +
                            "  - Méthode: {}\n" +
                            "  - Exception: {}\n" +
                            "  - Message: {}",
                    clientIp, userAgent, request.getRequestURI(),
                    request.getMethod(), authException.getClass().getSimpleName(),
                    authException.getMessage());
        }
    }

    /**
     * Obtient l'adresse IP réelle du client (gère les proxies et load balancers)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Détecte si l'application est en mode développement
     */
    private boolean isDevelopmentMode() {
        String profile = System.getProperty("spring.profiles.active");
        return profile != null && (profile.contains("dev") || profile.contains("local"));
    }

    /**
     * Classe interne pour encapsuler les détails d'erreur
     */
    private static class ErrorDetails {
        private final HttpStatus httpStatus;
        private final String errorCode;
        private final String userMessage;
        private final String technicalMessage;

        public ErrorDetails(HttpStatus httpStatus, String errorCode, String userMessage, String technicalMessage) {
            this.httpStatus = httpStatus;
            this.errorCode = errorCode;
            this.userMessage = userMessage;
            this.technicalMessage = technicalMessage;
        }

        public HttpStatus getHttpStatus() { return httpStatus; }
        public String getErrorCode() { return errorCode; }
        public String getUserMessage() { return userMessage; }
        public String getTechnicalMessage() { return technicalMessage; }
    }
}
