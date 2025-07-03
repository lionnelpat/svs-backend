package sn.svs.backoffice.web.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import sn.svs.backoffice.dto.AuthDTO;
import sn.svs.backoffice.dto.ErrorDTO;
import sn.svs.backoffice.exceptions.AuthExceptionHandler;
import sn.svs.backoffice.security.jwt.JwtUtils;
import sn.svs.backoffice.service.AuthService;
import sn.svs.backoffice.service.UserDetailsService;
import sn.svs.backoffice.domain.User;
import sn.svs.backoffice.service.UserReloadService;

import java.time.LocalDateTime;
import java.util.List;
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
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthService authService;
    private final UserDetailsService userDetailsService;
    private final UserReloadService userReloadService;

    @Autowired
    private AuthExceptionHandler authExceptionHandler;

    @RequestMapping(value = "/login", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }


    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody AuthDTO.LoginRequest loginRequest,
                                        HttpServletRequest request) {
        try {
            log.info("Tentative de connexion pour l'utilisateur: {}", loginRequest.getUsername());

            String clientIp = getClientIpAddress(request);

            // Authentifier l'utilisateur
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Recharger l'utilisateur avec ses rôles
            User user = userReloadService.reloadUserWithRoles(loginRequest.getUsername());

            log.info("Utilisateur rechargé avec {} rôle(s): {}",
                    user.getRoles().size(),
                    user.getRoles().stream().map(r -> r.getName().name()).toList());

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

            log.info("Connexion réussie pour l'utilisateur: {} depuis l'IP: {} avec rôles: {}",
                    user.getUsername(), clientIp, response.getUser().getRoles());

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return authExceptionHandler.handleAuthenticationException(e, request, loginRequest.getUsername());
        } catch (Exception e) {
            log.error("Erreur système lors de la connexion pour {}: {}", loginRequest.getUsername(), e.getMessage());

            ErrorDTO.Response errorResponse = ErrorDTO.Response.builder()
                    .path(request.getRequestURI())
                    .code("INTERNAL_SERVER_ERROR")
                    .method(request.getMethod())
                    .success(false)
                    .suggestions(ErrorDTO.Suggestion.builder()
                            .endpoint("/api/v1/auth/login")
                            .action("retry")
                            .message("Réessayez dans quelques instants. Si le problème persiste, contactez le support.")
                            .build())
                    .error(true)
                    .message("Erreur système temporaire. Veuillez réessayer.")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    /**
     * Informations de l'utilisateur connecté - CORRIGÉE
     */
    @GetMapping("/user-info")
    public ResponseEntity<AuthDTO.UserInfoResponse> getUserInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User userFromAuth = (User) authentication.getPrincipal();

                log.debug("User depuis authentication: {} (rôles: {})",
                        userFromAuth.getUsername(),
                        userFromAuth.getRoles() != null ? userFromAuth.getRoles().size() : "null");

                User user = userReloadService.reloadUserWithRoles(userFromAuth);

                log.info("User rechargé: {} avec {} rôle(s): {}",
                        user.getUsername(),
                        user.getRoles().size(),
                        user.getRoles().stream().map(r -> r.getName().name()).toList());

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

    // ========== AUTRES MÉTHODES INCHANGÉES ==========

    /**
     * Rafraîchissement du token d'accès - CORRIGÉ
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthDTO.RefreshResponse> refreshToken(@Valid @RequestBody AuthDTO.RefreshRequest refreshRequest) {
        try {
            log.debug("Demande de rafraîchissement de token");

            String refreshToken = refreshRequest.getRefreshToken();

            if (!jwtUtils.validateToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthDTO.RefreshResponse.builder()
                                .success(false)
                                .message("Token de rafraîchissement invalide")
                                .build());
            }

            if (!jwtUtils.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthDTO.RefreshResponse.builder()
                                .success(false)
                                .message("Type de token incorrect")
                                .build());
            }

            String username = jwtUtils.getUsernameFromToken(refreshToken);

            // CORRECTION: Recharger l'utilisateur avec ses rôles
            User user = userReloadService.reloadUserWithRoles(username);

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

