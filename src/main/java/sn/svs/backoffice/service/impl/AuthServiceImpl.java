package sn.svs.backoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.svs.backoffice.domain.Role;
import sn.svs.backoffice.domain.User;
import sn.svs.backoffice.dto.AuthDTO;
import sn.svs.backoffice.repository.RoleRepository;
import sn.svs.backoffice.repository.UserRepository;
import sn.svs.backoffice.service.AuthService;
import sn.svs.backoffice.service.EmailService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implémentation du service d'authentification
 * Gère toutes les opérations d'authentification et de gestion des comptes utilisateurs
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // Service d'envoi d'emails (à créer)

    /**
     * Inscription d'un nouvel utilisateur
     */
    @Override
    @Transactional
    public AuthDTO.RegisterResponse register(AuthDTO.RegisterRequest registerRequest) {
        try {
            log.info("Tentative d'inscription pour l'utilisateur: {}", registerRequest.getUsername());

            // Vérifier que les mots de passe correspondent
            if (!registerRequest.isPasswordsMatching()) {
                return AuthDTO.RegisterResponse.builder()
                        .success(false)
                        .message("Les mots de passe ne correspondent pas")
                        .build();
            }

            // Vérifier l'unicité du nom d'utilisateur
            if (userRepository.existsByUsernameIgnoreCase(registerRequest.getUsername())) {
                return AuthDTO.RegisterResponse.builder()
                        .success(false)
                        .message("Ce nom d'utilisateur est déjà utilisé")
                        .build();
            }

            // Vérifier l'unicité de l'email
            if (userRepository.existsByEmailIgnoreCase(registerRequest.getEmail())) {
                return AuthDTO.RegisterResponse.builder()
                        .success(false)
                        .message("Cette adresse email est déjà utilisée")
                        .build();
            }

            // Récupérer le rôle USER par défaut
            Role userRole = roleRepository.findUserRole()
                    .orElseThrow(() -> new RuntimeException("Rôle USER non trouvé dans la base de données"));

            // Créer le nouvel utilisateur
            User newUser = User.builder()
                    .username(registerRequest.getUsername())
                    .email(registerRequest.getEmail().toLowerCase())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .firstName(registerRequest.getFirstName())
                    .lastName(registerRequest.getLastName())
                    .phone(registerRequest.getPhone())
                    .isActive(true)
                    .isEmailVerified(false)
                    .emailVerificationToken(generateVerificationToken())
                    .roles(Set.of(userRole))
                    .createdBy("SYSTEM")
                    .build();

            // Sauvegarder l'utilisateur
            User savedUser = userRepository.save(newUser);

            // Envoyer l'email de vérification
            sendVerificationEmail(savedUser);

            // Construire la réponse
            AuthDTO.UserInfoResponse userInfo = AuthDTO.UserInfoResponse.builder()
                    .id(savedUser.getId())
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .firstName(savedUser.getFirstName())
                    .lastName(savedUser.getLastName())
                    .phone(savedUser.getPhone())
                    .roles(savedUser.getRoles().stream()
                            .map(role -> role.getName().name())
                            .toList())
                    .isActive(savedUser.getIsActive())
                    .isEmailVerified(savedUser.getIsEmailVerified())
                    .createdAt(savedUser.getCreatedAt())
                    .build();

            log.info("Inscription réussie pour l'utilisateur: {}", savedUser.getUsername());

            return AuthDTO.RegisterResponse.builder()
                    .success(true)
                    .message("Inscription réussie. Veuillez vérifier votre email.")
                    .user(userInfo)
                    .nextStep("Cliquez sur le lien de vérification envoyé à votre adresse email")
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de l'inscription pour {}: {}",
                    registerRequest.getUsername(), e.getMessage());

            return AuthDTO.RegisterResponse.builder()
                    .success(false)
                    .message("Erreur système lors de l'inscription")
                    .build();
        }
    }

    /**
     * Vérification de l'email avec un token
     */
    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        try {
            log.debug("Tentative de vérification d'email avec le token: {}", token.substring(0, 8) + "...");

            Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);

            if (userOpt.isEmpty()) {
                log.warn("Token de vérification d'email non trouvé: {}", token.substring(0, 8) + "...");
                return false;
            }

            User user = userOpt.get();

            // Vérifier si l'email n'est pas déjà vérifié
            if (user.getIsEmailVerified()) {
                log.info("Email déjà vérifié pour l'utilisateur: {}", user.getUsername());
                return true;
            }

            // Marquer l'email comme vérifié
            user.setIsEmailVerified(true);
            user.setEmailVerificationToken(null);
            user.setUpdatedAt(LocalDateTime.now());
            user.setUpdatedBy("EMAIL_VERIFICATION");

            userRepository.save(user);

            log.info("Email vérifié avec succès pour l'utilisateur: {}", user.getUsername());
            return true;

        } catch (Exception e) {
            log.error("Erreur lors de la vérification d'email: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Renvoie un email de vérification
     */
    @Override
    @Transactional
    public boolean resendVerificationEmail(String email) {
        try {
            log.debug("Demande de renvoi d'email de vérification pour: {}", email);

            Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

            if (userOpt.isEmpty()) {
                log.warn("Tentative de renvoi d'email de vérification pour un email inexistant: {}", email);
                return false; // Ne pas révéler si l'email existe ou non
            }

            User user = userOpt.get();

            // Vérifier si l'email n'est pas déjà vérifié
            if (user.getIsEmailVerified()) {
                log.info("Tentative de renvoi d'email de vérification pour un email déjà vérifié: {}", email);
                return true; // Considérer comme succès
            }

            // Générer un nouveau token de vérification
            user.setEmailVerificationToken(generateVerificationToken());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            // Envoyer l'email de vérification
            sendVerificationEmail(user);

            log.info("Email de vérification renvoyé avec succès pour: {}", email);
            return true;

        } catch (Exception e) {
            log.error("Erreur lors du renvoi d'email de vérification pour {}: {}", email, e.getMessage());
            return false;
        }
    }

    /**
     * Initie une réinitialisation de mot de passe
     */
    @Override
    @Transactional
    public boolean initiatePasswordReset(String email) {
        try {
            log.debug("Demande de réinitialisation de mot de passe pour: {}", email);

            Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

            if (userOpt.isEmpty()) {
                log.warn("Tentative de réinitialisation pour un email inexistant: {}", email);
                return true; // Ne pas révéler si l'email existe ou non (anti-enumeration)
            }

            User user = userOpt.get();

            // Vérifier que le compte est actif
            if (!user.getIsActive()) {
                log.warn("Tentative de réinitialisation pour un compte désactivé: {}", email);
                return true; // Ne pas révéler l'état du compte
            }

            // Générer un token de réinitialisation
            String resetToken = generateResetToken();
            LocalDateTime expiry = LocalDateTime.now().plusHours(1); // Expire dans 1 heure

            user.setPasswordResetToken(resetToken);
            user.setPasswordResetTokenExpiry(expiry);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            // Envoyer l'email de réinitialisation
            sendPasswordResetEmail(user, resetToken);

            log.info("Email de réinitialisation de mot de passe envoyé pour: {}", email);
            return true;

        } catch (Exception e) {
            log.error("Erreur lors de l'initiation de réinitialisation pour {}: {}", email, e.getMessage());
            return true; // Toujours retourner true pour éviter l'enumeration
        }
    }

    /**
     * Réinitialise le mot de passe avec un token
     */
    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        try {
            log.debug("Tentative de réinitialisation de mot de passe avec token: {}", token.substring(0, 8) + "...");

            Optional<User> userOpt = userRepository.findByPasswordResetToken(token);

            if (userOpt.isEmpty()) {
                log.warn("Token de réinitialisation non trouvé: {}", token.substring(0, 8) + "...");
                return false;
            }

            User user = userOpt.get();

            // Vérifier que le token n'est pas expiré
            if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
                log.warn("Token de réinitialisation expiré pour l'utilisateur: {}", user.getUsername());
                return false;
            }

            // Encoder le nouveau mot de passe
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordResetToken(null);
            user.setPasswordResetTokenExpiry(null);
            user.setUpdatedAt(LocalDateTime.now());
            user.setUpdatedBy("PASSWORD_RESET");

            // Réinitialiser les tentatives de connexion échouées
            user.resetLoginAttempts();

            userRepository.save(user);

            log.info("Mot de passe réinitialisé avec succès pour l'utilisateur: {}", user.getUsername());
            return true;

        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation de mot de passe: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Change le mot de passe d'un utilisateur connecté
     */
    @Override
    @Transactional
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        try {
            log.debug("Demande de changement de mot de passe pour l'utilisateur ID: {}", userId);

            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                log.warn("Utilisateur non trouvé pour le changement de mot de passe: {}", userId);
                return false;
            }

            User user = userOpt.get();

            // Vérifier le mot de passe actuel
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                log.warn("Mot de passe actuel incorrect pour l'utilisateur: {}", user.getUsername());
                return false;
            }

            // Vérifier que le nouveau mot de passe est différent
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                log.warn("Tentative de changement avec le même mot de passe pour: {}", user.getUsername());
                return false;
            }

            // Encoder et sauvegarder le nouveau mot de passe
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            user.setUpdatedBy("PASSWORD_CHANGE");

            // Réinitialiser les tentatives de connexion échouées
            user.resetLoginAttempts();

            userRepository.save(user);

            log.info("Mot de passe changé avec succès pour l'utilisateur: {}", user.getUsername());
            return true;

        } catch (Exception e) {
            log.error("Erreur lors du changement de mot de passe pour l'utilisateur {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie le statut d'un compte utilisateur
     */
    @Override
    @Transactional(readOnly = true)
    public AuthDTO.AccountStatusResponse getAccountStatus(String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsernameOrEmailIgnoreCase(username);

            if (userOpt.isEmpty()) {
                return AuthDTO.AccountStatusResponse.builder()
                        .exists(false)
                        .message("Utilisateur non trouvé")
                        .build();
            }

            User user = userOpt.get();
            boolean isLocked = user.getAccountLockedUntil() != null &&
                    user.getAccountLockedUntil().isAfter(LocalDateTime.now());

            return AuthDTO.AccountStatusResponse.builder()
                    .exists(true)
                    .isActive(user.getIsActive())
                    .isEmailVerified(user.getIsEmailVerified())
                    .isLocked(isLocked)
                    .lockedUntil(user.getAccountLockedUntil())
                    .failedAttempts(user.getLoginAttempts())
                    .message("Statut du compte récupéré avec succès")
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de la récupération du statut pour {}: {}", username, e.getMessage());
            return AuthDTO.AccountStatusResponse.builder()
                    .exists(false)
                    .message("Erreur lors de la récupération du statut")
                    .build();
        }
    }

    /**
     * Active ou désactive un compte utilisateur (Admin uniquement)
     */
    @Override
    @Transactional
    public boolean toggleUserStatus(Long userId, boolean isActive) {
        try {
            log.info("Changement de statut pour l'utilisateur ID {}: {}", userId, isActive ? "ACTIF" : "INACTIF");

            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                log.warn("Utilisateur non trouvé pour changement de statut: {}", userId);
                return false;
            }

            User user = userOpt.get();
            user.setIsActive(isActive);
            user.setUpdatedAt(LocalDateTime.now());
            user.setUpdatedBy("ADMIN_STATUS_CHANGE");

            userRepository.save(user);

            log.info("Statut changé avec succès pour l'utilisateur: {} -> {}",
                    user.getUsername(), isActive ? "ACTIF" : "INACTIF");
            return true;

        } catch (Exception e) {
            log.error("Erreur lors du changement de statut pour l'utilisateur {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Déverrouille un compte utilisateur (Admin uniquement)
     */
    @Override
    @Transactional
    public boolean unlockUserAccount(Long userId) {
        try {
            log.info("Déverrouillage du compte utilisateur ID: {}", userId);

            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                log.warn("Utilisateur non trouvé pour déverrouillage: {}", userId);
                return false;
            }

            User user = userOpt.get();
            user.resetLoginAttempts(); // Cette méthode reset aussi accountLockedUntil
            user.setUpdatedAt(LocalDateTime.now());
            user.setUpdatedBy("ADMIN_UNLOCK");

            userRepository.save(user);

            log.info("Compte déverrouillé avec succès pour l'utilisateur: {}", user.getUsername());
            return true;

        } catch (Exception e) {
            log.error("Erreur lors du déverrouillage pour l'utilisateur {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si un nom d'utilisateur est disponible
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        boolean available = !userRepository.existsByUsernameIgnoreCase(username);
        log.debug("Vérification de disponibilité du username '{}': {}", username, available);
        return available;
    }

    /**
     * Vérifie si un email est disponible
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        boolean available = !userRepository.existsByEmailIgnoreCase(email);
        log.debug("Vérification de disponibilité de l'email '{}': {}", email, available);
        return available;
    }

    /**
     * Nettoie les tokens expirés (tâche de maintenance)
     */
    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            log.info("Début du nettoyage des tokens expirés");

            LocalDateTime now = LocalDateTime.now();
            int cleanedCount = userRepository.cleanupExpiredResetTokens(now, now);

            log.info("Nettoyage terminé: {} tokens de réinitialisation expirés supprimés", cleanedCount);

        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des tokens expirés: {}", e.getMessage());
        }
    }

    // ========== MÉTHODES PRIVÉES UTILITAIRES ==========

    /**
     * Génère un token de vérification d'email unique
     */
    private String generateVerificationToken() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }

    /**
     * Génère un token de réinitialisation de mot de passe unique
     */
    private String generateResetToken() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }

