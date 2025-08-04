package sn.svs.backoffice.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.svs.backoffice.config.GlobalExceptionHandler;
import sn.svs.backoffice.domain.ennumeration.ShipClassification;
import sn.svs.backoffice.domain.ennumeration.ShipFlag;
import sn.svs.backoffice.domain.ennumeration.ShipType;
import sn.svs.backoffice.dto.ShipDTO;
import sn.svs.backoffice.service.ShipService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur REST pour la gestion des navires
 * SVS - Dakar, Sénégal
 */
@Slf4j
@RestController
@RequestMapping("api/v1/ships")
@RequiredArgsConstructor
@Tag(name = "Ships", description = "API de gestion des navires")
public class ShipController {

    private final ShipService shipService;

    /**
     * Créer un nouveau navire
     */
    @PostMapping
    @Operation(
            summary = "Créer un nouveau navire",
            description = "Crée un nouveau navire avec les informations fournies"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Navire créé avec succès",
                    content = @Content(schema = @Schema(implementation = ShipDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Navire déjà existant",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ShipDTO.Response> createShip(
            @Valid @RequestBody ShipDTO.CreateRequest createRequest) {

        log.info("Demande de création d'un navire: {}", createRequest.getNom());

        ShipDTO.Response response = shipService.createShip(createRequest);

        log.info("Navire créé avec succès - ID: {}", response.getId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Mettre à jour un navire existant
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Mettre à jour un navire",
            description = "Met à jour les informations d'un navire existant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Navire mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = ShipDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Navire non trouvé",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflit de données",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ShipDTO.Response> updateShip(
            @Parameter(description = "Identifiant du navire", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ShipDTO.UpdateRequest updateRequest) {

        log.info("Demande de mise à jour du navire ID: {}", id);

        ShipDTO.Response response = shipService.updateShip(id, updateRequest);

        log.info("Navire mis à jour avec succès - ID: {}", id);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer un navire par son identifiant
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Récupérer un navire",
            description = "Récupère les détails d'un navire par son identifiant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Navire trouvé",
                    content = @Content(schema = @Schema(implementation = ShipDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Navire non trouvé",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ShipDTO.Response> getShip(
            @Parameter(description = "Identifiant du navire", required = true)
            @PathVariable Long id) {

        log.debug("Demande de récupération du navire ID: {}", id);

        ShipDTO.Response response = shipService.getShipById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer tous les navires avec pagination et recherche
     */
    @GetMapping
    @Operation(
            summary = "Lister tous les navires",
            description = "Récupère la liste paginée de tous les navires avec possibilité de recherche et filtrage"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des navires récupérée",
                    content = @Content(schema = @Schema(implementation = ShipDTO.PageResponse.class)))
    })
    public ResponseEntity<ShipDTO.PageResponse> getAllShips(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Champ de tri", example = "nom")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direction du tri", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDirection,
            @Parameter(description = "Recherche textuelle (nom, IMO, MMSI, port d'attache)", example = "Dakar")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filtre par compagnie", example = "1")
            @RequestParam(required = false) Long compagnieId,
            @Parameter(description = "Filtre par type de navire", example = "CARGO")
            @RequestParam(required = false) ShipType typeNavire,
            @Parameter(description = "Filtre par pavillon", example = "SENEGAL")
            @RequestParam(required = false) ShipFlag pavillon,
            @Parameter(description = "Filtre par statut actif", example = "true")
            @RequestParam(required = false) Boolean active) {

        log.debug("Demande de liste des navires - Page: {}, Taille: {}, Tri: {} {}, Recherche: '{}', Compagnie: {}, Type: {}, Pavillon: {}, Actif: {}",
                page, size, sortBy, sortDirection, search, compagnieId, typeNavire, pavillon, active);

        // Créer le filtre avec tous les paramètres
        ShipDTO.SearchFilter filter = ShipDTO.SearchFilter.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .search(search)
                .compagnieId(compagnieId)
                .typeNavire(typeNavire)
                .pavillon(pavillon)
                .active(active)
                .build();

        ShipDTO.PageResponse response = shipService.searchShips(filter);

        log.debug("Recherche terminée - {} résultats trouvés sur {} total",
                response.getShips().size(), response.getTotal());

        return ResponseEntity.ok(response);
    }

    /**
     * Rechercher des navires avec filtres avancés
     */
    @PostMapping("/search")
    @Operation(
            summary = "Rechercher des navires",
            description = "Recherche des navires avec filtres avancés"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résultats de recherche",
                    content = @Content(schema = @Schema(implementation = ShipDTO.PageResponse.class)))
    })
    public ResponseEntity<ShipDTO.PageResponse> searchShips(
            @Valid @RequestBody ShipDTO.SearchFilter filter) {

        log.debug("Demande de recherche avec filtres: {}", filter);

        ShipDTO.PageResponse response = shipService.searchShips(filter);

        log.debug("Recherche terminée - {} résultats trouvés", response.getTotal());

        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer un navire (suppression logique)
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer un navire",
            description = "Supprime logiquement un navire (le marque comme inactif)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Navire supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Navire non trouvé",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteShip(
            @Parameter(description = "Identifiant du navire", required = true)
            @PathVariable Long id) {

        log.info("Demande de suppression du navire ID: {}", id);

        shipService.deleteShip(id);

        log.info("Navire supprimé avec succès - ID: {}", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Activer un navire
     */
    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "Activer un navire",
            description = "Active un navire précédemment désactivé"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Navire activé avec succès",
                    content = @Content(schema = @Schema(implementation = ShipDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Navire non trouvé",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ShipDTO.Response> activateShip(
            @Parameter(description = "Identifiant du navire", required = true)
            @PathVariable Long id) {

        log.info("Demande d'activation du navire ID: {}", id);

        ShipDTO.Response response = shipService.activateShip(id);

        log.info("Navire activé avec succès - ID: {}", id);

        return ResponseEntity.ok(response);
    }

    /**
     * Désactiver un navire
     */
    @PatchMapping("/{id}/deactivate")
    @Operation(
            summary = "Désactiver un navire",
            description = "Désactive un navire sans le supprimer"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Navire désactivé avec succès",
                    content = @Content(schema = @Schema(implementation = ShipDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Navire non trouvé",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ShipDTO.Response> deactivateShip(
            @Parameter(description = "Identifiant du navire", required = true)
            @PathVariable Long id) {

        log.info("Demande de désactivation du navire ID: {}", id);

        ShipDTO.Response response = shipService.deactivateShip(id);

        log.info("Navire désactivé avec succès - ID: {}", id);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer tous les navires actifs (pour les listes déroulantes)
     */
    @GetMapping("/active")
    @Operation(
            summary = "Lister les navires actifs",
            description = "Récupère la liste de tous les navires actifs"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des navires actifs",
                    content = @Content(schema = @Schema(implementation = ShipDTO.Response.class)))
    })
    public ResponseEntity<List<ShipDTO.Response>> getActiveShips() {

        log.debug("Demande de liste des navires actifs");

        List<ShipDTO.Response> response = shipService.getActiveShips();

        log.debug("Trouvé {} navires actifs", response.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer tous les navires actifs (version résumée)
     */
    @GetMapping("/active/summary")
    @Operation(
            summary = "Lister les navires actifs (résumé)",
            description = "Récupère la liste résumée de tous les navires actifs"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste résumée des navires actifs",
                    content = @Content(schema = @Schema(implementation = ShipDTO.Summary.class)))
    })
    public ResponseEntity<List<ShipDTO.Summary>> getActiveShipsSummary() {

        log.debug("Demande de liste résumée des navires actifs");

        List<ShipDTO.Summary> response = shipService.getActiveShipsSummary();

        log.debug("Trouvé {} navires actifs", response.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les navires d'une compagnie
     */
    @GetMapping("/company/{compagnieId}")
    @Operation(
            summary = "Navires d'une compagnie",
            description = "Récupère tous les navires d'une compagnie spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des navires de la compagnie",
                    content = @Content(schema = @Schema(implementation = ShipDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Compagnie non trouvée",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<List<ShipDTO.Response>> getShipsByCompany(
            @Parameter(description = "Identifiant de la compagnie", required = true)
            @PathVariable Long compagnieId,
            @Parameter(description = "Filtrer uniquement les navires actifs", example = "true")
            @RequestParam(defaultValue = "true") Boolean activeOnly) {

        log.debug("Demande de navires pour la compagnie ID: {}, actifs seulement: {}", compagnieId, activeOnly);

        List<ShipDTO.Response> response = shipService.getShipsByCompany(compagnieId, activeOnly);

        log.debug("Trouvé {} navires pour la compagnie ID: {}", response.size(), compagnieId);

        return ResponseEntity.ok(response);
    }

    /**
     * Rechercher un navire par numéro IMO
     */
    @GetMapping("/search/imo/{numeroIMO}")
    @Operation(
            summary = "Rechercher par IMO",
            description = "Recherche un navire par son numéro IMO"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Navire trouvé",
                    content = @Content(schema = @Schema(implementation = ShipDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Navire non trouvé")
    })
    public ResponseEntity<ShipDTO.Response> findByIMO(
            @Parameter(description = "Numéro IMO", required = true)
            @PathVariable String numeroIMO) {

        log.debug("Recherche de navire par IMO: {}", numeroIMO);

        Optional<ShipDTO.Response> response = shipService.findByNumeroIMO(numeroIMO);

        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Rechercher un navire par numéro MMSI
     */
    @GetMapping("/search/mmsi/{numeroMMSI}")
    @Operation(
            summary = "Rechercher par MMSI",
            description = "Recherche un navire par son numéro MMSI"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Navire trouvé",
                    content = @Content(schema = @Schema(implementation = ShipDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Navire non trouvé")
    })
    public ResponseEntity<ShipDTO.Response> findByMMSI(
            @Parameter(description = "Numéro MMSI", required = true)
            @PathVariable String numeroMMSI) {

        log.debug("Recherche de navire par MMSI: {}", numeroMMSI);

        Optional<ShipDTO.Response> response = shipService.findByNumeroMMSI(numeroMMSI);

        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupérer les navires passagers
     */
    @GetMapping("/passengers")
    @Operation(
            summary = "Navires passagers",
            description = "Récupère tous les navires pouvant transporter des passagers"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des navires passagers",
                    content = @Content(schema = @Schema(implementation = ShipDTO.Response.class)))
    })
    public ResponseEntity<List<ShipDTO.Response>> getPassengerShips() {

        log.debug("Demande de navires passagers");

        List<ShipDTO.Response> response = shipService.getPassengerShips();

        log.debug("Trouvé {} navires passagers", response.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir les statistiques des navires par type
     */
    @GetMapping("/statistics/types")
    @Operation(
            summary = "Statistiques par type",
            description = "Récupère les statistiques du nombre de navires par type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées")
    })
    public ResponseEntity<List<Object[]>> getStatisticsByType() {

        log.debug("Demande de statistiques par type de navire");

        List<Object[]> statistics = shipService.getShipStatisticsByType();

        return ResponseEntity.ok(statistics);
    }

    /**
     * Obtenir les statistiques des navires par pavillon
     */
    @GetMapping("/statistics/flags")
    @Operation(
            summary = "Statistiques par pavillon",
            description = "Récupère les statistiques du nombre de navires par pavillon"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées")
    })
    public ResponseEntity<List<Object[]>> getStatisticsByFlag() {

        log.debug("Demande de statistiques par pavillon");

        List<Object[]> statistics = shipService.getShipStatisticsByFlag();

        return ResponseEntity.ok(statistics);
    }

    /**
     * Obtenir les statistiques des navires par compagnie
     */
    @GetMapping("/statistics/companies")
    @Operation(
            summary = "Statistiques par compagnie",
            description = "Récupère les statistiques du nombre de navires par compagnie"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées")
    })
    public ResponseEntity<List<Object[]>> getStatisticsByCompany() {

        log.debug("Demande de statistiques par compagnie");

        List<Object[]> statistics = shipService.getShipStatisticsByCompany();

        return ResponseEntity.ok(statistics);
    }

    /**
     * Vérifier si un navire existe
     */
    @GetMapping("/{id}/exists")
    @Operation(
            summary = "Vérifier l'existence",
            description = "Vérifie si un navire existe avec l'identifiant donné"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vérification effectuée")
    })
    public ResponseEntity<Boolean> existsById(
            @Parameter(description = "Identifiant du navire", required = true)
            @PathVariable Long id) {

        log.debug("Vérification de l'existence du navire ID: {}", id);

        boolean exists = shipService.existsById(id);

        return ResponseEntity.ok(exists);
    }

    /**
     * Compter les navires d'une compagnie
     */
    @GetMapping("/company/{compagnieId}/count")
    @Operation(
            summary = "Compter les navires d'une compagnie",
            description = "Compte le nombre de navires actifs d'une compagnie"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nombre de navires récupéré"),
            @ApiResponse(responseCode = "404", description = "Compagnie non trouvée",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Long> countShipsByCompany(
            @Parameter(description = "Identifiant de la compagnie", required = true)
            @PathVariable Long compagnieId) {

        log.debug("Demande de comptage des navires pour la compagnie ID: {}", compagnieId);

        Long count = shipService.countShipsByCompany(compagnieId);

        log.debug("Compagnie ID: {} a {} navires", compagnieId, count);

        return ResponseEntity.ok(count);
    }


    /**
     * Récupérer tous les types de navires disponibles
     */
    @GetMapping("/types")
    @Operation(
            summary = "Types de navires",
            description = "Récupère la liste de tous les types de navires disponibles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des types de navires")
    })
    public ResponseEntity<Map<String, String>> getShipTypes() {

        log.debug("Demande de liste des types de navires");

        Map<String, String> types = new HashMap<>();
        for (ShipType type : ShipType.values()) {
            types.put(type.name(), type.getDisplayName());
        }

        return ResponseEntity.ok(types);
    }

    /**
     * Récupérer tous les pavillons disponibles
     */
    @GetMapping("/flags")
    @Operation(
            summary = "Pavillons de navires",
            description = "Récupère la liste de tous les pavillons disponibles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des pavillons")
    })
    public ResponseEntity<Map<String, String>> getShipFlags() {

        log.debug("Demande de liste des pavillons");

        Map<String, String> flags = new HashMap<>();
        for (ShipFlag flag : ShipFlag.values()) {
            flags.put(flag.name(), flag.getDisplayName());
        }

        return ResponseEntity.ok(flags);
    }

    /**
     * Récupérer toutes les classifications disponibles
     */
    @GetMapping("/classifications")
    @Operation(
            summary = "Classifications de navires",
            description = "Récupère la liste de toutes les classifications disponibles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des classifications")
    })
    public ResponseEntity<Map<String, String>> getShipClassifications() {

        log.debug("Demande de liste des classifications");

        Map<String, String> classifications = new HashMap<>();
        for (ShipClassification classification : ShipClassification.values()) {
            classifications.put(classification.name(), classification.getDisplayName());
        }

        return ResponseEntity.ok(classifications);
    }

}
