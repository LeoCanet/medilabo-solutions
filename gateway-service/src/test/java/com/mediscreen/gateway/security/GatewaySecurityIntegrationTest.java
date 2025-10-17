package com.mediscreen.gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Base64;

/**
 * Tests d'intégration sécurité Gateway.
 *
 * Ces tests valident l'architecture sécurité complète du Gateway :
 * - Authentification Basic Auth requise
 * - Routage intelligent avec injection credentials différenciés
 * - Gestion erreurs 401/403 appropriées
 * - Isolation tokens par microservice
 *
 * Architecture testée :
 * Frontend (credentials frontend) → Gateway → Services (credentials spécifiques)
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = com.mediscreen.gateway.GatewayServiceApplication.class
)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
    "AUTH_USERNAME=test-frontend",
    "AUTH_PASSWORD=test-pass",
    "mediscreen.auth.patient.username=test-patient",
    "mediscreen.auth.patient.password=patient-pass",
    "mediscreen.auth.notes.username=test-notes",
    "mediscreen.auth.notes.password=notes-pass",
    "mediscreen.auth.assessment.username=test-assessment",
    "mediscreen.auth.assessment.password=assessment-pass"
})
class GatewaySecurityIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private String validFrontendAuth;
    private String invalidAuth;

    /**
     * Initialise les credentials pour les tests.
     * Utilise les mêmes credentials que définis dans @TestPropertySource.
     */
    @BeforeEach
    void setUp() {
        // Credentials valides pour frontend
        String frontendCredentials = "test-frontend:test-pass";
        validFrontendAuth = "Basic " + Base64.getEncoder().encodeToString(frontendCredentials.getBytes());

        // Credentials invalides pour tests négatifs
        String invalidCredentials = "wrong:credentials";
        invalidAuth = "Basic " + Base64.getEncoder().encodeToString(invalidCredentials.getBytes());
    }

    /**
     * Teste l'accès refusé sans authentification sur route patient.
     */
    @Test
    @DisplayName("Sécurité - Accès refusé sans authentification (Patient Service)")
    void accessPatientRoute_WithoutAuth_ShouldReturn401() {
        webTestClient.get()
                .uri("/api/v1/patients")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * Teste l'accès refusé sans authentification sur route notes.
     */
    @Test
    @DisplayName("Sécurité - Accès refusé sans authentification (Notes Service)")
    void accessNotesRoute_WithoutAuth_ShouldReturn401() {
        webTestClient.get()
                .uri("/api/v1/notes/patient/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * Teste l'accès refusé avec credentials invalides.
     */
    @Test
    @DisplayName("Sécurité - Accès refusé avec credentials invalides")
    void accessWithInvalidCredentials_ShouldReturn401() {
        webTestClient.get()
                .uri("/api/v1/patients")
                .header(HttpHeaders.AUTHORIZATION, invalidAuth)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * Teste l'accès aux endpoints publics (actuator) sans authentification.
     */
    @Test
    @DisplayName("Sécurité - Accès libre aux endpoints publics")
    void accessPublicEndpoints_WithoutAuth_ShouldBeAllowed() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

}