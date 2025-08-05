package sn.svs.backoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import sn.svs.backoffice.config.MailProperties;
import org.springframework.stereotype.Service;
import sn.svs.backoffice.service.EmailService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true", matchIfMissing = true)
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties; // ← CHANGÉ: Injecter les propriétés

    @Override
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("Email envoyé avec succès à: {}", to);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", to, e.getMessage());
        }
    }

    @Override
    public void sendDeploymentNotification(String version, String environment, boolean success, String details) {
        String status = success ? "SUCCÈS" : "ÉCHEC";
        String subject = String.format("%s Déploiement %s - %s",
                mailProperties.getDeployment().getSubjectPrefix(), status, environment.toUpperCase());

        String content = buildDeploymentEmailContent(version, environment, success, details);

        mailProperties.getDeployment().getRecipients().forEach(recipient -> {
            sendSimpleEmail(recipient, subject, content);
        });
    }

    @Override
    public String buildDeploymentEmailContent(String version, String environment, boolean success, String details) {
        StringBuilder content = new StringBuilder();

        content.append("=== NOTIFICATION DE DÉPLOIEMENT SVS ===\n\n");

        content.append("📊 INFORMATIONS GÉNÉRALES\n");
        content.append("• Statut: ").append(success ? "✅ SUCCÈS" : "❌ ÉCHEC").append("\n");
        content.append("• Version: ").append(version != null ? version : "latest").append("\n");
        content.append("• Environnement: ").append(environment.toUpperCase()).append("\n");
        content.append("• Date/Heure: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n\n");

        if (success) {
            content.append("🚀 DÉPLOIEMENT RÉUSSI\n");
            content.append("L'application a été déployée avec succès et est maintenant disponible.\n\n");

            content.append("🔗 LIENS UTILES\n");
            content.append("• Frontend: https://svs-frontend.salanevision.com\n");
            content.append("• Backend API: https://svs-api-backend.salanevision.com\n");
            content.append("• Portainer: https://svs-portainer.salanevision.com\n");
            content.append("• Monitoring: https://svs-dozzle.salanevision.com\n\n");
        } else {
            content.append("❌ DÉPLOIEMENT ÉCHOUÉ\n");
            content.append("Le déploiement a rencontré des erreurs. Veuillez vérifier les logs.\n\n");
        }

        if (details != null && !details.trim().isEmpty()) {
            content.append("📝 DÉTAILS\n");
            content.append(details).append("\n\n");
        }

        content.append("---\n");
        content.append("Ce message a été généré automatiquement par le système SVS.\n");
        content.append("Pour plus d'informations, consultez Portainer ou les logs de l'application.");

        return content.toString();
    }

    @Override
    public void sendApplicationStartupNotification(String environment) {
        String subject = String.format("%s Application Démarrée - %s",
                mailProperties.getDeployment().getSubjectPrefix(), environment.toUpperCase());

        String content = String.format(
                new StringBuilder().append("=== APPLICATION SVS DÉMARRÉE ===\n\n").append("L'application SVS a démarré avec succès.\n\n").append("📊 INFORMATIONS\n").append("• Environnement: %s\n").append("• Date/Heure: %s\n").append("• Statut: ✅ OPÉRATIONNELLE\n\n").append("🔗 ACCÈS\n").append("• Frontend: https://svs-frontend.salanevision.com\n").append("• Backend API: https://svs-api-backend.salanevision.com\n\n").append("---\n").append("Notification automatique du système SVS.").toString(),
                environment.toUpperCase(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );

        mailProperties.getDeployment().getRecipients().forEach(recipient -> {
            sendSimpleEmail(recipient, subject, content);
        });
    }

    @Override
    public boolean testEmailConfiguration() {
        try {
            String subject = mailProperties.getDeployment().getSubjectPrefix() + " Test de Configuration";
            String content = "Ceci est un email de test pour vérifier la configuration SMTP.\n\n" +
                    "Si vous recevez cet email, la configuration est correcte !\n\n" +
                    "Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            sendSimpleEmail(mailProperties.getFrom(), subject, content);
            return true;
        } catch (Exception e) {
            log.error("Test de configuration email échoué: {}", e.getMessage());
            return false;
        }
    }
}