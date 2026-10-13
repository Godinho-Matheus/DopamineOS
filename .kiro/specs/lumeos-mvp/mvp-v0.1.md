# LumeOS v0.1 — Primeira versão utilizável

> **Camada de execução** sobre o `tasks.md`. Este documento NÃO substitui nem altera
> `requirements.md`, `design.md` ou `tasks.md` — ele apenas decompõe as tasks oficiais em
> **microentregas verticais** (fatias finas backend + frontend + teste) para chegar rápido a
> uma versão que duas pessoas consigam usar. A rastreabilidade oficial continua no `tasks.md`;
> os IDs de microentrega (B1, C2, …) são apenas de execução e **não** renumeram as tasks.

---

## 1. Objetivo da v0.1

O **LumeOS v0.1** é um **milestone intermediário utilizável**, não o encerramento do MVP
completo das specs. A meta é chegar o mais rápido possível a uma versão que **duas pessoas de
uma casa** consigam realmente usar — primeiro localmente, depois publicada por HTTPS.

A v0.1 deve permitir:

1. criar conta;
2. login / logout;
3. criar uma Casa (Household);
4. convidar outro usuário;
5. os dois usuários entrarem na mesma Casa;
6. cada usuário com sua Rotina/gamificação **privada**;
7. criar hábitos;
8. check-in;
9. acompanhar XP e histórico;
10. lista de compras **compartilhada** pela Casa;
11. acessar o sistema publicado por **HTTPS**.

Não é necessário concluir todo o MVP das specs para começar a usar o sistema. A v0.1 é o
"primeiro uso real".

---

## 2. Estado atual

- **Sprint 0 concluída** (branding LumeOS, config por ambiente/perfis, Flyway + schema V1,
  estrutura por domínios no backend, estrutura por features + API relativa no frontend,
  infraestrutura de testes). Checkpoint 0.7 aprovado e commitado/pushado.
- **Task 1.1 concluída** (commit `0f3190e`): domínio de identidade `User` (entidade, repository,
  DTOs, mapper, `PasswordEncoder` BCrypt, `UserService` com normalização de e-mail + validações +
  duplicidade).
- **Infraestrutura funcionando:** PostgreSQL via Flyway (V1 + compat dev/test V2),
  `ddl-auto=validate`, Testcontainers **2.0.5** (compatível com Docker Engine 29).
- **`mvnw.cmd clean verify` está verde** (unitários + ITs).
- **Autenticação ainda NÃO implementada**: não há Spring Security, JWT, cookie, CSRF, nem
  endpoints de auth. O frontend ainda consome o `game.service` legado por caminho relativo
  (`/api/v1/game`), sem guards/sessão.

---

## 3. O que entra na v0.1

- Identidade (já pronta — 1.1).
- Cadastro / login / logout / sessão.
- Household (Casa) + papéis OWNER/MEMBER.
- Convite (token seguro, uso único, 7 dias).
- Rotina / gamificação **por usuário autenticado** (GameProfile, hábitos, check-in, XP, histórico).
- Lista de compras **compartilhada** por Household.
- **Deploy alpha simples** (containers + Nginx + HTTPS; banco/API não públicos).

---

## 4. O que fica para depois (continua no roadmap, não é removido)

- Biblioteca / Leituras (Sprint 4).
- Home definitiva (Sprint 4).
- Configurações completas (Sprint 4).
- PWA + service worker (Sprint 5 / 5.A).
- CI/CD completo com GitHub Actions + GHCR (Sprint 5 / 5.K–5.L).
- Rollback automatizado (5.M).
- Observabilidade avançada (fora de escopo do MVP).
- Refinamentos visuais e automações de produção não essenciais.

Nada acima é descartado — apenas adiado para depois da v0.1-alpha.

---

## 5. Etapa B — Cadastro e Login

**Decompõe as tasks oficiais:** 1.2, 1.3, 1.4, 1.5, 1.10, 1.11 (parte de auth/sessão).
A Etapa A (User) já está concluída (1.1).

> Direção técnica: construir a cadeia de segurança em fatias finas, cada uma deixando o
> `clean verify` verde e algo observável. Backend primeiro nas fatias B1–B6; frontend em B7–B8;
> validação de ponta a ponta no gate.

