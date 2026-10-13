# Requirements Document

LumeOS (MVP)

## Introduction

O LumeOS é a evolução do produto atual **DopamineOS** (um rastreador de hábitos gamificado em RPG, single-player) para um **hub pessoal e compartilhado** destinado a organizar vida, rotina e casa. No MVP, o produto atende duas pessoas de um mesmo lar/casal, mas a modelagem **não pode impedir** uma futura evolução para SaaS multi-tenant.

O LumeOS é **um único produto** (não três aplicativos separados) composto por três módulos principais sobre uma base de fundações:

1. **Rotina / Hábitos Gamificados** — preserva o comportamento gamificado existente, agora por usuário autenticado.
2. **Lista de Compras Compartilhada** — por household (casa).
3. **Lista de Leituras** — por usuário.

As fundações incluem: identidade de usuários, autenticação, household/casa, autorização e isolamento de dados, navegação, persistência, configuração por ambiente, PWA e hospedagem em VPS.

Este trabalho é um **refatoramento incremental do projeto existente, feito no próprio repositório**, preservando o histórico Git. Não é uma reescrita big-bang e não substitui a stack atual.

### Princípios de evolução (restrições de alto nível)

- A stack atual é mantida: **Backend** Java 21, Spring Boot, Spring Web, Spring Data JPA, PostgreSQL, MapStruct, Lombok, Maven; **Frontend** Angular 21, TypeScript, RxJS, TailwindCSS, ApexCharts (quando fizer sentido); **Infra** Docker, Docker Compose, VPS Linux, reverse proxy central da VPS (compatível com Nginx), HTTPS. A VPS poderá hospedar outros projetos além do LumeOS no futuro.
- A gamificação **não** é removida.
- O backend **não** é substituído por Supabase/Firebase.
- Os dados existentes **não** são apagados silenciosamente.

## Glossary

- **LumeOS**: O produto resultante da evolução do DopamineOS; o sistema completo (frontend + backend + banco).
- **Sistema**: Qualquer componente do LumeOS responsável por atender a um requisito (backend ou frontend conforme o contexto).
- **API_LumeOS**: O backend Spring Boot exposto sob o prefixo `/api/v1/`.
- **App_Web**: O frontend Angular 21 (PWA) servido ao navegador.
- **User**: Pessoa (identidade): `id`, `nome`, `email`, `passwordHash`, `createdAt`. Representa quem faz login. **Não** contém dados de RPG.
- **GameProfile**: Perfil gamificado (personagem RPG) pertencente a um User: `classe`, `nivel`, `xpAtual`, `xpParaProximoNivel`, `moedas`, `forca`, `destreza`, `intelecto`, `carisma`, `constituicao`, `hpAtual`, `maxHp`, `mpAtual`, `maxMp`. Um User possui 0..1 GameProfile. Não existe campo genérico `xp`; a progressão usa `xpAtual` (XP acumulado no nível atual) e `xpParaProximoNivel` (limiar do nível atual).
- **Household**: Casa/lar compartilhado. Agrupa Users e os dados compartilhados (compras). No MVP, um User pertence a NO MÁXIMO UM Household; a estrutura relacional (User ↔ HouseholdMember ↔ Household) é preparada para N:N futuro, mas a restrição de um único Household por usuário vale apenas no MVP e pode ser removida sem alterar a modelagem.
- **HouseholdMember**: Vínculo entre User e Household, portador do papel (`OWNER` ou `MEMBER`). No MVP, cada User possui no máximo um HouseholdMember.
- **Convite_Household**: Mecanismo de convite baseado em TOKEN aleatório criptograficamente seguro, URL-safe, com entropia adequada (~128 bits ou mais), que permite a um segundo User ingressar em um Household existente.
- **Habit** (antigo **Protocolo**): Hábito/missão gamificado pertencente a um User (`nome`, `icone`, `cor`, `descricao`, `atributo`, `dificuldade`, `duracaoMinutos`). Mapeamento documentado: `Protocolo` → `Habit`.
- **CheckIn**: Ato de concluir um Habit, concedendo XP e moedas ao GameProfile do User e gerando um registro de atividade.
- **LogAtividade**: Registro histórico (snapshot) de um CheckIn (`nomeMissao`, `icone`, `xpGanho`, `duracaoMinutos`, `dataHora`), além de um `habitId` OPCIONAL. É um snapshot da ação realizada: preserva os dados do momento do check-in independentemente de o Habit de origem ainda existir. A referência `habitId` é opcional e NÃO provoca exclusão em cascata do histórico quando o Habit é removido.
- **ShoppingList**: Lista de compras pertencente a um Household. A modelagem adota explicitamente Household → ShoppingList → ShoppingItem. No MVP há apenas UMA ShoppingList ativa por Household (criada automaticamente com o nome "Compras" na criação do Household), mas a estrutura deixa espaço para múltiplas listas no futuro sem alterar a UX atual.
- **ShoppingItem**: Item de compra pertencente a uma ShoppingList de um Household: `id`, `shoppingListId`, `nome`, `quantidade` (decimal positivo, suporta frações), `unidade` (opcional), `categoria` (opcional), `comprado`, `createdBy`, `completedBy` (opcional), `createdAt`, `updatedAt`. A associação ao Household é DERIVADA através da ShoppingList (`ShoppingItem.shoppingListId` → `ShoppingList.householdId`); o ShoppingItem NÃO armazena `householdId` próprio, evitando redundância que permita inconsistência. `quantidade` é um valor decimal maior que 0 (ex.: 1 unidade, 0,5 kg, 1,5 L), persistido como `BigDecimal` no backend e `NUMERIC` no PostgreSQL.
- **ReadingEntry**: Entrada da biblioteca de leituras de um User: `id`, `userId`, `titulo`, `autor`, `status`, `nota`, `dataInicio`, `dataFim`, `observacoes`, `createdAt`, `updatedAt`.
- **StatusLeitura**: Enum `{QUERO_LER, LENDO, CONCLUIDO, ABANDONADO}`.
- **ClasseRPG**: Enum `{GUERREIRO, MAGO, LADINO}`.
- **Atributo**: Enum `{FORCA, DESTREZA, INTELECTO, CARISMA, CONSTITUICAO}`.
- **Dificuldade**: Enum `{EASY, MEDIUM, HARD, EPIC}` com recompensas associadas de XP e moedas (não há recompensa em "pontos").
- **Usuario_Autenticado**: O User cuja sessão foi validada por token JWT no cookie HttpOnly.
- **Flyway**: Ferramenta de migrações versionadas do banco PostgreSQL.
- **Reverse_Proxy**: Gateway/reverse proxy CENTRAL da VPS que detém as portas públicas 80/443 e roteia os domínios para os projetos hospedados (ex.: `lume.dominio` → `lumeos-web`), podendo atender outros projetos no futuro. O design define e justifica a estratégia exata de reverse proxy, evitando duas camadas Nginx redundantes sem justificativa.

### Convenção de tempo (timestamps)

- Para carimbos de tempo do sistema (`createdAt`, `updatedAt`, `dataHora`): preferir `OffsetDateTime`/`Instant` (com fuso).
- Para datas puras de domínio (ex.: `dataInicio`, `dataFim` de leitura): usar `LocalDate`.

---

## Requirements

### REQ-001 — Autenticação de Usuários

**Descrição:** O LumeOS deve exigir autenticação para acesso às funcionalidades, usando e-mail e senha, com sessão baseada em JWT transportado por cookie HttpOnly.

**Motivação:** A aplicação ficará exposta na internet; o modelo single-player sem autenticação atual é inadequado e inseguro.

**Regras:**
- Senhas são armazenadas apenas como hash BCrypt; a senha em texto puro nunca é persistida nem registrada em log.
- O token JWT é transportado em cookie HttpOnly; em produção o cookie deve ser `Secure` e usar atributo `SameSite` adequado.
- Não armazenar o token sensível em `localStorage` quando houver alternativa razoável (cookie HttpOnly é a alternativa adotada).
- O tempo de expiração do JWT é configurável por ambiente.
- O `JWT_SECRET` é obrigatório em produção e deve ser criptograficamente forte (ver REQ-015).
- O logout remove/expira o cookie de sessão.
- Refresh token está FORA DO ESCOPO do MVP, salvo se surgir necessidade tecnicamente justificada.
- Como a autenticação usa JWT em cookie HttpOnly, a proteção CSRF é OBRIGATÓRIA e permanece HABILITADA para operações mutáveis (POST/PUT/PATCH/DELETE). NÃO se deve usar `csrf.disable()` apenas por adotar JWT. Preferir a integração nativa do Spring Security com o Angular compatível com `CookieCsrfTokenRepository` / XSRF do Angular, em que o App_Web envia o token CSRF no header apropriado. A implementação concreta é detalhada no design.
- Endpoints: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `GET /api/v1/auth/me`.
- E-mail é único no Sistema.
- Endpoints sensíveis como login e validação de convite DEVEM ter proteção/rate limiting, que PODE ser implementado no reverse proxy (ver RNF-02 e RNF-16).

