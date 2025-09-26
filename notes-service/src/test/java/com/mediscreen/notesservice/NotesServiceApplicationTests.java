package com.mediscreen.notesservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test de chargement du contexte Spring Boot pour Notes Service.
 *
 * Ce test vérifie que l'application peut démarrer correctement avec
 * la configuration de test. Les variables AUTH_USERNAME/AUTH_PASSWORD
 * sont des credentials factices nécessaires pour éviter les erreurs de
 * placeholder Spring Boot au démarrage, même si la sécurité est désactivée
 * dans application-test.properties.
 *
 * Note technique : Spring Boot résout toutes les variables ${} au démarrage,
 * donc il faut fournir des valeurs même si elles ne sont pas utilisées.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "AUTH_USERNAME=test-user",    // Credentials factices pour éviter erreurs placeholder
    "AUTH_PASSWORD=test-pass"     // Ces valeurs ne sont jamais vérifiées
})
class NotesServiceApplicationTests {

	/**
	 * Vérifie que le contexte Spring Boot se charge correctement.
	 * Ce test garantit que toutes les configurations et beans
	 * sont correctement initialisés.
	 */
	@Test
	void contextLoads() {
		// Test réussi si le contexte Spring se charge sans exception
	}

}
