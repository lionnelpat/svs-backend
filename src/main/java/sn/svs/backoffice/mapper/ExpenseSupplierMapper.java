package sn.svs.backoffice.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import sn.svs.backoffice.domain.ExpenseSupplier;
import sn.svs.backoffice.dto.ExpenseSupplierDTO;

import java.util.List;

/**
 * Mapper pour l'entité ExpenseSupplier
 * SVS - Dakar, Sénégal
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface ExpenseSupplierMapper {

    /**
     * Convertit une CreateRequest en entité ExpenseSupplier
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    ExpenseSupplier toEntity(ExpenseSupplierDTO.CreateRequest request);

    /**
     * Convertit une entité ExpenseSupplier en Response
     */
    ExpenseSupplierDTO.Response toResponse(ExpenseSupplier supplier);

    /**
     * Convertit une liste d'entités ExpenseSupplier en liste de Response
     */
    List<ExpenseSupplierDTO.Response> toResponseList(List<ExpenseSupplier> suppliers);

    /**
     * Convertit une entité ExpenseSupplier en Summary
     */
    ExpenseSupplierDTO.Summary toSummary(ExpenseSupplier supplier);

    /**
     * Convertit une liste d'entités ExpenseSupplier en liste de Summary
     */
    List<ExpenseSupplierDTO.Summary> toSummaryList(List<ExpenseSupplier> suppliers);

    /**
     * Met à jour une entité ExpenseSupplier existante avec les données d'UpdateRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ExpenseSupplierDTO.UpdateRequest request, @MappingTarget ExpenseSupplier supplier);

    /**
     * Convertit une Page d'entités ExpenseSupplier en PageResponse
     */
    default ExpenseSupplierDTO.PageResponse toPageResponse(Page<ExpenseSupplier> page) {
        if (page == null) {
            return null;
        }

        return ExpenseSupplierDTO.PageResponse.builder()
                .suppliers(toResponseList(page.getContent()))
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
    default String buildSearchCriteria(ExpenseSupplierDTO.SearchFilter filter) {
        if (filter == null || filter.getSearch() == null || filter.getSearch().trim().isEmpty()) {
            return null;
        }
        return "%" + filter.getSearch().trim().toLowerCase() + "%";
    }
}
