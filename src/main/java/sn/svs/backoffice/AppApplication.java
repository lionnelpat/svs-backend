package sn.svs.backoffice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({LiquibaseProperties.class})
@OpenAPIDefinition(
		info = @Info(
				title = "API SVS",
				version = "1.0.0",
				description = "API pour la gestion des factures et dépenses maritimes - Dakar, Sénégal",
				contact = @Contact(
						name = "Équipe SVS",
						email = "support@svs-maritime.sn",
						url = "https://svs-maritime.sn"
				),
				license = @License(
						name = "Propriétaire SVS",
						url = "https://svs-maritime.sn/license"
				)
		)
)
@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		bearerFormat = "JWT",
		scheme = "bearer",
		in = SecuritySchemeIn.HEADER
)
public class AppApplication {

	private final Environment env;

	public AppApplication(Environment env) {
		this.env = env;
	}

	/**
	 * Point d'entrée principal de l'application
	 */
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(AppApplication.class);
		Environment env = app.run(args).getEnvironment();
		logApplicationStartup(env);
	}

	/**
	 * Initialise les propriétés de l'application et vérifie la configuration
	 */
	@PostConstruct
	public void initApplication() {
		Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());

		log.info("=== Application Maritime SVS ===");
		log.info("Profils actifs: {}", activeProfiles);

		if (activeProfiles.contains("dev") && activeProfiles.contains("prod")) {
			log.error("Configuration incorrecte: les profils 'dev' et 'prod' ne peuvent pas être actifs simultanément");
			throw new IllegalStateException("Profils incompatibles activés");
		}

		// Vérification de la configuration JWT en production
		if (activeProfiles.contains("prod")) {
			String jwtSecret = env.getProperty("maritime.security.jwt.secret-key");
			if (jwtSecret == null || jwtSecret.length() < 32) {
				log.error("Configuration JWT insuffisante pour la production");
				throw new IllegalStateException("JWT secret key trop courte pour la production");
			}
		}

		log.info("Application Maritime SVS initialisée avec succès");
	}

	/**
	 * Log les informations de démarrage de l'application
	 */
	private static void logApplicationStartup(Environment env) {
		String protocol = "http";
		if (env.getProperty("server.ssl.key-store") != null) {
			protocol = "https";
		}

		String serverPort = env.getProperty("server.port", "8080");
		String contextPath = env.getProperty("server.servlet.context-path", "/");
		String hostAddress = "localhost";

		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.warn("Impossible de déterminer l'adresse IP de l'hôte", e);
		}

		log.info("""
            
            ----------------------------------------------------------
            \t🚢 Application Maritime SVS démarrée avec succès! 🚢
            \t
            \t🌍 Accès local: \t\t{}://localhost:{}{}
            \t🌐 Accès externe: \t\t{}://{}:{}{}
            \t📚 Documentation: \t\t{}://{}:{}{}/swagger-ui.html
            \t📊 Monitoring: \t\t{}://{}:{}{}/management/health
            \t
            \t📋 Profil(s): \t\t{}
            \t🗃️ Base de données: \t{}
            ----------------------------------------------------------
            """,
				protocol, serverPort, contextPath,
				protocol, hostAddress, serverPort, contextPath,
				protocol, hostAddress, serverPort, contextPath,
				protocol, hostAddress, serverPort, contextPath,
				env.getActiveProfiles().length == 0 ? "default" : Arrays.toString(env.getActiveProfiles()),
				env.getProperty("spring.datasource.url", "H2 (en mémoire)")
		);
	}

}
