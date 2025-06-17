package sn.svs.backoffice.web.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import sn.svs.backoffice.dto.AuthDTO;
import sn.svs.backoffice.security.jwt.JwtUtils;
import sn.svs.backoffice.service.AuthService;
import sn.svs.backoffice.service.UserDetailsService;
import sn.svs.backoffice.domain.User;

import java.util.Map;

/**
 * Contrôleur REST pour l'authentification JWT
 * Gère la connexion, déconnexion et rafraîchissement des tokens
 *
 * Endpoints disponibles :
 * - POST /api/v1/auth/login - Connexion utilisateur
 * - POST /api/v1/auth/refresh-token - Rafraîchissement du token
 * - POST /api/v1/auth/logout - Déconnexion
 * - POST /api/v1/auth/validate-token - Validation d'un token
 * - GET /api/v1/auth/user-info - Informations utilisateur connecté
 * - POST /api/v1/auth/change-password - Changement de mot de passe
 * - POST /api/v1/auth/forgot-password - Mot de passe oublié
 * - POST /api/v1/auth/reset-password - Reset du mot de passe
 * - POST /api/v1/auth/verify-email - Vérification email
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {"http://localhost:4200", "https://svs-backoffice.com"})
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthService authService;
    private final UserDetailsService userDetailsService;

    /**
     * Connexion utilisateur
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDTO.LoginResponse> login(@Valid @RequestBody AuthDTO.LoginRequest loginRequest,
                                                       HttpServletRequest request) {
        try {
            log.info("Tentative de connexion pour l'utilisateur: {}", loginRequest.getUsername());

            // Extraire l'IP du client pour logs de sécurité
            String clientIp = getClientIpAddress(request);

            // Authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Récupérer l'utilisateur authentifié
            User user = (User) authentication.getPrincipal();

            // Générer les tokens JWT
            String accessToken = jwtUtils.generateAccessToken(user);
            String refreshToken = jwtUtils.generateRefreshToken(user);

            // Mettre à jour la dernière connexion
            userDetailsService.updateLastLogin(user.getUsername());

            // Construire la réponse
            AuthDTO.LoginResponse response = AuthDTO.LoginResponse.builder()
                    .success(true)
                    .message("Connexion réussie")
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtils.getTimeToExpirationInMinutes(accessToken))
                    .user(AuthDTO.UserInfoResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .roles(user.getRoles().stream()
                                    .map(role -> role.getName().name())
                                    .toList())
                            .isActive(user.getIsActive())
                            .isEmailVerified(user.getIsEmailVerified())
                            .lastLogin(user.getLastLogin())
                            .build())
                    .build();

            log.info("Connexion réussie pour l'utilisateur: {} depuis l'IP: {}",
                    user.getUsername(), clientIp);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Tentative de connexion échouée - Identifiants incorrects pour: {}",
                    loginRequest.getUsername());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthDTO.LoginResponse.builder()
                            .success(false)
                            .message("Identifiants incorrects")
                            .build());

        } catch (DisabledException e) {
            log.warn("Tentative de connexion échouée - Compte désactivé pour: {}",
                    loginRequest.getUsername());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthDTO.LoginResponse.builder()
                            .success(false)
                            .message("Votre compte est désactivé. Contactez un administrateur.")
                            .build());

        } catch (AuthenticationException e) {
            log.error("Erreur d'authentification pour l'utilisateur: {} - {}",
                    loginRequest.getUsername(), e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthDTO.LoginResponse.builder()
                            .success(false)
                            .message("Erreur d'authentification: " + e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("Erreur inattendue lors de la connexion pour: {} - {}",
                    loginRequest.getUsername(), e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthDTO.LoginResponse.builder()
                            .success(false)
                            .message("Erreur système. Veuillez réessayer.")
                            .build());
        }
    }

    /**
     * Rafraîchissement du token d'accès
     * POST /api/v1/auth/refresh-token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthDTO.RefreshResponse> refreshToken(@Valid @RequestBody AuthDTO.RefreshRequest refreshRequest) {
        try {
            log.debug("Demande de rafraîchissement de token");

            String refreshToken = refreshRequest.getRefreshToken();

            // Valider le refresh token
            if (!jwtUtils.validateToken(refreshToken)) {
                log.warn("Tentative de rafraîchissement avec token invalide");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthDTO.RefreshResponse.builder()
                                .success(false)
                                .message("Token de rafraîchissement invalide")
                                .build());
            }

            // Vérifier que c'est bien un refresh token
            if (!jwtUtils.isRefreshToken(refreshToken)) {
                log.warn("Tentative de rafraîchissement avec un token d'accès");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthDTO.RefreshResponse.builder()
                                .success(false)
                                .message("Type de token incorrect")
                                .build());
            }

            // Extraire l'utilisateur du token
            String username = jwtUtils.getUsernameFromToken(refreshToken);
            User user = (User) userDetailsService.loadUserByUsername(username);

            // Générer un nouveau token d'accès
            String newAccessToken = jwtUtils.generateAccessToken(user);
            String newRefreshToken = jwtUtils.generateRefreshToken(user);

            AuthDTO.RefreshResponse response = AuthDTO.RefreshResponse.builder()
                    .success(true)
                    .message("Token rafraîchi avec succès")
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtils.getTimeToExpirationInMinutes(newAccessToken))
                    .build();

            log.debug("Token rafraîchi avec succès pour l'utilisateur: {}", username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors du rafraîchissement du token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthDTO.RefreshResponse.builder()
                            .success(false)
                            .message("Erreur lors du rafraîchissement du token")
                            .build());
        }
    }

    /**
     * Validation d'un token JWT
     * POST /api/v1/auth/validate-token
     */
    @PostMapping("/validate-token")
    public ResponseEntity<AuthDTO.TokenValidationResponse> validateToken(@Valid @RequestBody AuthDTO.TokenValidationRequest request) {
        try {
            String token = request.getToken();
            boolean isValid = jwtUtils.validateToken(token);

            if (isValid) {
                Map<String, Object> tokenInfo = jwtUtils.getTokenInfo(token);

                return ResponseEntity.ok(
                        AuthDTO.TokenValidationResponse.builder()
                                .valid(true)
                                .message("Token valide")
                                .tokenInfo(tokenInfo)
                                .build()
                );
            } else {
                return ResponseEntity.ok(
                        AuthDTO.TokenValidationResponse.builder()
                                .valid(false)
                                .message("Token invalide ou expiré")
                                .build()
                );
            }

        } catch (Exception e) {
            log.error("Erreur lors de la validation du token: {}", e.getMessage());
            return ResponseEntity.ok(
                    AuthDTO.TokenValidationResponse.builder()
                            .valid(false)
                            .message("Erreur lors de la validation")
                            .build()
            );
        }
    }

    /**
     * Informations de l'utilisateur connecté
     * GET /api/v1/auth/user-info
     */
    @GetMapping("/user-info")
    public ResponseEntity<AuthDTO.UserInfoResponse> getUserInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();

                AuthDTO.UserInfoResponse userInfo = AuthDTO.UserInfoResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .phone(user.getPhone())
                        .roles(user.getRoles().stream()
                                .map(role -> role.getName().name())
                                .toList())
                        .isActive(user.getIsActive())
                        .isEmailVerified(user.getIsEmailVerified())
                        .lastLogin(user.getLastLogin())
                        .createdAt(user.getCreatedAt())
                        .build();

                return ResponseEntity.ok(userInfo);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des informations utilisateur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Changement de mot de passe
     * POST /api/v1/auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@Valid @RequestBody AuthDTO.ChangePasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();

                // Déléguer au service d'authentification
                boolean success = authService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());

                if (success) {
                    log.info("Mot de passe changé avec succès pour l'utilisateur: {}", user.getUsername());
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Mot de passe modifié avec succès"
                    ));
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of(
                                    "success", false,
                                    "message", "Mot de passe actuel incorrect"
                            ));
                }
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", "Utilisateur non authentifié"
                    ));

        } catch (Exception e) {
            log.error("Erreur lors du changement de mot de passe: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Erreur système lors du changement de mot de passe"
                    ));
        }
    }

    /**
     * Mot de passe oublié
     * POST /api/v1/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody AuthDTO.ForgotPasswordRequest request) {
        try {
            boolean success = authService.initiatePasswordReset(request.getEmail());

            // Toujours retourner succès pour éviter l'énumération d'emails
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Si cette adresse email existe, vous recevrez un lien de réinitialisation"
            ));

        } catch (Exception e) {
            log.error("Erreur lors de l'initiation du reset de mot de passe: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Si cette adresse email existe, vous recevrez un lien de réinitialisation"
            ));
        }
    }

    /**
     * Reset du mot de passe
     * POST /api/v1/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody AuthDTO.ResetPasswordRequest request) {
        try {
            boolean success = authService.resetPassword(request.getToken(), request.getNewPassword());

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Mot de passe réinitialisé avec succès"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", false,
                                "message", "Token de réinitialisation invalide ou expiré"
                        ));
            }

        } catch (Exception e) {
            log.error("Erreur lors du reset de mot de passe: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Erreur système lors de la réinitialisation"
                    ));
        }
    }

    /**
     * Vérification d'email
     * POST /api/v1/auth/verify-email
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        try {
            boolean success = authService.verifyEmail(token);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Email vérifié avec succès"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "success", false,
                                "message", "Token de vérification invalide ou expiré"
                        ));
            }

        } catch (Exception e) {
            log.error("Erreur lors de la vérification d'email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Erreur système lors de la vérification"
                    ));
        }
    }

    /**
     * Déconnexion (optionnel - JWT est stateless)
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        try {
            // Dans un système JWT stateless, la déconnexion côté serveur est optionnelle
            // Le client doit simplement supprimer le token

            // On peut logger la déconnexion pour audit
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                log.info("Déconnexion de l'utilisateur: {} depuis l'IP: {}",
                        user.getUsername(), getClientIpAddress(request));
            }

            // Nettoyer le contexte de sécurité
            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Déconnexion réussie"
            ));

        } catch (Exception e) {
            log.error("Erreur lors de la déconnexion: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Déconnexion réussie"
            ));
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Extrait l'adresse IP réelle du client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}

