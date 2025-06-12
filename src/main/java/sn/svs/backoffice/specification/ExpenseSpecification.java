package sn.svs.backoffice.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sn.svs.backoffice.domain.ennumeration.ExpenseStatus;
import sn.svs.backoffice.dto.ExpenseDTO;
import sn.svs.backoffice.domain.Expense;
import sn.svs.backoffice.domain.ExpenseCategory;
import sn.svs.backoffice.domain.ExpenseSupplier;
import sn.svs.backoffice.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Spécifications JPA pour les requêtes dynamiques sur les dépenses
 * Utilisé pour éviter les problèmes de typage des paramètres dans les requêtes JPQL
 */
@Component
public class ExpenseSpecification {

    /**
     * Crée une spécification combinée avec tous les filtres
     */
    public static Specification<Expense> withFilters(ExpenseDTO.SearchFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Toujours récupérer uniquement les dépenses actives
            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            // Filtre par catégorie
            if (filter.getCategorieId() != null) {
                Join<Expense, ExpenseCategory> categorieJoin = root.join("categorie", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(categorieJoin.get("id"), filter.getCategorieId()));
            }

            // Filtre par fournisseur
            if (filter.getFournisseurId() != null) {
                Join<Expense, ExpenseSupplier> fournisseurJoin = root.join("fournisseur", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(fournisseurJoin.get("id"), filter.getFournisseurId()));
            }

            // Filtre par statut
            if (filter.getStatut() != null) {
                predicates.add(criteriaBuilder.equal(root.get("statut"), filter.getStatut()));
            }

            // Filtre par mode de paiement
            if (filter.getPaymentMethodId() != null) {
                Join<Expense, PaymentMethod> paymentMethodJoin = root.join("paymentMethod", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(paymentMethodJoin.get("id"), filter.getPaymentMethodId()));
            }

            // Filtre par devise
            if (filter.getDevise() != null) {
                predicates.add(criteriaBuilder.equal(root.get("devise"), filter.getDevise()));
            }

            // Filtre par montant minimum (XOF)
            if (filter.getMinAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("montantXOF"), filter.getMinAmount()));
            }

            // Filtre par montant maximum (XOF)
            if (filter.getMaxAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("montantXOF"), filter.getMaxAmount()));
            }

            // Filtre par date de début
            if (filter.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateDepense"), filter.getStartDate()));
            }

            // Filtre par date de fin
            if (filter.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateDepense"), filter.getEndDate()));
            }

            // Recherche textuelle (titre, numéro, description)
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("titre")), searchPattern);
                Predicate numeroPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("numero")), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);

                predicates.add(criteriaBuilder.or(titlePredicate, numeroPredicate, descriptionPredicate));
            }

            // Combiner tous les prédicats avec AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Spécification pour la recherche textuelle uniquement
     */
    public static Specification<Expense> withTextSearch(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchText)) {
                return criteriaBuilder.conjunction(); // Pas de filtre si pas de texte
            }

            String searchPattern = "%" + searchText.toLowerCase() + "%";

            return criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("active")),
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("titre")), searchPattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("numero")), searchPattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)
                    )
            );
        };
    }

    /**
     * Spécification pour filtrer par période uniquement
     */
    public static Specification<Expense> withDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateDepense"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateDepense"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Spécification pour filtrer par statut
     */
    public static Specification<Expense> withStatus(ExpenseStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("active")),
                    criteriaBuilder.equal(root.get("statut"), status)
            );
        };
    }

    /**
     * Spécification pour filtrer par montant dans une plage
     */
    public static Specification<Expense> withAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            if (minAmount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("montantXOF"), minAmount));
            }

            if (maxAmount != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("montantXOF"), maxAmount));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
