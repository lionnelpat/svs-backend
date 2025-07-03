package sn.svs.backoffice.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filtre JWT qui intercepte toutes les requêtes pour vérifier l'authentification
 * Hérite de OncePerRequestFilter pour garantir une seule exécution par requête
 *
 * Flow du filtre :
 * 1. Extrait le token JWT du header Authorization
 * 2. Valide le token avec JwtUtils
 * 3. Charge les détails de l'utilisateur
 * 4. Configure le SecurityContext Spring Security
 * 5. Continue la chaîne de filtres
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    // Header d'autorisation JWT
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    // Endpoints publics qui ne nécessitent pas d'authentification
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/verify-email",
            "/management/health",
            "/swagger-ui",
            "/v3/api-docs",
            "/favicon.ico"
    );

    /**
     * Méthode principale du filtre qui traite chaque requête HTTP
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Log de la requête pour debug (optionnel)
            if (log.isDebugEnabled()) {
                log.debug("Traitement de la requête: {} {}",
                        request.getMethod(), request.getRequestURI());
            }

            // Vérifier si l'endpoint est public
            if (isPublicEndpoint(request)) {
                log.debug("Endpoint public détecté, pas d'authentification requise: {}",
                        request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // Extraire le token JWT du header
            String jwt = extractJwtFromRequest(request);

            // Si aucun token n'est présent, continuer sans authentification
            if (jwt == null) {
                log.debug("Aucun token JWT trouvé dans la requête");
                filterChain.doFilter(request, response);
                return;
            }

            // Valider le token JWT
            if (!jwtUtils.validateToken(jwt)) {
                log.warn("Token JWT invalide pour la requête: {} {}",
                        request.getMethod(), request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // Vérifier que c'est un token d'accès (pas un refresh token)
            if (!jwtUtils.isAccessToken(jwt)) {
                log.warn("Token de type incorrect pour l'authentification: {}",
                        jwtUtils.getTokenTypeFromToken(jwt));
                filterChain.doFilter(request, response);
                return;
            }

            // Extraire le nom d'utilisateur du token
            String username = jwtUtils.getUsernameFromToken(jwt);

            // Vérifier si l'utilisateur n'est pas déjà authentifié
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Charger les détails de l'utilisateur depuis la base de données
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Vérifier que l'utilisateur est actif selon le token
                Boolean isActiveFromToken = jwtUtils.isUserActiveFromToken(jwt);
                if (isActiveFromToken == null || !isActiveFromToken) {
                    log.warn("Utilisateur inactif selon le token: {}", username);
                    filterChain.doFilter(request, response);
                    return;
                }

                // Valider le token avec les détails de l'utilisateur
                if (userDetails != null && userDetails.isEnabled()) {

                    // Créer l'objet d'authentification Spring Security
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Ajouter les détails de la requête (IP, session, etc.)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Configurer le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Utilisateur authentifié avec succès: {} avec les rôles: {}",
                            username, userDetails.getAuthorities());

                    // Ajouter des headers de réponse utiles (optionnel)
                    addSecurityHeaders(response, jwt);
                }
            }

        } catch (Exception e) {
            log.error("Erreur lors de l'authentification JWT pour la requête: {} {} - Erreur: {}",
                    request.getMethod(), request.getRequestURI(), e.getMessage());

            // En cas d'erreur, nettoyer le contexte de sécurité
            SecurityContextHolder.clearContext();
        }

        // Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le token JWT du header Authorization de la requête
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(authHeader) && authHeader.startsWith(TOKEN_PREFIX)) {
            String token = authHeader.substring(TOKEN_PREFIX.length());
            log.debug("Token JWT extrait du header Authorization");
            return token;
        }

        // Tentative d'extraction depuis un paramètre de requête (optionnel, pour WebSocket par exemple)
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            log.debug("Token JWT extrait du paramètre de requête");
            return tokenParam;
        }

        return null;
    }

    /**
     * Vérifie si l'endpoint demandé est public (ne nécessite pas d'authentification)
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(endpoint -> {
                    // Correspondance exacte ou préfixe pour les endpoints avec wildcards
                    return requestURI.equals(endpoint) ||
                            requestURI.startsWith(endpoint + "/") ||
                            (endpoint.endsWith("/") && requestURI.startsWith(endpoint));
                });
    }

    /**
     * Ajoute des headers de sécurité à la réponse
     */
    private void addSecurityHeaders(HttpServletResponse response, String jwt) {
        try {
            // Ajouter le temps d'expiration du token (optionnel)
            long timeToExpiration = jwtUtils.getTimeToExpirationInMinutes(jwt);
            response.setHeader("X-Token-Expires-In", String.valueOf(timeToExpiration));

            // Ajouter des headers de sécurité généraux
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("X-XSS-Protection", "1; mode=block");

        } catch (Exception e) {
            log.warn("Erreur lors de l'ajout des headers de sécurité: {}", e.getMessage());
        }
    }

    /**
     * Détermine si le filtre doit être appliqué à cette requête
     * Par défaut, le filtre s'applique à toutes les requêtes
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Exclure les ressources statiques du filtrage
        return path.startsWith("/static/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.endsWith(".ico") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".css") ||
                path.endsWith(".js");
    }

    /**
     * Méthode utilitaire pour extraire l'adresse IP réelle du client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Log détaillé de la requête pour debugging (à utiliser en développement)
     */
    private void logRequestDetails(HttpServletRequest request, String jwt) {
        if (log.isTraceEnabled()) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("Détails de la requête JWT:\n");
            logMessage.append("  URI: ").append(request.getRequestURI()).append("\n");
            logMessage.append("  Méthode: ").append(request.getMethod()).append("\n");
            logMessage.append("  IP Client: ").append(getClientIpAddress(request)).append("\n");
            logMessage.append("  User-Agent: ").append(request.getHeader("User-Agent")).append("\n");

            if (jwt != null) {
                logMessage.append("  Token présent: Oui\n");
                logMessage.append("  Utilisateur: ").append(jwtUtils.getUsernameFromToken(jwt)).append("\n");
                logMessage.append("  Rôles: ").append(jwtUtils.getRolesFromToken(jwt)).append("\n");
                logMessage.append("  Expire dans: ").append(jwtUtils.getTimeToExpirationInMinutes(jwt)).append(" minutes\n");
            } else {
                logMessage.append("  Token présent: Non\n");
            }

            log.trace(logMessage.toString());
        }
    }
}