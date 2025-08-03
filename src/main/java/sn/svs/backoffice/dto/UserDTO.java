// ========== USER DTO ==========
package sn.svs.backoffice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.svs.backoffice.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTOs pour l'entité User
 * SVS - Dakar, Sénégal
 */
public class UserDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {

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

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
        private String firstName;

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
        private String lastName;

        @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
        private String phone;

        @NotEmpty(message = "Au moins un rôle doit être assigné")
        private Set<Long> roleIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {

        @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "Le nom d'utilisateur ne peut contenir que des lettres, chiffres, points, tirets et underscores")
        private String username;

        @Email(message = "Format d'email invalide")
        @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
        private String email;

        @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
        private String firstName;

        @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
        private String lastName;

        @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
        private String phone;

        private Boolean isActive;
        private Boolean isEmailVerified;

        private Set<Long> roleIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private Boolean isActive;
        private Boolean isEmailVerified;
        private LocalDateTime lastLogin;
        private Integer loginAttempts;
        private LocalDateTime accountLockedUntil;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;

        private List<RoleDTO.Summary> roles;

        public String getFullName() {
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            }
            return username;
        }

        public boolean isAccountLocked() {
            return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
        }

        public String getStatusLabel() {
            if (!isActive) return "Inactif";
            if (!isEmailVerified) return "Email non vérifié";
            if (isAccountLocked()) return "Compte verrouillé";
            return "Actif";
        }

        public static UserDTO.Response fromEntity(User user) {
            return UserDTO.Response.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .phone(user.getPhone())
                    .isActive(user.getIsActive())
                    .isEmailVerified(user.getIsEmailVerified())
                    .lastLogin(user.getLastLogin())
                    .loginAttempts(user.getLoginAttempts())
                    .accountLockedUntil(user.getAccountLockedUntil())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .createdBy(user.getCreatedBy())
                    .updatedBy(user.getUpdatedBy())
                    .roles(
                            user.getRoles().stream()
                                    .map(RoleDTO.Summary::fromEntity)
                                    .collect(Collectors.toList())
                    )
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {

        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private Boolean isActive;

        public String getFullName() {
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            }
            return username;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchFilter {

        private String search;
        private Boolean isActive;
        private Boolean isEmailVerified;
        private String roleName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {

        private List<Response> users;
        private long total;
        private int page;
        private int size;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}