**Critérios de Aceitação:**
1. QUANDO um visitante envia `POST /api/v1/auth/register` com `nome` de 1 a 100 caracteres, e-mail em formato válido e não cadastrado, e senha de 8 a 72 caracteres, O SISTEMA DEVE criar um User com `passwordHash` BCrypt e retornar status 201.
2. QUANDO um User envia `POST /api/v1/auth/login` com e-mail e senha corretos, O SISTEMA DEVE emitir um JWT em cookie HttpOnly com tempo de expiração configurável por ambiente e retornar status 200.
3. QUANDO um Usuario_Autenticado envia `GET /api/v1/auth/me`, O SISTEMA DEVE retornar os dados de identidade do próprio User (id, nome, e-mail) sem expor `passwordHash`.
4. QUANDO um Usuario_Autenticado envia `POST /api/v1/auth/logout`, O SISTEMA DEVE invalidar/expirar o cookie de sessão e retornar status 200.
5. QUANDO o App_Web executa logout, O SISTEMA DEVE encerrar a sessão de forma efetiva (não apenas visual), de modo que uma requisição subsequente a endpoint protegido retorne 401.

**Proteção CSRF:**
1. QUANDO uma requisição mutável (POST/PUT/PATCH/DELETE) é enviada sem token CSRF válido, O SISTEMA DEVE rejeitá-la com status 403.
2. O SISTEMA DEVE manter a proteção CSRF habilitada para operações mutáveis mesmo utilizando JWT em cookie HttpOnly, não aplicando `csrf.disable()` por esse motivo.

**Casos de Erro:**
1. SE o e-mail informado no registro já existir, ENTÃO O SISTEMA DEVE retornar status 409 com mensagem de conflito, sem revelar o `passwordHash` existente.
2. SE as credenciais de login forem inválidas, ENTÃO O SISTEMA DEVE retornar status 401 com mensagem genérica que não distingue "e-mail inexistente" de "senha incorreta".
3. SE um endpoint protegido for acessado sem token válido, ENTÃO O SISTEMA DEVE retornar status 401.
4. SE a senha informada no registro tiver menos de 8 ou mais de 72 caracteres (limite do BCrypt), ENTÃO O SISTEMA DEVE retornar status 400 com mensagem de validação.
5. SE o e-mail informado no registro não estiver em formato de endereço de e-mail válido, ENTÃO O SISTEMA DEVE retornar status 400 com mensagem de validação.
6. SE o `nome` informado no registro estiver vazio ou exceder 100 caracteres, ENTÃO O SISTEMA DEVE retornar status 400 com mensagem de validação.

**Permissões:** `register`, `login` são públicos. `logout` e `me` exigem Usuario_Autenticado.

---

### REQ-002 — Modelo de Identidade (User vs GameProfile)

**Descrição:** O LumeOS deve separar a identidade da pessoa (User) do personagem RPG (GameProfile), substituindo a entidade `Usuario` atual que mistura identidade, personagem e progressão.

**Motivação:** A entidade `Usuario` atual conflita identidade (nome) com personagem (classe, nível, XP, moedas, atributos, HP/MP) e métodos de negócio de progressão. A gamificação deve ser uma funcionalidade do usuário, não a sua identidade.

**Regras:**
- `User` contém: `id`, `nome`, `email`, `passwordHash`, `createdAt`.
- `GameProfile` contém os campos de personagem/progressão (`classe`, `nivel`, `xpAtual`, `xpParaProximoNivel`, `moedas`, `forca`, `destreza`, `intelecto`, `carisma`, `constituicao`, `hpAtual`, `maxHp`, `mpAtual`, `maxMp`). Não existe campo genérico `xp`.
- A relação é User 1 — 0..1 GameProfile.
- Nenhum campo de RPG pode residir na entidade `User`.
- A entidade Java pode se chamar `User`, mas a TABELA PostgreSQL deve ter nome inequívoco, preferencialmente `users` (ver REQ-014).

**Critérios de Aceitação:**
1. O SISTEMA DEVE persistir identidade (User) e personagem (GameProfile) em tabelas distintas relacionadas por chave estrangeira, de modo que nenhum campo de personagem/progressão exista na entidade `User`.
2. QUANDO um User ainda não criou personagem, O SISTEMA DEVE representar isso como ausência de GameProfile (0 registros) para aquele User.
3. QUANDO um GameProfile é criado, O SISTEMA DEVE associá-lo a exatamente um User e inicializar `nivel`=1, `xpAtual`=0 e `moedas`=0.
4. QUANDO um User é criado, O SISTEMA DEVE exigir `nome` com 1 a 100 caracteres, `email` com formato de endereço de e-mail válido e único, e registrar `createdAt` com o instante de criação.

**Casos de Erro:**
1. SE houver tentativa de criar um segundo GameProfile para um User que já possui um, ENTÃO O SISTEMA DEVE rejeitar a operação, retornar status 409 e preservar o GameProfile existente sem alterações.
2. SE houver tentativa de criar GameProfile sem User autenticado, ENTÃO O SISTEMA DEVE retornar status 401 e não persistir nenhum registro.
3. SE um User autenticado tentar ler ou modificar um GameProfile que não lhe pertence, ENTÃO O SISTEMA DEVE retornar status 404 e preservar o GameProfile alvo sem alterações.
4. SE houver tentativa de criar um User com `email` em formato inválido ou já existente, ENTÃO O SISTEMA DEVE retornar status 400 (formato) ou 409 (duplicado) e não persistir nenhum registro.

**Permissões:** Um User só pode ler e modificar o próprio GameProfile.

---

### REQ-003 — Household e Convite

**Descrição:** O LumeOS deve modelar Households (casas) com membros e papéis, permitindo que um segundo usuário ingresse em um Household existente por meio de um convite simples.

**Motivação:** Os dados de compras são compartilhados pela casa; é necessário um agrupamento (Household) que também não impeça múltiplos households no futuro.

**Regras:**
- Entidades: `Household`, `HouseholdMember` (com papel `OWNER` ou `MEMBER`), associadas a `User`. A estrutura relacional User ↔ HouseholdMember ↔ Household é preparada para N:N futuro.
- No MVP, um User pertence a NO MÁXIMO UM Household. NÃO existe seleção de Household_Ativo no MVP; o Household do Usuario_Autenticado é DERIVADO do seu único HouseholdMember. Não há `activeHouseholdId`, seletor de households, nem contexto de múltiplos households.
- Esta restrição de um único Household por usuário pode ser removida futuramente SEM alterar a estrutura relacional.
- O criador do Household recebe papel `OWNER`; quem ingressa por convite recebe `MEMBER`.
- QUANDO um Household é criado, o Sistema cria automaticamente a ShoppingList padrão com o nome "Compras" (ver REQ-008).
- O mecanismo de convite não exige fluxo de e-mail transacional no MVP.
- O token de convite é um TOKEN aleatório criptograficamente seguro, URL-safe, com entropia adequada (~128 bits ou mais) — não um código curto/previsível. É único no Sistema, expira 7 dias após a geração e é de uso único (consumido ao ser usado para ingressar).
- O token ORIGINAL (bruto) é entregue ao usuário APENAS no momento da geração do convite; o Sistema não o armazena em texto puro.
- O Sistema persiste APENAS um hash determinístico do token (preferencialmente SHA-256 ou equivalente adequado), não o token bruto.
- Ao consumir um convite, o Sistema calcula o hash do token apresentado e localiza o convite PELO HASH.
- O token bruto NUNCA é registrado em logs.
- Expiração e uso único permanecem inalterados. A implementação exata será detalhada no design.
- Apenas o `OWNER` gera convite.
- Endpoints sob `/api/v1/households/**`.

**Critérios de Aceitação:**
1. QUANDO um Usuario_Autenticado cria um Household, O SISTEMA DEVE criar o Household, um HouseholdMember com papel `OWNER` para esse User e a ShoppingList padrão com o nome "Compras".
2. QUANDO um OWNER solicita um convite, O SISTEMA DEVE gerar um token de convite aleatório criptograficamente seguro, URL-safe, com entropia adequada (~128 bits ou mais), único, associado ao Household, com expiração em 7 dias e estado não consumido.
3. QUANDO um Usuario_Autenticado envia um token de convite válido, não expirado e não consumido, O SISTEMA DEVE vinculá-lo ao Household correspondente com papel `MEMBER` e marcar o token como consumido.
4. O SISTEMA DEVE persistir apenas o hash determinístico (ex.: SHA-256) do token de convite, nunca o token em texto puro, e localizar o convite pelo hash ao consumi-lo.

