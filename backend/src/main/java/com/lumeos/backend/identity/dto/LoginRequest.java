package com.lumeos.backend.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credenciais de login (REQ-001 critério 2). O fluxo de autenticação em si chega na task 1.2;
 * aqui o DTO já existe com validação de formato de e-mail e senha obrigatória.
 */
public record LoginRequest(

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail deve estar em formato válido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha
) {}