### B1 — Spring Security mínimo + cadeia stateless + endpoint protegido
- **Objetivo:** adicionar `spring-boot-starter-security`, `SecurityFilterChain` com
  `SessionCreationPolicy.STATELESS`, liberar `GET /actuator/health` e deixar o resto autenticado.
  Reutilizar o `PasswordEncoder` já criado em 1.1.
- **Tasks oficiais:** 1.2 (1.2.1, 1.2.5 parcial).
- **Depende de:** 1.1 (feito).
- **Afeta:** `common/security` (SecurityConfig), `pom.xml`.
- **Validação automática:** IT — requisição a um endpoint protegido sem credencial → 401;
  `/actuator/health` → 200. `clean verify` verde.
- **Resultado visível:** um endpoint protegido responde 401 sem login.
- **Validação manual:** `curl` em endpoint protegido retorna 401; `/actuator/health` retorna 200.
- **Commit sugerido:** `feat: adiciona spring security stateless minimo`.

### B2 — JWT (emissão e validação)
- **Objetivo:** `JwtService` HS256 (claims mínimas `sub=userId`, `iat`, `exp`), TTL de `JWT_TTL`,
  validação de `JWT_SECRET` obrigatório em produção (falha o boot se ausente/fraco em prod).
- **Tasks oficiais:** 1.2 (1.2.2).
- **Depende de:** B1.
- **Afeta:** `common/security` (JwtService), configuração.
- **Validação automática:** teste unitário — emitir token e validar/parse; token expirado/alterado
  rejeitado.
- **Resultado visível:** serviço gera e valida um JWT em teste.
- **Validação manual:** teste automatizado cobre (sem UI ainda).
- **Commit sugerido:** `feat: adiciona emissao e validacao de JWT`.

### B3 — Filtro de cookie HttpOnly + usuário corrente
- **Objetivo:** `JwtCookieAuthFilter (OncePerRequestFilter)` lê o cookie `LUMEOS_SESSION`
  (`HttpOnly`, `Secure` por `COOKIE_SECURE`, `SameSite=Lax`), popula o `SecurityContext`;
  resolver de usuário corrente (`@CurrentUser`/`@AuthenticationPrincipal`).
- **Tasks oficiais:** 1.2 (1.2.3, 1.2.4, 1.2.6).
- **Depende de:** B2.
- **Afeta:** `common/security` (filtro, resolver), SecurityConfig.
- **Validação automática:** IT — requisição com cookie JWT válido autentica (200);
  cookie ausente/ inválido → 401.
- **Resultado visível:** autenticação por cookie funciona em teste de integração.
- **Validação manual:** coberto por IT (UI vem em B7/B8).
- **Commit sugerido:** `feat: autentica via cookie httponly com jwt`.

### B4 — Erros 401/403 padronizados + CSRF
- **Objetivo:** `JsonAuthenticationEntryPoint` (401) e `JsonAccessDeniedHandler` (403) com o mesmo
  `StandardError`; CSRF via `CookieCsrfTokenRepository.withHttpOnlyFalse()` (`XSRF-TOKEN` /
  `X-XSRF-TOKEN`), `GET /api/v1/auth/csrf` materializando o token; CSRF inválido → 403. Nunca
  `csrf.disable()`.
- **Tasks oficiais:** 1.3 (1.3.1–1.3.4), 1.4 (1.4.1–1.4.4).
- **Depende de:** B3.
- **Afeta:** `common/security` (handlers, CSRF), SecurityConfig, `common/exception`.
- **Validação automática:** IT — mutação sem `X-XSRF-TOKEN` → 403; com token → ok; 401/403 no
  formato `StandardError`.
- **Resultado visível:** mutação sem CSRF é bloqueada; corpo de erro padronizado.
- **Validação manual:** `curl` de POST sem CSRF → 403; `GET /auth/csrf` emite cookie `XSRF-TOKEN`.
- **Commit sugerido:** `feat: padroniza erros de seguranca e habilita csrf`.

### B5 — Endpoint de registro
- **Objetivo:** `POST /api/v1/auth/register` usando o `UserService` da 1.1 (201; e-mail duplicado
  → 409; validação → 400). `AuthController` + advice/`@ResponseStatus` para 409/400.