**Casos de Erro:**
1. SE o token de convite não existir, ENTÃO O SISTEMA DEVE retornar status 404 com mensagem explicativa e não criar vínculo.
2. SE o token de convite estiver expirado ou já consumido, ENTÃO O SISTEMA DEVE retornar status 410 com mensagem explicativa e não criar vínculo.
3. SE um User tentar ingressar em um Household do qual já é membro, ENTÃO O SISTEMA DEVE retornar status 409 e preservar o vínculo e o papel existentes sem alterações.
4. SE um Usuario_Autenticado que já pertence a um Household tentar criar ou ingressar em um SEGUNDO Household, ENTÃO O SISTEMA DEVE retornar status 409 e preservar o vínculo existente sem alterações.
5. SE um usuário não-OWNER tentar gerar convite, ENTÃO O SISTEMA DEVE retornar status 403 e não gerar token.
6. O token bruto do convite NÃO DEVE ser registrado em logs.

**Permissões:** Criar convite exige papel `OWNER`. Ingressar exige Usuario_Autenticado com token válido.

---

### REQ-004 — Autorização e Isolamento de Dados

**Descrição:** O LumeOS deve garantir que todo acesso a dados seja filtrado pelo usuário autenticado e/ou pelo household, derivando a titularidade sempre do token da sessão.

**Motivação:** Hoje não há autenticação e as consultas usam `findAll().findFirst()`. Com múltiplos usuários na internet, o isolamento é obrigatório.

**Regras:**
- O Sistema NUNCA confia em `userId` ou `householdId` enviados pelo frontend para determinar titularidade; a identidade vem do token JWT.
- Dados pessoais (Habits, LogAtividade, GameProfile, ReadingEntry) pertencem ao User.
- Dados de compras pertencem ao Household.
- Toda consulta é filtrada por `userId` do token (dados pessoais) ou pelos Households dos quais o User é membro (dados compartilhados).

**Critérios de Aceitação:**
1. QUANDO um Usuario_Autenticado consulta dados pessoais, O SISTEMA DEVE retornar apenas registros cujo `userId` corresponde ao User do token.
2. QUANDO um Usuario_Autenticado consulta dados de compras, O SISTEMA DEVE retornar apenas itens de Households dos quais o User é membro.
3. O SISTEMA DEVE derivar a titularidade (`userId` e `householdId`) exclusivamente da identidade do JWT validado, ignorando quaisquer valores de `userId`/`householdId` recebidos em body, query string, headers ou route params.
4. QUANDO um Usuario_Autenticado consulta uma coleção pessoal ou de compras e não possui registros correspondentes, O SISTEMA DEVE retornar status 200 com uma lista vazia.

**Casos de Erro:**
1. SE um User requisitar um recurso pessoal (Habit, LogAtividade, GameProfile, ReadingEntry) de outro User, ENTÃO O SISTEMA DEVE responder de forma idêntica a um recurso inexistente (status 404), sem vazar a existência do recurso.
2. SE um User requisitar um ShoppingItem de um Household do qual não é membro, ENTÃO O SISTEMA DEVE responder de forma idêntica a um recurso inexistente (status 404), sem vazar a existência do item.
3. SE o frontend enviar um `userId`/`householdId` divergente da sessão, ENTÃO O SISTEMA DEVE ignorar o valor enviado e usar a titularidade derivada do token.
4. SE uma requisição a endpoint protegido chegar sem token, ou com token malformado ou expirado, ENTÃO O SISTEMA DEVE retornar status 401 e não executar a operação.

**Permissões:** Aplica-se a todos os endpoints de domínio protegidos.

---

### REQ-005 — Onboarding (Primeiro e Segundo Usuário)

**Descrição:** O LumeOS deve oferecer um fluxo de onboarding em que o primeiro usuário cria conta, cria o Household e (opcionalmente) cria seu perfil gamificado, e o segundo usuário cria conta, ingressa no Household por convite e (opcionalmente) cria seu próprio perfil gamificado.

**Motivação:** Substituir o seed automático de usuário falso por um fluxo real e explícito de entrada no produto.

**Regras:**
- Criar GameProfile é opcional no onboarding.
- O segundo usuário entra no Household via Convite_Household.
- No MVP, cada User pertence a no máximo um Household; o onboarding não oferece criação ou ingresso em um segundo Household (ver REQ-003).
- O onboarding não executa qualquer operação destrutiva sobre dados globais.

**Critérios de Aceitação:**
1. QUANDO um novo User conclui o registro e não pertence a nenhum Household, O SISTEMA DEVE conduzir à criação de um Household ou ao ingresso por convite.
2. QUANDO um User cria um Household durante o onboarding, O SISTEMA DEVE marcá-lo como `OWNER` desse Household.
3. QUANDO um User opta por criar perfil gamificado no onboarding, O SISTEMA DEVE criar o GameProfile vinculado a esse User (ver REQ-006).
4. QUANDO um segundo User ingressa via convite, O SISTEMA DEVE associá-lo ao Household existente como `MEMBER`.

**Casos de Erro:**
1. SE um User sem Household tentar acessar funcionalidade de compras, ENTÃO O SISTEMA DEVE direcioná-lo ao fluxo de criação/ingresso de Household.
2. SE o token de convite informado no onboarding for inválido, ENTÃO O SISTEMA DEVE exibir erro e permitir nova tentativa (ver REQ-003).

**Permissões:** Qualquer Usuario_Autenticado pode executar seu próprio onboarding.

---

### REQ-006 — Perfil Gamificado (GameProfile)

**Descrição:** O LumeOS deve permitir que cada User crie e mantenha um perfil gamificado com classe RPG, atributos e progressão (XP, nível, moedas, HP/MP), preservando as regras de progressão existentes, agora por usuário.

**Motivação:** Preservar a mecânica gamificada do DopamineOS, isolando-a por usuário autenticado.

**Regras (progressão preservada):**
- Ao criar, o GameProfile inicia em `nivel` 1, `xpAtual` 0, `xpParaProximoNivel` 100 e `moedas` 0.
- Na subida de nível: enquanto `xpAtual` ≥ `xpParaProximoNivel`, repetir a iteração: `nivel` é incrementado em 1; o excedente de XP é transportado (`xpAtual` = `xpAtual` − `xpParaProximoNivel`); o novo `xpParaProximoNivel` = parte inteira (truncada) de `xpParaProximoNivel` × 1,5. Uma única concessão de XP pode cruzar múltiplos limiares, resultando em múltiplas subidas de nível. Ao final do laço, o personagem recebe cura total (`hpAtual`=`maxHp`, `mpAtual`=`maxMp`).
- `maxHp = constituicao × 10`; `maxMp = intelecto × 10`.
- Classes disponíveis e atributos iniciais (FORCA/DESTREZA/INTELECTO/CARISMA/CONSTITUICAO): `GUERREIRO` 10/5/3/5/10; `MAGO` 3/5/10/6/4; `LADINO` 5/10/5/8/5.
- Atributos: `FORCA`, `DESTREZA`, `INTELECTO`, `CARISMA`, `CONSTITUICAO`.
- Endpoints sob `/api/v1/gamification/**`.

**Critérios de Aceitação:**
1. QUANDO um Usuario_Autenticado cria um GameProfile informando uma das classes `GUERREIRO`, `MAGO` ou `LADINO`, O SISTEMA DEVE atribuir os cinco atributos iniciais correspondentes à classe (`GUERREIRO` 10/5/3/5/10; `MAGO` 3/5/10/6/4; `LADINO` 5/10/5/8/5, na ordem FORCA/DESTREZA/INTELECTO/CARISMA/CONSTITUICAO).
2. QUANDO um Usuario_Autenticado cria um GameProfile, O SISTEMA DEVE inicializar `nivel`=1, `xpAtual`=0, `xpParaProximoNivel`=100 e `moedas`=0.
3. QUANDO um Usuario_Autenticado cria um GameProfile, O SISTEMA DEVE calcular `maxHp`=`constituicao`×10 e `maxMp`=`intelecto`×10, e definir `hpAtual`=`maxHp` e `mpAtual`=`maxMp`.
4. ENQUANTO `xpAtual` ≥ `xpParaProximoNivel`, O SISTEMA DEVE repetir a subida de nível, em cada iteração incrementando `nivel` em 1, definindo `xpAtual`=`xpAtual`−`xpParaProximoNivel` e definindo o novo `xpParaProximoNivel` como a parte inteira (truncada) de `xpParaProximoNivel`×1,5, de modo que uma única concessão de XP que cruze múltiplos limiares produza múltiplas subidas de nível.
5. QUANDO ocorre uma ou mais subidas de nível, O SISTEMA DEVE aplicar cura total (`hpAtual`=`maxHp`, `mpAtual`=`maxMp`) ao final do laço.
6. QUANDO o App_Web solicita o perfil do Usuario_Autenticado, O SISTEMA DEVE retornar os cinco atributos nomeados com seus valores numéricos.
7. QUANDO o App_Web solicita um GameProfile, O SISTEMA DEVE retornar exclusivamente o GameProfile pertencente ao Usuario_Autenticado.

**Casos de Erro:**
1. SE um Usuario_Autenticado solicitar o GameProfile de outro User, ENTÃO O SISTEMA DEVE retornar status 404 e não expor dados alheios.
2. SE for solicitada operação de personagem para um Usuario_Autenticado sem GameProfile, ENTÃO O SISTEMA DEVE retornar status 404 com mensagem indicando a necessidade de criar o perfil primeiro.

