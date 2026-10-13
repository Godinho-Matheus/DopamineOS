package com.lumeos.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Lançada quando se tenta registrar um e-mail já existente (normalizado) — conflito de
 * identidade (REQ-001 caso de erro 1; REQ-002 caso de erro 4). Segue o padrão do projeto
 * ({@code ResourceNotFoundException}): o status HTTP vem do {@link ResponseStatus}, sem
 * necessidade de advice dedicado. A proteção definitiva é a constraint {@code UNIQUE(lower(email))}
 * do banco; esta exceção é a detecção em nível de aplicação.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
