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
    private String secretKey = "1124d6d93c8dddb0c94bbd0fc53eefb07996b568ffe28db4ab697e345fd9253bea6e8c88a3b23a125b264cd39ea054815447f33e6621b2fc68636069ab6e9fca";

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
