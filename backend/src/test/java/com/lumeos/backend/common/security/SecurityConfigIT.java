package com.lumeos.backend.common.security;

import com.lumeos.backend.support.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração da cadeia de segurança mínima stateless (microentrega B1 / task 1.2.1).
 *
 * <p>Validam, com o contexto completo (Testcontainers via {@link AbstractPostgresIT} +
 * {@code MockMvc} exercitando toda a cadeia de filtros do Spring Security):
 * <ul>
 *   <li>{@code GET /actuator/health} sem autenticação → 200 (público);</li>
 *   <li>{@code GET /api/v1/game/perfil} (endpoint protegido existente) sem autenticação → 401;</li>
 *   <li>não há redirect para formulário de login (não é 3xx para /login) nem 403;</li>
 *   <li>a aplicação permanece stateless (nenhuma sessão HTTP é criada na resposta).</li>
 * </ul>
 *
 * <p>Usa um endpoint protegido JÁ existente ({@code GameController} legado) de propósito, para
 * validar a cadeia sem adicionar superfície nova nesta microentrega.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIT extends AbstractPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEhPublicoRetorna200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void endpointProtegidoSemAutenticacaoRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/game/perfil"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointProtegidoNaoRedirecionaParaFormularioEnaoE403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/game/protocolos"))
                .andReturn();

        int statusCode = result.getResponse().getStatus();
        // Deve ser exatamente 401: não 3xx (redirect para /login) e não 403.
        assertThat(statusCode).isEqualTo(401);
        assertThat(result.getResponse().getRedirectedUrl()).isNull();
    }

    @Test
    void cadeiaPermaneceStatelessSemCriarSessao() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/game/perfil"))
                .andReturn();

        // Em modo STATELESS o container não cria sessão HTTP para servir a requisição.
        assertThat(result.getRequest().getSession(false)).isNull();
    }

    /**
     * Garante que o CSRF NÃO foi desabilitado (REQ-001). Uma mutação existente sem token CSRF
     * é barrada pelo CsrfFilter ANTES da autorização, resultando em 403. Se a cadeia estivesse
     * com {@code csrf.disable()}, a mesma requisição chegaria à autorização e retornaria 401.
     * Não valida a configuração definitiva (CookieCsrfTokenRepository), que é da B4.
     */
    @Test
    void csrfPermaneceHabilitadoParaMutacoes() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/game/setup")
                        .contentType("application/json")
                        .content("{}"))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("mutação sem token CSRF deve ser rejeitada pelo CsrfFilter (403), não 401")
                .isEqualTo(403);
    }
}
