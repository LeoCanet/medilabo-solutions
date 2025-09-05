package com.mediscreen.patientservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration OpenAPI pour Swagger UI
 * Necessaire pour Spring Boot 3.5.5
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Patient Service API")
                        .version("1.0.0")
                        .description("API REST pour la gestion des patients - Mediscreen Sprint 1")
                        .termsOfService("https://mediscreen.com/terms")
                        .contact(new Contact()
                                .name("Equipe Mediscreen")
                                .email("support@mediscreen.com")
                                .url("https://mediscreen.com/contact"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Serveur de developpement local"),
                        new Server()
                                .url("http://localhost:8888")
                                .description("Serveur via Gateway")
                ));
    }
}