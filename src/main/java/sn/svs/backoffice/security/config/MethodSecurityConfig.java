package sn.svs.backoffice.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Configuration pour activer la sécurité au niveau des méthodes
 * Permet l'utilisation des annotations @PreAuthorize, @PostAuthorize, etc.
 */
@Configuration
@EnableMethodSecurity(
        prePostEnabled = true,   // Active @PreAuthorize et @PostAuthorize
        securedEnabled = true,   // Active @Secured
        jsr250Enabled = true     // Active @RolesAllowed, @PermitAll, @DenyAll
)
public class MethodSecurityConfig {

    // Configuration automatique par Spring Security 6+
    // Plus besoin de GlobalMethodSecurityConfiguration
}
