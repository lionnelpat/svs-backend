
// ========== ROLE CONTROLLER CORRIGÉ ==========
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
import sn.svs.backoffice.dto.RoleDTO;
import sn.svs.backoffice.service.RoleService;

import java.util.List;
import java.util.Map;

import static sn.svs.backoffice.security.constants.SecurityConstants.HAS_ROLE_ADMIN;

/**
 * Contrôleur CRUD pour la gestion des rôles
 * SVS - Dakar, Sénégal
 */
@RestController
@RequestMapping("/api/v1/admin/roles")
@CrossOrigin(origins = {"http://localhost:4200", "https://svs-backoffice.com"})
@Slf4j
@RequiredArgsConstructor
//@PreAuthorize(HAS_ROLE_ADMIN)
public class RoleController {

    private final RoleService roleService;

    /**
     * Liste tous les rôles avec pagination
     * GET /api/v1/admin/roles
     */
    @GetMapping
    public ResponseEntity<RoleDTO.PageResponse> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.debug("Récupération des rôles - Page: {}, Size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        RoleDTO.PageResponse response = roleService.findAll(pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Liste tous les rôles actifs (pour les sélecteurs)
     * GET /api/v1/admin/roles/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<RoleDTO.Summary>> getActiveRoles() {
        log.debug("Récupération des rôles actifs");

        List<RoleDTO.Summary> roles = roleService.findAllActive();
        return ResponseEntity.ok(roles);
    }

    /**
     * Recherche des rôles
     * POST /api/v1/admin/roles/search
     */
    @PostMapping("/search")
    public ResponseEntity<RoleDTO.PageResponse> searchRoles(
            @RequestBody(required = false) RoleDTO.SearchFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.debug("Recherche de rôles avec filtre: {}", filter);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (filter == null) {
            filter = new RoleDTO.SearchFilter();
        }

        RoleDTO.PageResponse response = roleService.search(filter, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère un rôle par ID
     * GET /api/v1/admin/roles/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO.Response> getRoleById(@PathVariable Long id) {
        log.debug("Récupération du rôle ID: {}", id);

        try {
            RoleDTO.Response role = roleService.findById(id);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            log.warn("Rôle non trouvé avec l'ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crée un nouveau rôle
     * POST /api/v1/admin/roles
     */
    @PostMapping
    public ResponseEntity<RoleDTO.Response> createRole(@Valid @RequestBody RoleDTO.CreateRequest request) {
        log.info("Création d'un nouveau rôle: {}", request.getName());

        try {
            RoleDTO.Response createdRole = roleService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors de la création du rôle: {}", e.getMessage());
            // Ici on peut retourner une réponse d'erreur ou lever une exception
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Met à jour un rôle
     * PUT /api/v1/admin/roles/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO.Response> updateRole(@PathVariable Long id,
                                                       @Valid @RequestBody RoleDTO.UpdateRequest request) {
        log.info("Mise à jour du rôle ID: {}", id);

        try {
            RoleDTO.Response updatedRole = roleService.update(id, request);
            return ResponseEntity.ok(updatedRole);
        } catch (RuntimeException e) {
            log.warn("Erreur lors de la mise à jour du rôle {}: {}", id, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Active un rôle
     * PATCH /api/v1/admin/roles/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<RoleDTO.Response> activateRole(@PathVariable Long id) {
        log.info("Activation du rôle ID: {}", id);

        try {
            RoleDTO.Response activatedRole = roleService.activate(id);
            return ResponseEntity.ok(activatedRole);
        } catch (RuntimeException e) {
            log.warn("Erreur lors de l'activation du rôle {}: {}", id, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Désactive un rôle
     * PATCH /api/v1/admin/roles/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<RoleDTO.Response> deactivateRole(@PathVariable Long id) {
        log.info("Désactivation du rôle ID: {}", id);

        try {
            RoleDTO.Response deactivatedRole = roleService.deactivate(id);
            return ResponseEntity.ok(deactivatedRole);
        } catch (RuntimeException e) {
            log.warn("Erreur lors de la désactivation du rôle {}: {}", id, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Supprime un rôle
     * DELETE /api/v1/admin/roles/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRole(@PathVariable Long id) {
        log.info("Suppression du rôle ID: {}", id);

        try {
            if (!roleService.canDelete(id)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ce rôle ne peut pas être supprimé"));
            }

            roleService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Rôle supprimé avec succès"));
        } catch (RuntimeException e) {
            log.warn("Erreur lors de la suppression du rôle {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Vérifie si un rôle peut être supprimé
     * GET /api/v1/admin/roles/{id}/can-delete
     */
    @GetMapping("/{id}/can-delete")
    public ResponseEntity<Map<String, Boolean>> canDeleteRole(@PathVariable Long id) {
        log.debug("Vérification de suppression pour le rôle ID: {}", id);

        try {
            boolean canDelete = roleService.canDelete(id);
            return ResponseEntity.ok(Map.of("canDelete", canDelete));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}