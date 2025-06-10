package sn.svs.backoffice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration OpenAPI/Swagger pour l'API SVS Maritime
 * SVS - Dakar, Sénégal
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SVS Maritime API")
                        .description("API de gestion des factures de prestations maritimes - SVS Dakar, Sénégal")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Équipe SVS")
                                .email("dev@svs.sn")
                                .url("https://svs.sn"))
                        .license(new License()
                                .name("Propriétaire SVS")
                                .url("https://svs.sn/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Serveur de développement"),
                        new Server()
                                .url("http://192.168.1.2:" + serverPort)
                                .description("Serveur local réseau")
                ));
    }
}
