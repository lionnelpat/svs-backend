// ========== ROLE DTO ==========
package sn.svs.backoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import sn.svs.backoffice.domain.Role;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs pour l'entité Role
 * SVS - Dakar, Sénégal
 */
public class RoleDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {

        @NotNull(message = "Le nom du rôle est obligatoire")
        private Role.RoleName name;

        @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères")
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {

        @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères")
        private String description;

        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        private Long id;
        private Role.RoleName name;
        private String description;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long userCount;

        public String getDisplayName() {
            if (name != null) {
                switch (name) {
                    case ADMIN: return "Administrateur";
                    case MANAGER: return "Gestionnaire";
                    case USER: return "Utilisateur";
                    default: return name.name();
                }
            }
            return "Inconnu";
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {

        private Long id;
        private Role.RoleName name;
        private String description;
        private Boolean isActive;

        public String getDisplayName() {
            if (name != null) {
                switch (name) {
                    case ADMIN: return "Administrateur";
                    case MANAGER: return "Gestionnaire";
                    case USER: return "Utilisateur";
                    default: return name.name();
                }
            }
            return "Inconnu";
        }

        public static Summary fromEntity(Role role) {
            return Summary.builder()
                    .id(role.getId())
                    .name(role.getName()) // Si tu veux juste ADMIN / USER etc.
                    .description(role.getDescription())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchFilter {

        private String search;
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {

        private List<Response> roles;
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

