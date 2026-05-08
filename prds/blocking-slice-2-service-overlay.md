# PRD: Bloqueio — Slice 2, foreground service + overlay

**Date:** 2026-05-08
**Status:** Draft
**Parent:** Issue #21 — Bloqueio de apps com quota diária esgotada
**Depends on:** Slice 1 (`blocking-slice-1-domain.md`)

## Problem

Slice 1 entrega a decisão pura ("este pacote está bloqueado agora?"). Sozinha ela não faz nada acontecer. Para o usuário sentir o bloqueio, alguém precisa estar continuamente verificando qual app está em primeiro plano e, quando o app é um pacote bloqueado, sobrepor uma tela cheia que interrompa o uso. Esse é o coração funcional da feature: sem ele, a quota continua sendo um relatório.

## Background

A PRD-mãe define a mecânica:

- *Polling* via `UsageStatsManager.queryEvents` em um foreground service. Polling foi escolhido em vez de `AccessibilityService` para evitar a complicação de Play Store; o trade-off é uma latência de ~1s, aceitável para o P0.
- *Overlay* full-screen acionado por `SYSTEM_ALERT_WINDOW`/`TYPE_APPLICATION_OVERLAY`. A PRD-mãe favorece uma **Activity transparente** em tema Compose por consistência visual, em vez de uma `View` injetada via `WindowManager` direto.
- O bloqueio é *hard*: nada de snooze, nada de "+5 minutos". Único caminho é sair do app.

A camada de uso já tem a peça que importa: `UsageStatsManagerStats.usageToday()` (em `data/UsageStatsManagerStats.kt`) consome `queryEvents` com refcount de ACTIVITY_RESUMED/PAUSED para o dia. Esse arquivo é a referência de como falar com `UsageStatsManager` no projeto. O polling deste slice consulta `queryEvents` em janelas curtas para descobrir *qual é o app em foreground agora*, o que é uma consulta diferente ("último ACTIVITY_RESUMED não pareado") da que `usageToday()` faz ("acumulado do dia"). Mas o adapter pode crescer pra expor as duas, ou um adapter irmão pode aparecer; isso é decisão de implementação e não muda o produto.

