// ========== REPOSITORY USER ==========
package sn.svs.backoffice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.svs.backoffice.domain.Role;
import sn.svs.backoffice.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository pour l'entité User
 * Fournit les méthodes d'accès aux données pour la gestion des utilisateurs
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    /**
     * Trouve un utilisateur avec ses rôles chargés par username
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    /**
     * Trouve un utilisateur avec ses rôles chargés par email
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    /**
     * Trouve un utilisateur avec ses rôles chargés par username ou email
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE LOWER(u.username) = LOWER(:identifier) OR LOWER(u.email) = LOWER(:identifier)")
    Optional<User> findByUsernameOrEmailWithRoles(@Param("identifier") String identifier);

    /**
     * Trouve un utilisateur avec ses rôles chargés par ID
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);


    // ========== RECHERCHE PAR IDENTIFIANTS ==========

    /**
     * Recherche un utilisateur par nom d'utilisateur (insensible à la casse)
     */
    Optional<User> findByUsernameIgnoreCase(String username);

    /**
     * Recherche un utilisateur par email (insensible à la casse)
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Recherche un utilisateur par username ou email (insensible à la casse)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:identifier) OR LOWER(u.email) = LOWER(:identifier)")
    Optional<User> findByUsernameOrEmailIgnoreCase(@Param("identifier") String identifier);

    // ========== VÉRIFICATIONS D'EXISTENCE ==========

    /**
     * Vérifie si un utilisateur existe par nom d'utilisateur
     */
    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Vérifie si un utilisateur existe par email
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Vérifie si un utilisateur existe par username ou email (exclut un ID donné)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE (LOWER(u.username) = LOWER(:username) OR LOWER(u.email) = LOWER(:email)) AND u.id != :excludeId")
    boolean existsByUsernameOrEmailExcludingId(@Param("username") String username,
                                               @Param("email") String email,
                                               @Param("excludeId") Long excludeId);

    // ========== RECHERCHE PAR STATUT ==========

    /**
     * Trouve tous les utilisateurs actifs
     */
    List<User> findByIsActiveTrue();

    /**
     * Trouve tous les utilisateurs inactifs
     */
    List<User> findByIsActiveFalse();

    /**
     * Trouve tous les utilisateurs avec email vérifié
     */
    List<User> findByIsEmailVerifiedTrue();

    /**
     * Trouve tous les utilisateurs avec email non vérifié
     */
    List<User> findByIsEmailVerifiedFalse();

    /**
     * Trouve tous les utilisateurs actifs avec email vérifié
     */
    List<User> findByIsActiveTrueAndIsEmailVerifiedTrue();

    // ========== RECHERCHE PAR RÔLES ==========

    /**
     * Trouve tous les utilisateurs ayant un rôle spécifique
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRole(@Param("roleName") Role.RoleName roleName);

    /**
     * Trouve tous les utilisateurs ayant l'un des rôles spécifiés
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames")
    List<User> findByRoleIn(@Param("roleNames") Set<Role.RoleName> roleNames);

    /**
     * Trouve tous les utilisateurs administrateurs actifs
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ADMIN' AND u.isActive = true")
    List<User> findActiveAdministrators();

    /**
     * Trouve tous les utilisateurs managers actifs
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'MANAGER' AND u.isActive = true")
    List<User> findActiveManagers();

    // ========== RECHERCHE PAR DATES ==========

    /**
     * Trouve les utilisateurs créés entre deux dates
     */
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Trouve les utilisateurs connectés depuis une date donnée
     */
    List<User> findByLastLoginAfter(LocalDateTime date);

    /**
     * Trouve les utilisateurs jamais connectés
     */
    List<User> findByLastLoginIsNull();

    /**
     * Trouve les utilisateurs créés dans les X derniers jours
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :date")
    List<User> findUsersCreatedSince(@Param("date") LocalDateTime date);

    // ========== COMPTES VERROUILLÉS ET TENTATIVES ==========

    /**
     * Trouve tous les utilisateurs actuellement verrouillés
     */
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil > :now")
    List<User> findCurrentlyLockedUsers(@Param("now") LocalDateTime now);

    /**
     * Trouve les utilisateurs avec tentatives de connexion échouées
     */
    @Query("SELECT u FROM User u WHERE u.loginAttempts > 0")
    List<User> findUsersWithFailedAttempts();

    /**
     * Trouve les utilisateurs avec plus de X tentatives échouées
     */
    @Query("SELECT u FROM User u WHERE u.loginAttempts >= :attempts")
    List<User> findUsersWithFailedAttemptsGreaterThan(@Param("attempts") Integer attempts);

    // ========== TOKENS DE VÉRIFICATION/RESET ==========

    /**
     * Trouve un utilisateur par token de vérification d'email
     */
    Optional<User> findByEmailVerificationToken(String token);

    /**
     * Trouve un utilisateur par token de reset de mot de passe
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Trouve les utilisateurs avec token de reset expiré
     */
    @Query("SELECT u FROM User u WHERE u.passwordResetTokenExpiry IS NOT NULL AND u.passwordResetTokenExpiry < :now")
    List<User> findUsersWithExpiredResetTokens(@Param("now") LocalDateTime now);

    // ========== RECHERCHE TEXTUELLE ==========

    /**
     * Recherche d'utilisateurs par nom, prénom, username ou email (LIKE)
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    /**
     * Recherche d'utilisateurs actifs seulement
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND (" +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchActiveUsers(@Param("search") String search, Pageable pageable);

    // ========== PAGINATION ET TRI ==========

    /**
     * Trouve tous les utilisateurs actifs avec pagination
     */
    Page<User> findByIsActiveTrue(Pageable pageable);


    Page<User> findByIsActive(Boolean isActive, Pageable pageable);

    /**
     * Trouve tous les utilisateurs par rôle avec pagination
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRole(@Param("roleName") Role.RoleName roleName, Pageable pageable);

    // ========== OPÉRATIONS DE MISE À JOUR ==========

    /**
     * Met à jour le statut actif d'un utilisateur
     */
    @Modifying
    @Query("UPDATE User u SET u.isActive = :isActive, u.updatedAt = :updatedAt WHERE u.id = :userId")
    int updateUserActiveStatus(@Param("userId") Long userId,
                               @Param("isActive") Boolean isActive,
                               @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Met à jour le statut de vérification email
     */
    @Modifying
    @Query("UPDATE User u SET u.isEmailVerified = true, u.emailVerificationToken = null, u.updatedAt = :updatedAt WHERE u.id = :userId")
    int updateEmailVerificationStatus(@Param("userId") Long userId, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Réinitialise les tentatives de connexion
     */
    @Modifying
    @Query("UPDATE User u SET u.loginAttempts = 0, u.accountLockedUntil = null, u.updatedAt = :updatedAt WHERE u.id = :userId")
    int resetLoginAttempts(@Param("userId") Long userId, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Met à jour la dernière connexion
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin, u.updatedAt = :updatedAt WHERE u.id = :userId")
    int updateLastLogin(@Param("userId") Long userId,
                        @Param("lastLogin") LocalDateTime lastLogin,
                        @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Nettoie les tokens de reset expirés
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordResetToken = null, u.passwordResetTokenExpiry = null, u.updatedAt = :updatedAt " +
            "WHERE u.passwordResetTokenExpiry < :now")
    int cleanupExpiredResetTokens(@Param("now") LocalDateTime now, @Param("updatedAt") LocalDateTime updatedAt);

    // ========== STATISTIQUES ==========

    /**
     * Compte le nombre d'utilisateurs actifs
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();

    /**
     * Compte le nombre d'utilisateurs par rôle
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Long countUsersByRole(@Param("roleName") Role.RoleName roleName);

    /**
     * Compte les nouvelles inscriptions depuis une date
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    Long countNewUsersSince(@Param("date") LocalDateTime date);

    /**
     * Compte les utilisateurs connectés depuis une date
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin >= :date")
    Long countActiveUsersSince(@Param("date") LocalDateTime date);

    /**
     * Sélectionne tous les utilisateurs avec leurs rôles pour la pagination
     */
    @Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles",
            countQuery = "SELECT COUNT(DISTINCT u) FROM User u")
    Page<User> findAllWithRoles(Pageable pageable);
}