**Permissões:** Um User só lê/modifica o próprio GameProfile.

---

### REQ-007 — Hábitos / Missões Gamificadas

**Descrição:** O LumeOS deve preservar todas as capacidades de hábitos/missões do DopamineOS (criar, listar, remover, concluir via check-in com concessão de XP e moedas e registro de histórico), agora por usuário autenticado, e deve renomear o conceito `Protocolo` para `Habit` com mapeamento documentado.

**Motivação:** Preservar a funcionalidade central do produto, isolando-a por usuário e removendo comportamentos single-player e destrutivos.

**Regras:**
- Mapeamento de domínio: `Protocolo` → `Habit`. O documento de design deve registrar o mapeamento de campos e endpoints.
- Um CheckIn concede XP e moedas conforme a `Dificuldade` do Habit ao GameProfile do próprio User e gera um LogAtividade.
- Tabela de recompensa por `Dificuldade` (valores fixos, apenas XP e moedas; não há recompensa em "pontos"): EASY = 10 XP e 2 moedas; MEDIUM = 30 XP e 5 moedas; HARD = 60 XP e 15 moedas; EPIC = 100 XP e 50 moedas.
- O LogAtividade é um snapshot da ação realizada (preserva `nomeMissao`, `icone`, `xpGanho`, `duracaoMinutos`, `dataHora`) e mantém um `habitId` OPCIONAL. A remoção de um Habit NÃO apaga os LogAtividade já registrados; a referência `habitId` é anulável e NÃO provoca exclusão em cascata do histórico.
- A operação de reset/configuração NÃO deve apagar dados globais nem dados de outros usuários (substitui o `/setup` destrutivo atual).
- Endpoints de hábitos sob `/api/v1/habits/**` e endpoints de progressão sob `/api/v1/gamification/**`.

**Critérios de Aceitação:**
1. QUANDO um Usuario_Autenticado cria um Habit com dados válidos, O SISTEMA DEVE persistir o Habit associado ao `userId` extraído do token e retornar status 201.
2. QUANDO um Usuario_Autenticado lista Habits, O SISTEMA DEVE retornar exclusivamente os Habits cujo `userId` corresponde ao do token.
3. QUANDO um Usuario_Autenticado remove um Habit próprio, O SISTEMA DEVE removê-lo de forma permanente e retornar status 204 sem corpo.
4. QUANDO um Usuario_Autenticado remove um Habit, O SISTEMA DEVE preservar os LogAtividade já registrados daquele Habit (sem exclusão em cascata do histórico).
5. QUANDO um Usuario_Autenticado faz check-in de um Habit próprio, O SISTEMA DEVE conceder ao seu GameProfile o XP e as moedas exatos definidos pela `Dificuldade` (EASY 10/2; MEDIUM 30/5; HARD 60/15; EPIC 100/50), criar um LogAtividade associado ao `userId` do token e retornar status 200.
6. QUANDO um Usuario_Autenticado consulta o histórico, O SISTEMA DEVE retornar exclusivamente os LogAtividade cujo `userId` corresponde ao do token.

**Casos de Erro:**
1. SE um User tentar ler, remover ou fazer check-in de um Habit cujo `userId` não corresponde ao do seu token, ENTÃO O SISTEMA DEVE recusar a operação, não alterar dados e retornar status 404.
2. SE um check-in for solicitado por User sem GameProfile, ENTÃO O SISTEMA DEVE recusar o check-in, não conceder XP/moedas, não criar LogAtividade e retornar status 404 com mensagem indicando que o GameProfile precisa ser criado.
3. SE um Habit for criado sem `nome` ou `dificuldade`, ou com `nome` vazio ou acima de 100 caracteres, ENTÃO O SISTEMA DEVE rejeitar a criação e retornar status 400 indicando os campos inválidos.
4. SE uma operação de reset/configuração for invocada, ENTÃO O SISTEMA DEVE afetar exclusivamente os dados cujo `userId` corresponde ao do token, nunca dados globais ou de terceiros.

**Permissões:** Nenhum User pode visualizar ou modificar Habits, histórico ou GameProfile de outro User.

---

### REQ-008 — Lista de Compras Compartilhada

**Descrição:** O LumeOS deve oferecer uma lista de compras compartilhada por Household, em que todos os membros veem e manipulam os mesmos itens (adicionar, editar, marcar comprado, desmarcar comprado, excluir).

**Motivação:** Centralizar as compras da casa com visão compartilhada entre os membros do Household.

**Regras:**
- A modelagem adota explicitamente Household → ShoppingList → ShoppingItem, mesmo que o MVP tenha apenas UMA ShoppingList ativa por Household.
- A ShoppingList padrão ("Compras") é criada automaticamente quando o Household é criado (ver REQ-003). A estrutura deixa espaço para múltiplas listas no futuro sem alterar a UX atual.
- `ShoppingItem`: `id`, `shoppingListId`, `nome`, `quantidade`, `unidade` (opcional), `categoria` (opcional), `comprado`, `createdBy`, `completedBy` (opcional), `createdAt`, `updatedAt`.
- A associação ao Household é DERIVADA através da ShoppingList (`ShoppingItem.shoppingListId` → `ShoppingList.householdId`); o ShoppingItem NÃO armazena `householdId` próprio, evitando redundância que permita inconsistência.
- `quantidade` é um valor decimal positivo (> 0) que suporta frações (ex.: 1 unidade, 0,5 kg, 1,5 L), persistido como `BigDecimal` no backend e `NUMERIC` no PostgreSQL.
- Ordenação sugerida: itens pendentes primeiro, depois comprados.
- As alterações persistem imediatamente no servidor e são refletidas corretamente após recarregar.
- Realtime/WebSocket NÃO é exigido no MVP, mas a modelagem deve deixar espaço para evolução.
- Endpoints sob `/api/v1/shopping/**`.

**Critérios de Aceitação:**
1. QUANDO um membro do Household adiciona um item, O SISTEMA DEVE persistir o item na ShoppingList do Household do Usuario_Autenticado (derivado do seu único HouseholdMember), com `createdBy` igual ao User do token.
2. QUANDO qualquer membro do mesmo Household lista os itens, O SISTEMA DEVE retornar os mesmos itens compartilhados.
3. QUANDO um membro marca um item como comprado, O SISTEMA DEVE definir `comprado`=true, registrar `completedBy` com o User do token e atualizar `updatedAt`.
4. QUANDO um membro desmarca um item, O SISTEMA DEVE definir `comprado`=false, limpar `completedBy` e atualizar `updatedAt`.
5. QUANDO um membro edita ou exclui um item do Household, O SISTEMA DEVE persistir a alteração imediatamente e, nas edições, atualizar `updatedAt`.
6. QUANDO a lista é retornada, O SISTEMA DEVE ordenar itens pendentes antes dos comprados.

**Casos de Erro:**
1. SE um User de outro Household tentar ler ou modificar um ShoppingItem, ENTÃO O SISTEMA DEVE retornar status 404, sem vazar a existência do item.
2. SE um item for criado com `nome` vazio ou acima de 120 caracteres, ENTÃO O SISTEMA DEVE retornar status 400.
3. SE `quantidade` for informada como valor decimal não positivo (≤ 0), ENTÃO O SISTEMA DEVE retornar status 400.
4. SE `unidade` ou `categoria` for informada acima de 40 caracteres, ENTÃO O SISTEMA DEVE retornar status 400.

**Permissões:** Qualquer membro do Household pode criar, editar, marcar/desmarcar e excluir itens do próprio Household. Usuários de outros Households nunca acessam esses itens.

---

### REQ-009 — Lista de Leituras

**Descrição:** O LumeOS deve oferecer uma biblioteca pessoal de leituras por usuário, com adição, edição, exclusão, mudança de status, registro de início e conclusão, avaliação e visualização por status.

**Motivação:** Permitir que cada usuário organize seus livros de forma privada e independente do RPG e das compras.

**Regras:**
- `ReadingEntry`: `id`, `userId`, `titulo`, `autor`, `status`, `nota`, `dataInicio`, `dataFim`, `observacoes`, `createdAt`, `updatedAt`.
- `status` ∈ `{QUERO_LER, LENDO, CONCLUIDO, ABANDONADO}`.
- `nota` é opcional; quando presente, deve estar no intervalo 1..5.
- `dataFim` não pode ser anterior a `dataInicio`.
- QUANDO o status muda para `LENDO` e `dataInicio` está vazio/não informado, o Sistema define `dataInicio` = data atual.
- QUANDO o status muda para `CONCLUIDO` e `dataFim` está vazio/não informado, o Sistema define `dataFim` = data atual.
- O usuário pode editar `dataInicio` e `dataFim` posteriormente.
- O dado pertence exclusivamente ao User autenticado.
- Endpoints sob `/api/v1/library/**`.

