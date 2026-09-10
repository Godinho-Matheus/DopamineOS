# 🚀 Guia de Execução — DopamineOS

Este guia mostra, do zero, como colocar o DopamineOS para rodar na sua máquina.

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

O DopamineOS usa PostgreSQL. Ele espera, por padrão:

| Configuração | Valor padrão                                  |
|--------------|-----------------------------------------------|
| URL          | `jdbc:postgresql://localhost:5432/postgres`   |
| Usuário      | `postgres`                                    |
| Senha        | `123456`                                       |

### Passos
1. Certifique-se de que o serviço do PostgreSQL está **rodando**.
2. O banco `postgres` já existe por padrão em qualquer instalação — não precisa criar nada.
3. Se sua senha for **diferente** de `123456`, edite o arquivo:

   ```
   backend/src/main/resources/application.properties
   ```
   ```properties
   spring.datasource.username=postgres
   spring.datasource.password=SUA_SENHA_AQUI
   ```

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
- Usuário e senha em `application.properties` estão corretos?

### ❌ Porta 8080 já em uso
Adicione no `application.properties`:
```properties
server.port=8081
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
E ajuste a origem permitida no CORS (`backend/.../config/CorsConfig.java`) e/ou no `application.properties`.

### ❌ Frontend mostra erro de CORS
O backend só libera `http://localhost:4200` por padrão. Se você mudou a porta do frontend, atualize `CorsConfig.java`:
```java
config.setAllowedOrigins(List.of("http://localhost:4300"));
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
