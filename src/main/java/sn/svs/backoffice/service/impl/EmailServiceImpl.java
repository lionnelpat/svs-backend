// ========== IMPLÉMENTATION EMAILSERVICEIMPL ==========
package sn.svs.backoffice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sn.svs.backoffice.service.EmailService;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Implémentation du service d'envoi d'emails pour Salane Vision S.a.r.l
 * Gère l'envoi d'emails avec templates formels pour l'entreprise maritime
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.company-name}")
    private String companyName;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${app.email.signature.name}")
    private String signatureName;

    @Value("${app.email.signature.title}")
    private String signatureTitle;

    @Value("${app.email.company.address}")
    private String companyAddress;

    @Value("${app.email.company.phone}")
    private String companyPhone;

    @Value("${app.email.company.website}")
    private String companyWebsite;

    /**
     * Envoie un email simple (texte brut)
     */
    @Override
    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            javaMailSender.send(message);

            log.info("Email simple envoyé avec succès à: {}", to);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi d'email simple à {}: {}", to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi d'email", e);
        }
    }

    /**
     * Envoie un email HTML
     */
    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, companyName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);

            log.info("Email HTML envoyé avec succès à: {}", to);

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi d'email HTML à {}: {}", to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi d'email HTML", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Envoie un email avec template Thymeleaf
     */
    @Override
    public void sendTemplateEmail(String to, String subject, String templateName, Object templateData) {
        try {
            Context context = new Context(Locale.FRENCH);

            // Ajouter les données du template
            if (templateData instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) templateData;
                context.setVariables(dataMap);
            }

            // Ajouter les variables communes de l'entreprise
            addCompanyVariables(context);

            String htmlContent = templateEngine.process(templateName, context);
            sendHtmlEmail(to, subject, htmlContent);

            log.info("Email avec template '{}' envoyé avec succès à: {}", templateName, to);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi d'email avec template '{}' à {}: {}",
                    templateName, to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi d'email avec template", e);
        }
    }

    /**
     * Envoie un email de vérification de compte
     */
    public void sendEmailVerification(String to, String firstName, String verificationToken) {
        try {
            String verificationUrl = frontendBaseUrl + "/auth/verify-email?token=" + verificationToken;

            Context context = new Context(Locale.FRENCH);
            context.setVariable("firstName", firstName);
            context.setVariable("verificationUrl", verificationUrl);
            context.setVariable("validityHours", "24");
            addCompanyVariables(context);

            String htmlContent = templateEngine.process("email/email-verification", context);
            String subject = "Vérification de votre compte - " + companyName;

            sendHtmlEmail(to, subject, htmlContent);

            log.info("Email de vérification envoyé à: {}", to);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi d'email de vérification à {}: {}", to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi d'email de vérification", e);
        }
    }

    /**
     * Envoie un email de réinitialisation de mot de passe
     */
    public void sendPasswordReset(String to, String firstName, String resetToken) {
        try {
            String resetUrl = frontendBaseUrl + "/auth/reset-password?token=" + resetToken;

            Context context = new Context(Locale.FRENCH);
            context.setVariable("firstName", firstName);
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("validityHours", "1");
            addCompanyVariables(context);

            String htmlContent = templateEngine.process("email/password-reset", context);
            String subject = "Réinitialisation de votre mot de passe - " + companyName;

            sendHtmlEmail(to, subject, htmlContent);

            log.info("Email de réinitialisation envoyé à: {}", to);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi d'email de réinitialisation à {}: {}", to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi d'email de réinitialisation", e);
        }
    }

    /**
     * Envoie un email de bienvenue après vérification
     */
    public void sendWelcomeEmail(String to, String firstName) {
        try {
            Context context = new Context(Locale.FRENCH);
            context.setVariable("firstName", firstName);
            context.setVariable("loginUrl", frontendBaseUrl + "/auth/login");
            addCompanyVariables(context);

            String htmlContent = templateEngine.process("email/welcome", context);
            String subject = "Bienvenue chez " + companyName;

            sendHtmlEmail(to, subject, htmlContent);

            log.info("Email de bienvenue envoyé à: {}", to);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi d'email de bienvenue à {}: {}", to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi d'email de bienvenue", e);
        }
    }

    /**
     * Envoie un email de notification de changement de mot de passe
     */
    public void sendPasswordChangeNotification(String to, String firstName) {
        try {
            Context context = new Context(Locale.FRENCH);
            context.setVariable("firstName", firstName);
            context.setVariable("changeDate", LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm", Locale.FRENCH)));
            addCompanyVariables(context);

            String htmlContent = templateEngine.process("email/password-change-notification", context);
            String subject = "Modification de votre mot de passe - " + companyName;

            sendHtmlEmail(to, subject, htmlContent);

            log.info("Email de notification de changement de mot de passe envoyé à: {}", to);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi d'email de notification à {}: {}", to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi d'email de notification", e);
        }
    }

    /**
     * Ajoute les variables communes de l'entreprise au contexte
     */
    private void addCompanyVariables(Context context) {
        context.setVariable("companyName", companyName);
        context.setVariable("companyAddress", companyAddress);
        context.setVariable("companyPhone", companyPhone);
        context.setVariable("companyWebsite", companyWebsite);
        context.setVariable("signatureName", signatureName);
        context.setVariable("signatureTitle", signatureTitle);
        context.setVariable("currentYear", LocalDateTime.now().getYear());
        context.setVariable("currentDate", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH)));
    }
}
