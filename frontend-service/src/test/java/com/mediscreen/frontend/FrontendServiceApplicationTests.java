package com.mediscreen.frontend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test de chargement du contexte Spring Boot pour Frontend Service.
 *
 * Note technique : Frontend n'utilise pas de TestContainers car il n'a pas
 * de base de données dédiée. Il communique uniquement avec Gateway via Feign.
 */
@SpringBootTest
@TestPropertySource(properties = {
	"AUTH_USERNAME=test-frontend",
	"AUTH_PASSWORD=test-pass"
})
class FrontendServiceApplicationTests {

	@Test
	void contextLoads() {
		// Test réussi si le contexte Spring se charge sans exception
	}

}
