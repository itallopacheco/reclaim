---
description: Test setup and conventions
paths:
  - "app/src/test/**/*.kt"
  - "app/src/androidTest/**/*.kt"
---

# Tests

## Current state

Three real test suites live in this repo:

- **JVM domain tests** at `app/src/test/java/com/example/reclaim/domain/` — covers `apps/` (`SuggestAppsUseCase`, `SearchAppsUseCase`, `TodayScreenTimeUseCase`, `RankAppsForHomeUseCase`), `blocking/` (`ShouldBlockAppUseCase`), `habits/` (`HabitsTodaySummaryUseCase`), and `rewards/` (`CurrentRewardBalanceUseCase`, `ApplyRewardSpendUseCase`, `ApplyHabitRewardUseCase`, `ApplyHabitUnrewardUseCase`). Hand-rolled fakes in each feature's `fakes/` — most are immutable, but `FakeRewardsRepository` is mutable because the contract mutates. Pure JVM, no Android framework.
- **JVM data tests** at `app/src/test/java/com/example/reclaim/data/` — Robolectric-backed adapter tests for `PackageManagerAppCatalog`, `UsageStatsManagerStats`, `UsageEventsForegroundAppMonitor`, `DataStoreAddedAppsRepository`, `DataStoreHabitsRepository`, `DataStoreRewardsRepository`, and `RewardingHabitsRepository` (no Robolectric — pure delegation tests using domain fakes).
- **Compose UI tests** at `app/src/androidTest/java/com/example/reclaim/ui/screen/` — covers add/edit sheets for apps and habits, the three tab screens (`HomeScreen`, `AppsScreen`, `HabitsScreen`), the `BlockedAppScreen` overlay, and `OnboardingPermissionsScreen`. Mutable in-memory fakes in `fakes/` (separate from the JVM ones because `androidTest` and `test` source sets don't share code).

The default `ExampleUnitTest` and `ExampleInstrumentedTest` scaffolding files are still there — they validate the toolchain. Don't delete them.

## Frameworks available

- JUnit 4 (`junit:junit:4.13.2`)
- AndroidX JUnit (`androidx.test.ext:junit`)
- Espresso core
- Compose UI test (`androidx.compose.ui:ui-test-junit4`, `ui-test-manifest` for debug)

## Where new tests go

- **Unit tests** (no Android framework): `app/src/test/java/com/example/reclaim/...`. Mirror the `main` package structure.
- **Compose UI tests**: `app/src/androidTest/java/com/example/reclaim/...` using `createAndroidComposeRule<ComponentActivity>()`. Wrap the subject under `ReclaimTheme { ... }` so colors resolve.
- **Robolectric tests** for data adapters: `app/src/test/java/com/example/reclaim/data/...`. Robolectric stays out of `domain/` — those tests are pure JVM.

## Domain test conventions

For tests under `domain/`, see `domain.md`. Highlights:

- Test class = `<Subject>Test` in the same package as production
- Method names use Kotlin backticks (`fun \`does the thing\`()`)
- Hand-rolled fakes in `fakes/`, one file per interface, constructor-injected immutable state
- One behavior per test, one assertion per test
- No mocking framework

## Running

```bash
./gradlew test                    # all JVM unit tests (debug + release variants)
./gradlew :app:testDebugUnitTest  # debug variant only — what you want during TDD
./gradlew connectedAndroidTest    # instrumented + Compose UI tests (needs device/emulator)

# Run a single JVM test class (--tests is supported):
./gradlew :app:testDebugUnitTest --tests "com.example.reclaim.domain.apps.SuggestAppsUseCaseTest"

# Run a single instrumented test class (--tests is NOT supported here):
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.example.reclaim.ui.screen.AddAppSheetTest
```

**Locked-screen gotcha.** Running `connectedAndroidTest` against a locked device crashes with `IllegalStateException: No compose hierarchies found in the app...`. The error is misleading; the real cause is that the lock screen blocks the host Activity from launching. Always confirm the device is unlocked before running. JVM unit tests don't have this issue.

## Compose UI test patterns

Patterns proven on this codebase (Compose BOM 2024.09.00):

- **Extract a `*Content` composable for testability.** A screen wrapped in `ModalBottomSheet`/`Scaffold` is hard to drive directly. Expose an inner `*Content` taking the same parameters. Production calls it from inside the wrapper; tests call it directly with `composeRule.setContent`.
- **`contentDescription` belongs on the leaf semantics node, not the container.** A `BasicTextField` only takes focus when the semantics are on the field itself. `Modifier.semantics { contentDescription = "Search installed apps" }` goes on the `BasicTextField`, not its parent `Row`. `performTextInput` fails on a node that has no `RequestFocus` action.
- **Use `onAllNodesWithText(text).assertCountEquals(0)`** to assert absence. `assertDoesNotExist` exists in newer Compose UI test versions but isn't on the current BOM.
- **Pass test-only state as default-valued parameters.** `AddAppSheetContent(... initialQuota: Duration = QUOTA_DEFAULT)` lets the stepper-bound test reach 8h in one click instead of 28. Production keeps the default.
- **Mutable fakes for instrumented tests.** Production calls `addedApps.add(...)` on Save; the fake must accept mutation. The JVM fakes are deliberately immutable — duplicate them as mutable for `androidTest` rather than sharing.

## When to write tests

Match the global TDD policy in `~/.claude/CLAUDE.md`. For this codebase specifically:

- UI translation work (HTML → Compose) generally doesn't get tests — the `@Preview` is the verification.
- Add tests as soon as there's logic that isn't trivially visible in the preview: countdown timers, habit-completion state machines, time-budget math, navigation guards.
