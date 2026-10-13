package com.lumeos.backend.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Identidade da pessoa (REQ-002): mapeia EXATAMENTE a tabela {@code users} criada pela
 * baseline Flyway ({@code V1__baseline.sql}). Não contém nenhum campo de personagem/RPG —
 * a progressão vive em {@code GameProfile} (Design §8). O e-mail é sempre persistido já
 * normalizado (trim + lower-case) pelo {@link UserService}; a unicidade case-insensitive é
 * garantida pelo índice funcional {@code ux_users_lower_email}.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 72)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
