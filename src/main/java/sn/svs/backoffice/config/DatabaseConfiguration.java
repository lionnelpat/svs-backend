package sn.svs.backoffice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

/**
 * Configuration de la base de données et de l'audit JPA
 */
@Slf4j
@Configuration
@EnableJpaRepositories(basePackages = "sn.svs.backoffice.repository")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableTransactionManagement
public class DatabaseConfiguration {

    /**
     * Fournisseur d'auditeur pour l'audit automatique JPA
     * Récupère l'utilisateur connecté pour les champs created_by et updated_by
     */
    @Bean(name = "auditorProvider")
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    /**
     * Implémentation du fournisseur d'auditeur
     */
    public static class AuditorAwareImpl implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            return Optional.ofNullable(SecurityContextHolder.getContext())
                    .map(SecurityContext::getAuthentication)
                    .filter(Authentication::isAuthenticated)
                    .map(Authentication::getName)
                    .or(() -> Optional.of("system")); // Utilisateur par défaut pour les opérations système
        }
    }
}
