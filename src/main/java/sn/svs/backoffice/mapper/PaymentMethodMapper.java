package sn.svs.backoffice.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import sn.svs.backoffice.domain.PaymentMethod;
import sn.svs.backoffice.dto.PaymentMethodDTO;

import java.util.List;

/**
 * Mapper pour convertir entre les entités PaymentMethod et leurs DTOs
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PaymentMethodMapper {

    /**
     * Convertit un PaymentMethodCreateDTO en entité PaymentMethod
     */
    @Mapping(target = "id", ignore = true)
    PaymentMethod toEntity(PaymentMethodDTO.CreateRequest createDTO);

    /**
     * Convertit une entité PaymentMethod en PaymentMethodDTO
     */
    PaymentMethodDTO.Response toDTO(PaymentMethod paymentMethod);

    /**
     * Convertit une liste d'entités PaymentMethod en liste de DTOs
     */
    List<PaymentMethodDTO.Response> toDTOList(List<PaymentMethod> paymentMethods);


    List<PaymentMethodDTO.Response> toResponseList(List<PaymentMethod> paymentMethods);

    /**
     * Convertit une entité Operation en Summary
     */
    PaymentMethodDTO.Summary toSummary(PaymentMethod paymentMethod);

    /**
     * Convertit une liste d'entités Operation en liste de Summary
     */
    List<PaymentMethodDTO.Summary> toSummaryList(List<PaymentMethod> paymentMethods);

    /**
     * Met à jour une entité PaymentMethod existante avec les données d'un PaymentMethodUpdateDTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true) // Le code ne peut pas être modifié
    void updateEntityFromDTO(PaymentMethodDTO.UpdateRequest updateDTO, @MappingTarget PaymentMethod paymentMethod);

    /**
     * Met à jour une entité Operation existante avec les données d'UpdateRequest
     */
    @Mapping(target = "id", ignore = true)
    void updateEntity(PaymentMethodDTO.UpdateRequest request, @MappingTarget PaymentMethod paymentMethod);

    /**
     * Convertit une Page d'entités Operation en PageResponse
     */
    default PaymentMethodDTO.PageResponse toPageResponse(Page<PaymentMethod> page) {
        if (page == null) {
            return null;
        }

        return PaymentMethodDTO.PageResponse.builder()
                .paymentMethods(toResponseList(page.getContent()))
                .totalElements(page.getTotalElements())
                .currentPage(page.getNumber())
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
    default String buildSearchCriteria(PaymentMethodDTO.SearchFilter filter) {
        if (filter == null || filter.getSearch() == null || filter.getSearch().trim().isEmpty()) {
            return null;
        }
        return "%" + filter.getSearch().trim().toLowerCase() + "%";
    }

}
