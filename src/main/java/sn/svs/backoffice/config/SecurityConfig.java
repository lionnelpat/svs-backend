package sn.svs.backoffice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
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

import static sn.svs.backoffice.security.constants.SecurityConstants.ROLE_ADMIN;
import static sn.svs.backoffice.security.constants.SecurityConstants.ROLE_MANAGER;

/**
 * Configuration de sécurité Spring Security avec JWT
 * Définit les règles d'authentification et d'autorisation pour l'application
 *
 * Architecture de sécurité :
 * 1. Authentification JWT via filtre personnalisé
 * 2. Autorisation basée sur les rôles (ADMIN, MANAGER, USER)
 * 3. Protection CORS pour les appels frontend
 * 4. Gestion des erreurs d'authentification
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

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
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh-token",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password",
                                "/api/v1/auth/verify-email",
                                "/api/v1/auth/validate-token"
                        ).permitAll()

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

                        // ========== GESTION DES UTILISATEURS ==========
                        // Seuls les ADMIN peuvent créer/supprimer des utilisateurs
//                        .requestMatchers(HttpMethod.POST, "/api/v1/users").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
//
//                        // ADMIN et MANAGER peuvent lister et voir les utilisateurs
//                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasAnyRole("ADMIN", "MANAGER")
//
//                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
//
//                        // ADMIN et MANAGER peuvent modifier les utilisateurs
//                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasAnyRole("ADMIN", "MANAGER")
//
//                        // ========== GESTION DES RÔLES ==========
//                        // Seuls les ADMIN peuvent gérer les rôles
//                        .requestMatchers("/api/roles/**").hasRole("ADMIN")

                        // ========== FACTURES MARITIMES (MÉTIER) ==========
                        // Tous les utilisateurs authentifiés peuvent voir les factures
//                        .requestMatchers(HttpMethod.GET, "/api/v1/invoices/**").hasAnyRole("ADMIN", "MANAGER", "USER")
//
//                        // ADMIN et MANAGER peuvent créer/modifier des factures
//                        .requestMatchers(
//                                HttpMethod.POST, "/api/v1/invoices/**"
//                        ).hasAnyRole("ADMIN", "MANAGER")
//                        .requestMatchers(
//                                HttpMethod.PUT, "/api/v1/invoices/**"
//                        ).hasAnyRole("ADMIN", "MANAGER")
//                        .requestMatchers(
//                                HttpMethod.PATCH, "/api/v1/invoices/**"
//                        ).hasAnyRole("ADMIN", "MANAGER")
//
//                        // Seuls les ADMIN peuvent supprimer des factures
//                        .requestMatchers(HttpMethod.DELETE, "/api/v1/invoices/**").hasRole("ADMIN")

                        // ========== PROFIL UTILISATEUR ==========
                        // Chaque utilisateur peut gérer son propre profil
                        .requestMatchers(
                                "/api/v1/profile/**",
                                "/api/v1/auth/change-password"
                        ).authenticated()

                        // ========== TOUTES LES AUTRES REQUÊTES ==========
                        // Nécessitent une authentification
                        .anyRequest().authenticated()
                )

                // Ajouter le filtre JWT avant le filtre d'authentification par défaut
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuration CORS pour permettre les appels depuis le frontend Angular
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origines autorisées (à adapter selon ton environnement)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",    // Angular dev
                "http://localhost:3000",    // React dev (si besoin)
                "https://svs-backoffice.com", // Production
                "https://staging.svs-backoffice.com" // Staging
        ));

        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Headers autorisés
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Headers exposés au frontend
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Token-Expires-In",
                "X-Total-Count"
        ));

        // Autoriser les credentials (cookies, headers d'auth)
        configuration.setAllowCredentials(true);

        // Durée de cache pour les requêtes preflight
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Encodeur de mot de passe BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Force 12 pour plus de sécurité
    }

    /**
     * Fournisseur d'authentification personnalisé
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false); // Pour des messages d'erreur clairs
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
}