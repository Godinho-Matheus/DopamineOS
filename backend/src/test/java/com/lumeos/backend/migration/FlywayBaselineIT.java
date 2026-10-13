package com.lumeos.backend.migration;

import com.lumeos.backend.support.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração do schema versionado por Flyway (tasks 0.3.9 e 0.3.10).
 *
 * <p>Sobe um PostgreSQL vazio via Testcontainers; o Flyway aplica a baseline
 * ({@code V1__baseline.sql}) MAIS a migração de compatibilidade legada dev/test
 * ({@code V2__devtest_legacy_compat.sql}); o Hibernate roda com {@code ddl-auto=validate}
 * (configurado em {@link AbstractPostgresIT}). O simples carregamento do contexto já prova
 * que {@code validate} aceita tanto as entidades do schema alvo quanto as entidades legadas
 * ({@code Usuario}/{@code Protocolo}/{@code LogAtividade}) ainda mapeadas (Design §9, §10).
 */
@SpringBootTest
class FlywayBaselineIT extends AbstractPostgresIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoadsComFlywayEValidate() {
        // Se o contexto subiu, Flyway aplicou as migrações e ddl-auto=validate passou.
        assertThat(jdbcTemplate).isNotNull();
    }

    @Test
    void baselineV1CriaTodasAsTabelasAlvo() {
        String[] tabelasAlvo = {
                "users", "game_profiles", "households", "household_members",
                "household_invites", "habits", "activity_logs", "shopping_lists",
                "shopping_items", "reading_entries"
        };
        for (String tabela : tabelasAlvo) {
            assertThat(tabelaExiste(tabela))
                    .as("tabela alvo '%s' deve existir (V1__baseline.sql)", tabela)
                    .isTrue();
        }
    }

    @Test
    void compatibilidadeLegadaCriaTabelasTb() {
        String[] tabelasLegadas = {"tb_usuario", "tb_protocolos", "tb_log_atividade"};
        for (String tabela : tabelasLegadas) {
            assertThat(tabelaExiste(tabela))
                    .as("tabela legada de compat '%s' deve existir (V2 dev/test)", tabela)
                    .isTrue();
        }
    }

    private boolean tabelaExiste(String nome) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_name = ?",
                Integer.class, nome);
        return count != null && count > 0;
    }
}
