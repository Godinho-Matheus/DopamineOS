package com.lumeos.backend.identity;

import com.lumeos.backend.identity.dto.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de Bean Validation do {@link RegisterRequest} (tarefa 1.1.7): nome vazio ou
 * > 100 rejeitado; senha < 8 e > 72 rejeitada; e-mail inválido rejeitado. Não dependem de banco.
 */
class RegisterRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void requestValidoNaoViola() {
        Set<ConstraintViolation<RegisterRequest>> violacoes =
                validator.validate(new RegisterRequest("Ana", "ana@email.com", "senha1234"));
        assertThat(violacoes).isEmpty();
    }

    @Test
    void nomeVazioEhRejeitado() {
        Set<ConstraintViolation<RegisterRequest>> violacoes =
                validator.validate(new RegisterRequest("", "ana@email.com", "senha1234"));
        assertThat(violacoes).isNotEmpty();
    }

    @Test
    void nomeAcimaDe100EhRejeitado() {
        String nomeLongo = "a".repeat(101);
        Set<ConstraintViolation<RegisterRequest>> violacoes =
                validator.validate(new RegisterRequest(nomeLongo, "ana@email.com", "senha1234"));
        assertThat(violacoes).isNotEmpty();
    }

    @Test
    void senhaCurtaEhRejeitada() {
        Set<ConstraintViolation<RegisterRequest>> violacoes =
                validator.validate(new RegisterRequest("Ana", "ana@email.com", "1234567"));
        assertThat(violacoes).isNotEmpty();
    }

    @Test
    void senhaAcimaDe72EhRejeitada() {
        String senhaLonga = "a".repeat(73);
        Set<ConstraintViolation<RegisterRequest>> violacoes =
                validator.validate(new RegisterRequest("Ana", "ana@email.com", senhaLonga));
        assertThat(violacoes).isNotEmpty();
    }

    @Test
    void emailInvalidoEhRejeitado() {
        Set<ConstraintViolation<RegisterRequest>> violacoes =
                validator.validate(new RegisterRequest("Ana", "nao-eh-email", "senha1234"));
        assertThat(violacoes).isNotEmpty();
    }
}