/**
 * Envoie un email de vérification
 */
private void sendVerificationEmail(User user) {
    try {
        String subject = "Vérification de votre compte SVS Backoffice";
        String verificationUrl = buildVerificationUrl(user.getEmailVerificationToken());

        String emailContent = buildVerificationEmailContent(user.getFirstName(), verificationUrl);

        emailService.sendEmail(user.getEmail(), subject, emailContent);

        log.debug("Email de vérification envoyé à: {}", user.getEmail());

    } catch (Exception e) {
        log.error("Erreur lors de l'envoi de l'email de vérification pour {}: {}",
                user.getEmail(), e.getMessage());
    }
}

    /**
     * Envoie un email de réinitialisation de mot de passe
     */
    private void sendPasswordResetEmail(User user, String resetToken) {
        try {
            String subject = "Réinitialisation de votre mot de passe SVS Backoffice";
            String resetUrl = buildResetUrl(resetToken);

            String emailContent = buildResetEmailContent(user.getFirstName(), resetUrl);

            emailService.sendEmail(user.getEmail(), subject, emailContent);

            log.debug("Email de réinitialisation envoyé à: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation pour {}: {}",
                    user.getEmail(), e.getMessage());
        }
    }

    /**
     * Construit l'URL de vérification d'email
     */
    private String buildVerificationUrl(String token) {
        // URL du frontend Angular
        return "http://localhost:4200/auth/verify-email?token=" + token;
    }

    /**
     * Construit l'URL de réinitialisation de mot de passe
     */
    private String buildResetUrl(String token) {
        // URL du frontend Angular
        return "http://localhost:4200/auth/reset-password?token=" + token;
    }

    /**
     * Construit le contenu de l'email de vérification
     */
    private String buildVerificationEmailContent(String firstName, String verificationUrl) {
        return String.format("""
            Bonjour %s,
            
            Bienvenue sur SVS Backoffice - Système de gestion des prestations maritimes.
            
            Pour activer votre compte, veuillez cliquer sur le lien suivant :
            %s
            
            Ce lien est valide pendant 24 heures.
            
            Si vous n'avez pas créé ce compte, vous pouvez ignorer cet email.
            
            Cordialement,
            L'équipe SVS Backoffice
            """, firstName, verificationUrl);
    }

    /**
     * Construit le contenu de l'email de réinitialisation
     */
    private String buildResetEmailContent(String firstName, String resetUrl) {
        return String.format("""
            Bonjour %s,
            
            Vous avez demandé une réinitialisation de votre mot de passe pour SVS Backoffice.
            
            Pour créer un nouveau mot de passe, veuillez cliquer sur le lien suivant :
            %s
            
            Ce lien est valide pendant 1 heure.
            
            Si vous n'avez pas demandé cette réinitialisation, vous pouvez ignorer cet email en toute sécurité.
            
            Cordialement,
            L'équipe SVS Backoffice
            """, firstName, resetUrl);
    }
}
