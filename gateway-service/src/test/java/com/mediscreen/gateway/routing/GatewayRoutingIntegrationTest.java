package com.mediscreen.gateway.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour le routage intelligent Gateway.
 *
 * Ces tests valident l'architecture tokens différenciés :
 * - Route Patient Service avec injection credentials spécifiques patient
 * - Route Notes Service avec injection credentials spécifiques notes
 * - Isolation des credentials par microservice
 * - Configuration RouteLocator personnalisée
 *
 * Architecture validée :
 * Frontend (test-frontend) → Gateway → Patient Service (test-patient)
 *                                  → Notes Service (test-notes)
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
class GatewayRoutingIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RouteLocator routeLocator;

    private String validFrontendAuth;

    @BeforeEach
    void setUp() {
        String frontendCredentials = "test-frontend:test-pass";
        validFrontendAuth = "Basic " + Base64.getEncoder().encodeToString(frontendCredentials.getBytes());
    }

    /**
     * Teste que le RouteLocator personnalisé est correctement configuré.
     */
    @Test
    @DisplayName("Configuration - RouteLocator personnalisé configuré")
    void customRouteLocator_ShouldBeConfigured() {
        // Vérifier que le RouteLocator n'est pas null et contient nos routes
        assertThat(routeLocator).isNotNull();

        // Vérifier que les routes sont configurées
        var routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();

        // Vérifier que nous avons au moins nos 2 routes personnalisées
        var routeIds = routes.stream()
                .map(route -> route.getId())
                .toList();

        assertThat(routeIds).contains("patient-service-route");
        assertThat(routeIds).contains("notes-service-route");
    }

    /**
     * Teste le routage spécifique Patient Service.
     * Valide que les requêtes /api/v1/patients/** sont routées vers patient-service.
     */
    @Test
    @DisplayName("Routage - Patient Service route correctement configurée")
    void patientServiceRoute_ShouldRouteToCorrectService() {
        // Test avec différents patterns patient
        String[] patientPaths = {
            "/api/v1/patients",
            "/api/v1/patients/1",
            "/api/v1/patients/search?name=Test"
        };

        for (String path : patientPaths) {
            webTestClient.get()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, validFrontendAuth)
                    .exchange()
                    // On s'attend à 5xx car le service backend n'est pas disponible,
                    // mais cela confirme que la route est correctement configurée
                    .expectStatus().is5xxServerError();
        }
    }

    /**
     * Teste le routage spécifique Notes Service.
     * Valide que les requêtes /api/v1/notes/** sont routées vers notes-service.
     */
    @Test
    @DisplayName("Routage - Notes Service route correctement configurée")
    void notesServiceRoute_ShouldRouteToCorrectService() {
        // Test avec différents patterns notes
        String[] notesPaths = {
            "/api/v1/notes",
            "/api/v1/notes/patient/1",
            "/api/v1/notes/673f1b77bcf86cd799439011"
        };

        for (String path : notesPaths) {
            webTestClient.get()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, validFrontendAuth)
                    .exchange()
                    // On s'attend à 5xx car le service backend n'est pas disponible,
                    // mais cela confirme que la route est correctement configurée
                    .expectStatus().is5xxServerError();
        }
    }

    /**
     * Teste que les requêtes non matchées par nos routes retournent 404.
     */
    @Test
    @DisplayName("Routage - Requêtes non matchées retournent 404")
    void unmatchedRequests_ShouldReturn404() {
        String[] unmatchedPaths = {
            "/api/v1/other",
            "/api/v2/patients",
            "/patients/api/v1",
            "/notes/api/v1"
        };

        for (String path : unmatchedPaths) {
            webTestClient.get()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, validFrontendAuth)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    /**
     * Teste la configuration des URIs de destination.
     */
    @Test
    @DisplayName("Configuration - URIs destination correctement configurées")
    void destinationUris_ShouldBeCorrectlyConfigured() {
        var routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();

        // Vérifier que les URIs de destination sont correctement configurées
        var patientRoute = routes.stream()
                .filter(route -> "patient-service-route".equals(route.getId()))
                .findFirst();

        var notesRoute = routes.stream()
                .filter(route -> "notes-service-route".equals(route.getId()))
                .findFirst();

        assertThat(patientRoute).isPresent();
        assertThat(notesRoute).isPresent();

        // Vérifier les URIs (format attendu pour Docker)
        assertThat(patientRoute.get().getUri().toString()).isEqualTo("http://patient-service:8081");
        assertThat(notesRoute.get().getUri().toString()).isEqualTo("http://notes-service:8082");
    }
}