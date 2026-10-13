package com.lumeos.backend.identity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório de {@link User}. A busca é feita pelo e-mail JÁ normalizado — o
 * {@link UserService} normaliza (trim + lower-case) antes de consultar/persistir, e o e-mail
 * é SEMPRE armazenado normalizado, de modo que {@code findByEmail} resolve a busca
 * case-insensitive sem precisar de função SQL no mapeamento (REQ-002).
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
