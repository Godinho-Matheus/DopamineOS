# Política de autonomia — LumeOS

Este projeto segue SDD.

Fontes de verdade, nesta ordem:

1. .kiro/specs/lumeos-mvp/requirements.md
2. .kiro/specs/lumeos-mvp/design.md
3. .kiro/specs/lumeos-mvp/tasks.md
4. código existente

## Regra principal

Ao receber uma task para executar, implemente-a do início ao fim com autonomia.

NÃO pergunte ao usuário sobre decisões que possam ser determinadas por:
- requirements.md;
- design.md;
- tasks.md;
- padrões já existentes no código;
- convenções da stack;
- uma escolha técnica reversível e de baixo impacto.

## Quando NÃO perguntar

Não pedir confirmação para:

- criar, editar, mover ou renomear arquivos necessários à task;
- criar classes, DTOs, repositories, services, controllers e testes previstos;
- corrigir imports;
- refatorar código diretamente necessário à task;
- instalar dependências explicitamente previstas no design/tasks;
- executar mvn verify;
- executar npm test;
- executar npm run build;
- executar comandos de inspeção Git;
- corrigir erros de compilação causados pela própria alteração;
- corrigir testes quebrados pela própria alteração;
- escolher nomes internos de métodos, variáveis ou classes quando o domínio já estiver definido;
- escolher entre implementações equivalentes quando requirements/design não diferenciam comportamento.

Quando houver mais de uma solução tecnicamente válida, escolher a solução:

1. mais simples;
2. com menor diff;
3. compatível com a arquitetura aprovada;
4. que introduza menos dependências;
5. que mantenha o código atual funcional.

Registrar a escolha no resumo final, mas NÃO interromper a execução para perguntar.

## Quando perguntar

Perguntar SOMENTE se ocorrer pelo menos uma destas situações:

- requirements.md e design.md entram em conflito real;
- a decisão altera comportamento de negócio não especificado;
- a decisão exige segredo, senha, token, domínio ou credencial que não está disponível;
- a ação é destrutiva ou irreversível;
- seria necessário apagar dados existentes;
- seria necessário mudar uma decisão arquitetural aprovada;
- seria necessário adicionar tecnologia explicitamente fora do escopo;
- não existe forma segura de concluir a task sem informação externa do usuário.

Antes de perguntar, investigar código, specs e documentação disponível.

## Tratamento de erros

Se um comando, build ou teste falhar:

1. analisar o erro;
2. identificar a causa;
3. corrigir;
4. executar novamente.

Tentar até 3 abordagens razoáveis antes de pedir ajuda.

Não perguntar ao usuário apenas porque o primeiro comando falhou.

## Escopo

Executar somente a task solicitada e suas subtasks.

Não antecipar tasks futuras.

Pode realizar pequenas correções necessárias para manter build/testes verdes, mas não implementar funcionalidades futuras.

## Definition of Done

Antes de concluir uma task:

- executar os comandos de validação definidos no tasks.md;
- corrigir falhas relacionadas à task;
- confirmar que nenhuma funcionalidade anterior foi quebrada;
- revisar git diff;
- garantir que nenhum segredo foi versionado.

Só marcar a task/subtasks como concluídas depois da validação.

## Git

Pode executar autonomamente:

- git status
- git diff
- git log
- git grep
- git mv

Não fazer automaticamente:

- git push
- git push --force
- git reset --hard
- git clean -fd
- rebase destrutivo
- alteração de histórico remoto

Não criar commit automaticamente, a menos que o usuário solicite explicitamente.

## Comunicação

Não enviar atualizações intermediárias pedindo aprovação.

Trabalhar até:

- concluir a task; ou
- encontrar um bloqueio que se enquadre nas regras de "Quando perguntar".

Ao final informar apenas:

- task executada;
- arquivos principais alterados;
- decisões técnicas relevantes tomadas autonomamente;
- testes/build executados;
- resultado;
- problemas restantes, se houver.

## Checkpoints

Checkpoints do tasks.md são barreiras obrigatórias.

Nunca ultrapassar um checkpoint automaticamente.