package sn.svs.backoffice.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import sn.svs.backoffice.domain.User;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utilitaire JWT pour la génération, validation et extraction des informations des tokens
 * Utilise la bibliothèque JJWT 0.12.3 compatible avec Spring Boot 3.2.1
 */
@Component
@Slf4j
public class JwtUtils {

    // Configuration depuis application.yml
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private int jwtAccessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private int jwtRefreshTokenExpirationMs;

    @Value("${app.jwt.issuer}")
    private String jwtIssuer;

    // Claims personnalisés
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_FULL_NAME = "fullName";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_IS_ACTIVE = "isActive";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";

    // Types de tokens
    public enum TokenType {
        ACCESS_TOKEN, REFRESH_TOKEN
    }

    /**
     * Génère la clé secrète à partir de la configuration
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Génère un token d'accès JWT à partir de l'authentification
     */
    public String generateAccessToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return generateAccessToken(user);
    }

    /**
     * Génère un token d'accès JWT à partir de l'utilisateur
     */
//    public String generateAccessToken(User user) {
//        Map<String, Object> claims = buildUserClaims(user);
//        claims.put(CLAIM_TOKEN_TYPE, TokenType.ACCESS_TOKEN.name());
//
//        return buildToken(claims, user.getUsername(), jwtAccessTokenExpirationMs);
//    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        // Ajouter les rôles au token
        List<String> roleNames = user.getRoles() != null ?
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        claims.put("roles", roleNames);
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("fullName", user.getFirstName() + " " + user.getLastName());
        claims.put("isActive", user.getIsActive());
        claims.put("tokenType", "ACCESS_TOKEN");

        return buildToken(claims, user.getUsername(), jwtAccessTokenExpirationMs);
    }

    /**
     * Génère un token de rafraîchissement
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, user.getId());
        claims.put(CLAIM_EMAIL, user.getEmail());
        claims.put(CLAIM_TOKEN_TYPE, TokenType.REFRESH_TOKEN.name());

        return buildToken(claims, user.getUsername(), jwtRefreshTokenExpirationMs);
    }

    /**
     * Construit les claims personnalisés pour l'utilisateur
     */
    private Map<String, Object> buildUserClaims(User user) {
        Map<String, Object> claims = new HashMap<>();

        // Informations utilisateur
        claims.put(CLAIM_USER_ID, user.getId());
        claims.put(CLAIM_EMAIL, user.getEmail());
        claims.put(CLAIM_FULL_NAME, user.getFullName());
        claims.put(CLAIM_IS_ACTIVE, user.getIsActive());

        // Rôles de l'utilisateur
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put(CLAIM_ROLES, roles);

        return claims;
    }

    /**
     * Construit le token JWT avec les claims, subject et expiration
     */
    private String buildToken(Map<String, Object> claims, String subject, int expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrait le nom d'utilisateur du token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extrait la date d'expiration du token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extrait l'ID utilisateur du token
     */
    public Long getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get(CLAIM_USER_ID, Long.class));
    }

    /**
     * Extrait l'email du token
     */
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get(CLAIM_EMAIL, String.class));
    }

    /**
     * Extrait le nom complet du token
     */
    public String getFullNameFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get(CLAIM_FULL_NAME, String.class));
    }

    /**
     * Extrait les rôles du token
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return getClaimFromToken(token, claims -> (List<String>) claims.get(CLAIM_ROLES));
    }

    /**
     * Extrait le type de token
     */
    public TokenType getTokenTypeFromToken(String token) {
        String tokenType = getClaimFromToken(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
        return TokenType.valueOf(tokenType);
    }

    /**
     * Vérifie si l'utilisateur est actif selon le token
     */
    public Boolean isUserActiveFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get(CLAIM_IS_ACTIVE, Boolean.class));
    }

    /**
     * Extrait un claim spécifique du token
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait tous les claims du token
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction des claims du token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Vérifie si le token est expiré
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Erreur lors de la vérification de l'expiration du token: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Valide le token JWT
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            log.debug("Token JWT validé avec succès");
            return true;

        } catch (SignatureException e) {
            log.error("Signature JWT invalide: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Token JWT malformé: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Token JWT expiré: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT non supporté: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Chaîne de claims JWT vide: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erreur lors de la validation du token JWT: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Valide le token et vérifie qu'il correspond à un utilisateur spécifique
     */
    public boolean validateToken(String token, User user) {
        try {
            final String username = getUsernameFromToken(token);
            final Long userId = getUserIdFromToken(token);

            return (username.equals(user.getUsername())
                    && userId.equals(user.getId())
                    && !isTokenExpired(token)
                    && validateToken(token));

        } catch (Exception e) {
            log.error("Erreur lors de la validation du token pour l'utilisateur {}: {}",
                    user.getUsername(), e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si le token est un token d'accès
     */
    public boolean isAccessToken(String token) {
        try {
            TokenType tokenType = getTokenTypeFromToken(token);
            return TokenType.ACCESS_TOKEN.equals(tokenType);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du type de token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si le token est un token de rafraîchissement
     */
    public boolean isRefreshToken(String token) {
        try {
            TokenType tokenType = getTokenTypeFromToken(token);
            return TokenType.REFRESH_TOKEN.equals(tokenType);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du type de token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrait le token du header Authorization
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Calcule le temps restant avant expiration (en minutes)
     */
    public long getTimeToExpirationInMinutes(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            Date now = new Date();
            long diffInMillis = expiration.getTime() - now.getTime();
            return diffInMillis / (60 * 1000); // Conversion en minutes
        } catch (Exception e) {
            log.error("Erreur lors du calcul du temps d'expiration: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Convertit une Date en LocalDateTime
     */
    public LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Obtient les informations de base du token sous forme de Map
     */
    public Map<String, Object> getTokenInfo(String token) {
        Map<String, Object> tokenInfo = new HashMap<>();
        try {
            tokenInfo.put("username", getUsernameFromToken(token));
            tokenInfo.put("userId", getUserIdFromToken(token));
            tokenInfo.put("email", getEmailFromToken(token));
            tokenInfo.put("fullName", getFullNameFromToken(token));
            tokenInfo.put("roles", getRolesFromToken(token));
            tokenInfo.put("isActive", isUserActiveFromToken(token));
            tokenInfo.put("tokenType", getTokenTypeFromToken(token));
            tokenInfo.put("expirationDate", getExpirationDateFromToken(token));
            tokenInfo.put("timeToExpirationMinutes", getTimeToExpirationInMinutes(token));
            tokenInfo.put("isExpired", isTokenExpired(token));
            tokenInfo.put("isValid", validateToken(token));
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction des informations du token: {}", e.getMessage());
            tokenInfo.put("error", e.getMessage());
        }
        return tokenInfo;
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Ajoutez les rôles
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        claims.put("roles", roles);
        claims.put("fullName", ((User) userDetails).getFullName()); // Adaptez selon votre modèle User
        claims.put("isActive", ((User) userDetails).isActive());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuer("svs-backoffice")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtAccessTokenExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
}
