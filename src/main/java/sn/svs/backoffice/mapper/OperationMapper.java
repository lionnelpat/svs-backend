package sn.svs.backoffice.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import sn.svs.backoffice.domain.Operation;
import sn.svs.backoffice.dto.OperationDTO;

import java.util.List;

/**
 * Mapper pour l'entité Operation
 * SVS - Dakar, Sénégal
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface OperationMapper {

    /**
     * Convertit une CreateRequest en entité Operation
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    Operation toEntity(OperationDTO.CreateRequest request);

    /**
     * Convertit une entité Operation en Response
     */
    OperationDTO.Response toResponse(Operation operation);

    /**
     * Convertit une liste d'entités Operation en liste de Response
     */
    List<OperationDTO.Response> toResponseList(List<Operation> operations);

    /**
     * Convertit une entité Operation en Summary
     */
    OperationDTO.Summary toSummary(Operation operation);

    /**
     * Convertit une liste d'entités Operation en liste de Summary
     */
    List<OperationDTO.Summary> toSummaryList(List<Operation> operations);

    /**
     * Met à jour une entité Operation existante avec les données d'UpdateRequest
     */
    @Mapping(target = "id", ignore = true)
    void updateEntity(OperationDTO.UpdateRequest request, @MappingTarget Operation operation);

    /**
     * Convertit une Page d'entités Operation en PageResponse
     */
    default OperationDTO.PageResponse toPageResponse(Page<Operation> page) {
        if (page == null) {
            return null;
        }

        return OperationDTO.PageResponse.builder()
                .operations(toResponseList(page.getContent()))
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
    default String buildSearchCriteria(OperationDTO.SearchFilter filter) {
        if (filter == null || filter.getSearch() == null || filter.getSearch().trim().isEmpty()) {
            return null;
        }
        return "%" + filter.getSearch().trim().toLowerCase() + "%";
    }
}
