package sn.svs.backoffice.service;

// ========== INTERFACE EMAILSERVICE (À CRÉER SÉPARÉMENT) ==========

/**
 * Interface pour le service d'envoi d'emails
 * Cette interface sera implémentée séparément
 */
public interface EmailService {

    /**
     * Envoie un email simple
     */
    void sendEmail(String to, String subject, String content);

    /**
     * Envoie un email HTML
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * Envoie un email avec template
     */
    void sendTemplateEmail(String to, String subject, String templateName, Object templateData);
}
