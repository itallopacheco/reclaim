# PRD: Bloqueio — Slice 1, decisão de domínio

**Date:** 2026-05-08
**Status:** Draft
**Parent:** Issue #21 — Bloqueio de apps com quota diária esgotada

## Problem

A PRD-mãe descreve uma feature inteira: domínio de decisão, foreground service, polling, overlay, manifesto, onboarding e indicação visual. Tudo num PR só vira um "big bang" difícil de revisar e de testar com confiança. Antes de qualquer Android nesse fluxo, falta a peça pequena, isolada e 100% testável que responde uma única pergunta: *este pacote está atualmente em estado de bloqueio?* Sem ela, todo o resto do fluxo (serviço, overlay, indicador visual) repetiria a mesma lógica em locais diferentes e divergiria com o tempo.

## Background

`RankAppsForHomeUseCase` (`/Users/oicapivara/AndroidStudioProjects/Reclaim/app/src/main/java/com/example/reclaim/domain/apps/RankAppsForHomeUseCase.kt`) já cruza `AddedApp.dailyQuota` com `UsageStats.usageToday()` para emitir `HomeAppStatus.OVER`, mas esse é um cálculo voltado a *exibir* status na Home. Não dá pra reusá-lo direto pra responder "deve bloquear o pacote X agora?": ele opera sobre a lista inteira, retorna uma estrutura de UI (`HomeAppRow`), e mistura ranking com decisão. O slice 2 (serviço de polling) vai consultar essa decisão muitas vezes por minuto para um único pacote por vez, então a forma natural é um use case dedicado.

A PRD-mãe estabelece duas decisões já tomadas:

- **Quota zero significa "não pode usar nunca".** O bloqueio é permanente para esse pacote enquanto estiver adicionado.
- **A regra é puramente derivada:** `today >= quota`. Não há flag persistida; `usageToday()` é a única fonte de "tempo de hoje".

Este slice entrega só a função pura. Domínio JVM, sem `android.*`, com fakes em `app/src/test/.../domain/blocking/fakes/` no mesmo molde de `domain/apps/fakes/`. Nenhuma mudança de UI, nenhuma mudança no `AndroidManifest.xml`, nenhum serviço novo. Só código novo em `domain/blocking/` e seus testes.

## Requirements

### Must Have

- Novo pacote `domain/blocking/` contendo um único use case (`ShouldBlockAppUseCase` ou nome equivalente) que recebe um `packageName: String` e retorna `Boolean`.
- O use case depende apenas das interfaces de domínio já existentes (`AddedAppsRepository`, `UsageStats`). Não cria interface nova nem modifica as existentes.
- A regra é exatamente:
  - Se o pacote não está em `addedApps.list()`, retorna `false`.
  - Caso contrário, retorna `true` quando o tempo de uso de hoje desse pacote (em `usageStats.usageToday()`) for **maior ou igual** à `dailyQuota` do `AddedApp` correspondente.
- Pacote adicionado com `dailyQuota = Duration.ZERO` retorna `true` desde o instante em que `usageToday()` reporta zero (consistente com "quota 0 = não pode usar nunca").
- Pacote adicionado mas ausente do `Map` retornado por `usageToday()` é tratado como uso `Duration.ZERO`. Combinado com a regra acima: bloqueia apenas se a quota também for zero.
- Wiring em `ReclaimApplication`: nova lazy property/getter expondo o use case, no mesmo padrão de `todayScreenTime` e `rankAppsForHome`. Nenhum consumidor é adicionado nesse slice — só a porta fica disponível.

### Should Have

- Cobertura de testes JVM-only no pacote `app/src/test/java/com/example/reclaim/domain/blocking/`, reaproveitando os fakes existentes via subpackage `fakes/` se possível ou duplicando o estritamente necessário. Casos cobertos:
  - Pacote não adicionado → false.
  - Pacote adicionado, uso < quota → false.
  - Pacote adicionado, uso == quota → true.
  - Pacote adicionado, uso > quota → true.
  - Pacote adicionado com quota zero, uso zero → true.
  - Pacote adicionado, ausente em `usageToday()` (quota > 0) → false.
