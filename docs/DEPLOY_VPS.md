# Deploy / Migração — LumeOS

> Documento inicial de migração/deploy. A documentação completa de deploy (containers, Nginx,
> HTTPS, backups automáticos, CI) é escopo de uma task posterior (5.14). Aqui registra-se
> apenas o pré-requisito OBRIGATÓRIO de backup do banco legado antes de adotar o Flyway/schema
> novo (Design §10, §11; tasks 0.3, 0.3.7).

## Pré-requisito OBRIGATÓRIO: backup do banco legado antes de migrar

O LumeOS **não** aplica a migração `V1` sobre o banco de desenvolvimento legado (o que contém
as tabelas `tb_usuario`, `tb_protocolos`, `tb_log_atividade`). O LumeOS parte de um
**banco/schema NOVO e VAZIO** e o Flyway cria o schema a partir de `V1__baseline.sql`
(Design §10.3).

**Antes de apontar a aplicação para o banco novo, faça um backup completo do banco legado**
usando `pg_dump` no formato custom (`-Fc`):

```bash
pg_dump -Fc \
  -h "$LEGACY_DB_HOST" \
  -p "$LEGACY_DB_PORT" \
  -U "$LEGACY_DB_USER" \
  -d "$LEGACY_DB_NAME" \
  -f lumeos-legacy-$(date +%Y%m%d-%H%M%S).dump
```

- Guarde o arquivo `.dump` em local seguro (fora do repositório; **nunca** versionar dump nem
  credenciais).
- O backup satisfaz REQ-012 (nenhuma perda silenciosa de dados) e é a fonte para qualquer
  importação manual/opcional de dados antigos para o schema novo (Design §11.3).

## NÃO remover as tabelas legadas

As tabelas legadas **`tb_usuario`, `tb_protocolos` e `tb_log_atividade` NÃO devem ser removidas**
do banco legado (RNF-15). Elas:

- permanecem **somente** no banco de desenvolvimento legado e no dump preservado;
- **não** coexistem com o Flyway no schema do LumeOS (o schema novo não as referencia);
- **não** sofrem exclusão automática em nenhum momento.

## Novo banco/schema vazio + Flyway

1. Crie um banco/schema **novo e vazio** para o LumeOS.
2. Configure `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASSWORD` apontando para esse banco novo.
3. Na inicialização, o Flyway aplica `V1__baseline.sql` (schema alvo da Seção 9) e o Hibernate
   roda com `ddl-auto=validate` (nunca `update`): se o schema divergir, a aplicação **falha a
   inicialização** em vez de alterar silenciosamente (REQ-014).

### Produção vs dev/test (locations do Flyway)

- **Produção:** `spring.flyway.locations=classpath:db/migration` — aplica **apenas** a baseline
  (`V1` e futuras migrações de produção). O schema de produção permanece limpo.
- **Dev/test:** `spring.flyway.locations=classpath:db/migration,classpath:db/migration-devtest`
  — além da baseline, aplica a migração de compatibilidade `V2__devtest_legacy_compat.sql`, que
  cria `tb_usuario`/`tb_protocolos`/`tb_log_atividade` **apenas** para que `ddl-auto=validate`
  aceite as entidades JPA legadas ainda mapeadas (removidas no Sprint 2). Essa migração **não**
  é aplicada em produção.