**Critérios de Aceitação:**
1. QUANDO um Usuario_Autenticado adiciona um livro com `titulo` de 1 a 200 caracteres (não vazio e não composto apenas por espaços), O SISTEMA DEVE persistir a ReadingEntry com `userId` do token e `status` inicial informado (padrão `QUERO_LER`).
2. QUANDO um Usuario_Autenticado altera o status para `LENDO` sem informar `dataInicio`, O SISTEMA DEVE definir `dataInicio` com a data atual; QUANDO `dataInicio` for informada, O SISTEMA DEVE registrá-la, permitindo edição posterior.
3. QUANDO um Usuario_Autenticado altera o status para `CONCLUIDO` sem informar `dataFim`, O SISTEMA DEVE definir `dataFim` com a data atual; QUANDO `dataFim` for informada, O SISTEMA DEVE registrá-la, permitindo edição posterior, respeitando `dataFim` ≥ `dataInicio`.
4. QUANDO um Usuario_Autenticado atribui `nota` entre 1 e 5, O SISTEMA DEVE persistir a nota.
5. QUANDO um Usuario_Autenticado filtra por status, O SISTEMA DEVE retornar apenas as entradas próprias com o status solicitado.
6. QUANDO um Usuario_Autenticado edita ou exclui uma entrada própria, O SISTEMA DEVE persistir a alteração e, nas edições, atualizar `updatedAt`.

**Casos de Erro:**
1. SE `nota` for informada fora do intervalo 1..5, ENTÃO O SISTEMA DEVE retornar status 400.
2. SE `dataFim` for anterior a `dataInicio`, ENTÃO O SISTEMA DEVE retornar status 400.
3. SE um User tentar ler ou modificar uma ReadingEntry de outro User, ENTÃO O SISTEMA DEVE retornar status 404, sem vazar a existência da entrada.
4. SE uma entrada for criada com `titulo` vazio, composto apenas por espaços ou acima de 200 caracteres, ENTÃO O SISTEMA DEVE retornar status 400.
5. SE `status` for informado com valor fora do enum `{QUERO_LER, LENDO, CONCLUIDO, ABANDONADO}`, ENTÃO O SISTEMA DEVE retornar status 400.
6. SE `autor` exceder 120 caracteres ou `observacoes` exceder 2000 caracteres, ENTÃO O SISTEMA DEVE retornar status 400.

**Permissões:** Um User só lê/modifica as próprias ReadingEntry.

---

### REQ-010 — Home / Dashboard

**Descrição:** O LumeOS deve apresentar uma Home que conecta as funcionalidades, exibindo nível/XP, hábitos cadastrados/ativos do usuário, quantidade de compras pendentes, livro em leitura e atalhos — de forma simples, sem ser um quarto módulo.

**Motivação:** Dar uma visão unificada e atalhos, mantendo a simplicidade.

**Regras:**
- A Home agrega dados já existentes dos demais módulos; não introduz novo domínio de dados.
- Todos os dados exibidos respeitam o isolamento por usuário/household (REQ-004).

**Critérios de Aceitação:**
1. QUANDO um Usuario_Autenticado abre a Home, O SISTEMA DEVE exibir nível e XP do seu GameProfile (quando existir).
2. QUANDO um Usuario_Autenticado abre a Home, O SISTEMA DEVE exibir a quantidade de itens de compra pendentes do Household do Usuario_Autenticado (derivado do seu único HouseholdMember).
3. QUANDO um Usuario_Autenticado abre a Home, O SISTEMA DEVE exibir os hábitos cadastrados/ativos do próprio User (o Habit do MVP não possui recorrência, agendamento ou dia-da-semana).
4. QUANDO um Usuario_Autenticado abre a Home, O SISTEMA DEVE exibir o(s) livro(s) com status `LENDO` do próprio User.
5. QUANDO um Usuario_Autenticado abre a Home, O SISTEMA DEVE exibir atalhos de navegação para Rotina, Compras e Leituras.

**Casos de Erro:**
1. SE o User não possuir GameProfile, ENTÃO O SISTEMA DEVE exibir a Home sem a seção de nível/XP, oferecendo atalho para criar o perfil.
2. SE o User não possuir Household, ENTÃO O SISTEMA DEVE exibir a Home sem o contador de compras, oferecendo atalho para criar/ingressar em um Household.

**Permissões:** Qualquer Usuario_Autenticado vê sua própria Home.

---

### REQ-011 — Navegação do Frontend (LumeOS)

**Descrição:** O App_Web deve oferecer navegação com as seções Início, Rotina, Compras, Leituras e Configurações, mantendo a gamificação dentro de Rotina e sem terminologia artificial de RPG em Compras e Leituras.

**Motivação:** Alinhar a navegação ao novo produto unificado e evitar vazamento conceitual do RPG para módulos que não são de hábitos.

**Regras:**
- Seções de navegação: Início, Rotina (contém a gamificação), Compras, Leituras, Configurações.
- Compras e Leituras usam linguagem de domínio neutra (sem XP, missões, classes etc.).
- A navegação é responsiva para desktop e mobile (ver RNF-01).
- Escopo mínimo da seção Configurações no MVP: visualizar os dados do próprio User; visualizar informações do Household; (apenas OWNER) gerar convite; visualizar o GameProfile quando existir ou acessar sua criação; executar logout. NENHUM novo domínio de dados é criado exclusivamente para Configurações; a seção agrega dados já existentes dos demais domínios.

**Critérios de Aceitação:**
1. O SISTEMA DEVE apresentar as cinco seções de navegação (Início, Rotina, Compras, Leituras, Configurações).
2. QUANDO o usuário acessa Rotina, O SISTEMA DEVE expor as funcionalidades gamificadas (hábitos, perfil, histórico).
3. QUANDO o usuário acessa Compras ou Leituras, O SISTEMA DEVE usar exclusivamente terminologia de domínio neutra, sem termos de RPG.
4. QUANDO um Usuario_Autenticado acessa Configurações, O SISTEMA DEVE permitir visualizar os dados do próprio User, visualizar informações do seu Household, visualizar o GameProfile quando existir (ou acessar sua criação) e executar logout, sem criar novo domínio de dados.
5. QUANDO um Usuario_Autenticado com papel `OWNER` acessa Configurações, O SISTEMA DEVE permitir a geração de convite do Household (ver REQ-003).

**Casos de Erro:**
1. SE um usuário não autenticado tentar acessar qualquer seção protegida, ENTÃO O SISTEMA DEVE redirecioná-lo à tela de login (ver REQ-018).
2. SE um usuário não-OWNER tentar gerar convite a partir de Configurações, ENTÃO O SISTEMA DEVE retornar status 403 e não gerar token (ver REQ-003).

**Permissões:** As seções protegidas exigem Usuario_Autenticado.

---

### REQ-012 — Migração de Dados Legados

**Descrição:** O LumeOS não deve apagar silenciosamente os dados atuais; deve adotar uma estratégia explícita para o modelo legado (`tb_usuario`, `tb_protocolos`, `tb_log_atividade`), lidando com a limitação de o usuário legado não possuir e-mail/senha.

**Motivação:** O comportamento atual de `/setup` apaga tudo e o `GameLoader` cria usuário falso; é necessário preservar o comportamento funcional e os dados sem perda silenciosa.

**Regras:**
- Estratégias aceitáveis: "claim" do perfil legado pelo primeiro User criado; migração intermediária; ou procedimento de migração documentado.
- Não construir um sistema complexo apenas para migração.
- Se o banco atual for considerado de desenvolvimento e a migração automática não for segura, documentar claramente: por quê, como exportar, como reinicializar e como preservar o comportamento funcional.
- O comportamento de "apagar tudo no `/setup`" NUNCA é mantido.

**Critérios de Aceitação:**
1. O SISTEMA DEVE preservar os dados legados existentes até que uma estratégia de migração explícita seja executada (nenhuma exclusão automática na inicialização).
2. QUANDO a estratégia "claim" for adotada, O SISTEMA DEVE permitir que o primeiro User criado associe a si o GameProfile e os Habits derivados do personagem legado.
3. O SISTEMA DEVE registrar em documentação o procedimento de migração escolhido (passos de export, reinicialização e preservação funcional).

**Casos de Erro:**
1. SE a migração automática for considerada insegura, ENTÃO O SISTEMA DEVE abster-se de migrar automaticamente e exigir o procedimento documentado.
2. SE um "claim" for tentado sobre dados legados já reivindicados, ENTÃO O SISTEMA DEVE retornar status 409.

**Permissões:** O procedimento de migração é operacional/administrativo; o "claim" exige Usuario_Autenticado.

---

### REQ-013 — Estrutura de API Orientada a Domínio

**Descrição:** A API_LumeOS deve evoluir de `/api/v1/game/**` para APIs orientadas a domínio, usando REST pragmático, DTOs de request/response e respostas de erro padronizadas, sem retornar entidades JPA diretamente.

**Motivação:** O prefixo único `/api/v1/game` não acomoda os novos domínios; é preciso organizar por área funcional.