- **Tasks oficiais:** 1.5 (1.5.1 parcial, 1.5.3).
- **Depende de:** B4.
- **Afeta:** `identity` (AuthController ou controller de auth), `common/exception` (advice se preciso).
- **Validação automática:** IT (Testcontainers) — registro 201; duplicado 409; senha<8/ nome inválido → 400.
- **Resultado visível:** criar conta via HTTP.
- **Validação manual:** `curl` de registro cria usuário no banco.
- **Commit sugerido:** `feat: adiciona endpoint de registro`.

### B6 — Login / logout / me
- **Objetivo:** `POST /auth/login` (valida credenciais, emite cookie `LUMEOS_SESSION`, 200 /
  401 genérico), `POST /auth/logout` (expira cookie, 200), `GET /auth/me` (identidade do token,
  sem `passwordHash`). Sem refresh token.
- **Tasks oficiais:** 1.5 (1.5.1, 1.5.2, 1.5.4).
- **Depende de:** B5.
- **Afeta:** controller de auth, `common/security`.
- **Validação automática:** IT — login válido seta cookie; `me` com cookie → 200; após logout,
  endpoint protegido → 401; credencial inválida → 401 genérico.
- **Resultado visível:** fluxo completo de sessão via HTTP.
- **Validação manual:** `curl` login → cookie → `me` → logout.
- **Commit sugerido:** `feat: adiciona login, logout e me`.

### B7 — AuthService + guards + interceptor + bootstrap CSRF (frontend)
- **Objetivo:** `AuthService` (estado via `GET /auth/me`), `authGuard`, interceptor 401 →
  redireciona a login, bootstrap do `GET /auth/csrf` antes da 1ª mutação, XSRF do Angular.
- **Tasks oficiais:** 1.10 (1.10.1–1.10.3, 1.10.5).
- **Depende de:** B6.
- **Afeta:** `frontend/core/auth`, `core/guards`, `core/http`.
- **Validação automática:** testes de `AuthService`/guard (sujeito à limitação conhecida do
  runner Angular 21 + Vitest 4; garantir ao menos a compilação dos specs e `npm run build`).
- **Resultado visível:** app redireciona rota protegida para login quando sem sessão.
- **Validação manual:** no navegador, rota protegida sem login → vai para `/login`.
- **Commit sugerido:** `feat: adiciona authservice, guards e interceptor`.

### B8 — Telas de login e cadastro + rotas
- **Objetivo:** telas `/login` e `/cadastro`, rotas `/login` `/cadastro` públicas e as demais
  protegidas por `authGuard`; redirecionamento pós-auth (sem Household → `/onboarding`;
  com Household → `/home` — a lógica de Household entra na Etapa C; por ora, pós-login vai para a
  área autenticada existente).
- **Tasks oficiais:** 1.10 (1.10.4), 1.11 (1.11.1 — telas de criar conta/login).
- **Depende de:** B7.
- **Afeta:** `frontend/features` (login/cadastro), `app.routes.ts`, `layout`.
- **Validação automática:** `npm run build` verde; specs compilam.
- **Resultado visível:** telas reais de login e cadastro no navegador.
- **Validação manual:** cadastrar e logar pela UI.
- **Commit sugerido:** `feat: adiciona telas de login e cadastro`.

### B9 — Verificação ponta a ponta de autenticação (ajustes do gate)
- **Objetivo:** amarrar o fluxo: persistência de sessão ao recarregar, logout efetivo, proteção de
  rota. Ajustes finos (ex.: estado inicial do AuthService, tratamento de 401 global).
- **Tasks oficiais:** consolida 1.2–1.5, 1.10, 1.11 (auth).
- **Depende de:** B8.
- **Afeta:** frontend (ajustes), possivelmente backend (ajustes de cookie/CORS dev).
- **Validação automática:** `clean verify` (backend) + `npm run build` (frontend).
- **Resultado visível:** fluxo de autenticação completo pelo navegador.
- **Validação manual:** o roteiro do Gate B abaixo.
- **Commit sugerido:** `test: valida fluxo de autenticacao ponta a ponta`.

---

## 6. Gate manual B — Autenticação

**PARAR aqui.** Deve ser possível, localmente:

1. abrir a aplicação;
2. cadastrar um usuário;
3. fazer login;
4. recarregar a página e **permanecer autenticado**;
5. fazer logout;
6. tentar acessar uma rota protegida sem login e **ser redirecionado** para `/login`.

