---
description: Test setup and conventions
paths:
  - "app/src/test/**/*.kt"
  - "app/src/androidTest/**/*.kt"
---

# Tests

## Current state

Real tests live in `app/src/test/java/com/example/reclaim/domain/apps/` covering the use cases (`SuggestAppsUseCase`, `SearchAppsUseCase`). Hand-rolled fakes in a `fakes/` subpackage stand in for the interfaces. Pure JVM, no Android framework.

The default `ExampleUnitTest` and `ExampleInstrumentedTest` scaffolding files are still there — they validate the toolchain. Don't delete them.

No Compose UI tests have been written yet. The PRDs for fatias B and C call for them; they'll land in `app/src/androidTest/`.

## Frameworks available

- JUnit 4 (`junit:junit:4.13.2`)
- AndroidX JUnit (`androidx.test.ext:junit`)
- Espresso core
- Compose UI test (`androidx.compose.ui:ui-test-junit4`, `ui-test-manifest` for debug)

## Where new tests go

- **Unit tests** (no Android framework): `app/src/test/java/com/example/reclaim/...`. Mirror the `main` package structure.
- **Compose UI tests**: `app/src/androidTest/java/com/example/reclaim/...` using `createComposeRule()`. Wrap the subject under `ReclaimTheme { ... }` so colors resolve.
- **Robolectric tests** (when fatia C lands): `app/src/test/java/com/example/reclaim/data/...`. Robolectric stays out of `domain/` — those tests are pure JVM.

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

# Run a single test class (TDD loop):
./gradlew :app:testDebugUnitTest --tests "com.example.reclaim.domain.apps.SuggestAppsUseCaseTest"
```

## When to write tests

Match the global TDD policy in `~/.claude/CLAUDE.md`. For this codebase specifically:

- UI translation work (HTML → Compose) generally doesn't get tests — the `@Preview` is the verification.
- Add tests as soon as there's logic that isn't trivially visible in the preview: countdown timers, habit-completion state machines, time-budget math, navigation guards.
