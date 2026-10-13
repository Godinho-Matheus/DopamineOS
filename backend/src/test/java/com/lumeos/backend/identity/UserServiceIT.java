package com.lumeos.backend.identity;

import com.lumeos.backend.common.exception.EmailAlreadyExistsException;
import com.lumeos.backend.identity.dto.UserResponse;
import com.lumeos.backend.identity.mapper.UserMapper;
import com.lumeos.backend.support.AbstractPostgresIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de integração do domínio de identidade com PostgreSQL real (Testcontainers), tarefa
 * 1.1.7. O que depende da constraint real {@code UNIQUE(lower(email))} e do mapeamento JPA
 * contra a tabela {@code users} NÃO é mockado.
 */
@SpringBootTest
class UserServiceIT extends AbstractPostgresIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @BeforeEach
    void limparUsuarios() {
        // O container Postgres é compartilhado entre os métodos da classe e não há rollback
        // automático; limpar a tabela mantém cada teste determinístico e independente.
        userRepository.deleteAll();
    }

    @Test
    void criaUsuarioPersistidoComId() {
        User user = userService.register("Ana", "ana@email.com", "senha1234");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(userRepository.findById(user.getId())).isPresent();
    }

    @Test
    void normalizaEmailComEspacos() {
        User user = userService.register("Bruno", "  bruno@email.com  ", "senha1234");

        assertThat(user.getEmail()).isEqualTo("bruno@email.com");
        assertThat(userRepository.findByEmail("bruno@email.com")).isPresent();
    }

    @Test
    void normalizaEmailComMaiusculas() {
        User user = userService.register("Carla", "Carla@Email.COM", "senha1234");

        assertThat(user.getEmail()).isEqualTo("carla@email.com");
    }

    @Test
    void rejeitaEmailDuplicadoCaseInsensitive() {
        userService.register("Diego", "User@Email.com", "senha1234");

        assertThatThrownBy(() -> userService.register("Outro", "user@email.com", "outrasenha"))
                .isInstanceOf(EmailAlreadyExistsException.class);

        // Apenas uma conta foi criada: User@Email.com e user@email.com são o mesmo e-mail.
        assertThat(userRepository.findByEmail("user@email.com")).isPresent();
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void senhaPersistidaComoBcrypt() {
        User user = userService.register("Elena", "elena@email.com", "senhaSecreta");

        assertThat(user.getPasswordHash()).isNotEqualTo("senhaSecreta");
        assertThat(user.getPasswordHash()).startsWith("$2");
        assertThat(passwordEncoder.matches("senhaSecreta", user.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("senhaErrada", user.getPasswordHash())).isFalse();
    }

    @Test
    void userResponseNuncaContemPasswordHash() {
        User user = userService.register("Fabio", "fabio@email.com", "senha1234");

        UserResponse response = userMapper.toResponse(user);

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.nome()).isEqualTo("Fabio");
        assertThat(response.email()).isEqualTo("fabio@email.com");
        // UserResponse não possui campo de passwordHash: nenhum de seus componentes o expõe.
        assertThat(response.toString()).doesNotContain(user.getPasswordHash());
    }
}