- Os testes seguem a convenção do repositório: JUnit4, classe `<Subject>Test`, métodos com nome em backticks descritivos, fakes em subpackage `fakes/`.

### Out of Scope

- Foreground service, polling, overlay, qualquer código sob `data/` que rode no Android.
- Mudanças no `AndroidManifest.xml`.
- Mudanças em telas de Onboarding ou indicação visual nos cards (sliced em PRDs separados).
- Migração de `AddedAppsRepository` ou `UsageStats` para `Flow`. O refactor de Flow é PRD próprio (`refactor-repositories-to-flow.md`) e não é pré-requisito desse slice — o use case continua síncrono enquanto as interfaces continuarem síncronas.
- "Reset diário" como lógica deste use case. O reset é consequência natural de `usageToday()` retornar zero no novo dia. Não há código de relógio aqui.

## Constraints

- **Domínio puro JVM.** Sem imports `android.*`, sem `kotlinx.coroutines` (as interfaces de domínio são síncronas hoje).
- **Single responsibility.** O use case responde uma pergunta booleana. Nada de retornar razões, motivos, "minutos restantes" ou estruturas mais ricas. Outros consumidores podem precisar disso, e quando precisarem, ganham seu próprio use case.
- **Zero lugar de chamada novo.** Nada em `ui/` chama o use case nesse PR. A intenção é que o slice 2 seja o primeiro consumidor.
- **Convenções existentes.** Diretório, nome do arquivo, organização dos fakes, estilo de teste: tudo igual a `domain/apps/`. Nada de inventar padrão novo.

## Acceptance Criteria

### Decisão pura

- Dado um `packageName` que **não** está em `addedApps.list()`, quando `invoke(packageName)` é chamado, então retorna `false` qualquer que seja o conteúdo de `usageToday()`.
- Dado um `AddedApp(packageName = "x", dailyQuota = 30.minutes)` e `usageToday()["x"] = 29.minutes`, quando `invoke("x")` é chamado, então retorna `false`.
- Dado um `AddedApp(packageName = "x", dailyQuota = 30.minutes)` e `usageToday()["x"] = 30.minutes`, quando `invoke("x")` é chamado, então retorna `true`.
- Dado um `AddedApp(packageName = "x", dailyQuota = 30.minutes)` e `usageToday()["x"] = 45.minutes`, quando `invoke("x")` é chamado, então retorna `true`.

### Quota zero e ausência em usageToday

- Dado um `AddedApp(packageName = "x", dailyQuota = Duration.ZERO)` e `usageToday()["x"] = Duration.ZERO`, quando `invoke("x")` é chamado, então retorna `true`.
- Dado um `AddedApp(packageName = "x", dailyQuota = 30.minutes)` e `usageToday()` que **não contém** a chave `"x"`, quando `invoke("x")` é chamado, então retorna `false`.
- Dado um `AddedApp(packageName = "x", dailyQuota = Duration.ZERO)` e `usageToday()` que **não contém** a chave `"x"`, quando `invoke("x")` é chamado, então retorna `true`.

### Wiring

- Dado o singleton `ReclaimApplication`, quando o consumidor pede o use case (mesma forma como `todayScreenTime` é exposto hoje), então recebe uma instância pronta sem passos extras.
- Dado o restante do app, quando esse PR é mergeado, então nenhuma tela, serviço ou tarefa background passa a depender do novo use case (nenhum consumidor neste slice).

### Estrutura

- Dado o repositório após o merge, quando se inspeciona `app/src/main/java/com/example/reclaim/domain/`, então existe um diretório `blocking/` contendo o arquivo do use case e nada além disso.
- Dado o repositório após o merge, quando se inspeciona `app/src/test/java/com/example/reclaim/domain/`, então existe um diretório `blocking/` com a classe de teste do use case e, se necessário, um subpackage `fakes/`.
- Dado o repositório após o merge, quando se executa `./gradlew :app:testDebugUnitTest --tests "com.example.reclaim.domain.blocking.*"`, então todos os testes do novo pacote passam.
