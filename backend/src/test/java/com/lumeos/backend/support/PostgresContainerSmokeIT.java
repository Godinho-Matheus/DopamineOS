package com.lumeos.backend.support;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test (task 0.6.3) do pipeline de testes de integração com Testcontainers.
 *
 * <p>Valida que o container PostgreSQL sobe e que uma query trivial funciona, sem depender de
 * um Postgres externo. Não carrega o contexto Spring, mantendo o teste leve e focado no pipeline.
 */
class PostgresContainerSmokeIT extends AbstractPostgresIT {

    @Test
    void postgresContainerStartsAndRespondsToTrivialQuery() throws Exception {
        assertThat(POSTGRES.isRunning()).isTrue();

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(1);
        }
    }
}