Ao atingir este gate, o Kiro para e aguarda feedback.

---

## 7. Etapa C — Casa compartilhada

**Decompõe:** 1.7, 1.8 e a parte necessária de 1.11 (onboarding de Household).

### C1 — Domínio Household + criação transacional
- **Objetivo:** entidades `Household`, `HouseholdMember` (enum `HouseholdRole`), repositórios;
  `ShoppingList` + `ShoppingListRepository` mínimos (bootstrap); criação transacional
  Household + OWNER + ShoppingList "Compras" (`active=true`); guarda de **1 Household por User**
  (UNIQUE + validação → 409 no 2º).
- **Tasks oficiais:** 1.7 (1.7.1–1.7.6).
- **Depende de:** Gate B.
- **Afeta:** `household` (entidades/repos/service), `shopping` (ShoppingList bootstrap).
- **Validação automática:** IT — criar Household cria OWNER + lista "Compras"; 2º Household → 409.
- **Resultado visível:** criar Casa via HTTP.
- **Validação manual:** `POST /households` cria a Casa; 2ª tentativa → 409.
- **Commit sugerido:** `feat: adiciona household com criacao transacional`.

### C2 — Convites (geração + consumo seguro)
- **Objetivo:** `HouseholdInvite` (só hash SHA-256 do token), token `SecureRandom` ≥128 bits
  Base64URL retornado 1x; expira em 7 dias; uso único; só OWNER gera (não-OWNER → 403); consumo
  **transacional e atômico** (UPDATE condicional de `consumed_at` + criação do `HouseholdMember`
  na mesma transação; rollback junto se falhar).
- **Tasks oficiais:** 1.8 (1.8.1–1.8.6).
- **Depende de:** C1.
- **Afeta:** `household` (invite entity/service/endpoints).
- **Validação automática:** IT — convite válido vincula MEMBER; expirado/consumido → 410;
  inexistente → 404; já membro/ outro household → 409; não-OWNER → 403; rollback transacional.
- **Resultado visível:** gerar e consumir convite via HTTP.
- **Validação manual:** OWNER gera token; 2º usuário entra com o token.
- **Commit sugerido:** `feat: adiciona convites de household`.

### C3 — Endpoints de Household + `/households/me`
- **Objetivo:** `POST /households`, `GET /households/me` (com membros/papel), `POST /households/invites`,
  `POST /households/join`. DTOs/mapper.
- **Tasks oficiais:** 1.7 (1.7.5), 1.8 (1.8.4).
- **Depende de:** C2.
- **Afeta:** `household` (controller, dto, mapper).
- **Validação automática:** IT dos 4 endpoints com códigos corretos.
- **Resultado visível:** API de Casa completa.
- **Validação manual:** `curl` do fluxo criar/convidar/entrar/ver.
- **Commit sugerido:** `feat: adiciona endpoints de household e convite`.

### C4 — Onboarding de Household no frontend
- **Objetivo:** tela `/onboarding` (criar Casa ou entrar por convite); redirecionamento pós-auth
  por vínculo de Household (sem Household → `/onboarding`; com → `/home`/área autenticada).
  GameProfile **não** entra aqui (é da Etapa D).
- **Tasks oficiais:** 1.11 (1.11.1–1.11.4, parte de Household).
- **Depende de:** C3.
- **Afeta:** `frontend/features` (onboarding/household), rotas, `household.service`.
- **Validação automática:** `npm run build`; specs compilam.
- **Resultado visível:** telas de criar/entrar em Casa.
- **Validação manual:** roteiro do Gate C.
- **Commit sugerido:** `feat: adiciona onboarding de household no frontend`.

---

## Gate manual C — Casa compartilhada

**PARAR.** Com **dois usuários**:

- A cria a Casa → gera convite;
- B entra com o convite;
- ambos pertencem à **mesma Casa** (`/households/me` mostra os dois).

---

## 8. Etapa D — Rotina (gamificação por usuário)

**Decompõe:** 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.8, 2.9, 2.10 — apenas o necessário.
**Priorizar reutilização da UI atual** (dashboard radar, quest-editor, histórico). Não redesenhar.

### D1 — GameProfile (entidade + criação por classe)
- **Objetivo:** `GameProfile` (com `@Version`), criação por classe inicializando atributos/XP/HP/MP;
  `POST /gamification/profile` (409 no 2º), `GET /gamification/profile`; isolamento (perfil alheio → 404).
