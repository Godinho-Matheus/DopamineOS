package com.lumeos.backend.common.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.oauth2.jwt.JwtException;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testes unitários do JwtService (microentrega B2). */
class JwtServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-bytes-ok";
    private static final String OUTRO_SEGREDO = "outro-segredo-de-teste-com-32-bytes-ou-mais";
    private static final long TTL = 3600;
    private static final Instant AGORA = Instant.parse("2026-01-01T12:00:00Z");

    private static Clock relogio(Instant instante) {
        return Clock.fixed(instante, ZoneOffset.UTC);
    }

    private static JwtService servico(String segredo, Instant instante) {
        return new JwtService(new JwtProperties(segredo, TTL), true, relogio(instante));
    }

    private final JwtService jwtService = servico(SEGREDO, AGORA);

    @Test
    void tokenEmitidoEhValidoEDevolveOMesmoUserId() {
        String token = jwtService.emitir(42L);

        assertThat(jwtService.validar(token)).isEqualTo(42L);
    }

    @Test
    void tokenUsaHs256EContemApenasSubIatExp() throws Exception {
        SignedJWT jwt = SignedJWT.parse(jwtService.emitir(42L));
        JWTClaimsSet claims = jwt.getJWTClaimsSet();

        assertThat(jwt.getHeader().getAlgorithm()).isEqualTo(JWSAlgorithm.HS256);
        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.getIssueTime().toInstant()).isEqualTo(AGORA);
        assertThat(claims.getExpirationTime().toInstant()).isEqualTo(AGORA.plusSeconds(TTL));
        assertThat(claims.getClaims().keySet()).containsExactlyInAnyOrder("sub", "iat", "exp");
        assertThat(claims.getClaim("householdId")).isNull();
    }

    @Test
    void tokenAlteradoEhRejeitado() {
        String[] partes = jwtService.emitir(42L).split("\\.");
        Base64.Decoder dec = Base64.getUrlDecoder();
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
        String payload = new String(dec.decode(partes[1]), StandardCharsets.UTF_8).replace("\"42\"", "\"1\"");
        String adulterado = partes[0] + "." + enc.encodeToString(payload.getBytes(StandardCharsets.UTF_8))
                + "." + partes[2];

        assertThatThrownBy(() -> jwtService.validar(adulterado)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenAssinadoComOutroSegredoEhRejeitado() {
        String token = servico(OUTRO_SEGREDO, AGORA).emitir(42L);

        assertThatThrownBy(() -> jwtService.validar(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenExpiradoEhRejeitado() {
        String token = jwtService.emitir(42L);
        JwtService depoisDoTtl = servico(SEGREDO, AGORA.plusSeconds(TTL + 1));

        assertThatThrownBy(() -> depoisDoTtl.validar(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenMalformadoEhRejeitado() {
        assertThatThrownBy(() -> jwtService.validar("nao-e-um-jwt")).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.validar("a.b.c")).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.validar("")).isInstanceOf(JwtException.class);
    }

    @Test
    void producaoSemSegredoNaoInicia() {
        MockEnvironment prod = new MockEnvironment();
        prod.setActiveProfiles("prod");

        assertThatThrownBy(() -> new JwtService(new JwtProperties("", TTL), prod))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET não definido");
    }

    @Test
    void producaoComSegredoCurtoNaoIniciaENaoExpoeOSegredo() {
        MockEnvironment prod = new MockEnvironment();
        prod.setActiveProfiles("prod");
        String curto = "curto-demais-31-bytes-xxxxxxxxx";

        assertThatThrownBy(() -> new JwtService(new JwtProperties(curto, TTL), prod))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET é fraco")
                .hasMessageNotContaining(curto);
    }

    @Test
    void devSemSegredoUsaSegredoTemporario() {
        MockEnvironment dev = new MockEnvironment();
        dev.setActiveProfiles("dev");

        JwtService temporario = new JwtService(new JwtProperties(null, TTL), dev);

        assertThat(temporario.validar(temporario.emitir(7L))).isEqualTo(7L);
    }
}
