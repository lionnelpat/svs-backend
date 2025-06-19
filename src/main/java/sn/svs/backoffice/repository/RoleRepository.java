package sn.svs.backoffice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.svs.backoffice.domain.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository pour l'entité Role
 * Fournit les méthodes d'accès aux données pour la gestion des rôles
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // ========== RECHERCHE PAR NOM ==========

    /**
     * Recherche un rôle par son nom
     */
    Optional<Role> findByName(Role.RoleName name);

    /**
     * Vérifie si un rôle existe par nom
     */
    boolean existsByName(Role.RoleName name);

    // ========== RECHERCHE PAR STATUT ==========

    /**
     * Trouve tous les rôles actifs
     */
    List<Role> findByIsActiveTrue();

    /**
     * Trouve tous les rôles inactifs
     */
    List<Role> findByIsActiveFalse();


    Page<Role> findByIsActive(Boolean isActive, Pageable pageable);

    Page<Role> findByIsActiveIsFalse(Boolean isActive, Pageable pageable);

    // ========== RECHERCHE MULTIPLE ==========

    /**
     * Trouve plusieurs rôles par leurs noms
     */
    List<Role> findByNameIn(Set<Role.RoleName> names);

    /**
     * Trouve tous les rôles actifs par leurs noms
     */
    @Query("SELECT r FROM Role r WHERE r.name IN :names AND r.isActive = true")
    List<Role> findActiveRolesByNames(@Param("names") Set<Role.RoleName> names);

    // ========== RECHERCHE PAR DATES ==========

    /**
     * Trouve les rôles créés entre deux dates
     */
    List<Role> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Trouve les rôles créés depuis une date donnée
     */
    @Query("SELECT r FROM Role r WHERE r.createdAt >= :date")
    List<Role> findRolesCreatedSince(@Param("date") LocalDateTime date);

    // ========== STATISTIQUES ET UTILISATION ==========

    /**
     * Trouve les rôles avec le nombre d'utilisateurs assignés
     */
    @Query("SELECT r, COUNT(u) FROM Role r LEFT JOIN r.users u GROUP BY r")
    List<Object[]> findRolesWithUserCount();

    /**
     * Trouve les rôles utilisés (ayant au moins un utilisateur)
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.users u")
    List<Role> findUsedRoles();

    /**
     * Trouve les rôles non utilisés (sans utilisateur assigné)
     */
    @Query("SELECT r FROM Role r WHERE r.users IS EMPTY")
    List<Role> findUnusedRoles();

    /**
     * Compte le nombre d'utilisateurs pour un rôle donné
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Long countUsersWithRole(@Param("roleName") Role.RoleName roleName);

    // ========== RECHERCHE POUR VALIDATION ==========

    /**
     * Trouve les rôles par défaut (les 3 rôles principaux)
     */
    @Query("SELECT r FROM Role r WHERE r.name IN ('ADMIN', 'MANAGER', 'USER')")
    List<Role> findDefaultRoles();

    /**
     * Vérifie si tous les rôles par défaut existent
     */
    @Query("SELECT COUNT(r) FROM Role r WHERE r.name IN ('ADMIN', 'MANAGER', 'USER')")
    Long countDefaultRoles();

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Trouve le rôle ADMIN
     */
    @Query("SELECT r FROM Role r WHERE r.name = 'ADMIN'")
    Optional<Role> findAdminRole();

    /**
     * Trouve le rôle MANAGER
     */
    @Query("SELECT r FROM Role r WHERE r.name = 'MANAGER'")
    Optional<Role> findManagerRole();

    /**
     * Trouve le rôle USER (rôle par défaut)
     */
    @Query("SELECT r FROM Role r WHERE r.name = 'USER'")
    Optional<Role> findUserRole();

    /**
     * Trouve tous les rôles sauf ADMIN (pour les utilisateurs non-admin)
     */
    @Query("SELECT r FROM Role r WHERE r.name != 'ADMIN' AND r.isActive = true")
    List<Role> findNonAdminRoles();

    // ========== STATISTIQUES ==========

    /**
     * Compte le nombre total de rôles actifs
     */
    @Query("SELECT COUNT(r) FROM Role r WHERE r.isActive = true")
    Long countActiveRoles();

    /**
     * Obtient les statistiques d'usage des rôles
     */
    @Query("SELECT r.name, r.description, COUNT(u) as userCount " +
            "FROM Role r LEFT JOIN r.users u " +
            "WHERE r.isActive = true " +
            "GROUP BY r.id, r.name, r.description " +
            "ORDER BY COUNT(u) DESC")
    List<Object[]> getRoleUsageStatistics();
}
