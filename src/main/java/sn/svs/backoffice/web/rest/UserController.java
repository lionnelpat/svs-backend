package sn.svs.backoffice.web.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.svs.backoffice.dto.UserDTO;
import sn.svs.backoffice.service.UserService;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur CRUD pour la gestion des utilisateurs
 * SVS - Dakar, Sénégal
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class UserController {

    private final UserService userService;

    /**
     * Liste tous les utilisateurs avec pagination
     * GET /api/v1/admin/users
     */
    @GetMapping
    public ResponseEntity<UserDTO.PageResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.debug("Récupération des utilisateurs - Page: {}, Size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        UserDTO.PageResponse response = userService.findAll(pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Liste tous les utilisateurs actifs (pour les sélecteurs)
     * GET /api/v1/admin/users/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<UserDTO.Summary>> getActiveUsers() {
        log.debug("Récupération des utilisateurs actifs");

        List<UserDTO.Summary> users = userService.findAllActive();
        return ResponseEntity.ok(users);
    }

    /**
     * Recherche des utilisateurs
     * POST /api/v1/admin/users/search
     */
    @PostMapping("/search")
    public ResponseEntity<UserDTO.PageResponse> searchUsers(
            @RequestBody(required = false) UserDTO.SearchFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.debug("Recherche d'utilisateurs avec filtre: {}", filter);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (filter == null) {
            filter = new UserDTO.SearchFilter();
        }

        UserDTO.PageResponse response = userService.search(filter, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère un utilisateur par ID
     * GET /api/v1/admin/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO.Response> getUserById(@PathVariable Long id) {
        log.debug("Récupération de l'utilisateur ID: {}", id);

        try {
            UserDTO.Response user = userService.findById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.warn("Utilisateur non trouvé avec l'ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crée un nouvel utilisateur
     * POST /api/v1/admin/users
     */
    @PostMapping
    public ResponseEntity<UserDTO.Response> createUser(@Valid @RequestBody UserDTO.CreateRequest request) {
        log.info("Création d'un nouvel utilisateur: {}", request.getUsername());

        try {
            UserDTO.Response createdUser = userService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors de la création de l'utilisateur: {}", e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Met à jour un utilisateur
     * PUT /api/v1/admin/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO.Response> updateUser(@PathVariable Long id,
                                                       @Valid @RequestBody UserDTO.UpdateRequest request) {
        log.info("Mise à jour de l'utilisateur ID: {}", id);

        try {
            UserDTO.Response updatedUser = userService.update(id, request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            log.warn("Erreur lors de la mise à jour de l'utilisateur {}: {}", id, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Active un utilisateur
     * PATCH /api/v1/admin/users/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserDTO.Response> activateUser(@PathVariable Long id) {
        log.info("Activation de l'utilisateur ID: {}", id);

        try {
            UserDTO.Response activatedUser = userService.activate(id);
            return ResponseEntity.ok(activatedUser);
        } catch (RuntimeException e) {
            log.warn("Erreur lors de l'activation de l'utilisateur {}: {}", id, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Désactive un utilisateur
     * PATCH /api/v1/admin/users/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserDTO.Response> deactivateUser(@PathVariable Long id) {
        log.info("Désactivation de l'utilisateur ID: {}", id);

        try {
            UserDTO.Response deactivatedUser = userService.deactivate(id);
            return ResponseEntity.ok(deactivatedUser);
        } catch (RuntimeException e) {
            log.warn("Erreur lors de la désactivation de l'utilisateur {}: {}", id, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Déverrouille un compte utilisateur
     * PATCH /api/v1/admin/users/{id}/unlock
     */
    @PatchMapping("/{id}/unlock")
    public ResponseEntity<UserDTO.Response> unlockUser(@PathVariable Long id) {
        log.info("Déverrouillage du compte utilisateur ID: {}", id);

        try {
            UserDTO.Response unlockedUser = userService.unlock(id);
            return ResponseEntity.ok(unlockedUser);
        } catch (RuntimeException e) {
            log.warn("Erreur lors du déverrouillage de l'utilisateur {}: {}", id, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Supprime un utilisateur
     * DELETE /api/v1/admin/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        log.info("Suppression de l'utilisateur ID: {}", id);

        try {
            if (!userService.canDelete(id)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Cet utilisateur ne peut pas être supprimé"));
            }

            userService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé avec succès"));
        } catch (RuntimeException e) {
            log.warn("Erreur lors de la suppression de l'utilisateur {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Vérifie si un utilisateur peut être supprimé
     * GET /api/v1/admin/users/{id}/can-delete
     */
    @GetMapping("/{id}/can-delete")
    public ResponseEntity<Map<String, Boolean>> canDeleteUser(@PathVariable Long id) {
        log.debug("Vérification de suppression pour l'utilisateur ID: {}", id);

        try {
            boolean canDelete = userService.canDelete(id);
            return ResponseEntity.ok(Map.of("canDelete", canDelete));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

