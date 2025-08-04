// ========== ROLE MAPPER ==========
package sn.svs.backoffice.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import sn.svs.backoffice.domain.Role;
import sn.svs.backoffice.dto.RoleDTO;

import java.util.List;

/**
 * Mapper pour l'entité Role
 * SVS - Dakar, Sénégal
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface RoleMapper {

    /**
     * Convertit une CreateRequest en entité Role
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "users", ignore = true)
    Role toEntity(RoleDTO.CreateRequest request);

    /**
     * Convertit une entité Role en Response
     */
    @Mapping(target = "userCount", ignore = true)
    RoleDTO.Response toResponse(Role role);

    /**
     * Convertit une liste d'entités Role en liste de Response
     */
    List<RoleDTO.Response> toResponseList(List<Role> roles);

    /**
     * Convertit une entité Role en Summary
     */
    RoleDTO.Summary toSummary(Role role);

    /**
     * Convertit une liste d'entités Role en liste de Summary
     */
    List<RoleDTO.Summary> toSummaryList(List<Role> roles);

    /**
     * Met à jour une entité Role existante avec les données d'UpdateRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "users", ignore = true)
    void updateEntity(RoleDTO.UpdateRequest request, @MappingTarget Role role);

    /**
     * Convertit une Page d'entités Role en PageResponse
     */
    default RoleDTO.PageResponse toPageResponse(Page<Role> page) {
        if (page == null) {
            return null;
        }

        return RoleDTO.PageResponse.builder()
                .roles(toResponseList(page.getContent()))
                .total(page.getTotalElements())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * Convertit un SearchFilter en critères pour la recherche
     */
    default String buildSearchCriteria(RoleDTO.SearchFilter filter) {
        if (filter == null || filter.getSearch() == null || filter.getSearch().trim().isEmpty()) {
            return null;
        }
        return "%" + filter.getSearch().trim().toLowerCase() + "%";
    }
}