- **Tasks oficiais:** 2.1 (2.1.1–2.1.6).
- **Depende de:** Gate C.
- **Afeta:** `gamification`.
- **Validação automática:** IT — 3 classes inicializam atributos/HP/MP corretos; 2º perfil → 409; alheio → 404.
- **Resultado visível:** criar personagem via HTTP.
- **Commit sugerido:** `feat: adiciona gameprofile`.

### D2 — Progressão (laço de nível) + isolamento transversal
- **Objetivo:** serviço de progressão com laço `while` (múltiplos level-ups), cura **só** se houve
  subida; base de autorização por titularidade (`findByIdAndUserId`) reutilizável.
- **Tasks oficiais:** 2.2 (2.2.1–2.2.3), 1.6 (base de isolamento, se ainda não feita em B).
- **Depende de:** D1.
- **Afeta:** `gamification`, utilitário de titularidade em `common`.
- **Validação automática:** testes — subida simples, múltipla, sem cura sem subida, cura com subida.
- **Commit sugerido:** `feat: adiciona progressao de nivel`.

### D3 — Locking otimista do check-in (infra)
- **Objetivo:** `@Version` no GameProfile + coordenador de retry (até 3x, nova transação por
  tentativa, fora da transação); 409 ao esgotar. Sem ActivityLog ainda.
- **Tasks oficiais:** 2.3 (2.3.1–2.3.4).
- **Depende de:** D2.
- **Afeta:** `gamification`.
- **Validação automática:** teste de concorrência (apenas GameProfile) resolve por retry; esgota → 409.
- **Commit sugerido:** `feat: adiciona locking otimista no gameprofile`.

### D4 — Habit (migração Protocolo→Habit) + CRUD
- **Objetivo:** entidade `Habit` por usuário (campos preservados); `POST/GET/DELETE /habits`
  filtrados por `userId`; habit alheio → 404.
- **Tasks oficiais:** 2.4 (2.4.1–2.4.5).
- **Depende de:** D1.
- **Afeta:** `habits`.
- **Validação automática:** IT — CRUD; isolamento A/B; validações → 400.
- **Resultado visível:** criar/listar/excluir hábitos via HTTP.
- **Commit sugerido:** `feat: adiciona habitos`.

### D5 — ActivityLog (histórico snapshot)
- **Objetivo:** `ActivityLog` (`habit_id` nullable ON DELETE SET NULL, snapshot, TIMESTAMPTZ);
  `GET /habits/history` (só do token, desc).
- **Tasks oficiais:** 2.5 (2.5.1–2.5.4).
- **Depende de:** D4.
- **Afeta:** `habits`.
- **Validação automática:** IT — histórico do token; excluir Habit preserva ActivityLog.
- **Commit sugerido:** `feat: adiciona historico de atividades`.

### D6 — Check-in transacional
- **Objetivo:** `POST /gamification/checkin/{habitId}`: carrega habit próprio → GameProfile →
  recompensa exata por dificuldade → progressão → salva → ActivityLog → commit; rollback atômico
  perfil+log na falha otimista; sem GameProfile → 404.
- **Tasks oficiais:** 2.6 (2.6.1–2.6.5).
- **Depende de:** D3, D4, D5.
- **Afeta:** `gamification`, `habits`.
- **Validação automática:** IT — recompensas EASY/MEDIUM/HARD/EPIC; sem perfil → 404; rollback.
- **Resultado visível:** check-in dá XP e registra histórico.
- **Commit sugerido:** `feat: adiciona check-in transacional`.

### D7 — Frontend da Rotina migrado para as novas APIs
- **Objetivo:** migrar dashboard (radar ApexCharts), quest-editor, histórico e check-in para
  `features/habits` consumindo `/habits` e `/gamification`. Preservar a UI atual. Integrar criação
  opcional de GameProfile ao onboarding/área autenticada.
- **Tasks oficiais:** 2.8 (2.8.1–2.8.4).
- **Depende de:** D6, Gate B/C (frontend auth/household).
- **Afeta:** `frontend/features/habits`, layout/sidebar.
- **Validação automática:** `npm run build`; specs compilam.
- **Resultado visível:** Rotina funcional no navegador, por usuário autenticado.
- **Commit sugerido:** `feat: migra rotina para o frontend por features`.