**Regras:**
- Prefixos: `/api/v1/auth/**`, `/api/v1/users/**`, `/api/v1/households/**`, `/api/v1/habits/**`, `/api/v1/gamification/**`, `/api/v1/shopping/**`, `/api/v1/library/**`.
- Entidades JPA nunca são retornadas diretamente; usam-se DTOs (MapStruct), separando request/response quando necessário.
- Respostas de erro seguem um formato padronizado consistente (ex.: `timestamp`, `status`, `error`, `message`, `path`).

**Critérios de Aceitação:**
1. O SISTEMA DEVE expor cada domínio sob seu respectivo prefixo de API listado nas regras.
2. QUANDO qualquer endpoint retorna dados, O SISTEMA DEVE retornar DTOs e nunca entidades JPA.
3. QUANDO ocorre um erro tratado, O SISTEMA DEVE retornar o corpo de erro no formato padronizado.

**Casos de Erro:**
1. SE um recurso não existir, ENTÃO O SISTEMA DEVE retornar status 404 com o corpo de erro padronizado.
2. SE ocorrer uma falha não tratada, ENTÃO O SISTEMA DEVE retornar status 500 com o corpo de erro padronizado e, em produção, sem stack trace exposta ao cliente (ver REQ-015).

**Permissões:** Conforme cada domínio; todos os endpoints de domínio exigem autenticação, exceto `register`/`login`.

---

### REQ-014 — Migrações de Banco com Flyway

**Descrição:** O LumeOS deve introduzir migrações versionadas com Flyway, deixando de depender de `ddl-auto=update`, com `ddl-auto=validate` (ou equivalente seguro) no alvo de produção, e aplicar constraints e índices adequados.

**Motivação:** O `ddl-auto=update` atual é inseguro para produção e não versiona mudanças estruturais.

**Regras:**
- Todas as mudanças estruturais do schema ocorrem via migrações Flyway versionadas.
- Em produção, `spring.jpa.hibernate.ddl-auto` é `validate` (ou equivalente seguro).
- Adicionar constraints (chaves estrangeiras, unicidade de e-mail, não-nulos) e índices adequados.
- Os nomes das tabelas são explícitos e inequívocos (ex.: entidade `User` → tabela `users`; e também `game_profiles`, `habits`, `activity_logs`, `households`, `household_members`, `shopping_lists`, `shopping_items`, `reading_entries`).
- A FK de `LogAtividade` (tabela `activity_logs`) para `Habit` (tabela `habits`) via `habitId` é anulável (nullable) e NÃO provoca exclusão em cascata; a FK obrigatória de `LogAtividade` é para `User` via `userId`. Assim, remover um Habit preserva o histórico (ver REQ-007).

**Critérios de Aceitação:**
1. O SISTEMA DEVE aplicar o schema por meio de migrações Flyway versionadas na inicialização.
2. QUANDO a aplicação inicia em produção, O SISTEMA DEVE operar com `ddl-auto=validate` (ou equivalente seguro), sem alterar o schema automaticamente.
3. O SISTEMA DEVE definir unicidade de `email` em `User` e as chaves estrangeiras `User`→`GameProfile`, `User`→`Habit`, `User`→`LogAtividade` (com `habitId` opcional/anulável e sem exclusão em cascata), `User`→`HouseholdMember`→`Household`, `Household`→`ShoppingList`, `ShoppingList`→`ShoppingItem` (via `shoppingListId`) e `User`→`ReadingEntry`, de modo que `ShoppingItem` referencie `ShoppingList` e NÃO referencie `Household` diretamente, e a FK `LogAtividade`→`Habit` seja anulável e sem exclusão em cascata.
4. O SISTEMA DEVE usar nomes de tabela explícitos e inequívocos (ex.: `User` → `users`; `game_profiles`, `habits`, `activity_logs`, `households`, `household_members`, `shopping_lists`, `shopping_items`, `reading_entries`).

**Casos de Erro:**
1. SE o schema do banco divergir das migrações esperadas em produção, ENTÃO O SISTEMA DEVE falhar na inicialização (validação) em vez de alterar o schema silenciosamente.
2. SE uma migração falhar, ENTÃO O SISTEMA DEVE interromper a inicialização e registrar o erro.

**Permissões:** Operação de infraestrutura/inicialização.

---

### REQ-015 — Configuração por Ambiente e Segredos

**Descrição:** O LumeOS deve separar configuração de desenvolvimento e produção e obter segredos por variáveis de ambiente, sem credenciais versionadas.

**Motivação:** Hoje as credenciais do banco estão fixas em `application.properties` e `show-sql`/stacktrace estão sempre ligados; isso é inadequado para produção.

**Regras:**
- O backend aceita variáveis de ambiente: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `SPRING_PROFILES_ACTIVE`, entre outras.
- Nenhum segredo (senha de banco, segredo JWT) é commitado no repositório.
- Configurações de desenvolvimento e produção são separadas (perfis Spring).
- Em produção, `server.error.include-stacktrace` não expõe stack trace ao cliente.

**Critérios de Aceitação:**
1. QUANDO a aplicação inicia, O SISTEMA DEVE ler host, porta, nome, usuário e senha do banco a partir de variáveis de ambiente.
2. O SISTEMA DEVE ler o segredo JWT de variável de ambiente (`JWT_SECRET`).
3. O SISTEMA DEVE selecionar comportamento de desenvolvimento ou produção conforme `SPRING_PROFILES_ACTIVE`.
4. Em produção, O SISTEMA DEVE omitir a stack trace do corpo de resposta de erro.

**Casos de Erro:**
1. SE `JWT_SECRET` não estiver definido em produção, ENTÃO O SISTEMA DEVE falhar na inicialização com mensagem clara, em vez de usar um valor padrão inseguro.
2. SE as variáveis de banco estiverem ausentes em produção, ENTÃO O SISTEMA DEVE falhar na inicialização com mensagem clara.

**Permissões:** Operação de infraestrutura/inicialização.

---

### REQ-016 — CORS e Mesma Origem

**Descrição:** O LumeOS deve operar preferencialmente em mesma origem via Reverse_Proxy, com configuração de CORS adequada a cada ambiente, substituindo a configuração atual fixa em `http://localhost:4200`.

**Motivação:** CORS fixo em localhost e URL de API fixa impedem produção; mesma origem simplifica auth/cookies/CORS/deploy.

**Regras:**
- Em produção, App_Web e API_LumeOS são servidos sob a mesma origem pelo Reverse_Proxy (`/` → web, `/api` → api).
- Em desenvolvimento, o proxy do Angular encaminha `/api` → `localhost:8080`.
- A origem permitida por CORS é configurável por ambiente, não fixa em código.
- `allowCredentials` permanece compatível com o uso de cookie de sessão.

**Critérios de Aceitação:**
1. QUANDO a aplicação roda em produção atrás do Reverse_Proxy, O SISTEMA DEVE atender requisições de mesma origem sem depender de CORS para o fluxo principal.
2. O SISTEMA DEVE obter as origens permitidas de CORS a partir de configuração por ambiente.
3. QUANDO em desenvolvimento, O App_Web DEVE acessar a API por caminho relativo `/api/...`, encaminhado via proxy do Angular para `localhost:8080`.

**Casos de Erro:**
1. SE uma origem não permitida realizar requisição cross-origin em produção, ENTÃO O SISTEMA DEVE rejeitá-la conforme a política de CORS configurada.

**Permissões:** Operação de infraestrutura/configuração.

---

### REQ-017 — Estrutura do Frontend Orientada a Features e API Relativa

**Descrição:** O App_Web deve ser refatorado para uma estrutura orientada a features, remover a URL de API fixa (usando caminho relativo `/api/...`) e introduzir AuthService, guards de rota e interceptor HTTP quando necessário.

**Motivação:** A estrutura atual é plana com `apiUrl` fixo em `http://localhost:8080/api/v1/game`; isso impede produção e dificulta manutenção.

**Regras:**
- Estrutura alvo: `core/{auth,guards,http,config}`, `layout/{shell,sidebar}`, `features/{home,habits,shopping,library}`, `shared/{components,models,utils}`.
- Avaliar a estrutura existente antes de mover arquivos; não mover sem necessidade.
- Remover `apiUrl` fixo; chamar `/api/...` (relativo).
- Introduzir `AuthService`, guards de rota e interceptor HTTP (por ex., para anexar/renovar sessão e tratar 401) quando necessário; serviços por feature.

**Critérios de Aceitação:**
1. O App_Web DEVE realizar todas as chamadas de API usando caminho relativo `/api/...`, sem URL absoluta fixa.
2. O App_Web DEVE organizar o código nas camadas `core`, `layout`, `features` e `shared`.
3. QUANDO o backend responde 401 a uma chamada protegida, O App_Web DEVE conduzir o usuário ao fluxo de login.

**Casos de Erro:**
1. SE uma requisição falhar por rede ou erro de servidor, ENTÃO o App_Web DEVE exibir mensagem de erro ao usuário sem travar a navegação.

