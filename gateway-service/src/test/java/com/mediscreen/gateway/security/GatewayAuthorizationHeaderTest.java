package com.mediscreen.gateway.security;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Tests WireMock - Injection headers Authorization par Gateway
 *
 * OBJECTIF : Vérifier que la Gateway injecte les bons credentials Basic Auth
 * vers chaque microservice backend
 *
 * ARCHITECTURE TESTÉE :
 * - Frontend → Gateway (avec credentials frontend)
 * - Gateway → Patient Service (injection credentials patient)
 * - Gateway → Notes Service (injection credentials notes)
 * - Gateway → Assessment Service (injection credentials assessment)
 *
 * APPROCHE :
 * - @SpringBootTest(classes = WireMockConfig.class) pour charger UNIQUEMENT la config de test
 * - WireMock démarre AVANT Spring via @DynamicPropertySource
 * - URIs variabilisées (mediscreen.services.*.uri) surchargées avec ports WireMock dynamiques
 * - Même approche que SecurityConfig de production (injection via @Value)
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = GatewayAuthorizationHeaderTest.WireMockConfig.class
)
@DisplayName("Tests WireMock - Injection headers Authorization Gateway")
class GatewayAuthorizationHeaderTest {

    @Autowired
    private WebTestClient webTestClient;

    private static WireMockServer patientServiceMock;
    private static WireMockServer notesServiceMock;
    private static WireMockServer assessmentServiceMock;

    // Credentials backend attendus (injectés par Gateway)
    private static final String PATIENT_USERNAME = "mediscreen-patient";
    private static final String PATIENT_PASSWORD = "patientpass123";
    private static final String PATIENT_AUTH_HEADER = "Basic " +
            Base64.getEncoder().encodeToString((PATIENT_USERNAME + ":" + PATIENT_PASSWORD).getBytes());

    private static final String NOTES_USERNAME = "mediscreen-notes";
    private static final String NOTES_PASSWORD = "notespass123";
    private static final String NOTES_AUTH_HEADER = "Basic " +
            Base64.getEncoder().encodeToString((NOTES_USERNAME + ":" + NOTES_PASSWORD).getBytes());

    private static final String ASSESSMENT_USERNAME = "mediscreen-assessment";
    private static final String ASSESSMENT_PASSWORD = "assessmentpass123";
    private static final String ASSESSMENT_AUTH_HEADER = "Basic " +
            Base64.getEncoder().encodeToString((ASSESSMENT_USERNAME + ":" + ASSESSMENT_PASSWORD).getBytes());

    /**
     * Configuration de test WireMock (remplace SecurityConfig)
     */
    @org.springframework.boot.SpringBootConfiguration
    @org.springframework.boot.autoconfigure.EnableAutoConfiguration
    @org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
    static class WireMockConfig {

        @Bean
        public RouteLocator testRouteLocator(
                RouteLocatorBuilder builder,
                @Value("${mediscreen.services.patient.uri}") String patientServiceUri,
                @Value("${mediscreen.services.notes.uri}") String notesServiceUri,
                @Value("${mediscreen.services.assessment.uri}") String assessmentServiceUri,
                @Value("${mediscreen.auth.patient.username}") String patientUsername,
                @Value("${mediscreen.auth.patient.password}") String patientPassword,
                @Value("${mediscreen.auth.notes.username}") String notesUsername,
                @Value("${mediscreen.auth.notes.password}") String notesPassword,
                @Value("${mediscreen.auth.assessment.username}") String assessmentUsername,
                @Value("${mediscreen.auth.assessment.password}") String assessmentPassword) {

            return builder.routes()
                    .route("patient-service-route", r -> r
                            .path("/api/v1/patients/**")
                            .filters(f -> f.filter(createAuthHeaderFilter(patientUsername, patientPassword)))
                            .uri(patientServiceUri)
                    )
                    .route("notes-service-route", r -> r
                            .path("/api/v1/notes/**")
                            .filters(f -> f.filter(createAuthHeaderFilter(notesUsername, notesPassword)))
                            .uri(notesServiceUri)
                    )
                    .route("assessment-service-route", r -> r
                            .path("/api/v1/assess/**")
                            .filters(f -> f.filter(createAuthHeaderFilter(assessmentUsername, assessmentPassword)))
                            .uri(assessmentServiceUri)
                    )
                    .build();
        }

        private GatewayFilter createAuthHeaderFilter(String username, String password) {
            return (exchange, chain) -> {
                String credentials = username + ":" + password;
                String authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

                return chain.filter(exchange.mutate()
                        .request(exchange.getRequest().mutate()
                                .header(HttpHeaders.AUTHORIZATION, authHeader)
                                .build())
                        .build());
            };
        }

        @Bean
        public SecurityWebFilterChain testSecurityWebFilterChain(ServerHttpSecurity http) {
            return http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                    .build();
        }
    }