### D8 — Remoção do legado single-player (após e2e)
- **Objetivo:** remover `findAll().findFirst()`, `/setup` destrutivo, `GameController`/`GameService`
  legados; `GameLoader` só dev; aposentar a compat dev/test via migration de cleanup (sem apagar
  banco legado). **Só após** D7 validado e o gate end-to-end.
- **Tasks oficiais:** 2.10 (2.10.1–2.10.5) — precedido do checkpoint 2.9.
- **Depende de:** D7 + gate e2e da Rotina.
- **Afeta:** backend legado, migrations dev/test.
- **Validação automática:** `clean verify` sem artefatos legados; validate passa.
- **Commit sugerido:** `refactor: remove modelo single-player legado`.

---

## Gate manual D — Rotina

**PARAR.** Usuário A e B possuem, de forma independente e privada:

- personagens (GameProfile) distintos;
- hábitos distintos;
- XP distinto;
- histórico distinto.

Nenhum vê/altera a Rotina do outro.

---

## 9. Etapa E — Compras compartilhadas

**Decompõe:** 3.1, 3.2, 3.3.

### E1 — ShoppingItem (backend) + CRUD/marcar
- **Objetivo:** `ShoppingItem` sobre a lista "Compras" do Household (sem `household_id` próprio;
  `quantidade` BigDecimal NUMERIC(12,3) > 0); endpoints `GET/POST /shopping/items`,
  `PUT /{id}`, `PATCH /{id}/comprar`, `PATCH /{id}/desmarcar`, `DELETE /{id}`.
- **Tasks oficiais:** 3.1 (3.1.1–3.1.5).
- **Depende de:** Gate C (Household + lista "Compras" já existem desde C1).
- **Afeta:** `shopping`.
- **Validação automática:** IT — criar/editar; quantidade decimal; quantidade ≤ 0 → 400; limites.
- **Commit sugerido:** `feat: adiciona itens de compra`.

### E2 — Isolamento por Household + ordenação
- **Objetivo:** household derivado via `User→HouseholdMember→Household→ShoppingList→ShoppingItem`;
  nunca confiar em `householdId` do cliente; item de outro household → 404; ordenação
  pendentes-antes-de-comprados; `completed_by` registrado.
- **Tasks oficiais:** 3.2 (3.2.1–3.2.3).
- **Depende de:** E1.
- **Afeta:** `shopping`.
- **Validação automática:** IT — compartilhamento entre membros; isolamento entre households; ordenação.
- **Commit sugerido:** `feat: isola e ordena a lista de compras`.

### E3 — Frontend de compras (responsivo)
- **Objetivo:** `features/shopping` com visualizar/adicionar/editar/marcar/desmarcar/excluir;
  responsivo e fácil no celular; sem WebSocket (estado atualiza ao recarregar).
- **Tasks oficiais:** 3.3 (3.3.1–3.3.3).
- **Depende de:** E2.
- **Afeta:** `frontend/features/shopping`.
- **Validação automática:** `npm run build`; specs compilam.
- **Resultado visível:** lista de compras compartilhada no navegador.
- **Commit sugerido:** `feat: adiciona tela de compras compartilhada`.

---

## Gate manual E — Compras

**PARAR.** Fluxo com dois usuários da mesma Casa:

1. Usuário A adiciona **"Leite"**;
2. Usuário B atualiza e **vê "Leite"**;
3. Usuário B marca como **comprado**;
4. Usuário A atualiza e vê **comprado** (com `completed_by` = B).

Usuário de outra Casa nunca vê esses itens.

---

## 10. Etapa F — Publicação alpha (LumeOS v0.1-alpha)

**Caminho reduzido** usando só o necessário da Sprint 5. **Não** exigir antes da alpha: PWA,
GitHub Actions/CD automático, rollback automatizado, observabilidade avançada.

### F1 — Dockerfile backend
- **Objetivo:** multi-stage Maven/JDK21 → JRE21; healthcheck `curl /actuator/health`.
- **Tasks oficiais:** 5.2 (5.2.1–5.2.2).
- **Depende de:** Gate E (produto funcional local).
- **Validação:** `docker compose build` do serviço api.
- **Commit sugerido:** `chore: adiciona dockerfile do backend`.

