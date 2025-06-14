package sn.svs.backoffice.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import sn.svs.backoffice.domain.Invoice;
import sn.svs.backoffice.domain.InvoiceLineItem;
import sn.svs.backoffice.dto.InvoiceDTO;

import java.util.List;

/**
 * Mapper MapStruct pour l'entité Invoice
 * Gère les conversions entre entités et DTOs avec relations complètes
 * SVS - Dakar, Sénégal
 */
@Mapper(
        componentModel = "spring",
        uses = {CompanyMapper.class, ShipMapper.class, OperationMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface InvoiceMapper {

    // ========================================
    // CONVERSIONS PRINCIPALES
    // ========================================

    /**
     * Convertit une requête de création en entité Invoice
     * Les montants seront calculés automatiquement dans l'entité
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numero", ignore = true) // Généré automatiquement
    @Mapping(target = "sousTotal", ignore = true) // Calculé automatiquement
    @Mapping(target = "tva", ignore = true) // Calculé automatiquement
    @Mapping(target = "montantTotal", ignore = true) // Calculé automatiquement
    @Mapping(target = "statut", constant = "BROUILLON") // Statut par défaut
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "compagnie", ignore = true) // Relations gérées séparément
    @Mapping(target = "navire", ignore = true)
    @Mapping(source = "prestations", target = "prestations")
    Invoice toEntity(InvoiceDTO.CreateRequest request);

    /**
     * Convertit une entité Invoice en DTO Response avec toutes les relations
     * IMPORTANT: Inclut compagnie, navire et prestations avec leurs détails
     */
    @Mapping(source = "compagnie", target = "compagnie") // Relation Company mappée
    @Mapping(source = "navire", target = "navire") // Relation Ship mappée
    @Mapping(source = "prestations", target = "prestations") // Relations InvoiceLineItem mappées
    @Mapping(target = "enRetard", expression = "java(invoice.isEnRetard())")
    @Mapping(target = "modifiable", expression = "java(invoice.isModifiable())")
    @Mapping(target = "supprimable", expression = "java(invoice.isSupprimable())")
    InvoiceDTO.Response toDto(Invoice invoice);

    /**
     * Convertit une liste d'entités en liste de DTOs
     */
    List<InvoiceDTO.Response> toDtoList(List<Invoice> invoices);

    /**
     * Met à jour une entité existante avec les données d'une requête de mise à jour
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numero", ignore = true) // Ne pas modifier le numéro
    @Mapping(target = "sousTotal", ignore = true) // Recalculé automatiquement
    @Mapping(target = "tva", ignore = true) // Recalculé automatiquement
    @Mapping(target = "montantTotal", ignore = true) // Recalculé automatiquement
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "compagnie", ignore = true)
    @Mapping(target = "navire", ignore = true)
    @Mapping(source = "prestations", target = "prestations")
    void updateEntityFromDto(InvoiceDTO.UpdateRequest request, @MappingTarget Invoice invoice);

    // ========================================
    // CONVERSIONS POUR INVOICE LINE ITEMS
    // ========================================

    /**
     * Convertit une requête de création de ligne en entité InvoiceLineItem
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoiceId", ignore = true) // Sera défini lors de l'association
    @Mapping(target = "montantXOF", ignore = true) // Calculé automatiquement
    @Mapping(target = "montantEURO", ignore = true) // Calculé automatiquement
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "operation", ignore = true) // Relation gérée séparément
    InvoiceLineItem toLineItemEntity(InvoiceDTO.CreateInvoiceLineItemRequest request);

    /**
     * Convertit une entité InvoiceLineItem en DTO Response avec relation Operation
     * IMPORTANT: Inclut les détails de l'opération
     */
    @Mapping(source = "operation", target = "operation") // Relation Operation mappée
    InvoiceDTO.InvoiceLineItemResponse toLineItemDto(InvoiceLineItem lineItem);

    /**
     * Convertit une liste de requêtes de lignes en entités
     */
    List<InvoiceLineItem> toLineItemEntityList(List<InvoiceDTO.CreateInvoiceLineItemRequest> requests);

    /**
     * Convertit une liste d'entités de lignes en DTOs
     */
    List<InvoiceDTO.InvoiceLineItemResponse> toLineItemDtoList(List<InvoiceLineItem> lineItems);

    // ========================================
    // CONVERSIONS POUR PAGINATION
    // ========================================

    /**
     * Convertit une Page Spring en PageResponse DTO
     */
    @Mapping(source = "content", target = "invoices")
    @Mapping(source = "totalElements", target = "total")
    @Mapping(source = "number", target = "page")
    @Mapping(source = "size", target = "size")
    @Mapping(source = "totalPages", target = "totalPages")
    @Mapping(source = "first", target = "first")
    @Mapping(source = "last", target = "last")
    @Mapping(target = "hasNext", expression = "java(!page.isLast())")
    @Mapping(target = "hasPrevious", expression = "java(!page.isFirst())")
    InvoiceDTO.PageResponse toPageResponse(Page<Invoice> page);

    // ========================================
    // CONVERSIONS POUR EXPORT
    // ========================================

    /**
     * Convertit une entité Invoice en DTO d'export
     */
    @Mapping(source = "compagnie.nom", target = "compagnie")
    @Mapping(source = "navire.nom", target = "navire")
    @Mapping(target = "dateFacture", expression = "java(invoice.getDateFacture().toString())")
    @Mapping(target = "dateEcheance", expression = "java(invoice.getDateEcheance().toString())")
    @Mapping(target = "statut", expression = "java(invoice.getStatut().getLabel())")
    @Mapping(source = "montantTotal", target = "montantXOF")
    @Mapping(target = "montantEURO", expression = "java(calculateTotalEuro(invoice))")
    InvoiceDTO.ExportDataResponse toExportData(Invoice invoice);

    /**
     * Convertit une liste d'entités en données d'export
     */
    List<InvoiceDTO.ExportDataResponse> toExportDataList(List<Invoice> invoices);

    // ========================================
    // CONVERSIONS POUR IMPRESSION
    // ========================================

    /**
     * Convertit une entité Invoice en données d'impression
     */
    @Mapping(source = ".", target = "invoice")
    @Mapping(target = "entreprise", expression = "java(getEntrepriseInfo())")
    InvoiceDTO.PrintDataResponse toPrintData(Invoice invoice);

    // ========================================
    // MÉTHODES UTILITAIRES POUR LES CALCULS
    // ========================================

    /**
     * Calcule le montant total en euros d'une facture
     * Somme tous les montants EURO des lignes de facture
     */
    default java.math.BigDecimal calculateTotalEuro(Invoice invoice) {
        if (invoice.getPrestations() == null || invoice.getPrestations().isEmpty()) {
            return null;
        }

        return invoice.getPrestations().stream()
                .filter(line -> line.getMontantEURO() != null)
                .map(InvoiceLineItem::getMontantEURO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    /**
     * Retourne les informations de l'entreprise pour l'impression
     * TODO: À personnaliser selon votre configuration
     */
    default InvoiceDTO.EntrepriseInfoResponse getEntrepriseInfo() {
        return InvoiceDTO.EntrepriseInfoResponse.builder()
                .nom("Salane Vision Sarl")
                .adresse("Cité Elisabeth DIOUF, villa 60, Dakar, Sénégal")
                .telephone("+221 77 656 66 09 / +221 76 590 69 89")
                .email("facturation@svs.sn")
                .ninea("009219869")
                .rccm("SN-DKR-2022-B-6304")
                .logo("assets/images/logo.jpeg")
                .build();
    }

    // ========================================
    // MÉTHODES APRÈS MAPPING POUR ASSOCIATIONS
    // ========================================
    /**
     * Configuration après mapping pour les requêtes de mise à jour
     * Maintient les associations entre lignes et facture
     */
    @AfterMapping
    default void linkLineItemsAfterUpdate(@MappingTarget Invoice invoice, InvoiceDTO.UpdateRequest request) {
        if (invoice.getPrestations() != null && invoice.getId() != null) {
            invoice.getPrestations().forEach(lineItem -> {
                if (lineItem.getInvoiceId() == null) {
                    lineItem.setInvoiceId(invoice.getId());
                    lineItem.setInvoice(invoice);
                }
            });
        }
    }
}
