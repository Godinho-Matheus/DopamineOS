package com.lumeos.backend.common.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cadeia de segurança mínima e <strong>stateless</strong> (microentrega B1 / task 1.2.1,
 * parte da 1.2.5). O objetivo desta fatia é apenas distinguir requisições públicas de
 * protegidas: sem sessão HTTP, sem JWT, sem cookie, sem CSRF customizado, sem endpoints de
 * autenticação. Essas peças chegam nas microentregas seguintes (B2–B6).
 *
 * <p>Reutiliza o {@code PasswordEncoder} (BCrypt) já definido em
 * {@link PasswordEncoderConfig} (criado na 1.1); este arquivo não redefine esse bean.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Sem sessão HTTP: cada requisição é autenticada por si só (base para o JWT por
                // cookie que chega na B2/B3). O SecurityContext não é persistido entre requisições.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Health check é público (já exposto pelo Actuator com show-details=never).
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        // Todo o restante exige autenticação. Como ainda não há mecanismo de login
                        // nesta B1, qualquer requisição a um recurso protegido resulta em 401.
                        .anyRequest().authenticated())
                // Mecanismos padrão que o starter liga sozinho e que NÃO queremos: nenhum
                // formulário HTML de login e nenhum HTTP Basic. A autenticação real (cookie
                // HttpOnly + JWT) chega na B2/B3.
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // CSRF permanece HABILITADO (comportamento padrão do Spring Security), conforme
                // o design (REQ-001: nunca csrf.disable()). Nesta B1 não há configuração
                // customizada; o CookieCsrfTokenRepository (XSRF-TOKEN / X-XSRF-TOKEN) e o
                // endpoint de bootstrap chegam na B4.
                // Garante 401 (não 403, não redirect para formulário) para requisições não
                // autenticadas a recursos protegidos. O corpo de erro padronizado (StandardError)
                // e o AccessDeniedHandler 403 chegam na B4; aqui mantemos apenas o status 401.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint()));

        return http.build();
    }

    /**
     * Entry point mínimo: responde {@code 401 Unauthorized} sem corpo elaborado. A resposta
     * JSON padronizada ({@code StandardError}) é da B4; nesta fase só importa o status correto.
     */
    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
