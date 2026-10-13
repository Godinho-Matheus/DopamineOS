package com.lumeos.backend.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados de registro de um novo {@code User} (REQ-001 critério 1; REQ-001 casos de erro 4/5/6).
 * {@code senha} 8..72 reflete o limite do BCrypt; {@code nome} 1..100; {@code email} válido
 * com até 254 caracteres.
 */
public record RegisterRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 1, max = 100, message = "O nome deve ter entre 1 e 100 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail deve estar em formato válido")
        @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String senha
) {}
