# PRD: Bloqueio — Slice 3, permissões + Onboarding

**Date:** 2026-05-08
**Status:** Draft
**Parent:** Issue #21 — Bloqueio de apps com quota diária esgotada
**Related:** Slice 2 (`blocking-slice-2-service-overlay.md`) — assume essas permissões

## Problem

O foreground service e a tela de bloqueio (Slice 2) só funcionam em produção quando o usuário concedeu manualmente a permissão de sobreposição de tela e quando o manifesto declara as permissões certas para o tipo de serviço usado. O Onboarding atual (`OnboardingPermissionsScreen.kt`) só conhece "Acesso a uso". Sem este slice, o usuário instala o Reclaim, ignora o aviso e descobre depois que "o app não bloqueia nada" — uma falha silenciosa do produto.

## Background

`AndroidManifest.xml` hoje declara apenas `PACKAGE_USAGE_STATS` e `QUERY_ALL_PACKAGES`. Para o Slice 2 entrar em produção precisamos somar:

- `FOREGROUND_SERVICE` — base para foreground services em qualquer API.
- `FOREGROUND_SERVICE_SPECIAL_USE` — `compileSdk 36`/`minSdk 34` exige que o serviço declare um tipo. Os tipos pré-categorizados (location, mediaPlayback, dataSync, etc.) não cabem em "vigiar uso e bloquear". `specialUse` é o catch-all, e exige uma `<property>` justificando, lida pela Play Console.
- `SYSTEM_ALERT_WINDOW` — desenha sobre outros apps. Não é permissão de runtime no fluxo padrão; é manual via `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`.
- `RECEIVE_BOOT_COMPLETED` + receiver — para o serviço subir após reboot conforme PRD-mãe. (Discutível se vai junto neste slice ou no 2; por simplicidade do produto, vai junto com as outras permissões aqui, já que é uma permissão.)

A tela `OnboardingPermissionsScreen` já tem o padrão do que queremos: lista de cards com explicação, estado, botão "Conceder" que abre uma activity de Settings, e re-checagem em `onResume` quando o usuário volta. Este slice replica esse padrão para a permissão de overlay.

A PRD-mãe define que o Onboarding **não é uma trava**: o usuário pode pular. Se pular, a Home mostra um aviso de que o bloqueio está inativo, com atalho para a configuração. Esse aviso na Home é parte deste slice (faz par com a permissão).

## Requirements

### Must Have

#### Manifesto

- `AndroidManifest.xml` declara as permissões: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `SYSTEM_ALERT_WINDOW`, `RECEIVE_BOOT_COMPLETED`.
- O serviço (a ser implementado em Slice 2 ou já presente, dependendo da ordem de merge) é declarado com `android:foregroundServiceType="specialUse"` e a `<property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" />` com texto justificando o uso para a Play Console (linguagem clara em PT/EN).
- A `BlockingActivity` (do Slice 2) está declarada no manifesto com tema transparente e flags conforme PRD do Slice 2.
- Um `BroadcastReceiver` para `BOOT_COMPLETED` está declarado, exportado conforme exigência da plataforma, e sobe o serviço quando há ao menos um `AddedApp`.

#### Onboarding

- `OnboardingPermissionsScreen` passa a listar **duas** permissões:
  1. **Acesso a uso** (existente): mantém o texto e fluxo atual.
  2. **Sobreposição de tela** (nova): texto explicando que é o que permite ao Reclaim mostrar a tela de bloqueio quando o limite é atingido. Botão "Conceder" abre `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` com `Uri` do pacote do app.
- Cada item mostra estado atual (`Concedido` ou `Pendente`), com a mesma diferenciação visual já usada para Acesso a uso.
- Ao retornar do Settings (lifecycle `onResume`), os dois estados são re-verificados sem ação manual.
- O botão "Continuar" da tela permanece habilitado mesmo com permissões pendentes (não é trava). Não há mudança no botão "Pular" se ele já existir.
- O texto de cada card é honesto sobre as consequências: a sobreposição de tela explica em uma frase que sem ela "o bloqueio não consegue aparecer sobre os apps".

#### Aviso na Home

- Quando o app está rodando e há ao menos um `AddedApp` mas a permissão de sobreposição não está concedida, a Home mostra um aviso (banner ou linha destacada acima do hero ring) com texto curto explicando que o bloqueio está inativo, e um atalho ("Conceder") que abre a mesma `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`.
- O aviso desaparece automaticamente quando a permissão é concedida (re-check em `onResume`).
- Se o usuário **não tem** `AddedApp` algum, o aviso não aparece, mesmo sem permissão. Bloqueio sem nenhum app pra bloquear é um não-problema.

### Should Have

