package com.lumeos.backend.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Expõe o {@link PasswordEncoder} (BCrypt) usado pelo {@code UserService} para hashear senhas
 * (REQ-001/RNF-03). Fica em {@code common.security} por ser transversal: a cadeia de segurança
 * completa da task 1.2 reutiliza este mesmo bean, sem redefini-lo.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
