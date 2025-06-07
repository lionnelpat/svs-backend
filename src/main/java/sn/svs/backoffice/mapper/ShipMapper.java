package sn.svs.backoffice.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import sn.svs.backoffice.domain.Company;
import sn.svs.backoffice.domain.Ship;
import sn.svs.backoffice.dto.ShipDTO;

import java.util.List;

/**
 * Mapper pour l'entité Ship et ses DTOs
 * SVS - Dakar, Sénégal
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CompanyMapper.class}
)
public interface ShipMapper {

    /**
     * Convertit CreateRequest vers Ship entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "compagnieId", target = "compagnie")
    Ship toEntity(ShipDTO.CreateRequest createRequest);

    /**
     * Convertit Ship entity vers Response DTO
     */
    @Mapping(source = "compagnie", target = "compagnie")
    @Mapping(source = "compagnie.id", target = "compagnieId")
    ShipDTO.Response toResponse(Ship ship);

    /**
     * Met à jour une entité Ship avec les données d'UpdateRequest
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numeroIMO", ignore = true)
    @Mapping(source = "compagnieId", target = "compagnie")
    void updateEntityFromDto(ShipDTO.UpdateRequest updateRequest, @MappingTarget Ship ship);


    /**
     * Convertit une liste d'entités vers une liste de DTOs Response
     */
    List<ShipDTO.Response> toResponseList(List<Ship> ships);

    /**
     * Convertit Ship entity vers Summary DTO
     */
    @Mapping(source = "compagnie.nom", target = "compagnieNom")
    ShipDTO.Summary toSummary(Ship ship);

    /**
     * Convertit une liste d'entités vers une liste de DTOs Summary
     */
    List<ShipDTO.Summary> toSummaryList(List<Ship> ships);

    /**
     * Convertit une Page Spring vers PageResponse DTO
     */
    default ShipDTO.PageResponse toPageResponse(Page<Ship> page) {
        if (page == null) {
            return null;
        }

        return ShipDTO.PageResponse.builder()
                .ships(toResponseList(page.getContent()))
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
    default void cleanupData(@MappingTarget Ship ship) {
        // Nettoyage des espaces
        if (ship.getNom() != null) {
            ship.setNom(ship.getNom().trim());
        }
        if (ship.getNumeroIMO() != null) {
            ship.setNumeroIMO(ship.getNumeroIMO().trim().toUpperCase());
        }
        if (ship.getNumeroMMSI() != null) {
            ship.setNumeroMMSI(ship.getNumeroMMSI().trim());
        }
        if (ship.getNumeroAppel() != null) {
            ship.setNumeroAppel(ship.getNumeroAppel().trim().toUpperCase());
        }
        if (ship.getPortAttache() != null) {
            ship.setPortAttache(ship.getPortAttache().trim());
        }

        // Validation spécifique pour les numéros IMO et MMSI
        validateIMONumber(ship);
        validateMMSINumber(ship);
    }

    /**
     * Validation du numéro IMO
     */
    default void validateIMONumber(Ship ship) {
        if (ship.getNumeroIMO() != null) {
            String imo = ship.getNumeroIMO();
            // Ajouter le préfixe IMO si nécessaire
            if (!imo.startsWith("IMO")) {
                ship.setNumeroIMO("IMO" + imo);
            }
        }
    }

    /**
     * Validation du numéro MMSI
     */
    default void validateMMSINumber(Ship ship) {
        if (ship.getNumeroMMSI() != null) {
            String mmsi = ship.getNumeroMMSI();
            // Vérifier que le MMSI contient uniquement des chiffres
            if (!mmsi.matches("\\d{9}")) {
                // Log ou exception si le format n'est pas correct
                // Pour l'instant, on garde tel quel
            }
        }
    }



    /**
     * Méthode pour créer une référence Company
     */
    default Company createCompanyReference(Long compagnieId) {
        if (compagnieId == null) {
            return null;
        }
        Company company = new Company();
        company.setId(compagnieId);
        return company;
    }
}