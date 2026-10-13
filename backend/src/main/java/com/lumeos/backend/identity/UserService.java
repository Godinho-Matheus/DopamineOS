package com.lumeos.backend.identity;

import com.lumeos.backend.common.exception.EmailAlreadyExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * Serviço de identidade: cria {@link User} normalizando e-mail, validando nome/senha e
 * armazenando a senha apenas como hash BCrypt (REQ-001/REQ-002/RNF-03).
 *
 * <p>Regras aplicadas:
 * <ul>
 *   <li>e-mail normalizado com {@code trim()} + {@code toLowerCase(Locale.ROOT)} e SEMPRE
 *       persistido já normalizado;</li>
 *   <li>nome 1..100 e senha 8..72 (limite do BCrypt);</li>
 *   <li>duplicidade detectada pelo e-mail normalizado; a constraint {@code UNIQUE(lower(email))}
 *       do banco é a proteção definitiva (corrida tratada abaixo).</li>
 * </ul>
 *
 * <p>A senha em texto puro nunca é logada nem retornada; o {@code passwordHash} nunca sai em
 * um response.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra um novo usuário. O e-mail é normalizado antes de qualquer verificação ou
     * persistência; a senha é armazenada como hash BCrypt.
     *
     * @throws IllegalArgumentException       se nome (1..100) ou senha (8..72) forem inválidos
     * @throws EmailAlreadyExistsException    se já existir usuário com o e-mail normalizado
     */
    @Transactional
    public User register(String nome, String email, String senha) {
        String nomeValidado = validarNome(nome);
        validarSenha(senha);
        String emailNormalizado = normalizeEmail(email);

        if (userRepository.existsByEmail(emailNormalizado)) {
            throw new EmailAlreadyExistsException("E-mail já cadastrado");
        }

        User user = new User();
        user.setNome(nomeValidado);
        user.setEmail(emailNormalizado);
        user.setPasswordHash(passwordEncoder.encode(senha));

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // Corrida: outro registro com o mesmo e-mail passou pela checagem acima e a
            // constraint UNIQUE(lower(email)) do banco rejeitou. Protege a invariante de
            // unicidade de forma definitiva.
            throw new EmailAlreadyExistsException("E-mail já cadastrado");
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email));
    }

    /** Normaliza o e-mail: {@code trim()} + {@code toLowerCase(Locale.ROOT)}. */
    public static String normalizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("O e-mail é obrigatório");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String validarNome(String nome) {
        String nomeTrim = nome == null ? null : nome.trim();
        if (nomeTrim == null || nomeTrim.isEmpty() || nomeTrim.length() > 100) {
            throw new IllegalArgumentException("O nome deve ter entre 1 e 100 caracteres");
        }
        return nomeTrim;
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < 8 || senha.length() > 72) {
            throw new IllegalArgumentException("A senha deve ter entre 8 e 72 caracteres");
        }
    }
}
