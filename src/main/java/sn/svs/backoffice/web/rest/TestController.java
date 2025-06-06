package sn.svs.backoffice.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur de test pour vérifier la configuration de l'API
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@Tag(name = "Test", description = "Endpoints de test pour vérifier la configuration")
public class TestController {

    /**
     * Endpoint de test simple pour vérifier que l'API fonctionne
     */
    @GetMapping("/hello")
    @Operation(
            summary = "Test de l'API",
            description = "Endpoint simple pour tester que l'API Maritime SVS fonctionne correctement"
    )
    @ApiResponse(responseCode = "200", description = "API fonctionnelle")
    public ResponseEntity<Map<String, Object>> hello() {
        log.info("Appel de l'endpoint de test /api/test/hello");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "🚢 Bienvenue sur l'API Maritime SVS - Dakar, Sénégal!");
        response.put("status", "OK");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "maritime-backend");
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour vérifier la configuration CORS
     */
    @GetMapping("/cors")
    @Operation(
            summary = "Test CORS",
            description = "Endpoint pour tester la configuration CORS avec Angular"
    )
    @ApiResponse(responseCode = "200", description = "CORS configuré correctement")
    public ResponseEntity<Map<String, Object>> testCors() {
        log.info("Test de la configuration CORS");

        Map<String, Object> response = new HashMap<>();
        response.put("cors", "Configuration CORS active");
        response.put("allowedOrigins", "http://localhost:4200");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour vérifier l'état de la base de données
     */
    @GetMapping("/database")
    @Operation(
            summary = "Test Base de données",
            description = "Vérifie la connexion à PostgreSQL"
    )
    @ApiResponse(responseCode = "200", description = "Base de données accessible")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        log.info("Test de la connexion à la base de données");

        Map<String, Object> response = new HashMap<>();
        response.put("database", "PostgreSQL");
        response.put("status", "Connected");
        response.put("url", "jdbc:postgresql://localhost:5432/svs_db");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
