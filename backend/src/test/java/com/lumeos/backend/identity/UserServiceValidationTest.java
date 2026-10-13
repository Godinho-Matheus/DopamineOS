package com.lumeos.backend.identity;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários da validação de domínio do {@link UserService} (tarefa 1.1.7). A validação
 * de nome/senha ocorre ANTES de qualquer acesso ao repositório, então estes casos não tocam o
 * banco — o {@link UserRepository} nem chega a ser chamado. Normalização de e-mail também é
 * verificada aqui como lógica pura.
 */
class UserServiceValidationTest {

    private final UserService userService =
            new UserService(repositorioQueNaoDeveSerChamado(), new BCryptPasswordEncoder());

    @Test
    void normalizaEmailTrimELowercase() {
        assertThat(UserService.normalizeEmail("  User@Email.COM  ")).isEqualTo("user@email.com");
    }

    @Test
    void nomeVazioLancaIllegalArgument() {
        assertThatThrownBy(() -> userService.register("   ", "a@email.com", "senha1234"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nomeAcimaDe100LancaIllegalArgument() {
        assertThatThrownBy(() -> userService.register("a".repeat(101), "a@email.com", "senha1234"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void senhaCurtaLancaIllegalArgument() {
        assertThatThrownBy(() -> userService.register("Ana", "a@email.com", "1234567"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void senhaLongaLancaIllegalArgument() {
        assertThatThrownBy(() -> userService.register("Ana", "a@email.com", "a".repeat(73)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Repositório proxy que falha se qualquer método for invocado — prova que a validação de
     * nome/senha acontece antes de tocar a camada de persistência.
     */
    private static UserRepository repositorioQueNaoDeveSerChamado() {
        return (UserRepository) java.lang.reflect.Proxy.newProxyInstance(
                UserServiceValidationTest.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("toString".equals(method.getName())) {
                        return "no-op UserRepository";
                    }
                    throw new AssertionError(
                            "UserRepository não deveria ser chamado para entrada inválida: "
                                    + method.getName());
                });
    }
}
