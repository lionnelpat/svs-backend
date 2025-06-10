package sn.svs.backoffice.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import sn.svs.backoffice.domain.ExpenseCategory;
import sn.svs.backoffice.dto.ExpenseCategoryDTO;

import java.util.List;

/**
 * Mapper pour l'entité ExpenseCategory
 * SVS - Dakar, Sénégal
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface ExpenseCategoryMapper {

    /**
     * Convertit une CreateRequest en entité ExpenseCategory
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", constant = "true")
    ExpenseCategory toEntity(ExpenseCategoryDTO.CreateRequest request);

    /**
     * Convertit une entité ExpenseCategory en Response
     */
    ExpenseCategoryDTO.Response toResponse(ExpenseCategory category);

    /**
     * Convertit une liste d'entités ExpenseCategory en liste de Response
     */
    List<ExpenseCategoryDTO.Response> toResponseList(List<ExpenseCategory> categories);

    /**
     * Convertit une entité ExpenseCategory en Summary
     */
    ExpenseCategoryDTO.Summary toSummary(ExpenseCategory category);

    /**
     * Convertit une liste d'entités ExpenseCategory en liste de Summary
     */
    List<ExpenseCategoryDTO.Summary> toSummaryList(List<ExpenseCategory> categories);

    /**
     * Met à jour une entité ExpenseCategory existante avec les données d'UpdateRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ExpenseCategoryDTO.UpdateRequest request, @MappingTarget ExpenseCategory category);

    /**
     * Convertit une Page d'entités ExpenseCategory en PageResponse
     */
    default ExpenseCategoryDTO.PageResponse toPageResponse(Page<ExpenseCategory> page) {
        if (page == null) {
            return null;
        }

        return ExpenseCategoryDTO.PageResponse.builder()
                .categories(toResponseList(page.getContent()))
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
    default String buildSearchCriteria(ExpenseCategoryDTO.SearchFilter filter) {
        if (filter == null || filter.getSearch() == null || filter.getSearch().trim().isEmpty()) {
            return null;
        }
        return "%" + filter.getSearch().trim().toLowerCase() + "%";
    }
}
