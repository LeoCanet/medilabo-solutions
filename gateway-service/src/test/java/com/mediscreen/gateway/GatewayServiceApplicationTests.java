package com.mediscreen.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test de chargement du contexte Spring Boot pour Gateway Service.
 *
 * Ce test vérifie que l'application Gateway peut démarrer correctement avec
 * la configuration de sécurité et de routage. Les variables d'authentification
 * sont des credentials factices nécessaires pour éviter les erreurs de
 * placeholder Spring Boot au démarrage.
 *
 * Note technique : Gateway n'utilise pas de TestContainers car il n'a pas
 * de base de données dédiée, contrairement aux services Patient et Notes.
 */
@SpringBootTest
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
class GatewayServiceApplicationTests {

	/**
	 * Vérifie que le contexte Spring Boot Gateway se charge correctement.
	 * Ce test garantit que toutes les configurations (sécurité, routage, filtres)
	 * sont correctement initialisées.
	 */
	@Test
	void contextLoads() {
		// Test réussi si le contexte Spring se charge sans exception
	}

}
