package sn.svs.backoffice.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import sn.svs.backoffice.domain.Expense;
import sn.svs.backoffice.domain.ennumeration.ExpenseStatus;
import sn.svs.backoffice.dto.ExpenseDTO;

import java.util.List;

/**
 * Mapper pour l'entité Expense et ses DTOs
 * SVS - Dakar, Sénégal
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ExpenseMapper {

    /**
     * Convertit CreateRequest vers Expense entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categorie", ignore = true)
    @Mapping(target = "fournisseur", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    Expense toEntity(ExpenseDTO.CreateRequest createRequest);

    /**
     * Convertit Expense entity vers Response DTO
     */
    @Mapping(target = "categorieNom", source = "categorie.nom")
    @Mapping(target = "fournisseurNom", source = "fournisseur.nom")
    @Mapping(target = "paymentMethodNom", source = "paymentMethod.nom")
    @Mapping(target = "statutLabel", source = "statut")
    ExpenseDTO.Response toResponse(Expense expense);

    /**
     * Convertit une liste d'entités vers une liste de DTOs Response
     */
    List<ExpenseDTO.Response> toResponseList(List<Expense> expenses);

    /**
     * Met à jour une entité Expense avec les données d'UpdateRequest
     * Les champs null dans le DTO ne sont pas mappés (IGNORE)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categorie", ignore = true)
    @Mapping(target = "fournisseur", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    void updateEntityFromDto(ExpenseDTO.UpdateRequest updateRequest, @MappingTarget Expense expense);

    /**
     * Convertit une Page Spring vers PageResponse DTO
     */
    default ExpenseDTO.PageResponse toPageResponse(Page<Expense> page) {
        if (page == null) {
            return null;
        }

        return ExpenseDTO.PageResponse.builder()
                .expenses(toResponseList(page.getContent()))
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
    default void cleanupData(@MappingTarget Expense expense) {
        // Nettoyage des espaces
        if (expense.getNumero() != null) {
            expense.setNumero(expense.getNumero().trim().toUpperCase());
        }
        if (expense.getTitre() != null) {
            expense.setTitre(expense.getTitre().trim());
        }
        if (expense.getDescription() != null) {
            expense.setDescription(expense.getDescription().trim());
        }

        // Génération automatique du numéro si absent
        expense.genererNumeroSiAbsent();

        // Calculs automatiques des montants selon la devise
        if (expense.getDevise() != null && expense.getTauxChange() != null) {
            if (expense.isDeviseXOF() && expense.getMontantXOF() != null && expense.getMontantEURO() == null) {
                expense.calculerMontantEURO();
            } else if (expense.isDeviseEUR() && expense.getMontantEURO() != null && expense.getMontantXOF() == null) {
                expense.calculerMontantXOF();
            }
        }

        // Validation métier
        validateBusinessRules(expense);
    }

    /**
     * Validation des règles métier
     */
    default void validateBusinessRules(Expense expense) {
        // Si devise XOF, montantXOF est obligatoire
        if (expense.isDeviseXOF() && expense.getMontantXOF() == null) {
            throw new IllegalArgumentException("Le montant XOF est obligatoire pour les dépenses en Francs CFA");
        }

        // Si devise EUR, montantEURO est obligatoire
        if (expense.isDeviseEUR() && expense.getMontantEURO() == null) {
            throw new IllegalArgumentException("Le montant EUR est obligatoire pour les dépenses en Euros");
        }

        // La date de dépense ne peut pas être dans le futur
        if (expense.getDateDepense() != null && expense.getDateDepense().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("La date de dépense ne peut pas être dans le futur");
        }
    }

    /**
     * Mapping personnalisé pour le libellé du statut
     */
    default String mapStatutToLabel(ExpenseStatus statut) {
        return statut != null ? statut.getLabel() : null;
    }

    /**
     * Helpers pour les mappings par ID
     */
    @Named("idToExpense")
    default Expense toEntity(Long id) {
        if (id == null) {
            return null;
        }
        Expense expense = new Expense();
        expense.setId(id);
        return expense;
    }

    @Named("idToExpenseCategory")
    default sn.svs.backoffice.domain.ExpenseCategory toCategoryEntity(Long categorieId) {
        if (categorieId == null) {
            return null;
        }
        sn.svs.backoffice.domain.ExpenseCategory category = new sn.svs.backoffice.domain.ExpenseCategory();
        category.setId(categorieId);
        return category;
    }

    @Named("idToExpenseSupplier")
    default sn.svs.backoffice.domain.ExpenseSupplier toSupplierEntity(Long fournisseurId) {
        if (fournisseurId == null) {
            return null;
        }
        sn.svs.backoffice.domain.ExpenseSupplier supplier = new sn.svs.backoffice.domain.ExpenseSupplier();
        supplier.setId(fournisseurId);
        return supplier;
    }

    @Named("idToPaymentMethod")
    default sn.svs.backoffice.domain.PaymentMethod toPaymentMethodEntity(Long paymentMethodId) {
        if (paymentMethodId == null) {
            return null;
        }
        sn.svs.backoffice.domain.PaymentMethod paymentMethod = new sn.svs.backoffice.domain.PaymentMethod();
        paymentMethod.setId(paymentMethodId);
        return paymentMethod;
    }

    /**
     * Mapping personnalisé pour les stats
     */
    default ExpenseDTO.StatsResponse.StatutCount toStatutCount(
            ExpenseStatus statut,
            Long count,
            java.math.BigDecimal totalAmount) {
        return ExpenseDTO.StatsResponse.StatutCount.builder()
                .statut(statut)
                .label(statut.getLabel())
                .count(count)
                .totalAmount(totalAmount)
                .build();
    }

    default ExpenseDTO.StatsResponse.CategorieCount toCategorieCount(
            Long categorieId,
            String categorieNom,
            Long count,
            java.math.BigDecimal totalAmount) {
        return ExpenseDTO.StatsResponse.CategorieCount.builder()
                .categorieId(categorieId)
                .categorieNom(categorieNom)
                .count(count)
                .totalAmount(totalAmount)
                .build();
    }

    default ExpenseDTO.StatsResponse.MonthlyExpense toMonthlyExpense(
            Integer year,
            Integer month,
            Long count,
            java.math.BigDecimal totalAmount) {

        String monthLabel = getMonthLabel(month, year);

        return ExpenseDTO.StatsResponse.MonthlyExpense.builder()
                .year(year)
                .month(month)
                .monthLabel(monthLabel)
                .count(count)
                .totalAmount(totalAmount)
                .build();
    }

    /**
     * Génère le libellé du mois en français
     */
    default String getMonthLabel(Integer month, Integer year) {
        if (month == null || year == null) {
            return "";
        }

        String[] monthNames = {
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        };

        if (month >= 1 && month <= 12) {
            return monthNames[month - 1] + " " + year;
        }

        return month + "/" + year;
    }
}