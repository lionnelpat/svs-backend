package sn.svs.backoffice.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import sn.svs.backoffice.domain.Company;
import sn.svs.backoffice.dto.CompanyDTO;

import java.util.List;

/**
 * Mapper pour l'entité Company et ses DTOs
 * SVS - Dakar, Sénégal
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CompanyMapper {

    /**
     * Convertit CreateRequest vers Company entity
     */
    @Mapping(target = "id", ignore = true)
    Company toEntity(CompanyDTO.CreateRequest createRequest);

    /**
     * Convertit Company entity vers Response DTO
     */
    CompanyDTO.Response toResponse(Company company);

    /**
     * Convertit une liste d'entités vers une liste de DTOs Response
     */
    List<CompanyDTO.Response> toResponseList(List<Company> companies);

    /**
     * Met à jour une entité Company avec les données d'UpdateRequest
     * Les champs null dans le DTO ne sont pas mappés (IGNORE)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(CompanyDTO.UpdateRequest updateRequest, @MappingTarget Company company);

    /**
     * Convertit une Page Spring vers PageResponse DTO
     */
    default CompanyDTO.PageResponse toPageResponse(Page<Company> page) {
        if (page == null) {
            return null;
        }

        return CompanyDTO.PageResponse.builder()
                .companies(toResponseList(page.getContent()))
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
     * Validation et nettoyage des données après mapping
     */
    @AfterMapping
    default void cleanupData(@MappingTarget Company company) {
        // Nettoyage des espaces
        if (company.getNom() != null) {
            company.setNom(company.getNom().trim());
        }
        if (company.getRaisonSociale() != null) {
            company.setRaisonSociale(company.getRaisonSociale().trim());
        }
        if (company.getEmail() != null) {
            company.setEmail(company.getEmail().trim().toLowerCase());
        }
        if (company.getEmailContact() != null) {
            company.setEmailContact(company.getEmailContact().trim().toLowerCase());
        }
        if (company.getRccm() != null) {
            company.setRccm(company.getRccm().trim().toUpperCase());
        }
        if (company.getNinea() != null) {
            company.setNinea(company.getNinea().trim());
        }
        if (company.getSiteWeb() != null && !company.getSiteWeb().trim().isEmpty()) {
            String siteWeb = company.getSiteWeb().trim().toLowerCase();
            if (!siteWeb.startsWith("http://") && !siteWeb.startsWith("https://")) {
                company.setSiteWeb("https://" + siteWeb);
            } else {
                company.setSiteWeb(siteWeb);
            }
        }
    }

    @Named("idToCompany")
    default Company toEntity(Long id) {
        if (id == null) {
            return null;
        }
        Company company = new Company();
        company.setId(id);
        return company;
    }
}