A camada de permissões necessária para que esse slice funcione (`FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `SYSTEM_ALERT_WINDOW`) é abordada no Slice 3. Este slice **assume** que o manifesto e o onboarding já contemplam essas permissões. Em desenvolvimento, o engenheiro concede manualmente via Settings antes de testar. Em produção, Slice 3 entra junto.

A overlay activity é uma nova `Activity` declarada no manifesto, com tema transparente e `launchMode` adequado para reaparecer toda vez que o pacote bloqueado volta ao topo. Ela hospeda uma tela Compose dedicada (provisoriamente `BlockedAppScreen`, distinta de `LockScreen.kt` existente, que é placeholder de outra coisa).

## Requirements

### Must Have

#### Foreground service

- Existe uma classe `Service` em `data/` (sugestão de nome: `BlockingService`) declarada como foreground service do tipo `specialUse` no manifesto.
- O serviço sobe automaticamente ao receber `BOOT_COMPLETED` se o usuário tem ao menos um app adicionado, e na primeira vez que o app é aberto após instalação se há ao menos um app adicionado.
- O serviço é desligado quando o usuário remove o último app adicionado.
- O serviço exibe uma notificação persistente em canal de notificação de baixa prioridade. Texto: linguagem de produto, sem urgência ("Reclaim está protegendo seus limites"). Sem ações na notificação.
- O serviço faz polling em intervalo configurável internamente, com valor inicial de 1 segundo, sem expor controle ao usuário.
- O serviço usa `UsageStatsManager.queryEvents` em uma janela curta (a definir, p.ex. últimos 10s) para identificar o último pacote em foreground. Não faz `queryUsageStats` per-package em loop.

#### Decisão e overlay

- A cada tick, o serviço pergunta a `ShouldBlockAppUseCase(currentForeground)` e, se retornar `true` **e** o overlay ainda não está visível em cima desse pacote, dispara a `BlockingActivity`.
- Se o pacote em foreground muda para algo diferente do bloqueado (home, outro app), o serviço encerra a `BlockingActivity` (caso ainda esteja na top do back stack) ou, no mínimo, não a redispara.
- Quando o usuário volta para o pacote bloqueado, a próxima detecção redispara o overlay em até 1s.
- O serviço é resiliente a falhas pontuais de `queryEvents` (sem permissão temporariamente revogada, exceções) — ele loga e continua o ciclo sem cair.

#### BlockingActivity / overlay

- Activity transparente, em tema Compose (`ReclaimTheme`), declarada no manifesto com `excludeFromRecents=true`, `launchMode=singleInstance`, `showWhenLocked=false`, e `taskAffinity=""` para não compor com a back stack do app principal.
- A activity hospeda uma tela `BlockedAppScreen` que recebe via `Intent` extras: `packageName`, label do app, ícone, `usageToday`, `dailyQuota`.
- A tela mostra ícone, nome do app, frase clara "Você atingiu o limite de hoje", linha "X min de Y min", e um único botão de ação: "Sair".
- "Sair" e o botão Voltar do sistema fazem o mesmo: `startActivity` do `Intent.ACTION_MAIN`/`CATEGORY_HOME` e `finish()` da `BlockingActivity`. O usuário vê a home do sistema.
- A tela não tem outras ações, links, atalhos, ou caminhos para o app bloqueado.
- Toques na tela de bloqueio nunca chegam ao app por baixo (consequência da activity ser opaca de fato; não pode ter regiões transparentes interativas).

#### Permissão ausente

- Se a permissão `SYSTEM_ALERT_WINDOW` não estiver concedida, o serviço continua subindo, mas registra que o overlay está indisponível e **não tenta** disparar a `BlockingActivity` (evita crash com `SecurityException`).
- Esse estado "bloqueio inativo" fica disponível como leitura para a Home (o aviso na Home é Slice 3/4; aqui basta a flag/estado existir).

#### DI

- `ReclaimApplication` ganha o wiring necessário (instância do serviço como tipo Android, não vai como property; o que cabe aqui é o use case e os adapters de polling/overlay launcher, no mesmo padrão dos demais).

### Should Have

- Testes:
  - JVM/Robolectric sobre o componente que converte `queryEvents` em "pacote em foreground", garantindo que ele identifica o último ACTIVITY_RESUMED na janela.
  - Compose UI test sobre `BlockedAppScreen` validando: render do nome, ícone, tempo `X min de Y min`, presença e clique no botão "Sair".
  - Teste do "círculo de decisão" do serviço usando fakes para `ShouldBlockAppUseCase` e para o componente de polling, garantindo que: pacote bloqueado dispara overlay; pacote não bloqueado não dispara; transição "bloqueado → home" não redispara.
- Logs de diagnóstico em nível DEBUG marcando: tick, pacote detectado, decisão, ação tomada. Sem PII além do `packageName`.

### Out of Scope

- **Onboarding e manifesto.** Vivem no Slice 3. Este slice supõe permissões concedidas em dev.
- **Indicação visual "bloqueando agora" nos cards.** Slice 4.
- **Snooze / earn-back / desbloqueio por hábitos.** PRD-mãe explicita que está fora.
- **Detecção via `AccessibilityService`.** Polling resolve.
- **Persistência do estado de bloqueio.** Decisão é sempre derivada de `usageToday() ≥ quota`.
- **Internacionalização das strings da tela de bloqueio.** PT-BR só, no mesmo padrão do resto do app hoje.

## Constraints

- **Single feature branch / single PR.**
- **Convenções do projeto.** Service e adapter ficam em `data/`. A activity e a tela ficam em `ui/screen/` com sufixo coerente (a `LockScreen.kt` existente é design placeholder; este slice cria uma tela nova distinta — não reaproveitar até alinhar).
- **Tema e tokens.** A `BlockedAppScreen` usa só tokens `Reclaim*`. Sem hex hardcoded.
- **Compose preview.** A `BlockedAppScreen` tem `@Preview` no fim do arquivo, como qualquer outra tela do projeto.
- **Permissões em runtime.** O serviço *checa* `Settings.canDrawOverlays(context)` antes de tentar `startActivity` da `BlockingActivity`; sem permissão, não chama. Não tenta abrir a tela do sistema daqui — isso é responsabilidade do Onboarding (Slice 3).
- **Bateria.** Polling de 1s é P0; medir consumo é Out of Scope, mas o intervalo é centralizado em uma constante para ajuste fácil.
- **Sem AccessibilityService, sem WorkManager, sem AlarmManager.** Apenas o foreground service longo-vida.

## Acceptance Criteria

### Subida e descida do serviço

- Dado o usuário com ao menos um `AddedApp`, quando o app é aberto pela primeira vez após instalação, então `BlockingService` está rodando em foreground com a notificação visível.
- Dado o dispositivo passou por reboot e o usuário tem ao menos um `AddedApp`, quando o boot completa, então `BlockingService` sobe sem o usuário precisar abrir o app.
- Dado o usuário remove o último `AddedApp` em uso, quando a remoção é confirmada, então `BlockingService` é encerrado e a notificação some.
- Dado o usuário não tem nenhum `AddedApp`, quando o app é aberto pela primeira vez, então `BlockingService` não sobe.

### Detecção e overlay

- Dado um `AddedApp` "x" com quota 30 min e uso de hoje 30 min, quando o usuário abre o app "x", então a `BlockingActivity` aparece em até 1 segundo cobrindo a tela inteira.
- Dado o overlay está visível sobre "x", quando o usuário tenta tocar em qualquer área da tela, então só a `BlockedAppScreen` recebe os toques.
- Dado o overlay está visível, quando o usuário toca em "Sair", então o sistema vai para a home (`Intent.ACTION_MAIN` + `CATEGORY_HOME`) e a `BlockingActivity` é finalizada.
- Dado o overlay está visível, quando o usuário aperta o botão Voltar do sistema, então o comportamento é idêntico a "Sair" (vai para home, não retorna a "x").
- Dado o usuário foi para a home, quando ele abre "x" novamente pelo recents/launcher, então o overlay reaparece em até 1 segundo.
- Dado um `AddedApp` "x" com quota 30 min e uso 25 min, quando o usuário abre "x", então a `BlockingActivity` não aparece.
- Dado um pacote "y" que não é `AddedApp`, quando o usuário abre "y", então a `BlockingActivity` não aparece, qualquer que seja `usageToday()["y"]`.

### Não-redispara incorretamente

- Dado o overlay está visível sobre "x", quando o serviço faz o próximo tick e "x" continua em foreground, então o serviço não inicia uma segunda `BlockingActivity` por cima da existente.
- Dado o overlay foi finalizado pelo usuário (saiu para home) e a home está em foreground, quando o serviço faz o próximo tick, então não dispara `BlockingActivity` (não há pacote bloqueado em foreground).

### Permissão ausente

- Dado a permissão `SYSTEM_ALERT_WINDOW` está negada, quando o serviço detecta um pacote bloqueado em foreground, então não tenta iniciar a `BlockingActivity` e o app por baixo continua acessível.
- Dado a permissão é negada, quando o serviço roda, então ele não crasha e mantém a notificação ativa.
- Dado a permissão acabou de ser concedida (sem reiniciar o serviço), quando o próximo tick detecta um pacote bloqueado, então a `BlockingActivity` é iniciada normalmente.

### Render da tela de bloqueio

- Dado a `BlockedAppScreen` é renderizada para um `AddedApp` "Instagram" com quota 30 min e uso 35 min, quando a tela aparece, então mostra o ícone do Instagram, o nome "Instagram", a frase de limite atingido, o texto "35 min de 30 min" (ou formatação consistente do projeto) e o botão "Sair".
- Dado a `BlockedAppScreen` está aberta, quando o usuário inspeciona a UI, então não há nenhum botão, link ou afordância que volte ao app bloqueado.

### Notificação

- Dado o serviço está em execução, quando o usuário expande a notificação, então vê uma frase de produto sem ações requerendo interação.
- Dado o usuário tenta dispensar a notificação, quando o swipe completa, então a notificação permanece (ongoing) — comportamento padrão de foreground service.