    /**
     * Démarrage des serveurs WireMock AVANT Spring Boot
     * Configuration des propriétés dynamiques (URIs WireMock + credentials)
     */
    @DynamicPropertySource
    static void configureWireMockAndCredentials(DynamicPropertyRegistry registry) {
        // Démarrage des serveurs WireMock sur ports dynamiques
        patientServiceMock = new WireMockServer(wireMockConfig().dynamicPort());
        notesServiceMock = new WireMockServer(wireMockConfig().dynamicPort());
        assessmentServiceMock = new WireMockServer(wireMockConfig().dynamicPort());

        patientServiceMock.start();
        notesServiceMock.start();
        assessmentServiceMock.start();

        // Configuration URIs WireMock pour injection dans routes de test (variabilisées comme en production)
        registry.add("mediscreen.services.patient.uri", () -> "http://localhost:" + patientServiceMock.port());
        registry.add("mediscreen.services.notes.uri", () -> "http://localhost:" + notesServiceMock.port());
        registry.add("mediscreen.services.assessment.uri", () -> "http://localhost:" + assessmentServiceMock.port());

        // Configuration credentials backend pour injection dans filtres
        registry.add("mediscreen.auth.patient.username", () -> PATIENT_USERNAME);
        registry.add("mediscreen.auth.patient.password", () -> PATIENT_PASSWORD);
        registry.add("mediscreen.auth.notes.username", () -> NOTES_USERNAME);
        registry.add("mediscreen.auth.notes.password", () -> NOTES_PASSWORD);
        registry.add("mediscreen.auth.assessment.username", () -> ASSESSMENT_USERNAME);
        registry.add("mediscreen.auth.assessment.password", () -> ASSESSMENT_PASSWORD);
    }

    @BeforeEach
    void setUp() {
        // Reset WireMock avant chaque test
        WireMock.configureFor("localhost", patientServiceMock.port());
        patientServiceMock.resetAll();

        WireMock.configureFor("localhost", notesServiceMock.port());
        notesServiceMock.resetAll();

        WireMock.configureFor("localhost", assessmentServiceMock.port());
        assessmentServiceMock.resetAll();
    }

    @AfterAll
    static void tearDownAll() {
        // Arrêt des serveurs WireMock après tous les tests
        if (patientServiceMock != null && patientServiceMock.isRunning()) {
            patientServiceMock.stop();
        }
        if (notesServiceMock != null && notesServiceMock.isRunning()) {
            notesServiceMock.stop();
        }
        if (assessmentServiceMock != null && assessmentServiceMock.isRunning()) {
            assessmentServiceMock.stop();
        }
    }

    @Test
    @DisplayName("Gateway doit injecter credentials Patient Service lors routage /api/v1/patients/**")
    void shouldInjectPatientAuthHeaderWhenRoutingToPatientService() {
        // Given - WireMock simule Patient Service
        WireMock.configureFor("localhost", patientServiceMock.port());
        patientServiceMock.stubFor(get(urlPathEqualTo("/api/v1/patients/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"prenom\":\"John\",\"nom\":\"Doe\"}")));

        // When - Frontend envoie requête à Gateway
        webTestClient.get()
                .uri("/api/v1/patients/1")
                .exchange()
                .expectStatus().isOk();

        // Then - Vérification que WireMock a reçu les credentials PATIENT (pas frontend)
        WireMock.configureFor("localhost", patientServiceMock.port());
        patientServiceMock.verify(getRequestedFor(urlPathEqualTo("/api/v1/patients/1"))
                .withHeader("Authorization", equalTo(PATIENT_AUTH_HEADER)));
    }

    @Test
    @DisplayName("Gateway doit injecter credentials Notes Service lors routage /api/v1/notes/**")
    void shouldInjectNotesAuthHeaderWhenRoutingToNotesService() {
        // Given - WireMock simule Notes Service
        WireMock.configureFor("localhost", notesServiceMock.port());
        notesServiceMock.stubFor(get(urlPathEqualTo("/api/v1/notes/patient/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        // When - Frontend envoie requête à Gateway
        webTestClient.get()
                .uri("/api/v1/notes/patient/1")
                .exchange()
                .expectStatus().isOk();

        // Then - Vérification que WireMock a reçu les credentials NOTES (pas frontend)
        WireMock.configureFor("localhost", notesServiceMock.port());
        notesServiceMock.verify(getRequestedFor(urlPathEqualTo("/api/v1/notes/patient/1"))
                .withHeader("Authorization", equalTo(NOTES_AUTH_HEADER)));
    }

    @Test
    @DisplayName("Gateway doit injecter credentials Assessment Service lors routage /api/v1/assess/**")
    void shouldInjectAssessmentAuthHeaderWhenRoutingToAssessmentService() {
        // Given - WireMock simule Assessment Service
        WireMock.configureFor("localhost", assessmentServiceMock.port());
        assessmentServiceMock.stubFor(get(urlPathEqualTo("/api/v1/assess/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"patientId\":1,\"riskLevel\":\"NONE\"}")));

        // When - Frontend envoie requête à Gateway
        webTestClient.get()
                .uri("/api/v1/assess/1")
                .exchange()
                .expectStatus().isOk();

        // Then - Vérification que WireMock a reçu les credentials ASSESSMENT (pas frontend)
        WireMock.configureFor("localhost", assessmentServiceMock.port());
        assessmentServiceMock.verify(getRequestedFor(urlPathEqualTo("/api/v1/assess/1"))
                .withHeader("Authorization", equalTo(ASSESSMENT_AUTH_HEADER)));
    }

    @Test
    @DisplayName("Gateway doit retourner 404 pour routes non matchées")
    void shouldReturn404ForUnmatchedRoutes() {
        // Given - Chemins non couverts par les routes définies
        String[] unmatchedPaths = {
                "/api/v1/other",        // Aucune route pour ce path
                "/api/v2/patients",     // Mauvaise version API
                "/patients/api/v1",     // Mauvais ordre du path
                "/notes/api/v1"         // Mauvais ordre du path
        };

        // When/Then - Toutes ces requêtes doivent retourner 404
        for (String path : unmatchedPaths) {
            webTestClient.get()
                    .uri(path)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }
}
