package sn.svs.backoffice.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration CORS pour permettre les requêtes depuis l'application Angular
 */
@Setter
@Getter
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "maritime.cors")
public class WebCorsConfiguration {

    // Getters et Setters pour @ConfigurationProperties
    private List<String> allowedOrigins = List.of("http://localhost:4200");
    private List<String> allowedMethods = List.of("*");
    private List<String> allowedHeaders = List.of("*");
    private List<String> exposedHeaders = List.of("Authorization", "Link", "X-Total-Count", "X-Maritime-Alert");
    private boolean allowCredentials = true;
    private long maxAge = 1800;

    /**
     * Configuration principale CORS pour l'API
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuration CORS - Origines autorisées: {}", allowedOrigins);

        CorsConfiguration configuration = new CorsConfiguration();

        // Configuration des origines autorisées
        configuration.setAllowedOriginPatterns(allowedOrigins);

        // Configuration des méthodes HTTP autorisées
        configuration.setAllowedMethods(getAllowedMethodsList());

        // Configuration des headers autorisés
        configuration.setAllowedHeaders(allowedHeaders);

        // Headers exposés dans les réponses
        configuration.setExposedHeaders(exposedHeaders);

        // Autoriser les cookies et credentials
        configuration.setAllowCredentials(allowCredentials);

        // Temps de cache pour les requêtes OPTIONS
        configuration.setMaxAge(maxAge);

        // Enregistrement de la configuration
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/management/**", configuration);

        return source;
    }

    /**
     * Convertit les méthodes autorisées en liste complète si "*" est spécifié
     */
    private List<String> getAllowedMethodsList() {
        if (allowedMethods.contains("*")) {
            return Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD");
        }
        return allowedMethods;
    }

}