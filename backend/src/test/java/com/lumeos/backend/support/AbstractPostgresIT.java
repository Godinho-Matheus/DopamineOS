package com.lumeos.backend.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base reutilizável para testes de integração (Design §33).
 *
 * <p>Sobe um PostgreSQL real via Testcontainers e registra as propriedades do datasource
 * via {@link DynamicPropertySource}, de modo que os testes de integração NÃO dependam de
 * um Postgres externo nem da variável de ambiente {@code DB_PASSWORD}.
 *
 * <p>Regime de teste (task 0.3): o schema é criado pelo <strong>Flyway</strong> e o Hibernate
 * apenas valida o mapeamento ({@code ddl-auto=validate}). As duas locations do Flyway são
 * configuradas explicitamente — a baseline ({@code classpath:db/migration}) MAIS a migração de
 * compatibilidade legada ({@code classpath:db/migration-devtest}) — para que {@code validate}
 * aceite as entidades legadas ({@code Usuario}/{@code Protocolo}/{@code LogAtividade}) ainda
 * mapeadas (Design §10). Isso torna o teste independente do perfil ativo.
 *
 * <p><strong>Container singleton:</strong> o PostgreSQL é iniciado UMA ÚNICA vez e compartilhado
 * por TODAS as classes de teste de integração (padrão singleton container do Testcontainers).
 * O ciclo {@code @Container}/{@code @Testcontainers} prende o container à classe e o encerra ao
 * fim dela; como o Spring faz cache do contexto (datasource) entre classes de teste com a mesma
 * configuração, reiniciar o container por classe deixaria o datasource cacheado apontando para um
 * container já parado (conexão recusada). Com o singleton, a URL permanece válida entre as classes;
 * o container é derrubado pelo Ryuk no encerramento da JVM.
 */
public abstract class AbstractPostgresIT {

    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        // Flyway cria o schema (baseline + compat legada dev/test); Hibernate apenas valida.
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations",
                () -> "classpath:db/migration,classpath:db/migration-devtest");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }
}
