// src/main/java/sn/svs/backoffice/web/rest/EmailTestController.java
package sn.svs.backoffice.web.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.svs.backoffice.service.EmailService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/email")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EmailTestController {

    private final EmailService emailService;

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testEmail() {
        boolean success = emailService.testEmailConfiguration();

        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Email de test envoyé avec succès" : "Échec de l'envoi de l'email de test"
        ));
    }

    @PostMapping("/deployment-notification")
    public ResponseEntity<Map<String, Object>> testDeploymentNotification(
            @RequestParam(defaultValue = "latest") String version,
            @RequestParam(defaultValue = "staging") String environment,
            @RequestParam(defaultValue = "true") boolean success,
            @RequestParam(required = false) String details) {

        try {
            emailService.sendDeploymentNotification(version, environment, success, details);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notification de déploiement envoyée"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Erreur: " + e.getMessage()
            ));
        }
    }
}
