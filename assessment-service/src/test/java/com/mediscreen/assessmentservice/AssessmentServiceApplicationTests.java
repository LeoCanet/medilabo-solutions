package com.mediscreen.assessmentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test de contexte Spring pour Assessment Service
 * Vérifie que l'application Spring Boot se charge correctement
 */
@SpringBootTest
@TestPropertySource(properties = {
        "mediscreen.auth.username=test-assessment",
        "mediscreen.auth.password=test-pass",
        "AUTH_USERNAME=test-user",
        "AUTH_PASSWORD=test-pass"
})
class AssessmentServiceApplicationTests {

    @Test
    void contextLoads() {
        // Ce test vérifie simplement que le contexte Spring se charge sans erreur
    }

}
