package com.lumeos.backend.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração do JWT (microentrega B2 / task 1.2.2), ligada às chaves já existentes no
 * {@code application.yml}:
 * <ul>
 *   <li>{@code jwt.secret} ← {@code JWT_SECRET}: segredo HS256 em texto UTF-8 (mínimo 32 bytes);</li>
 *   <li>{@code jwt.ttl} ← {@code JWT_TTL}: tempo de vida do token em segundos.</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, long ttl) {
}
