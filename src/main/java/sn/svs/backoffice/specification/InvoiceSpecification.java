package sn.svs.backoffice.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sn.svs.backoffice.domain.Company;
import sn.svs.backoffice.domain.Invoice;
import sn.svs.backoffice.domain.Ship;
import sn.svs.backoffice.domain.ennumeration.InvoiceStatus;
import sn.svs.backoffice.dto.InvoiceDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Spécifications JPA pour les requêtes dynamiques sur les factures
 * Utilisé pour éviter les problèmes de typage des paramètres dans les requêtes JPQL
 * SVS - Dakar, Sénégal
 */
@Component
public class InvoiceSpecification {

    /**
     * Crée une spécification combinée avec tous les filtres
     */
    public static Specification<Invoice> withFilters(InvoiceDTO.SearchFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Toujours récupérer uniquement les factures actives
            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            // Filtre par compagnie
            if (filter.getCompagnieId() != null) {
                Join<Invoice, Company> compagnieJoin = root.join("compagnie", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(compagnieJoin.get("id"), filter.getCompagnieId()));
            }

            // Filtre par navire
            if (filter.getNavireId() != null) {
                Join<Invoice, Ship> navireJoin = root.join("navire", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(navireJoin.get("id"), filter.getNavireId()));
            }

            // Filtre par statut
            if (filter.getStatut() != null) {
                predicates.add(criteriaBuilder.equal(root.get("statut"), filter.getStatut()));
            }

            // Filtre par montant minimum (montant total)
            if (filter.getMinAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("montantTotal"), filter.getMinAmount()));
            }

            // Filtre par montant maximum (montant total)
            if (filter.getMaxAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("montantTotal"), filter.getMaxAmount()));
            }

            // Filtre par date de début
            if (filter.getDateDebut() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateFacture"), filter.getDateDebut()));
            }

            // Filtre par date de fin
            if (filter.getDateFin() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateFacture"), filter.getDateFin()));
            }

            // Filtre par mois spécifique
            if (filter.getMois() != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.function("MONTH", Integer.class, root.get("dateFacture")),
                        filter.getMois()));
            }

            // Filtre par année spécifique
            if (filter.getAnnee() != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.function("YEAR", Integer.class, root.get("dateFacture")),
                        filter.getAnnee()));
            }

            // Recherche textuelle (numéro, notes, nom compagnie, nom navire)
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";

                // Recherche dans le numéro de facture
                Predicate numeroPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("numero")), searchPattern);

                // Recherche dans les notes
                Predicate notesPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("notes")), searchPattern);

                // Recherche dans le nom de la compagnie
                Join<Invoice, Company> compagnieJoin = root.join("compagnie", JoinType.LEFT);
                Predicate compagniePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(compagnieJoin.get("nom")), searchPattern);

                // Recherche dans le nom du navire
                Join<Invoice, Ship> navireJoin = root.join("navire", JoinType.LEFT);
                Predicate navirePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(navireJoin.get("nom")), searchPattern);

                predicates.add(criteriaBuilder.or(
                        numeroPredicate,
                        notesPredicate,
                        compagniePredicate,
                        navirePredicate
                ));
            }

            // Combiner tous les prédicats avec AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Spécification pour la recherche textuelle uniquement
     */
    public static Specification<Invoice> withTextSearch(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchText)) {
                return criteriaBuilder.conjunction(); // Pas de filtre si pas de texte
            }

            String searchPattern = "%" + searchText.toLowerCase() + "%";

            // Jointures pour rechercher dans les entités liées
            Join<Invoice, Company> compagnieJoin = root.join("compagnie", JoinType.LEFT);
            Join<Invoice, Ship> navireJoin = root.join("navire", JoinType.LEFT);

            return criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("active")),
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("numero")), searchPattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("notes")), searchPattern),
                            criteriaBuilder.like(criteriaBuilder.lower(compagnieJoin.get("nom")), searchPattern),
                            criteriaBuilder.like(criteriaBuilder.lower(navireJoin.get("nom")), searchPattern)
                    )
            );
        };
    }

    /**
     * Spécification pour filtrer par période uniquement
     */
    public static Specification<Invoice> withDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateFacture"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateFacture"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Spécification pour filtrer par statut
     */
    public static Specification<Invoice> withStatus(InvoiceStatus status) {
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
    public static Specification<Invoice> withAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            if (minAmount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("montantTotal"), minAmount));
            }

            if (maxAmount != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("montantTotal"), maxAmount));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Spécification pour filtrer par compagnie
     */
    public static Specification<Invoice> withCompagnie(Long compagnieId) {
        return (root, query, criteriaBuilder) -> {
            if (compagnieId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("active")),
                    criteriaBuilder.equal(root.get("compagnieId"), compagnieId)
            );
        };
    }

    /**
     * Spécification pour filtrer par navire
     */
    public static Specification<Invoice> withNavire(Long navireId) {
        return (root, query, criteriaBuilder) -> {
            if (navireId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("active")),
                    criteriaBuilder.equal(root.get("navireId"), navireId)
            );
        };
    }

    /**
     * Spécification pour filtrer par année et mois
     */
    public static Specification<Invoice> withYearAndMonth(Integer year, Integer month) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            if (year != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.function("YEAR", Integer.class, root.get("dateFacture")), year));
            }

            if (month != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.function("MONTH", Integer.class, root.get("dateFacture")), month));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Spécification pour les factures échues (en retard)
     */
    public static Specification<Invoice> withOverdue() {
        return (root, query, criteriaBuilder) -> {
            LocalDate today = LocalDate.now();

            return criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("active")),
                    criteriaBuilder.in(root.get("statut")).value(InvoiceStatus.EMISE).value(InvoiceStatus.BROUILLON),
                    criteriaBuilder.lessThan(root.get("dateEcheance"), today)
            );
        };
    }

    /**
     * Spécification pour les factures payées dans une période
     */
    public static Specification<Invoice> withPaidInPeriod(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isTrue(root.get("active")));
            predicates.add(criteriaBuilder.equal(root.get("statut"), InvoiceStatus.PAYEE));

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateFacture"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateFacture"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Spécification pour les factures en attente (brouillon)
     */
    public static Specification<Invoice> withPendingStatus() {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("active")),
                    criteriaBuilder.equal(root.get("statut"), InvoiceStatus.BROUILLON)
            );
        };
    }

    /**
     * Spécification pour les factures avec un montant supérieur à un seuil
     */
    public static Specification<Invoice> withAmountGreaterThan(BigDecimal threshold) {
        return (root, query, criteriaBuilder) -> {
            if (threshold == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("active")),
                    criteriaBuilder.greaterThan(root.get("montantTotal"), threshold)
            );
        };
    }

    /**
     * Spécification pour combiner plusieurs filtres avec OR
     */
    public static Specification<Invoice> withMultipleCompanies(List<Long> compagnieIds) {
        return (root, query, criteriaBuilder) -> {
            if (compagnieIds == null || compagnieIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("active")),
                    root.get("compagnieId").in(compagnieIds)
            );
        };
    }

    /**
     * Spécification pour combiner plusieurs statuts avec OR
     */
    public static Specification<Invoice> withMultipleStatuses(List<InvoiceStatus> statuses) {
        return (root, query, criteriaBuilder) -> {
            if (statuses == null || statuses.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("active")),
                    root.get("statut").in(statuses)
            );
        };
    }

    /**
     * Spécification pour les factures créées dans une période
     */
    public static Specification<Invoice> withCreatedInPeriod(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        criteriaBuilder.function("DATE", LocalDate.class, root.get("createdAt")), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        criteriaBuilder.function("DATE", LocalDate.class, root.get("createdAt")), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Spécification pour exclure certains statuts
     */
    public static Specification<Invoice> withoutStatuses(List<InvoiceStatus> excludedStatuses) {
        return (root, query, criteriaBuilder) -> {
            if (excludedStatuses == null || excludedStatuses.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("active")),
                    criteriaBuilder.not(root.get("statut").in(excludedStatuses))
            );
        };
    }
}
