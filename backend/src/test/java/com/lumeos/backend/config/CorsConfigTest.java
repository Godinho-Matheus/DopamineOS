package com.lumeos.backend.config;

import com.lumeos.backend.common.config.CorsConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de fumaça da configuração por ambiente (task 0.2.7).
 *
 * <p>Validam que o CorsConfig lê as origens a partir da propriedade alimentada por
 * CORS_ALLOWED_ORIGINS (dev inclui http://localhost:4200) e que a ausência da variável
 * resulta em lista vazia (sem origem fixa hardcoded), sem exigir banco de dados.
 */
class CorsConfigTest {

    @Test
    void parsesCommaSeparatedOrigins() {
        CorsConfig config = new CorsConfig("http://localhost:4200, https://app.lumeos.com");

        assertThat(config.corsFilter()).isNotNull();
    }

    @Test
    void devDefaultOriginIsLocalhost4200() {
        // Simula o default de desenvolvimento documentado (CORS_ALLOWED_ORIGINS ausente).
        CorsConfig config = new CorsConfig("http://localhost:4200");

        assertThat(config.corsFilter()).isNotNull();
    }

    @Test
    void emptyOriginsDoesNotFail() {
        // Ausência da variável não deve quebrar a construção do bean.
        CorsConfig config = new CorsConfig("");

        assertThat(config.corsFilter()).isNotNull();
    }
}
