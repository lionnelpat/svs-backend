package sn.svs.backoffice.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sn.svs.backoffice.domain.Role;
import sn.svs.backoffice.domain.User;
import sn.svs.backoffice.dto.UserDTO;
import sn.svs.backoffice.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserWithRolesService {

    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Transactional
    public User loadUserWithRoles(String username) {
        log.debug("Chargement de l'utilisateur {} avec ses rôles", username);

        // Chargement de l'utilisateur seul (sans les rôles pour éviter les problèmes Hibernate)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));

        // Création d'un nouvel objet User détaché pour éviter les problèmes de persistance
        User detachedUser = createDetachedUser(user);

        // Chargement des rôles avec requête native
        List<Role> roles = loadRolesForUser(user.getId());

        // Attribution des rôles au user détaché
        detachedUser.setRoles(new HashSet<>(roles));

        log.debug("Utilisateur {} chargé avec {} rôles", username, roles.size());

        return detachedUser;
    }

    @Transactional
    public Page<User> findAllUsersWithRoles(Pageable pageable) {
        log.debug("Chargement de tous les utilisateurs avec leurs rôles - Page: {}", pageable.getPageNumber());

        // Étape 1: Récupérer les utilisateurs sans les rôles
        Page<User> usersPage = userRepository.findAll(pageable);

        if (usersPage.isEmpty()) {
            return usersPage;
        }

        // Étape 2: Enrichir chaque utilisateur avec ses rôles
        List<User> usersWithRoles = usersPage.getContent().stream()
                .map(this::enrichUserWithRoles)
                .collect(Collectors.toList());

        return new PageImpl<>(usersWithRoles, pageable, usersPage.getTotalElements());
    }

    @Transactional
    public Page<User> searchUsersWithRoles(UserDTO.SearchFilter filter, Pageable pageable) {
        log.debug("Recherche d'utilisateurs avec filtres: {}", filter);

        Page<User> usersPage;

        if (hasSearchCriteria(filter)) {
            // Recherche avec critères
            usersPage = searchWithCriteria(filter, pageable);
        } else {
            // Recherche simple
            usersPage = userRepository.findAll(pageable);
        }

        if (usersPage.isEmpty()) {
            return usersPage;
        }

        // Enrichir avec les rôles
        List<User> usersWithRoles = usersPage.getContent().stream()
                .map(this::enrichUserWithRoles)
                .collect(Collectors.toList());

        return new PageImpl<>(usersWithRoles, pageable, usersPage.getTotalElements());
    }

    /**
     * Charge les rôles pour plusieurs utilisateurs en une seule requête
     */
    @SuppressWarnings("unchecked")
    private Map<Long, List<Role>> loadRolesForUsers(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Requête native optimisée pour charger tous les rôles
        String sql = """
            SELECT ur.user_id, r.* 
            FROM user_roles ur
            INNER JOIN roles r ON ur.role_id = r.id
            WHERE ur.user_id IN (:userIds)
            ORDER BY ur.user_id, r.name
            """;

        Query query = entityManager.createNativeQuery(sql, "UserRoleMapping");
        query.setParameter("userIds", userIds);

        List<Object[]> results = query.getResultList();

        Map<Long, List<Role>> userRolesMap = new HashMap<>();

        for (Object[] result : results) {
            Long userId = ((Number) result[0]).longValue();
            Role role = (Role) result[1];

            userRolesMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(role);
        }

        return userRolesMap;
    }

    /**
     * Enrichit un utilisateur avec ses rôles
     */
    private User enrichUserWithRoles(User user) {
        // Créer un utilisateur détaché
        User detachedUser = createDetachedUser(user);

        // Charger les rôles pour cet utilisateur
        List<Role> roles = loadRolesForSingleUser(user.getId());
        detachedUser.setRoles(new HashSet<>(roles));

        return detachedUser;
    }

    /**
     * Charge les rôles pour un seul utilisateur
     */
    private List<Role> loadRolesForSingleUser(Long userId) {
        String jpql = "SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId";
        return entityManager.createQuery(jpql, Role.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * Vérifie s'il y a des critères de recherche
     */
    private boolean hasSearchCriteria(UserDTO.SearchFilter filter) {
        return (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) ||
                filter.getIsActive() != null ||
                filter.getIsEmailVerified() != null ||
                filter.getCreatedFrom() != null ||
                filter.getCreatedTo() != null ||
                (filter.getRoleIds() != null && !filter.getRoleIds().isEmpty());
    }

    /**
     * Recherche avec critères (version simplifiée)
     */
    private Page<User> searchWithCriteria(UserDTO.SearchFilter filter, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = new ArrayList<>();

        // Recherche textuelle
        if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
            String searchTerm = "%" + filter.getSearch().toLowerCase() + "%";
            Predicate searchPredicate = cb.or(
                    cb.like(cb.lower(root.get("username")), searchTerm),
                    cb.like(cb.lower(root.get("email")), searchTerm),
                    cb.like(cb.lower(root.get("firstName")), searchTerm),
                    cb.like(cb.lower(root.get("lastName")), searchTerm)
            );
            predicates.add(searchPredicate);
        }

        // Filtres booléens
        if (filter.getIsActive() != null) {
            predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
        }

        if (filter.getIsEmailVerified() != null) {
            predicates.add(cb.equal(root.get("isEmailVerified"), filter.getIsEmailVerified()));
        }

        // Filtres de date
        if (filter.getCreatedFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedFrom()));
        }

        if (filter.getCreatedTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedTo()));
        }

        // Appliquer les prédicats
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }

        // Tri
        if (pageable.getSort().isSorted()) {
            List<Order> orders = pageable.getSort().stream()
                    .map(order -> order.isAscending() ?
                            cb.asc(root.get(order.getProperty())) :
                            cb.desc(root.get(order.getProperty())))
                    .collect(Collectors.toList());
            query.orderBy(orders);
        }

        // Exécution
        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<User> users = typedQuery.getResultList();

        // Compter le total
        long total = countUsersWithCriteria(filter);

        return new PageImpl<>(users, pageable, total);
    }

    /**
     * Compte les utilisateurs avec critères
     */
    private long countUsersWithCriteria(UserDTO.SearchFilter filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> root = countQuery.from(User.class);

        countQuery.select(cb.count(root));

        List<Predicate> predicates = new ArrayList<>();

        if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
            String searchTerm = "%" + filter.getSearch().toLowerCase() + "%";
            Predicate searchPredicate = cb.or(
                    cb.like(cb.lower(root.get("username")), searchTerm),
                    cb.like(cb.lower(root.get("email")), searchTerm),
                    cb.like(cb.lower(root.get("firstName")), searchTerm),
                    cb.like(cb.lower(root.get("lastName")), searchTerm)
            );
            predicates.add(searchPredicate);
        }

        if (filter.getIsActive() != null) {
            predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
        }

        if (filter.getIsEmailVerified() != null) {
            predicates.add(cb.equal(root.get("isEmailVerified"), filter.getIsEmailVerified()));
        }

        if (filter.getCreatedFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedFrom()));
        }

        if (filter.getCreatedTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedTo()));
        }

        if (!predicates.isEmpty()) {
            countQuery.where(predicates.toArray(new Predicate[0]));
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    /**
     * Crée une copie détachée de l'utilisateur pour éviter les problèmes Hibernate
     */
    private User createDetachedUser(User originalUser) {
        return User.builder()
                .id(originalUser.getId())
                .username(originalUser.getUsername())
                .email(originalUser.getEmail())
                .password(originalUser.getPassword())
                .firstName(originalUser.getFirstName())
                .lastName(originalUser.getLastName())
                .phone(originalUser.getPhone())
                .isActive(originalUser.getIsActive())
                .isEmailVerified(originalUser.getIsEmailVerified())
                .emailVerificationToken(originalUser.getEmailVerificationToken())
                .passwordResetToken(originalUser.getPasswordResetToken())
                .passwordResetTokenExpiry(originalUser.getPasswordResetTokenExpiry())
                .lastLogin(originalUser.getLastLogin())
                .loginAttempts(originalUser.getLoginAttempts())
                .accountLockedUntil(originalUser.getAccountLockedUntil())
                .createdAt(originalUser.getCreatedAt())
                .updatedAt(originalUser.getUpdatedAt())
                .createdBy(originalUser.getCreatedBy())
                .updatedBy(originalUser.getUpdatedBy())
                .roles(new HashSet<>()) // Sera rempli après
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<Role> loadRolesForUser(Long userId) {
        return entityManager.createNativeQuery(
                        "SELECT r.* FROM roles r " +
                                "INNER JOIN user_roles ur ON r.id = ur.role_id " +
                                "WHERE ur.user_id = ?1", Role.class)
                .setParameter(1, userId)
                .getResultList();
    }
}
