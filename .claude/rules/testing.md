---
description: Test setup and conventions
paths:
  - "app/src/test/**/*.kt"
  - "app/src/androidTest/**/*.kt"
---

# Tests

## Current state

Only the default Android Studio scaffolding is committed:

- `app/src/test/java/com/example/reclaim/ExampleUnitTest.kt` — JVM unit test
- `app/src/androidTest/java/com/example/reclaim/ExampleInstrumentedTest.kt` — instrumented test

No domain logic exists yet, so there are no real tests to mirror. Don't delete the scaffolding files — they validate the test toolchain works.

## Frameworks available

- JUnit 4 (`junit:junit:4.13.2`)
- AndroidX JUnit (`androidx.test.ext:junit`)
- Espresso core
- Compose UI test (`androidx.compose.ui:ui-test-junit4`, `ui-test-manifest` for debug)

## Where new tests go

- **Unit tests** (no Android framework): `app/src/test/java/com/example/reclaim/...`. Mirror the `main` package structure.
- **Compose UI tests**: `app/src/androidTest/java/com/example/reclaim/...` using `createComposeRule()`. Wrap the subject under `ReclaimTheme { ... }` so colors resolve.

## Running

```bash
./gradlew test                    # all JVM unit tests
./gradlew connectedAndroidTest    # instrumented + Compose UI tests (needs device/emulator)
./gradlew :app:testDebugUnitTest  # debug variant only
```

## When to write tests

Match the global TDD policy in `~/.claude/CLAUDE.md`. For this codebase specifically:

- UI translation work (HTML → Compose) generally doesn't get tests — the `@Preview` is the verification.
- Add tests as soon as there's logic that isn't trivially visible in the preview: countdown timers, habit-completion state machines, time-budget math, navigation guards.
