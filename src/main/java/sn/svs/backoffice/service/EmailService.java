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
    void sendSimpleEmail(String to, String subject, String content);

    /**
     * Envoie un email HTML
     */
    void sendDeploymentNotification(String version, String environment, boolean success, String details);

    /**
     * Envoie un email avec template
     */
    String buildDeploymentEmailContent(String version, String environment, boolean success, String details);

    /**
     * Envoie une notification de démarrage de l'application
     */

    void sendApplicationStartupNotification(String environment);

    boolean testEmailConfiguration();
}