**Permissões:** N/A (frontend).

---

### REQ-018 — Guards de Rota e Sessão no Frontend

**Descrição:** O App_Web deve proteger rotas que exigem autenticação por meio de guards e refletir corretamente o estado de sessão, substituindo o logout meramente visual atual.

**Motivação:** Hoje não há guards nem interceptor e o logout é apenas visual; isso é inseguro.

**Regras:**
- Rotas protegidas (Início, Rotina, Compras, Leituras, Configurações) exigem sessão válida.
- O estado de autenticação do frontend é derivado de `GET /api/v1/auth/me` ou equivalente, não de dado local manipulável.
- Logout no frontend aciona `POST /api/v1/auth/logout` e limpa o estado local.

**Critérios de Aceitação:**
1. QUANDO um usuário não autenticado tenta acessar uma rota protegida, O App_Web DEVE redirecioná-lo para a tela de login.
2. QUANDO a sessão é válida, O App_Web DEVE permitir o acesso às rotas protegidas.
3. QUANDO o usuário faz logout, O App_Web DEVE chamar o endpoint de logout e, em seguida, impedir acesso às rotas protegidas sem novo login.

**Casos de Erro:**
1. SE o endpoint de verificação de sessão retornar 401, ENTÃO o App_Web DEVE tratar o usuário como não autenticado.

**Permissões:** N/A (frontend).

---

### REQ-019 — PWA Instalável

**Descrição:** O LumeOS deve ser um PWA instalável, com manifesto e service worker, nome e ícone "LumeOS", permitindo instalação na tela inicial, sem cachear respostas privadas de API.

**Motivação:** Oferecer experiência instalável; porém sem comprometer a segurança dos dados privados.

**Regras:**
- Manifesto com nome "LumeOS" e ícone correspondente.
- Service worker cacheia shell/CSS/JS/assets estáticos.
- O service worker NÃO deve cachear respostas privadas de API.
- Não é objetivo oferecer offline-first completo; mutações continuam exigindo conexão.

**Critérios de Aceitação:**
1. O App_Web DEVE fornecer um manifesto válido e um service worker e DEVE ser instalável nos navegadores que oferecem suporte a instalação; onde o navegador disponibiliza prompt de instalação (ex.: Android/Chrome), o App_Web DEVE ser instalável por esse meio, e no iOS o App_Web DEVE orientar o usuário a usar "Adicionar à Tela de Início", sem prometer prompt automático em plataformas que não o oferecem.
2. O App_Web DEVE exibir nome "LumeOS" e ícone próprio ao ser instalado.
3. O service worker DEVE cachear apenas shell e assets estáticos, excluindo respostas de endpoints de API privados.

**Casos de Erro:**
1. SE não houver conexão, ENTÃO o App_Web DEVE permitir carregar o shell em cache, mas as operações de mutação DEVEM indicar necessidade de conexão.
2. SE o navegador não suportar instalação de PWA, ENTÃO o App_Web DEVE continuar funcional como aplicação web normal.

**Permissões:** N/A (frontend/PWA).

---

### REQ-020 — Containerização com Docker

**Descrição:** O LumeOS deve ser empacotado em containers Docker com builds multi-stage, orquestrados por Docker Compose, com containers `lumeos-web`, `lumeos-api` e `lumeos-db` em rede interna.

**Motivação:** Padronizar build e deploy reprodutível no VPS.

**Regras:**
- Build multi-stage: backend Maven → JRE; frontend build Node → Nginx estático.
- Containers: `lumeos-web`, `lumeos-api`, `lumeos-db`.
- Rede interna Docker; persistência do banco via volume.
- Compose: `docker-compose.yml` (dev quando útil) e/ou `docker-compose.prod.yml`.
- Não usar `ng serve` nem servidor de desenvolvimento Maven em produção.

**Critérios de Aceitação:**
1. O SISTEMA DEVE prover Dockerfiles multi-stage para backend (Maven→JRE) e frontend (Node build→Nginx).
2. O SISTEMA DEVE definir os serviços `lumeos-web`, `lumeos-api` e `lumeos-db` em arquivo(s) Compose.
3. O SISTEMA DEVE persistir os dados do PostgreSQL em volume Docker.
4. Os containers DEVEM se comunicar por rede interna Docker.

**Casos de Erro:**
1. SE um container de dependência (ex.: `lumeos-db`) não estiver saudável, ENTÃO o `lumeos-api` DEVE aguardar/retentar conforme healthcheck antes de considerar-se pronto (ver REQ-025).

**Permissões:** Operação de infraestrutura.

---

### REQ-021 — Hospedagem em VPS, Reverse Proxy e HTTPS

**Descrição:** O LumeOS deve ser hospedado em um VPS Linux que poderá HOSPEDAR OUTROS PROJETOS além do LumeOS no futuro. As portas públicas 80/443 pertencem a um reverse proxy/gateway CENTRAL da VPS, que roteia os domínios para os projetos hospedados (ex.: `lume.dominio` → `lumeos-web`). O tráfego é servido via HTTPS com certificado Let's Encrypt, expondo publicamente apenas as portas 80/443.

**Motivação:** Disponibilizar o produto na internet de forma segura e simples, sem impedir que a VPS hospede outros projetos.

**Regras:**
- Fluxo conceitual: Internet → Reverse Proxy central da VPS → (`lume.dominio` → `lumeos-web`; outros projetos no futuro).
- A stack do LumeOS é composta por `lumeos-web`, `lumeos-api` e `lumeos-db`. Nenhum desses serviços deve disputar diretamente as portas 80/443 com projetos futuros.
- `lumeos-api` e `lumeos-db` permanecem na rede interna; o banco NUNCA é publicado na internet.
- TLS via Let's Encrypt no reverse proxy central.
- Portas públicas apenas 80/443 (no gateway central); a porta 5432 NÃO é publicada; a 8080 não é pública (fica atrás do reverse proxy).
- O PostgreSQL não é exposto à internet (apenas rede interna Docker).
- O documento de design DEVE escolher e justificar a estratégia exata de reverse proxy, EVITANDO duas camadas Nginx redundantes sem justificativa.

**Critérios de Aceitação:**
1. O SISTEMA DEVE rotear o domínio do LumeOS para o `lumeos-web` por meio do reverse proxy central da VPS, encaminhando `/api` para a `lumeos-api`.
2. O SISTEMA DEVE servir o tráfego via HTTPS com certificado Let's Encrypt.
3. O SISTEMA DEVE expor publicamente apenas as portas 80 e 443 no gateway central da VPS.
4. O SISTEMA DEVE manter PostgreSQL (5432) e backend (8080) inacessíveis diretamente pela internet.
5. O SISTEMA DEVE permitir que a VPS hospede outros projetos sem que os serviços do LumeOS disputem diretamente as portas 80/443.

**Casos de Erro:**
1. SE uma requisição chegar via HTTP, ENTÃO o reverse proxy central DEVE redirecioná-la para HTTPS.
2. SE houver tentativa de conexão externa à porta 5432, ENTÃO ela DEVE ser recusada (porta não publicada / firewall).

**Permissões:** Operação de infraestrutura.

---

### REQ-022 — Backup e Restauração do Banco

**Descrição:** O LumeOS deve prover backup agendado do PostgreSQL via `pg_dump` com política de retenção e procedimento de restauração documentado.

**Motivação:** Garantir recuperabilidade dos dados; evitar perda em caso de falha do VPS.

**Regras:**
- Backup via `pg_dump` agendado.
- Retenção, por exemplo, últimos 7 diários + semanais.
- Procedimento de restauração documentado.
- Backup externo (fora do VPS) é recomendação futura.

**Critérios de Aceitação:**
1. O SISTEMA DEVE executar backup do banco de forma agendada via `pg_dump`.
2. O SISTEMA DEVE reter os backups conforme a política definida (ex.: 7 diários + semanais).
3. A documentação DEVE descrever o procedimento de restauração a partir de um backup.

**Casos de Erro:**
1. SE um backup agendado falhar, ENTÃO o SISTEMA DEVE registrar o erro em log para diagnóstico.

**Permissões:** Operação de infraestrutura.

---

### REQ-023 — CI/CD com GitHub Actions

**Descrição:** O LumeOS deve ter um pipeline mínimo de CI/CD em GitHub Actions que, em push/merge para `main`, execute testes de backend e frontend, builds, construa imagens Docker e faça deploy no VPS, sem armazenar credenciais do VPS no repositório.

**Motivação:** Automatizar validação e entrega de forma simples e segura.

**Regras:**
- Em push/merge para `main`: testes de backend, testes de frontend, build de frontend, build de backend, imagens Docker, deploy no VPS.
- Preferir GitHub Actions → GHCR → VPS `docker compose pull && up -d`.
- Credenciais do VPS nunca no repositório; usar GitHub Secrets.

