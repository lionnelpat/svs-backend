package sn.svs.backoffice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import sn.svs.backoffice.security.jwt.JwtAuthenticationEntryPoint;
import sn.svs.backoffice.security.jwt.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration de sécurité Spring Security avec JWT
 * Définit les règles d'authentification et d'autorisation pour l'application
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Configuration principale de la chaîne de filtres de sécurité
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF car nous utilisons JWT (stateless)
                .csrf(AbstractHttpConfigurer::disable)
                // Configuration CORS
                .cors(Customizer.withDefaults())
                // Gestion des erreurs d'authentification
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                // Configuration des sessions (stateless pour JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Configuration des autorisations
                .authorizeHttpRequests(authz -> authz
                        // ========== ENDPOINTS PUBLICS ==========
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // ========== RESSOURCES STATIQUES ==========
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/static/**",
                                "/favicon.ico"
                        ).permitAll()

                        // ========== DOCUMENTATION API ==========
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // ========== ACTUATOR (MONITORING) ==========
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // ========== TOUTES LES AUTRES REQUÊTES ==========
                        .anyRequest().authenticated()
                )

                // Ajouter le filtre JWT avant le filtre d'authentification par défaut
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Encodeur de mot de passe BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Fournisseur d'authentification personnalisé
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    /**
     * Gestionnaire d'authentification
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ CORRECTION 1: Ajouter TOUS les domaines possibles
        config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:4200",
                "https://svs-frontend.model-technologie.com",
                "https://*.model-technologie.com", // Wildcard pour tous les sous-domaines
                "http://localhost:*" // Pour le développement local
        ));

        // ✅ CORRECTION 2: Ajouter OPTIONS pour les requêtes préliminaires
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        // ✅ CORRECTION 3: En-têtes complets
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Total-Count",
                "X-Total-Pages",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Headers",
                "X-Requested-With",
                "Origin",
                "Accept",
                "X-Auth-Token"
        ));

        // ✅ CORRECTION 4: En-têtes exposés
        config.setExposedHeaders(Arrays.asList(
                "X-Total-Count",
                "X-Total-Pages",
                "Authorization"
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Cache des requêtes préliminaires

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


}