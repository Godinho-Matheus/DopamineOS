package com.lumeos.backend.common.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Emissão e validação isoladas de JWT HS256 (microentrega B2 / task 1.2.2; Design §12.2).
 *
 * <p>O token carrega apenas {@code sub} (userId), {@code iat} e {@code exp = iat + ttl}. Nada de
 * household, papel ou dados de negócio. Encoder e decoder usam HS256 explicitamente.
 *
 * <p>Segredo ({@code JWT_SECRET}, texto UTF-8, mínimo 32 bytes):
 * <ul>
 *   <li>produção: ausente, vazio ou curto impede a inicialização;</li>
 *   <li>dev/test: se ausente, usa um segredo aleatório em memória só durante a execução;</li>
 *   <li>um segredo informado mas curto é recusado em qualquer ambiente.</li>
 * </ul>
 * O valor do segredo nunca é registrado em log.
 *
 * <p>Nesta B2 o serviço não está ligado à cadeia de segurança (isso é da B3).
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtService {

    static final int MIN_SECRET_BYTES = 32;

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final NimbusJwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    private final Duration ttl;
    private final Clock clock;

    @Autowired
    public JwtService(JwtProperties properties, Environment environment) {
        this(properties, environment.acceptsProfiles(Profiles.of("prod")), Clock.systemUTC());
    }

    JwtService(JwtProperties properties, boolean producao, Clock clock) {
        if (properties.ttl() <= 0) {
            throw new IllegalStateException("JWT_TTL deve ser um número positivo de segundos.");
        }
        SecretKey key = new SecretKeySpec(resolverSegredo(properties.secret(), producao), "HmacSHA256");

        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        JwtTimestampValidator expiracao = new JwtTimestampValidator(Duration.ZERO);
        expiracao.setClock(clock);
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        this.decoder.setJwtValidator(expiracao);

        this.ttl = Duration.ofSeconds(properties.ttl());
        this.clock = clock;
    }

    /** Emite um JWT HS256 cujo {@code sub} é o userId. */
    public String emitir(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId é obrigatório para emitir o token");
        }
        Instant agora = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .issuedAt(agora)
                .expiresAt(agora.plus(ttl))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /**
     * Valida assinatura (HS256), formato e expiração e devolve o userId do {@code sub}.
     *
     * @throws JwtException se o token for inválido por qualquer motivo
     */
    public Long validar(String token) {
        if (token == null || token.isBlank()) {
            throw new BadJwtException("Token ausente");
        }
        Jwt jwt = decoder.decode(token);
        String sub = jwt.getSubject();
        try {
            return Long.valueOf(sub);
        } catch (NumberFormatException | NullPointerException e) {
            throw new BadJwtException("Token sem sub válido", e);
        }
    }

    private static byte[] resolverSegredo(String segredo, boolean producao) {
        if (segredo == null || segredo.isBlank()) {
            if (producao) {
                throw new IllegalStateException("JWT_SECRET não definido. Em produção é obrigatório "
                        + "informar um segredo com no mínimo " + MIN_SECRET_BYTES + " bytes (256 bits).");
            }
            log.warn("JWT_SECRET não definido: usando um segredo temporário gerado em memória só "
                    + "para esta execução. Os tokens emitidos deixam de valer quando a aplicação "
                    + "reiniciar. Defina JWT_SECRET para evitar isso.");
            byte[] temporario = new byte[MIN_SECRET_BYTES];
            new SecureRandom().nextBytes(temporario);
            return temporario;
        }
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET é fraco: tem " + bytes.length + " bytes, "
                    + "mas o mínimo para HS256 é " + MIN_SECRET_BYTES + " bytes (256 bits).");
        }
        return bytes;
    }
}
