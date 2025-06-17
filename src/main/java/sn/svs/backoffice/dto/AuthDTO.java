package sn.svs.backoffice.dto;

// ========== CLASSE PRINCIPALE AUTHDTO ==========

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Classe principale contenant tous les DTOs d'authentification JWT
 * Centralise toutes les classes de transfert de données pour l'authentification
 */
public class AuthDTO {

    // ========== LOGIN REQUEST ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {

        @NotBlank(message = "Le nom d'utilisateur est obligatoire")
        @Size(min = 3, max = 100, message = "Le nom d'utilisateur doit contenir entre 3 et 100 caractères")
        private String username; // Peut être username ou email

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        private String password;

        // Optionnel: se souvenir de moi (pour durée token plus longue)
        private Boolean rememberMe = false;
    }

    // ========== LOGIN RESPONSE ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoginResponse {

        private Boolean success;

        private String message;

        private String accessToken;

        private String refreshToken;

        private String tokenType = "Bearer";

        private Long expiresIn; // en minutes

        private UserInfoResponse user;

        private LocalDateTime timestamp = LocalDateTime.now();
    }

    // ========== REFRESH TOKEN REQUEST ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshRequest {

        @NotBlank(message = "Le token de rafraîchissement est obligatoire")
        private String refreshToken;
    }

    // ========== REFRESH TOKEN RESPONSE ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RefreshResponse {

        private Boolean success;

        private String message;

        private String accessToken;

        private String refreshToken;

        private String tokenType = "Bearer";

        private Long expiresIn; // en minutes

        private LocalDateTime timestamp = LocalDateTime.now();
    }

    // ========== TOKEN VALIDATION REQUEST ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TokenValidationRequest {

        @NotBlank(message = "Le token est obligatoire")
        private String token;
    }

    // ========== TOKEN VALIDATION RESPONSE ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TokenValidationResponse {

        private Boolean valid;

        private String message;

        private Map<String, Object> tokenInfo;

        private LocalDateTime timestamp = LocalDateTime.now();
    }

    // ========== USER INFO RESPONSE ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfoResponse {

        private Long id;

        private String username;

        private String email;

        private String firstName;

        private String lastName;

        private String phone;

        private List<String> roles;

        private Boolean isActive;

        private Boolean isEmailVerified;

        private LocalDateTime lastLogin;

        private LocalDateTime createdAt;

        // Méthode utilitaire pour le nom complet
        public String getFullName() {
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            }
            return username;
        }
    }

    // ========== CHANGE PASSWORD REQUEST ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChangePasswordRequest {

        @NotBlank(message = "Le mot de passe actuel est obligatoire")
        private String currentPassword;

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 8, message = "Le nouveau mot de passe doit contenir au moins 8 caractères")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Le mot de passe doit contenir au moins une minuscule, une majuscule, un chiffre et un caractère spécial")
        private String newPassword;

        @NotBlank(message = "La confirmation du mot de passe est obligatoire")
        private String confirmPassword;

        // Validation personnalisée pour vérifier que les mots de passe correspondent
        public boolean isPasswordsMatching() {
            return newPassword != null && newPassword.equals(confirmPassword);
        }
    }

    // ========== FORGOT PASSWORD REQUEST ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForgotPasswordRequest {

        @NotBlank(message = "L'adresse email est obligatoire")
        @Email(message = "Format d'email invalide")
        @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
        private String email;
    }

    // ========== RESET PASSWORD REQUEST ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResetPasswordRequest {

        @NotBlank(message = "Le token de réinitialisation est obligatoire")
        private String token;

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Le mot de passe doit contenir au moins une minuscule, une majuscule, un chiffre et un caractère spécial")
        private String newPassword;

        @NotBlank(message = "La confirmation du mot de passe est obligatoire")
        private String confirmPassword;

        // Validation personnalisée pour vérifier que les mots de passe correspondent
        public boolean isPasswordsMatching() {
            return newPassword != null && newPassword.equals(confirmPassword);
        }
    }

    // ========== REGISTER REQUEST ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {

        @NotBlank(message = "Le nom d'utilisateur est obligatoire")
        @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "Le nom d'utilisateur ne peut contenir que des lettres, chiffres, points, tirets et underscores")
        private String username;

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
        private String email;

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Le mot de passe doit contenir au moins une minuscule, une majuscule, un chiffre et un caractère spécial")
        private String password;

        @NotBlank(message = "La confirmation du mot de passe est obligatoire")
        private String confirmPassword;

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
        private String firstName;

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
        private String lastName;

        @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
        private String phone;

        // Validation personnalisée
        public boolean isPasswordsMatching() {
            return password != null && password.equals(confirmPassword);
        }
    }

    // ========== REGISTER RESPONSE ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RegisterResponse {

        private Boolean success;

        private String message;

        private UserInfoResponse user;

        private LocalDateTime timestamp = LocalDateTime.now();

        // Instructions pour l'utilisateur
        private String nextStep;
    }

    // ========== EMAIL VERIFICATION REQUEST ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmailVerificationRequest {

        @NotBlank(message = "Le token de vérification est obligatoire")
        private String token;
    }

    // ========== RESEND VERIFICATION EMAIL REQUEST ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResendVerificationRequest {

        @NotBlank(message = "L'adresse email est obligatoire")
        @Email(message = "Format d'email invalide")
        private String email;
    }

    // ========== GENERIC RESPONSE ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GenericResponse {

        private Boolean success;

        private String message;

        private Map<String, Object> data;

        private LocalDateTime timestamp = LocalDateTime.now();
    }

    // ========== LOGOUT REQUEST ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LogoutRequest {

        // Optionnel: token à invalider (pour blacklist future)
        private String token;

        // Optionnel: déconnecter de tous les appareils
        private Boolean logoutAll = false;
    }

    // ========== ACCOUNT STATUS REQUEST ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountStatusRequest {

        @NotBlank(message = "L'identifiant utilisateur est obligatoire")
        private String username;
    }

    // ========== ACCOUNT STATUS RESPONSE ==========
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AccountStatusResponse {

        private Boolean exists;

        private Boolean isActive;

        private Boolean isEmailVerified;

        private Boolean isLocked;

        private LocalDateTime lockedUntil;

        private Integer failedAttempts;

        private String message;

        private LocalDateTime timestamp = LocalDateTime.now();
    }
}