### F2 — Dockerfile frontend
- **Objetivo:** Node build → `nginx:alpine` estático com `try_files` (SPA fallback); não faz proxy
  de `/api`; healthcheck `wget`.
- **Tasks oficiais:** 5.3 (5.3.1–5.3.3).
- **Depende de:** F1.
- **Validação:** `docker compose build` do serviço web.
- **Commit sugerido:** `chore: adiciona dockerfile do frontend`.

### F3 — docker-compose.prod + PostgreSQL persistente + variáveis
- **Objetivo:** `docker-compose.prod.yml` com `lumeos-web`/`lumeos-api`/`lumeos-db`; volume
  `lumeos_pgdata`; `restart: unless-stopped`; healthchecks; `depends_on` db healthy; **sem
  publicar portas de DB/API**; redes (proxy externa + interna). Variáveis via `.env` não versionado.
- **Tasks oficiais:** 5.4 (5.4.1–5.4.3), parte de 5.D.
- **Depende de:** F2.
- **Validação:** `docker compose config` + `up -d` local; app sobe; dados persistem após restart.
- **Commit sugerido:** `chore: adiciona compose de producao`.

### F4 — Reverse proxy Nginx + HTTPS (Let's Encrypt)
- **Objetivo:** `proxy-net` externa pré-criada; Nginx central (80/443) roteando `/` → web e
  `/api` → api (sem rotear `/actuator`); HTTP→HTTPS; Certbot containerizado (volumes de certs +
  webroot ACME; renovação pelo host).
- **Tasks oficiais:** 5.5 (5.5.1–5.5.3), 5.6 (5.6.1–5.6.2).
- **Depende de:** F3 + **ação do usuário** (VPS, domínio, DNS).
- **Validação:** domínio responde HTTPS; HTTP redireciona; `/actuator` não público; 5432/8080 não públicos.
- **Commit sugerido:** `chore: adiciona proxy nginx e https`.

### F5 — Backup básico + documentação mínima de deploy
- **Objetivo:** backup `pg_dump` via container (sem depender de vars no host); doc mínima em
  `docs/DEPLOY_VPS.md` (já iniciada) cobrindo subir/atualizar/backup/restore do alpha.
- **Tasks oficiais:** 5.9 (parte), 5.14 (parte).
- **Depende de:** F4.
- **Validação:** executar um backup e um restore de teste.
- **Commit sugerido:** `chore: adiciona backup basico e doc de deploy alpha`.

---

## Gate manual F — Publicação alpha

**PARAR.** `https://<dominio>` acessível; login/CSRF/sessão funcionam; Rotina privada e Compras
compartilhadas funcionam entre dois usuários; dados persistem após restart dos containers; DB/API
não públicos; `/actuator` não público.

---

## 11. Como testar localmente

> Seção operacional — manter atualizada a cada microentrega concluída.

**Pré-requisitos:** Docker Desktop (Engine 29 OK), JDK 21, Node 18+/npm, PostgreSQL (ou usar o
container). O backend lê config por variáveis de ambiente (perfil `dev` por padrão).

**Banco (dev):** apontar `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD` para um PostgreSQL local
(ou subir um container `postgres:16-alpine`). Exportar `DB_PASSWORD` antes de rodar o backend.
Flyway aplica o schema automaticamente (dev inclui a location de compat dev/test).

**Backend:** em `backend/`, `mvnw.cmd spring-boot:run` (perfil `dev`). API em `http://localhost:8080`.

**Frontend:** em `frontend/`, `npm install` (1ª vez) e `npm start`. App em `http://localhost:4200`.
O proxy de dev encaminha `/api` → `http://localhost:8080` (mesma origem).

**URLs:** frontend `http://localhost:4200`; API (via proxy) `/api/v1/...`; health
`http://localhost:8080/actuator/health`.

**Fluxo já disponível (hoje):** apenas o domínio de identidade (`User`) existe no backend; ainda
**não há** endpoints de auth nem telas novas — o fluxo utilizável começa a aparecer a partir do
**Gate B**. A aplicação legada de gamificação (single-player) ainda responde em `/api/v1/game` até
ser substituída na Etapa D.

