# PRD: Bloqueio — Slice 4, indicador "bloqueando agora"

**Date:** 2026-05-08
**Status:** Draft
**Parent:** Issue #21 — Bloqueio de apps com quota diária esgotada
**Depends on:** Slice 1 (`blocking-slice-1-domain.md`)

## Problem

Hoje, com a feature de bloqueio implementada (slices 1–3), a Apps tab continua falando a mesma língua de antes: cada card tem um status visual de uso vs. quota (`OK`/`WARN`/`OVER`), mas não distingue um app que **passou** da quota em algum momento de um app que está **literalmente sendo bloqueado neste instante**. Para o usuário, "vermelho porque estourei" e "vermelho porque o Reclaim vai me parar se eu abrir" são dois estados diferentes. Sem essa distinção, o card se torna ruidoso e o feedback do produto fica abstrato — o bloqueio não tem rosto na UI.

## Background

`RankAppsForHomeUseCase` produz `HomeAppRow` com `HomeAppStatus.OVER` quando `today >= quota`. Isso é, na prática, o mesmo critério que `ShouldBlockAppUseCase` usa. A diferença não é de cálculo, é de leitura: um diz "estourou hoje" pra fins de ranking; o outro diz "agora seria bloqueado" pra fins de ação. Hoje os dois colapsam numa coisa só, e o card mostra só um status histórico.

A PRD-mãe lista esse indicador como **Should Have** no escopo total, e o explicita: "o card mostra um indicador de 'bloqueando agora', distinto do estado `OVER` puramente histórico". Esse slice cumpre exatamente esse requisito, e nada além.

A pergunta de produto é: o que faz "bloqueando agora" ser diferente de "OVER"? Hoje, em termos de regra, nada — `OVER` ↔ `today >= quota` ↔ bloqueia. Mas no futuro próximo (PRDs já listados como Out of Scope da mãe: earn-back, snooze, janela horária), pode haver um app `OVER` que não está bloqueando porque o usuário ganhou tempo via hábito. Modelar `BLOCKING_NOW` como estado de primeira classe agora deixa esse caminho aberto sem refactor depois.

## Requirements

### Must Have

#### Modelo

- `HomeAppRow` (ou estrutura equivalente consumida pelos cards) ganha a capacidade de carregar um estado **"bloqueando agora"**, distinto de `OVER`. Forma sugerida (a confirmar na implementação): novo valor de `HomeAppStatus` (p.ex. `BLOCKED`) ou um campo booleano ortogonal `isBlockingNow`. A escolha não muda os critérios de aceitação visuais.
- O cálculo desse estado reaproveita `ShouldBlockAppUseCase` do Slice 1. O ranking e a soma de tempo continuam intocados.

#### Apps tab

- Cada card de app na Apps tab mostra, quando o app está atualmente em estado de bloqueio, um indicador visual claro e distinto do estado `OVER` simples. Forma mínima esperada: rótulo textual "Bloqueando agora" (ou equivalente em PT-BR) + cor/ícone diferenciado, em vez de só a barra/contador no vermelho de `OVER`.
- Quando o app está `OVER` mas **não** está em estado de bloqueio (cenário futuro de earn-back), o card mostra `OVER` como antes. Hoje, na prática, os dois coincidem; o card precisa estar pronto para divergir.
- Tap no card continua tendo o mesmo destino atual (Edit App). O indicador não cria um novo destino.

#### Empty para o caso comum

- Quando nenhum app está em estado de bloqueio, a Apps tab renderiza idêntica ao que renderizava antes deste slice. Nada de elemento "vazio" ou marcador residual. O indicador só existe quando faz sentido.

### Should Have

- O mesmo indicador na Home (no card que cada app ocupa abaixo do hero ring), pelo mesmo critério, pelo mesmo componente reutilizado.
- Testes:
  - JVM unit test no use case que produz as linhas (ou no que quer que carregue `isBlockingNow`/`BLOCKED`), cobrindo: app bloqueando agora → flag verdadeira; app `OVER` cuja decisão de bloqueio é falsa (cenário hipotético, montável com fakes) → flag falsa; app abaixo da quota → flag falsa.
  - Compose UI test sobre o card com e sem indicador, validando o texto "Bloqueando agora" e a presença/ausência conforme o estado.

### Out of Scope

- **Earn-back, snooze, janelas horárias.** O slice prepara o terreno para divergência entre `OVER` e `BLOCKING`, mas não introduz nenhuma fonte que faça os dois divergirem.
- **Dashboard / tela dedicada de "apps bloqueando".** O indicador é por card, não tela própria.
- **Ações no card** (desbloquear pelo card, snooze, etc.). Nenhuma ação nova.
- **Notificações push** quando um app entra em estado de bloqueio.
- **Animações.** Estado é estático; nada de transição animada nesta entrega.

## Constraints

- **Tokens.** O indicador usa cores e tipografia já presentes em `ui/theme/`. Sem hex.
- **Reuso de componente.** Se a Home e a Apps tab compartilham um card hoje, o indicador é adicionado uma vez e aparece nos dois. Se forem dois componentes separados, o indicador é um sub-componente isolado consumido pelos dois.
- **Não introduz Flow** se o repositório ainda não tiver migrado. Se o refactor de Flow já tiver mergeado, este slice consome a versão Flow naturalmente.
- **Não muda ranking.** A ordem da lista permanece a mesma; só a *renderização* de um card específico muda.

## Acceptance Criteria

### Card mostra "bloqueando agora"

- Dado um `AddedApp` "Instagram" com quota 30 min e uso de hoje 30 min (estado de bloqueio ativo), quando o usuário abre a Apps tab, então o card do Instagram mostra um indicador "Bloqueando agora" claramente distinto do estado `OVER` puramente histórico.
- Dado um `AddedApp` com uso 25 min e quota 30 min, quando a Apps tab renderiza, então seu card **não** mostra o indicador "Bloqueando agora".
- Dado um pacote que não é `AddedApp`, quando a Apps tab renderiza, então ele não aparece com indicador (e em geral nem aparece, conforme comportamento atual da tab).

### Card sem bloqueio renderiza como antes

- Dado nenhum `AddedApp` está em estado de bloqueio, quando a Apps tab renderiza, então a aparência é equivalente à versão pré-slice — nenhum elemento residual de "Bloqueando agora" visível.

### Reuso na Home (Should)

- Dado a Home renderiza os apps abaixo do hero ring e um deles está em estado de bloqueio, quando a Home aparece, então esse card mostra o mesmo indicador "Bloqueando agora" que aparece na Apps tab.

### Modelo

- Dado o use case que alimenta os cards, quando ele é chamado para um cenário em que `ShouldBlockAppUseCase` retornaria `true` para "x", então a linha correspondente a "x" carrega o estado/flag de "bloqueando agora".
- Dado o use case, quando `ShouldBlockAppUseCase` retornaria `false`, então a linha não carrega esse estado, mesmo que `today >= quota` em algum cenário hipotético futuro (representado nos testes via fakes que desacoplam `OVER` de "bloqueia").
