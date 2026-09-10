# 🎮 DopamineOS

**DopamineOS** é um rastreador de hábitos gamificado. A ideia é simples: você transforma tarefas da vida real (treinar, ler, dormir, beber água) em "missões" de RPG. Ao concluir cada missão você ganha XP, moedas, sobe de nível e evolui os atributos do seu personagem.

É um projeto full-stack com **Spring Boot** (backend) e **Angular** (frontend).

---

## 📚 Índice

- [Visão geral](#-visão-geral)
- [Arquitetura](#-arquitetura)
- [Stack tecnológica](#-stack-tecnológica)
- [Conceitos do jogo](#-conceitos-do-jogo)
- [Estrutura de pastas](#-estrutura-de-pastas)
- [API REST](#-api-rest)
- [Documentação detalhada](#-documentação-detalhada)

---

## 🔎 Visão geral

O DopamineOS é um app de **um único jogador** (single-player). Existe apenas um personagem no banco de dados por vez. O fluxo básico é:

1. **Setup** — você cria o personagem escolhendo nome e uma classe de RPG (Guerreiro, Mago ou Ladino) e seleciona missões iniciais.
2. **Dashboard** — visualiza status (HP, MP, XP, nível), um gráfico de radar dos atributos e a lista de missões do dia.
3. **Check-in** — ao concluir uma missão, ganha XP e moedas, e a atividade é registrada no histórico.
4. **Grimório** — cria, edita e apaga missões personalizadas.
5. **Histórico** — consulta todas as atividades concluídas, agrupadas por dia.

---

## 🏗 Arquitetura

```
┌────────────────────────┐         HTTP/JSON          ┌──────────────────────────┐
│   Frontend (Angular)    │  ───────────────────────▶  │   Backend (Spring Boot)   │
│   http://localhost:4200 │                            │   http://localhost:8080   │
│                         │  ◀───────────────────────  │   /api/v1/game/**         │
└────────────────────────┘                            └────────────┬─────────────┘
                                                                    │ JPA / Hibernate
                                                                    ▼
                                                        ┌──────────────────────────┐
                                                        │      PostgreSQL           │
                                                        │      localhost:5432       │
                                                        └──────────────────────────┘
```

O backend segue uma arquitetura em camadas clássica:

```
Controller  →  Service  →  Repository  →  Banco
    │            │
    │            └── Regras de negócio (XP, nível, recompensas)
    └── Recebe/retorna DTOs (nunca entidades JPA cruas)

Mapper (MapStruct) converte Entidade ⇄ DTO
```

---

## 🧰 Stack tecnológica

### Backend
- **Java 21**
<<<<<<< HEAD
- **Spring Boot 3.3.1** (Web, Data JPA, Validation, Actuator, DevTools)
- **PostgreSQL** (persistência)
- **MapStruct 1.5.5** (mapeamento entidade ⇄ DTO)
- **Lombok** (reduz boilerplate)
- **Maven** (build)
=======
- **Spring Boot 4.0.1** (Web, JPA, Validation)
- **Banco de Dados:** PostgreSQL
- **Maven** (Gerenciamento de dependências)
>>>>>>> ba2a2e6a7fdd81af1712754c6c820533dddf2103

### Frontend
- **Angular 21** (componentes standalone)
- **TypeScript 5.9**
- **TailwindCSS 3.4** (estilização)
- **ApexCharts / ng-apexcharts** (gráfico de radar dos atributos)
- **RxJS 7.8** (programação reativa / HTTP)

---

## 🎲 Conceitos do jogo

### Classes de RPG (`ClasseRPG`)

Cada classe define os atributos iniciais do personagem:

| Classe      | Força | Destreza | Intelecto | Carisma | Constituição | Foco                        |
|-------------|:-----:|:--------:|:---------:|:-------:|:------------:|-----------------------------|
| **Guerreiro** |  10   |    5     |     3     |    5    |      10      | Exercício físico, disciplina |
| **Mago**      |   3   |    5     |    10     |    6    |       4      | Estudos, leitura            |
| **Ladino**    |   5   |   10     |     5     |    8    |       5      | Flexibilidade, soft skills  |

### Atributos (`Atributo`)
`FORCA`, `DESTREZA`, `INTELECTO`, `CARISMA`, `CONSTITUICAO`

Cada missão está ligada a um atributo. Os status derivados são calculados assim:
- **HP máximo** = `constituicao × 10`
- **MP máximo** = `intelecto × 10`

### Dificuldade (`Dificuldade`)

Define a recompensa de cada missão:

| Dificuldade | XP base | Gold base | Pontos de atributo |
|-------------|:-------:|:---------:|:------------------:|
| **EASY**    |   10    |     2     |         1          |
| **MEDIUM**  |   30    |     5     |         2          |
| **HARD**    |   60    |    15     |         3          |
| **EPIC**    |  100    |    50     |         5          |

### Progressão de nível
- Cada personagem começa no **nível 1** precisando de **100 XP** para o próximo nível.
- Ao subir de nível, o requisito de XP cresce **×1.5** e o personagem é totalmente curado (HP e MP restaurados).

---

## 📁 Estrutura de pastas

```
DopamineOS/
├── backend/                          # API Spring Boot
│   ├── src/main/java/com/dopamineos/backend/
│   │   ├── BackendApplication.java   # Ponto de entrada
│   │   ├── config/
│   │   │   ├── CorsConfig.java       # Libera acesso do frontend (CORS)
│   │   │   └── GameLoader.java       # Cria personagem/missões seed no 1º boot
│   │   ├── controller/
│   │   │   └── GameController.java   # Endpoints REST /api/v1/game
│   │   ├── dto/                      # Objetos de transferência (records)
│   │   │   ├── LogAtividadeDTO.java
│   │   │   ├── ProtocoloDTO.java
│   │   │   ├── SetupRequest.java
│   │   │   └── UsuarioDTO.java
│   │   ├── entity/                   # Entidades JPA
│   │   │   ├── LogAtividade.java
│   │   │   ├── Protocolo.java
│   │   │   ├── Usuario.java
│   │   │   └── enums/
│   │   │       ├── Atributo.java
│   │   │       ├── ClasseRPG.java
│   │   │       └── Dificuldade.java
│   │   ├── exception/                # Tratamento de erros
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── StandardError.java
│   │   ├── mapper/
│   │   │   └── GameMapper.java       # MapStruct: entidade ⇄ DTO
│   │   ├── repository/               # Spring Data JPA
│   │   │   ├── LogAtividadeRepository.java
│   │   │   ├── ProtocoloRepository.java
│   │   │   └── UsuarioRepository.java
│   │   └── service/
│   │       └── GameService.java      # Regras de negócio
│   └── src/main/resources/
│       └── application.properties    # Config do banco e JPA
│
└── frontend/                         # App Angular
    └── src/app/
        ├── app.config.ts             # Providers (router, http, animações)
        ├── app.routes.ts             # Rotas
        ├── services/
        │   └── game.service.ts       # Cliente HTTP + interfaces/enums
        └── components/
            ├── setup/                # Criação do personagem
            ├── dashboard/            # Visão geral + check-in
            ├── quest-editor/         # Grimório (CRUD de missões)
            ├── history/              # Histórico de atividades
            └── sidebar/              # Menu lateral
```

---

## 🌐 API REST

Base: `http://localhost:8080/api/v1/game`

| Método   | Endpoint            | Descrição                                        | Corpo                       |
|----------|---------------------|--------------------------------------------------|-----------------------------|
| `GET`    | `/perfil`           | Retorna o personagem atual                       | —                           |
| `POST`   | `/setup`            | Cria personagem + missões iniciais (apaga tudo)  | `SetupRequest`              |
| `GET`    | `/protocolos`       | Lista todas as missões                           | —                           |
| `POST`   | `/protocolo`        | Cria uma missão avulsa                           | `ProtocoloDTO`              |
| `POST`   | `/protocolos`       | Cria várias missões de uma vez                   | `List<ProtocoloDTO>`        |
| `DELETE` | `/protocolo/{id}`   | Apaga uma missão                                 | —                           |
| `POST`   | `/checkin/{id}`     | Conclui uma missão (ganha XP/gold)               | —                           |
| `GET`    | `/historico`        | Lista o histórico de atividades (mais recente 1º)| —                           |

### Exemplo — `POST /setup`
```json
{
  "nome": "Alex",
  "classe": "GUERREIRO",
  "missoes": [
    {
      "nome": "Treino Hipertrofia",
      "icone": "💪",
      "atributo": "FORCA",
      "dificuldade": "HARD",
      "duracaoMinutos": 60
    }
  ]
}
```
> Se `missoes` vier vazio ou ausente, o backend cria automaticamente um **deck padrão**.

### Erros
Todos os erros seguem o formato `StandardError`:
```json
{
  "timestamp": "2026-09-10T12:00:00Z",
  "status": 404,
  "error": "Recurso não encontrado",
  "message": "Nenhum personagem encontrado. Faça o Setup primeiro.",
  "path": "/api/v1/game/perfil"
}
```

---

## 📖 Documentação detalhada

- **[Guia de execução](./docs/SETUP.md)** — instalar dependências e rodar o projeto do zero.
- **[Tutorial básico](./docs/TUTORIAL.md)** — passo a passo de uso da aplicação.

---

## ⚠️ Observações

- Não há autenticação. Qualquer um com acesso à API pode alterar os dados. Adequado apenas para uso local/pessoal.
- O `POST /setup` **apaga todos os dados existentes** (histórico, missões e personagem) antes de criar o novo. É um reset total.
- O `spring.jpa.hibernate.ddl-auto=update` cria/atualiza as tabelas automaticamente — não é necessário rodar SQL manual.
