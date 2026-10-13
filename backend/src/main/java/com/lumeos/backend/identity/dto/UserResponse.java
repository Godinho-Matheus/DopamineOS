package com.lumeos.backend.identity.dto;

/**
 * Representação pública de um {@code User} (REQ-001 critério 3; REQ-002). NUNCA expõe
 * {@code passwordHash} — contém apenas identidade (id, nome, e-mail).
 */
public record UserResponse(
        Long id,
        String nome,
        String email
) {}