**Critérios de Aceitação:**
1. QUANDO há push/merge em `main`, O pipeline DEVE executar testes de backend e frontend.
2. QUANDO os testes passam, O pipeline DEVE construir os artefatos e as imagens Docker.
3. O pipeline DEVE realizar o deploy no VPS (preferencialmente via GHCR e `docker compose pull && up -d`).
4. O pipeline DEVE obter credenciais sensíveis exclusivamente de GitHub Secrets.

**Casos de Erro:**
1. SE qualquer etapa de teste falhar, ENTÃO o pipeline DEVE interromper e não realizar o deploy.
2. SE o deploy falhar, ENTÃO a documentação DEVE descrever um procedimento básico de rollback (ver REQ-024).

**Permissões:** Operação de infraestrutura/CI.

---

### REQ-024 — Documentação de Deploy no VPS

**Descrição:** O LumeOS deve documentar o deploy no VPS em `docs/DEPLOY_VPS.md`, cobrindo criação do servidor, usuário de deploy, acesso por chave SSH, firewall UFW, Docker/Compose, domínio/DNS, HTTPS, variáveis de ambiente, deploy inicial, atualização, rollback básico, logs, backup e restore.

**Motivação:** Garantir que o deploy e a operação sejam reproduzíveis e seguros.

**Regras:**
- Sem login por senha de root; acesso por chave SSH e usuário de deploy.
- Banco acessível apenas na rede interna Docker.
- O documento cobre: criação do servidor, usuário de deploy, chave SSH, UFW, Docker/Compose, domínio/DNS, HTTPS, variáveis de ambiente, deploy inicial, atualização, rollback básico, logs, backup e restore.

**Critérios de Aceitação:**
1. O documento `docs/DEPLOY_VPS.md` DEVE existir e cobrir todos os tópicos listados nas regras.
2. A documentação DEVE instruir desabilitar login por senha de root e usar chave SSH.
3. A documentação DEVE instruir manter o banco apenas na rede interna Docker.

**Casos de Erro:**
1. SE um passo de deploy exigir segredo, ENTÃO a documentação DEVE orientar o uso de variáveis de ambiente/`.env` não versionado, nunca valores no repositório.

**Permissões:** Documentação/operacional.

---

### REQ-025 — Observabilidade do MVP

**Descrição:** O LumeOS deve prover observabilidade mínima via Spring Boot Actuator com `/actuator/health`, logs de container e healthchecks Docker, sem introduzir stack pesada.

**Motivação:** Permitir diagnóstico básico de saúde e operação sem complexidade excessiva.

**Regras:**
- Expor `/actuator/health`.
- Logs disponíveis via container.
- Healthchecks Docker nos serviços.
- Não introduzir observabilidade pesada no MVP.

**Critérios de Aceitação:**
1. O SISTEMA DEVE expor `/actuator/health` indicando o estado da aplicação.
2. Os serviços Docker DEVEM declarar healthchecks.
3. Os logs dos containers DEVEM estar acessíveis para diagnóstico.

**Casos de Erro:**
1. SE a aplicação estiver indisponível ou degradada, ENTÃO `/actuator/health` DEVE refletir estado não saudável.

**Permissões:** Operação de infraestrutura.

---

### REQ-026 — Migração de Identidade e Marca (DopamineOS → LumeOS)

**Descrição:** O LumeOS deve migrar a identidade e a marca de DopamineOS para LumeOS em README, títulos do Angular, textos, sidebar, pacote Java (`com.lumeos.backend`), `spring.application.name` (`lumeos-backend`), `artifactId`/nome Maven quando apropriado, docs, nomes de containers e configuração de deploy, preservando o histórico Git e refatorando o projeto existente.

**Motivação:** O produto passa a se chamar LumeOS; a marca DopamineOS/RPG está espalhada por todo o código, textos e configs.

**Regras:**
- Refatorar o projeto existente no mesmo repositório, preservando histórico Git (não criar projeto novo vazio).
- Renomear pacote Java para `com.lumeos.backend`.
- Ajustar `spring.application.name` para `lumeos-backend` e `artifactId`/nome Maven quando apropriado.
- Atualizar README, títulos e textos do Angular, sidebar, docs, nomes de containers e configuração de deploy.

**Critérios de Aceitação:**
1. O SISTEMA DEVE usar o pacote Java `com.lumeos.backend` no backend.
2. O SISTEMA DEVE definir `spring.application.name` como `lumeos-backend`.
3. O App_Web DEVE exibir títulos e textos com a marca "LumeOS" (incluindo sidebar e títulos de páginas).
4. A documentação (README e docs) DEVE referir-se ao produto como LumeOS.
5. O repositório DEVE preservar o histórico Git (refatoração in-place, sem recriação do projeto).

**Casos de Erro:**
1. SE restar referência à marca "DopamineOS" em elementos de interface voltados ao usuário final após a migração, ENTÃO ela DEVE ser corrigida para "LumeOS".

**Permissões:** N/A (migração de código/branding).

---

## Requisitos Não Funcionais

### RNF-01 — Responsividade
QUANDO acessado em desktop ou mobile, O App_Web DEVE apresentar layout responsivo e navegável em ambas as larguras, sem quebra de conteúdo essencial.

### RNF-02 — Segurança
O SISTEMA DEVE usar BCrypt para senhas, JWT em cookie HttpOnly (Secure em produção, SameSite adequado), autorização por sessão, proteção CSRF HABILITADA para operações mutáveis (POST/PUT/PATCH/DELETE) mesmo com JWT em cookie (sem `csrf.disable()` por esse motivo) e nunca confiar em identificadores de titularidade enviados pelo cliente.

### RNF-03 — Persistência
O SISTEMA DEVE persistir de forma durável os dados de usuários, households, perfis gamificados, hábitos, histórico, compras e leituras em PostgreSQL.

### RNF-04 — Isolamento de Dados Pessoais
O SISTEMA DEVE garantir que hábitos, histórico, GameProfile e leituras de um User não sejam acessíveis por outro User.

### RNF-05 — Isolamento por Household
O SISTEMA DEVE garantir que itens de compra de um Household não sejam acessíveis por usuários de outro Household.

### RNF-06 — Configuração por Ambiente
O SISTEMA DEVE separar configuração de desenvolvimento e produção e obter configuração sensível por variáveis de ambiente.

### RNF-07 — Recuperabilidade (Backup)
O SISTEMA DEVE possuir backup agendado do banco com retenção definida e procedimento de restauração documentado.

### RNF-08 — Build Reprodutível
O SISTEMA DEVE permitir builds reprodutíveis de backend e frontend via Maven e ferramentas Node, empacotados por Docker multi-stage.

### RNF-09 — Deploy via Containers
O SISTEMA DEVE ser implantado por containers Docker orquestrados por Docker Compose.

### RNF-10 — Código Legível e Modular
O SISTEMA DEVE organizar backend e frontend de forma modular e legível (API orientada a domínio; frontend orientado a features).

### RNF-11 — Compatibilidade com VPS Linux
O SISTEMA DEVE operar em um único VPS Linux atrás de reverse proxy Nginx.

### RNF-12 — PWA
O App_Web DEVE ser instalável como PWA, sem cachear respostas privadas de API.

### RNF-13 — Sem Dependência de Serviços Pagos Externos
O SISTEMA DEVE funcionar em sua operação básica sem depender de serviços pagos externos de terceiros.

### RNF-14 — Nenhum Segredo Versionado
O repositório NÃO DEVE conter segredos (senha de banco, segredo JWT, credenciais de VPS); segredos são fornecidos por variáveis de ambiente/GitHub Secrets.

### RNF-15 — Sem Perda Silenciosa dos Dados Existentes
O SISTEMA NÃO DEVE apagar dados existentes de forma automática ou silenciosa; qualquer migração/limpeza é explícita e documentada.

### RNF-16 — Rate Limiting em Endpoints Sensíveis
Endpoints sensíveis como login e validação de convite DEVEM ter proteção/rate limiting, que PODE ser implementado no reverse proxy central da VPS.

---

## Fora do Escopo do MVP

Os itens abaixo estão explicitamente fora do escopo deste MVP:

- Aplicativo nativo Android/iOS.
- Pagamentos e planos SaaS.
- Múltiplos households pela UI e seleção de múltiplos households (a modelagem apenas não impede evolução futura; no MVP cada User pertence a no máximo um Household).
- Recuperação de senha.
- Verificação de e-mail.
- Refresh token (salvo necessidade tecnicamente justificada).
- Recorrência/agendamento avançado de hábitos.
- Remoção/transferência de ownership de household.
- Chat entre usuários.
- Notificações push.
- WebSocket obrigatório / realtime obrigatório (a modelagem apenas deixa espaço).
- Integração bancária e controle financeiro.
- Calendário compartilhado.
- IA e recomendação de livros.
- Integração com Google Books / Open Library / APIs de ISBN / capa automática.
- Controle de estoque, múltiplos mercados, comparação de preços e orçamento nas compras.
- Redis, RabbitMQ, Kubernetes, microserviços.
- Observabilidade pesada (stacks completas de métricas/tracing/log centralizado).