- Testes:
  - Compose UI test em `OnboardingPermissionsScreen` cobrindo: ambos os cards visíveis, estados pendente/concedido renderizam corretamente, botão "Conceder" da sobreposição dispara o intent esperado (mock de launcher), e o "Continuar" permanece clicável com permissões pendentes.
  - Compose UI test no banner da Home: aparece quando há `AddedApp` e overlay negado; somem quando overlay concedido; some quando não há `AddedApp`.
- Helper minúsculo em `data/` (ou inline em uma `Permissions.kt`) que centraliza `Settings.canDrawOverlays(context)` para não espalhar a chamada por várias telas.

### Out of Scope

- **Solicitação programática de `SYSTEM_ALERT_WINDOW`.** Não existe no fluxo padrão; é sempre manual via Settings.
- **Texto da `<property>` finalizado para Play Console.** Este PRD entrega um texto utilizável; o conteúdo definitivo para submissão à loja é responsabilidade de quem submeter.
- **Reescrita visual do Onboarding.** Apenas adiciona a segunda linha; mantém layout e tom existentes.
- **Localização (i18n).** PT-BR como o resto do app hoje.
- **Notification permission.** A notificação do serviço é foreground service ongoing — em Android 13+ exige `POST_NOTIFICATIONS` para notificações regulares, mas a notificação de FGS é especial e não é regida por essa permissão de runtime no mesmo modo. Tratar `POST_NOTIFICATIONS` é Out of Scope; se aparecer crash em testes manuais, abrir issue dedicada.
- **Mudança no estado de "bloqueio inativo" exposto pelo serviço.** O Slice 2 já provê o que basta para a Home decidir se mostra o aviso (basta consultar `Settings.canDrawOverlays`).

## Constraints

- **Manifesto é um arquivo só.** Diff cirúrgico, com comentários inline justificando `specialUse` e a `<property>`.
- **Onboarding mantém o padrão existente.** Mesma estrutura de card, mesma tipografia, mesmos tokens. O único componente potencialmente novo é uma extensão do existente para receber dois itens.
- **Aviso da Home segue tokens.** `ReclaimWarn` ou tom equivalente já presente em `Color.kt`. Sem hex.
- **Re-checagem em `onResume`.** Reuso do hook `OnResume` existente em `ReclaimNavHost` (ou substituto se o refactor de Flow já tiver mergeado).

## Acceptance Criteria

### Manifesto

- Dado o repositório após o merge, quando se inspeciona `AndroidManifest.xml`, então estão declaradas: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `SYSTEM_ALERT_WINDOW`, `RECEIVE_BOOT_COMPLETED`.
- Dado o manifesto, quando se inspeciona a declaração do serviço de bloqueio, então `android:foregroundServiceType="specialUse"` está presente e há `<property>` com `android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE`.

### Onboarding com permissão de overlay

- Dado o usuário entra na tela de permissões e nenhuma das duas está concedida, quando a tela renderiza, então ele vê duas linhas — Acesso a uso e Sobreposição de tela — ambas marcadas como Pendente.
- Dado a permissão de overlay está pendente, quando o usuário toca em "Conceder" no card de sobreposição, então o app abre `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` para o pacote do Reclaim.
- Dado o usuário concede a permissão e volta ao app, quando a tela recebe `onResume`, então a linha de sobreposição passa para Concedida sem ação manual.
- Dado uma das permissões está pendente, quando o usuário toca em "Continuar", então a navegação avança normalmente para a Home.

### Re-checagem

- Dado a tela de Onboarding está aberta com sobreposição como Pendente, quando o usuário sai do app, vai a Settings, concede e volta, então a tela mostra Concedida no próximo `onResume`.
- Dado a Home está aberta com aviso visível e o usuário concede a permissão por outra via, quando ele volta para o app, então o aviso desaparece sem reload manual.

### Aviso na Home

- Dado o usuário tem ao menos um `AddedApp` e a permissão de sobreposição não está concedida, quando a Home renderiza, então existe um aviso visível afirmando que o bloqueio está inativo, com botão "Conceder".
- Dado o aviso está visível, quando o usuário toca em "Conceder", então o app abre a tela de Settings de overlay para o pacote do Reclaim.
- Dado o usuário tem zero `AddedApp` e a permissão está negada, quando a Home renderiza, então não há aviso de bloqueio inativo.
- Dado a permissão está concedida (independente do número de `AddedApp`), quando a Home renderiza, então não há aviso.

### Boot

- Dado o usuário tem ao menos um `AddedApp` e o dispositivo é reiniciado, quando o boot completa, então o receiver dispara e o serviço de bloqueio sobe.
- Dado o usuário tem zero `AddedApp` e o dispositivo é reiniciado, quando o boot completa, então o serviço **não** sobe.
