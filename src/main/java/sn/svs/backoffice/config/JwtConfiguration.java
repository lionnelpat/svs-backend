package sn.svs.backoffice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration des propriétés JWT
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "svs.security.jwt")
public class JwtConfiguration {

    /**
     * Clé secrète pour signer les tokens JWT
     * IMPORTANT: Doit être changée en production
     */
    private String secretKey = "SVS-Maritime-Default-Secret-Key-Change-In-Production";

    /**
     * Durée de validité du token en secondes (par défaut 24h)
     */
    private long tokenValidityInSeconds = 86400;

    /**
     * Durée de validité du token "Remember Me" en secondes (par défaut 30 jours)
     */
    private long tokenValidityInSecondsForRememberMe = 2592000;

    /**
     * Préfixe du header Authorization
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Nom du header contenant le token
     */
    private String headerName = "Authorization";
}
