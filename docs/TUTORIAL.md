# 🎓 Tutorial Básico — DopamineOS

Este tutorial ensina a usar o DopamineOS do começo ao fim: criar seu personagem, cumprir missões e acompanhar sua evolução.

> Antes de começar, certifique-se de que o backend e o frontend estão rodando. Veja o **[Guia de Execução](./SETUP.md)**. Com tudo no ar, acesse `http://localhost:4200`.

---

## Passo 1 — Criar seu personagem (Setup)

Ao abrir o app pela primeira vez, você cai na tela de **Setup**, dividida em duas etapas.

### Etapa 1: Identidade
1. Digite o **nome** do seu personagem.
2. Escolha uma **classe**. A classe define seus atributos iniciais e combina com o tipo de rotina que você quer criar:

   | Classe        | Ideal para quem foca em...                     |
   |---------------|------------------------------------------------|
   | ⚔️ **Guerreiro** | Exercícios físicos e disciplina               |
   | 🔮 **Mago**      | Estudos, leitura e aprendizado                |
   | 🗡️ **Ladino**    | Flexibilidade, finanças e habilidades sociais |

3. Clique em **avançar**.

### Etapa 2: Missões iniciais
1. Selecione as tarefas que você quer no seu dia a dia (beber água, treinar, ler, meditar, etc.).
2. Clique em **finalizar**.

> ⚠️ **Importante:** O Setup **reseta todos os dados anteriores**. Toda vez que você o completa, o personagem, as missões e o histórico antigos são apagados e um novo começa do zero. Se você não escolher nenhuma missão, um **deck padrão** é criado automaticamente.

Após finalizar, você é levado para o **Dashboard**.

---

## Passo 2 — Conhecer o Dashboard

O Dashboard é a tela principal. Aqui você vê:

- **Status do personagem**: nível, XP (barra de progresso), HP, MP e moedas.
- **Gráfico de radar**: mostra visualmente a distribuição dos seus 5 atributos (Força, Destreza, Intelecto, Carisma, Constituição).
- **Lista de missões do dia**: as tarefas que você pode concluir.
- **Diário de bordo**: as atividades já concluídas.

---

## Passo 3 — Concluir uma missão (Check-in)

Este é o coração do jogo:

1. No Dashboard, encontre a missão que você concluiu na vida real.
2. Clique no botão de **executar / check-in** dela.
3. O sistema:
   - calcula o **XP** e as **moedas** com base na dificuldade da missão;
   - aplica as recompensas ao seu personagem;
   - registra a atividade no histórico;
   - atualiza a tela na hora (barra de XP, gráfico, etc.).

### Recompensas por dificuldade

| Dificuldade | XP  | Moedas |
|-------------|:---:|:------:|
| **EASY**    | 10  |   2    |
| **MEDIUM**  | 30  |   5    |
| **HARD**    | 60  |  15    |
| **EPIC**    | 100 |  50    |

### Subir de nível
Quando seu XP atinge o necessário para o próximo nível:
- Seu **nível aumenta**.
- O requisito de XP do próximo nível cresce (×1.5).
- Você é **totalmente curado** (HP e MP restaurados ao máximo).

---

## Passo 4 — Gerenciar missões no Grimório

O **Grimório** (menu lateral → 📜 Grimório de Missões) é onde você cria e apaga missões personalizadas.

### Criar uma missão
1. Preencha o formulário: nome, ícone, atributo, dificuldade e duração (em minutos).
2. Use os **presets** (atalhos prontos como "Ler Livro", "Correr", "Meditar") para preencher rápido.
3. Clique em **salvar**. A missão aparece na lista e fica disponível no Dashboard.

### Apagar uma missão
1. Encontre a missão na lista.
2. Clique em **deletar** e confirme.
3. A missão é removida permanentemente.

---

## Passo 5 — Acompanhar o Histórico

O **Histórico** (menu lateral → ⏳ Histórico) mostra todas as atividades que você já concluiu, **agrupadas por dia**. Para cada dia você vê:

- as missões concluídas naquele dia;
- o **total de XP** ganho no dia.

É a sua linha do tempo de progresso — ótima para ver a consistência dos seus hábitos.

---

## Fluxo completo (resumo)

```
Setup (cria personagem + missões)
        │
        ▼
Dashboard (vê status e missões)
        │
        ▼
Check-in em uma missão ──▶ ganha XP/moedas ──▶ sobe de nível
        │                                            │
        ▼                                            ▼
Histórico registra a atividade            Gráfico de atributos atualiza
        │
        ▼
Grimório (cria/apaga missões personalizadas)
```

---

## Dicas

- **Comece pequeno.** Crie poucas missões fáceis (EASY) no início para pegar o ritmo.
- **Use a classe a seu favor.** Se seu foco é estudar, o Mago combina com missões de Intelecto.
- **Missões EPIC valem muito XP**, mas geralmente representam tarefas longas (ex: "Dormir 8h", "Deep Work"). Use com moderação para não desbalancear.
- **Quer recomeçar do zero?** Basta refazer o Setup — ele limpa tudo e inicia um novo personagem.
