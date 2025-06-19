// ========== DATA INITIALIZATION SERVICE ==========
package sn.svs.backoffice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sn.svs.backoffice.domain.Role;
import sn.svs.backoffice.domain.User;
import sn.svs.backoffice.repository.RoleRepository;
import sn.svs.backoffice.repository.UserRepository;

import java.util.Set;

/**
 * Service d'initialisation des données par défaut
 * Crée l'utilisateur admin et les rôles système au premier démarrage
 * SVS - Dakar, Sénégal
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== Initialisation des données SVS Backoffice - Salane Vision S.a.r.l ===");

        try {
            // 1. Créer les rôles système s'ils n'existent pas
            createDefaultRoles();

            // 2. Créer l'utilisateur admin par défaut s'il n'existe pas
            createDefaultAdminUser();

            log.info("=== Initialisation des données terminée avec succès ===");

        } catch (Exception e) {
            log.error("Erreur lors de l'initialisation des données: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Crée les rôles système par défaut
     */
    private void createDefaultRoles() {
        log.info("Vérification et création des rôles système...");

        // Rôle ADMIN
        createRoleIfNotExists(
                Role.RoleName.ADMIN,
                "Administrateur système - Accès complet à toutes les fonctionnalités"
        );

        // Rôle MANAGER
        createRoleIfNotExists(
                Role.RoleName.MANAGER,
                "Gestionnaire - Gestion des opérations maritimes et des factures"
        );

        // Rôle USER
        createRoleIfNotExists(
                Role.RoleName.USER,
                "Utilisateur standard - Consultation des données et opérations de base"
        );

        log.info("Rôles système créés/vérifiés avec succès");
    }

    /**
     * Crée un rôle s'il n'existe pas déjà
     */
    private void createRoleIfNotExists(Role.RoleName roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = Role.builder()
                    .name(roleName)
                    .description(description)
                    .isActive(true)
                    .build();

            roleRepository.save(role);
            log.info("Rôle créé: {} - {}", roleName, description);
        } else {
            log.debug("Rôle déjà existant: {}", roleName);
        }
    }

    /**
     * Crée l'utilisateur admin par défaut
     */
    private void createDefaultAdminUser() {
        log.info("Vérification et création de l'utilisateur admin par défaut...");

        // Vérifier si l'utilisateur admin existe déjà
        if (userRepository.existsByUsernameIgnoreCase("admin")) {
            log.info("Utilisateur admin déjà existant - pas de création nécessaire");
            return;
        }

        // Récupérer le rôle ADMIN
        Role adminRole = roleRepository.findByName(Role.RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("Rôle ADMIN non trouvé. Impossible de créer l'utilisateur admin."));
        log.info("Rôle ADMIN trouvé - création de l'utilisateur admin...");
        // Créer l'utilisateur admin
        User adminUser = User.builder()
                .username("admin")
                .email("admin@salanevision.sn")
                .password(passwordEncoder.encode("Admin123!"))
                .firstName("Super")
                .lastName("Administrateur")
                .phone("+221 33 XXX XX XX")
                .isActive(true)
                .isEmailVerified(true) // Admin pré-vérifié
                .roles(Set.of(adminRole))
                .createdBy("SYSTEM_INIT")
                .build();

        userRepository.save(adminUser);

        log.info("✅ Utilisateur admin créé avec succès:");
        log.info("   👤 Username: admin");
        log.info("   📧 Email: admin@salanevision.sn");
        log.info("   🔐 Password: Admin123!");
        log.info("   🏢 Entreprise: Salane Vision S.a.r.l");
        log.info("   📍 Localisation: Dakar, Sénégal");
        log.info("");
        log.warn("⚠️  IMPORTANT: Changez le mot de passe par défaut après la première connexion !");
        log.info("");
    }
}
