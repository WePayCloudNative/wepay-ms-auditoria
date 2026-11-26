package cl.duoc.wepay_ms_auditoria;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prueba de integración: levanta la aplicación completa.
 * Usa el perfil "local" (H2 en memoria) para no depender de un Oracle
 * ni en tu PC ni en GitHub Actions.
 */
@SpringBootTest
@ActiveProfiles("local")
class WepayMsAuditoriaApplicationTests {

	@Test
	void contextLoads() {
	}

}