**Limitações atuais:** sem autenticação; `npm test` (Vitest) bloqueado por bug de toolchain
Angular 21 + Vitest 4 (build/compilação verdes) — validação de frontend por `npm run build` +
teste manual até resolvermos o runner.

---

## 12. Mapeamento para o tasks.md

> O `tasks.md` continua sendo a **rastreabilidade oficial**. A tabela abaixo liga cada
> microentrega às tasks/subtasks oficiais e ao gate correspondente.

| Microentrega | Task oficial | Subtasks oficiais | Gate |
|---|---|---|---|
| (A) 1.1 User | 1.1 | 1.1.1–1.1.7 | — (concluída) |
| B1 Security mínimo | 1.2 | 1.2.1, 1.2.5 | B |
| B2 JWT | 1.2 | 1.2.2 | B |
| B3 Cookie HttpOnly + usuário corrente | 1.2 | 1.2.3, 1.2.4, 1.2.6 | B |
| B4 Erros 401/403 + CSRF | 1.3, 1.4 | 1.3.1–1.3.4, 1.4.1–1.4.4 | B |
| B5 Registro | 1.5 | 1.5.1 (parte), 1.5.3 | B |
| B6 Login/logout/me | 1.5 | 1.5.1, 1.5.2, 1.5.4 | B |
| B7 AuthService/guards/interceptor | 1.10 | 1.10.1–1.10.3, 1.10.5 | B |
| B8 Telas login/cadastro | 1.10, 1.11 | 1.10.4, 1.11.1 | B |
| B9 E2E auth | 1.2–1.5, 1.10, 1.11 | consolidação | B |
| C1 Household transacional | 1.7 | 1.7.1–1.7.6 | C |
| C2 Convites | 1.8 | 1.8.1–1.8.6 | C |
| C3 Endpoints household | 1.7, 1.8 | 1.7.5, 1.8.4 | C |
| C4 Onboarding household (frontend) | 1.11 | 1.11.1–1.11.4 | C |
| D1 GameProfile | 2.1 | 2.1.1–2.1.6 | D |
| D2 Progressão + isolamento | 2.2, 1.6 | 2.2.1–2.2.3, 1.6.1–1.6.2 | D |
| D3 Locking otimista | 2.3 | 2.3.1–2.3.4 | D |
| D4 Habit CRUD | 2.4 | 2.4.1–2.4.5 | D |
| D5 ActivityLog | 2.5 | 2.5.1–2.5.4 | D |
| D6 Check-in | 2.6 | 2.6.1–2.6.5 | D |
| D7 Frontend Rotina | 2.8 | 2.8.1–2.8.4 | D |
| D8 Remover legado | 2.9 (checkpoint), 2.10 | 2.10.1–2.10.5 | D |
| E1 ShoppingItem backend | 3.1 | 3.1.1–3.1.5 | E |
| E2 Isolamento/ordenação | 3.2 | 3.2.1–3.2.3 | E |
| E3 Frontend compras | 3.3 | 3.3.1–3.3.3 | E |
| F1 Dockerfile backend | 5.2 | 5.2.1–5.2.2 | F |
| F2 Dockerfile frontend | 5.3 | 5.3.1–5.3.3 | F |
| F3 Compose prod + DB persistente | 5.4 | 5.4.1–5.4.3 | F |
| F4 Nginx + HTTPS | 5.5, 5.6 | 5.5.1–5.5.3, 5.6.1–5.6.2 | F |
| F5 Backup + doc mínima | 5.9, 5.14 | (parte) | F |

**Fora da v0.1 (continuam no roadmap):** 1.9/1.12 checkpoints, 2.7/2.11 checkpoints,
3.4 checkpoint, Sprint 4 (Leituras/Home/Config), 5.1 PWA, 5.7 hardening completo, 5.8 rate limiting,
5.10 observabilidade, 5.11–5.13 CI/CD/rollback, 5.15/5.16 checklist/checkpoint, 4.x gerais.

---

## 13. Critério de conclusão da v0.1

Duas pessoas devem conseguir:

- criar conta;
- autenticar (login/logout, sessão persistente);
- compartilhar uma Casa (convite + ingresso);
- usar Rotina **privada** (personagem, hábitos, XP);
- fazer check-in;
- consultar histórico;
- compartilhar a lista de compras;
- usar a aplicação publicada via **HTTPS**;
- preservar os dados após restart dos containers.
