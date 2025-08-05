package sn.svs.backoffice.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    private String from;
    private boolean enabled = true;
    private Deployment deployment = new Deployment();

    @Data
    public static class Deployment {
        private List<String> recipients;
        private String subjectPrefix = "[SVS Deployment]";
    }
}
