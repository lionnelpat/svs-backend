// ========== USER MAPPER ==========
package sn.svs.backoffice.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import sn.svs.backoffice.domain.Role;
import sn.svs.backoffice.domain.User;
import sn.svs.backoffice.dto.RoleDTO;
import sn.svs.backoffice.dto.UserDTO;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper pour l'entité User
 * SVS - Dakar, Sénégal
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        uses = {RoleMapper.class}
)
public interface UserMapper {

    /**
     * Convertit une CreateRequest en entité User
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Géré dans le service
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isEmailVerified", constant = "true")
    @Mapping(target = "loginAttempts", constant = "0")
    @Mapping(target = "emailVerificationToken", ignore = true)
    @Mapping(target = "passwordResetToken", ignore = true)
    @Mapping(target = "passwordResetTokenExpiry", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "accountLockedUntil", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "roles", ignore = true) // Géré dans le service
    User toEntity(UserDTO.CreateRequest request);

    /**
     * Convertit une entité User en Response
     */
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserDTO.Response toResponse(User user);



    /**
     * Convertit une liste d'entités User en liste de Response
     */
    List<UserDTO.Response> toResponseList(List<User> users);

    /**
     * Convertit une entité User en Summary
     */
    UserDTO.Summary toSummary(User user);

    /**
     * Convertit une liste d'entités User en liste de Summary
     */
    List<UserDTO.Summary> toSummaryList(List<User> users);

    /**
     * Met à jour une entité User existante avec les données d'UpdateRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "emailVerificationToken", ignore = true)
    @Mapping(target = "passwordResetToken", ignore = true)
    @Mapping(target = "passwordResetTokenExpiry", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "loginAttempts", ignore = true)
    @Mapping(target = "accountLockedUntil", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)// Géré dans le service
    void updateEntity(UserDTO.UpdateRequest request, @MappingTarget User user);

    /**
     * Convertit une Page d'entités User en PageResponse
     */
//    default UserDTO.PageResponse toPageResponse(Page<User> page) {
//        if (page == null) {
//            return null;
//        }
//
//        return UserDTO.PageResponse.builder()
//                .users(toResponseList(page.getContent()))
//                .total(page.getTotalElements())
//                .page(page.getNumber())
//                .size(page.getSize())
//                .totalPages(page.getTotalPages())
//                .first(page.isFirst())
//                .last(page.isLast())
//                .hasNext(page.hasNext())
//                .hasPrevious(page.hasPrevious())
//                .build();
//    }

    default UserDTO.PageResponse toPageResponse(Page<User> page) {
        List<UserDTO.Response> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return UserDTO.PageResponse.builder()
                .users(content)
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .totalPages(page.getNumber())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * Convertit un SearchFilter en critères pour la recherche
     */
    default String buildSearchCriteria(UserDTO.SearchFilter filter) {
        if (filter == null || filter.getSearch() == null || filter.getSearch().trim().isEmpty()) {
            return null;
        }
        return "%" + filter.getSearch().trim().toLowerCase() + "%";
    }


    // Méthode helper pour mapper les rôles
    default List<RoleDTO.Summary> mapRoles(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(RoleDTO.Summary::fromEntity)
                .collect(Collectors.toList());
    }
}
