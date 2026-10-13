# 🚀 Guia de Execução — LumeOS

Este guia mostra, do zero, como colocar o LumeOS para rodar na sua máquina.

---

## 1. Pré-requisitos

Confirme que você tem tudo instalado antes de começar:

| Ferramenta      | Versão mínima | Como verificar        |
|-----------------|:-------------:|-----------------------|
| **Java (JDK)**  |      21       | `java -version`       |
| **Maven**       |     3.8+      | `mvn -version` *(opcional — há wrapper)* |
| **Node.js**     |      18+      | `node -v`             |
| **npm**         |      11+      | `npm -v`              |
| **PostgreSQL**  |      14+      | rodando na porta 5432 |

> 💡 Se você não tiver o Maven instalado, o projeto inclui o **Maven Wrapper** (`mvnw.cmd`), que baixa e usa o Maven automaticamente. Basta trocar `mvn` por `mvnw.cmd` nos comandos.

---

## 2. Configurar o banco de dados

O LumeOS usa PostgreSQL. A configuração vem de **variáveis de ambiente** (nenhum segredo é versionado). Os defaults de desenvolvimento são:

| Variável          | Default (dev)   | Observação                                 |
|-------------------|-----------------|--------------------------------------------|
| `DB_HOST`         | `localhost`     | Host do PostgreSQL                          |
| `DB_PORT`         | `5432`          | Porta do PostgreSQL                         |
| `DB_NAME`         | `postgres`      | Banco                                       |
| `DB_USER`         | `postgres`      | Usuário                                     |
| `DB_PASSWORD`     | *(vazio)*       | **Sem default versionado** — defina a senha |

> A URL é montada automaticamente como `jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}`.

### Passos
1. Certifique-se de que o serviço do PostgreSQL está **rodando**.
2. O banco `postgres` já existe por padrão em qualquer instalação — não precisa criar nada.
3. Defina a senha do seu PostgreSQL local via variável de ambiente **antes** de subir o backend
   (nunca comite a senha). Exemplos:

   ```cmd
   rem Windows (cmd) — sessão atual
   set DB_PASSWORD=SUA_SENHA_AQUI
   ```
   ```bash
   # Linux/macOS
   export DB_PASSWORD=SUA_SENHA_AQUI
   ```

   Você pode copiar `backend/.env.example` para `backend/.env` (não versionado) e preencher os valores
   para carregar via seu runner/IDE. As demais variáveis (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`)
   só precisam ser definidas se diferirem dos defaults de desenvolvimento acima.

> As tabelas (`tb_usuario`, `tb_protocolos`, `tb_log_atividade`) são criadas **automaticamente** pelo Hibernate no primeiro boot. Você não precisa rodar nenhum script SQL.

---

## 3. Rodar o Backend (Spring Boot)

Abra um terminal na pasta do backend:

```cmd
cd d:\PROJETOS\DopamineOS\DopamineOS\backend
```

Suba a aplicação:

```cmd
mvnw.cmd spring-boot:run
```
*(ou `mvn spring-boot:run` se tiver o Maven instalado)*

### O que acontece no primeiro boot
- O Hibernate cria as tabelas.
- O `GameLoader` cria um personagem seed **"Alex, o Paladino"** (Guerreiro) e o deck de missões padrão.

### Verificar que subiu
O backend roda em **`http://localhost:8080`**. Teste no navegador:

```
http://localhost:8080/api/v1/game/perfil
```

Você deve receber um JSON com os dados do personagem.

---

## 4. Rodar o Frontend (Angular)

Abra **outro** terminal na pasta do frontend:

```cmd
cd d:\PROJETOS\DopamineOS\DopamineOS\frontend
```

Na **primeira vez**, instale as dependências:

```cmd
npm install
```

Depois inicie o servidor de desenvolvimento:

```cmd
npm start
```

O app abre em **`http://localhost:4200`**.

---

## 5. Ordem de inicialização

Sempre suba nesta ordem:

```
1. PostgreSQL   (porta 5432)
2. Backend      (porta 8080)
3. Frontend     (porta 4200)
```

> Se abrir o frontend antes do backend estar pronto, as chamadas vão falhar com "erro de conexão". Basta recarregar a página depois que o backend subir.

---

## 6. Solução de problemas

### ❌ "Connection refused" / erro ao conectar no banco
- O PostgreSQL está rodando?
- A porta é mesmo 5432?
- As variáveis `DB_USER`/`DB_PASSWORD` estão corretas para o seu PostgreSQL?

### ❌ Porta 8080 já em uso
Defina a porta via variável de ambiente antes de subir o backend:
```cmd
set SERVER_PORT=8081
```
E atualize a URL da API no frontend (`frontend/src/app/services/game.service.ts`):
```typescript
private readonly apiUrl = 'http://localhost:8081/api/v1/game';
```

### ❌ Porta 4200 já em uso
Rode o Angular em outra porta:
```cmd
npm start -- --port 4300
```
E ajuste a origem permitida de CORS via variável de ambiente `CORS_ALLOWED_ORIGINS`.

### ❌ Frontend mostra erro de CORS
O backend libera `http://localhost:4200` por padrão em dev. Se você mudou a porta do frontend,
defina as origens permitidas via variável de ambiente (lista separada por vírgula):
```cmd
set CORS_ALLOWED_ORIGINS=http://localhost:4300
```

### ❌ "Nenhum personagem encontrado" (404)
Isso é esperado se o banco estiver vazio e o seed não rodou. Faça o **Setup** pela tela inicial do app, ou reinicie o backend com o banco limpo para o `GameLoader` recriar o seed.

---

## 7. Comandos úteis

### Backend
```cmd
mvnw.cmd spring-boot:run       # Roda a aplicação
mvnw.cmd clean package          # Gera o JAR em target/
mvnw.cmd test                   # Roda os testes
```

### Frontend
```cmd
npm start        # Servidor de desenvolvimento (localhost:4200)
npm run build    # Build de produção (pasta dist/)
npm test         # Roda os testes (Vitest)
```
