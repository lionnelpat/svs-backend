package sn.svs.backoffice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuration de sécurité pour l'application Maritime SVS
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Configuration de la chaîne de filtres de sécurité
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuration de la sécurité Spring Security - Mode DÉVELOPPEMENT");

        http
                // Configuration CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Désactivation CSRF pour les API REST
                .csrf(AbstractHttpConfigurer::disable)

                // Configuration des headers de sécurité
                .headers(headers -> headers
                        .contentTypeOptions(contentTypeOptions -> {})
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )

                // Configuration de session stateless pour JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configuration des autorisations - TRÈS PERMISSIVE POUR LE DÉVELOPPEMENT
                .authorizeHttpRequests(authz -> authz
                        // TOUT EST AUTORISÉ POUR LE MOMENT - À MODIFIER EN PRODUCTION
                        .requestMatchers("/**").permitAll()
                        .anyRequest().permitAll()
                );

        log.info("✅ Configuration de sécurité appliquée - TOUS LES ENDPOINTS SONT OUVERTS");
        return http.build();
    }

    /**
     * Encodeur de mot de passe BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
