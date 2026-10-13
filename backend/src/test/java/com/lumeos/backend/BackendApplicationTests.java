package com.lumeos.backend;

import com.lumeos.backend.support.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Teste de integração do contexto da aplicação.
 *
 * <p>Estende {@link AbstractPostgresIT} para subir um PostgreSQL via Testcontainers, de modo
 * que a inicialização do contexto NÃO dependa de um Postgres externo nem de {@code DB_PASSWORD}
 * (Design §33). O schema é criado pelo Flyway e o Hibernate valida o mapeamento
 * ({@code ddl-auto=validate}, incluindo a location de compatibilidade legada — task 0.3).
 */
@SpringBootTest
class BackendApplicationTests extends AbstractPostgresIT {

	@Test
	void contextLoads() {
	}

}
