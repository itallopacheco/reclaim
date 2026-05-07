# Reclaim

Android screen-time app. Early-stage PoC. UI is being translated from a Claude Design HTML export — see `~/Downloads/Reclaim _standalone_.html` for the source designs.

## Stack

- Kotlin 2.0.21, Android Gradle Plugin 8.13.2
- Jetpack Compose (BOM 2024.09.00), Material3
- Navigation Compose 2.8.4
- Material Icons Extended (from BOM)
- compileSdk 36, targetSdk 36, minSdk 34, JVM target 11
- Single module: `:app`, namespace `com.example.reclaim`

Version catalog: `gradle/libs.versions.toml`. Add new deps there, then reference via `libs.*` in `app/build.gradle.kts`.

## Commands

```bash
./gradlew :app:compileDebugKotlin   # fast compile check
./gradlew :app:assembleDebug        # build debug APK
./gradlew :app:installDebug         # install to connected device/emulator
./gradlew test                      # all JVM unit tests (debug + release variants)
./gradlew :app:testDebugUnitTest    # JVM unit tests, debug variant only (faster)
./gradlew connectedAndroidTest      # instrumented tests (needs device)
./gradlew lint                      # Android lint
./gradlew clean
```

During TDD, run a single test class to keep the loop tight:

```bash
# JVM unit test (--tests is supported by JUnit Gradle):
./gradlew :app:testDebugUnitTest --tests "com.example.reclaim.domain.apps.SuggestAppsUseCaseTest"

# Instrumented / Compose UI test (--tests is NOT supported here):
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.example.reclaim.ui.screen.AddAppSheetTest
```

> Instrumented tests need the device unlocked. The "No compose hierarchies found" error from `connectedAndroidTest` usually means the lock screen blocked the host Activity from launching, not a real Compose problem. Unlock and re-run.

Launch after install: `adb shell am start -n com.example.reclaim/.MainActivity`.

For UI iteration prefer Android Studio's `@Preview` over running the app — every screen has a preview at the bottom of its file.

## Package layout

```
app/src/main/java/com/example/reclaim/
├── MainActivity.kt              # entry point, hosts ReclaimNavHost under ReclaimTheme
├── navigation/
│   ├── Destinations.kt          # sealed Destination(route) + TabDestination enum
│   └── ReclaimNavHost.kt        # NavHost wiring; screens get callbacks, not NavController
├── domain/
│   └── apps/                    # pure-JVM domain layer for the "added apps" feature
│       ├── App.kt, SuggestedApp.kt, AddedApp.kt
│       ├── AppCatalog.kt, UsageStats.kt, AddedAppsRepository.kt   # interfaces
│       ├── SuggestAppsUseCase.kt
│       └── SearchAppsUseCase.kt
├── data/                        # adapters that implement the domain interfaces
│   ├── DemoAppCatalog.kt        # static list of 10 popular apps (bootstrap; replaced in fatia C)
│   ├── DemoUsageStats.kt        # static avg-usage map matching the demo catalog
│   └── InMemoryAddedAppsRepository.kt   # process-lifetime store; idempotent add() by packageName
└── ui/
    ├── main/
    │   └── MainScaffold.kt      # tab bar host (Home / Apps / Habits) + FAB
    ├── screen/                  # one file per screen, public @Composable
    │   ├── OnboardingValueScreen.kt, OnboardingPermissionsScreen.kt
    │   ├── HomeScreen.kt, AppsScreen.kt, HabitsScreen.kt
    │   ├── AddAppSheet.kt, AddHabitSheet.kt    # ModalBottomSheet routes
    │   └── LockScreen.kt        # full design translation, the rest are stubs
    └── theme/
        ├── Color.kt             # Reclaim* design tokens
        ├── Theme.kt             # ReclaimTheme, light-only, no dynamic color
        └── Type.kt
```

Start destination is `Destination.OnboardingValue`. After onboarding, `Destination.Home` is the entry to the tab graph.

## Architecture

Three loose layers. No DI framework yet, no ViewModels yet.

- **`ui/`** — Compose screens and the tab scaffold. Screens are pure presentation: state via `remember`, navigation via callbacks. Screens consume domain interfaces directly (constructor parameters), no ViewModel layer.
- **`domain/`** — pure Kotlin/JVM. Use cases (`class Foo(deps...) { fun invoke(...) }`) operating on data classes (`App`, `SuggestedApp`, `AddedApp`). Dependencies are interfaces (`AppCatalog`, `UsageStats`, `AddedAppsRepository`).
- **`data/`** — adapters implementing the domain interfaces. Today: `Demo*` static singletons + `InMemoryAddedAppsRepository`. Fatia C will swap these for `PackageManagerAppCatalog`, `UsageStatsManagerStats`, `DataStoreAddedAppsRepository`. Filters that depend on real-world knowledge (e.g. excluding Reclaim's own package) belong here, **not** in the domain.

Domain code is tested in pure JVM with hand-rolled fakes in `app/src/test/.../domain/apps/fakes/`. No mocking framework, no Robolectric in the domain tests.

## Conventions

- **Screen composables** are public top-level functions taking callbacks (`onBack`, `onGoToHabits`, ...). They never receive `NavController`. Wiring lives in `ReclaimNavHost.kt`.
- **Section composables** within a screen (TopBar, Hero, FooterCta, ...) are `private @Composable` in the same file. Don't extract to siblings unless reused.
- **Screen-local state** uses `remember { mutableStateOf(...) }`. No ViewModel layer yet.
- **Colors come from `ui/theme/Color.kt`** (`ReclaimTeal`, `ReclaimInk`, `ReclaimBg`, etc.). Never hardcode hex in screens. `ReclaimTheme` maps tokens onto Material3 slots; using `MaterialTheme.colorScheme.primary` resolves to `ReclaimTeal`.
- **No dynamic color.** `ReclaimTheme` is locked to a static light scheme so the design palette renders consistently.
- **Sizes in dp**, font sizes in sp. The HTML uses `text-[14.5px]` style — translate as `14.5.sp`.
- **Icons:** Lucide icons in the design map to closest Material Icons Extended equivalents (e.g. `lucide:sparkles` → `Icons.Filled.AutoAwesome`, `lucide:target` → `Icons.Filled.GpsFixed`, `lucide:book-open` → `Icons.AutoMirrored.Filled.MenuBook`). Document the mapping inline if non-obvious.

## Design translation flow

1. Source: `~/Downloads/Reclaim _standalone_.html` (Claude Design export). Real markup is in a JSON-stringified blob inside a JS bundler envelope — extract with Python before grepping.
2. The export defines CSS variables at the top (`--bg`, `--ink`, `--teal`, etc.). All are mirrored in `ui/theme/Color.kt` as `Reclaim*` constants.
3. When implementing a new screen: extract its template from the HTML, translate 1:1 to Compose, reuse existing tokens, add `@Preview` at the bottom.

## Tests

JUnit4 for JVM unit tests, `androidx.compose.ui:ui-test-junit4` for Compose UI tests on a real device.

Layout:

```
app/src/test/java/com/example/reclaim/
└── domain/apps/
    ├── SuggestAppsUseCaseTest.kt          # 6 tests
    ├── SearchAppsUseCaseTest.kt           # 4 tests
    └── fakes/
        ├── FakeAppCatalog.kt
        ├── FakeUsageStats.kt
        └── FakeAddedAppsRepository.kt

app/src/androidTest/java/com/example/reclaim/ui/screen/
├── AddAppSheetTest.kt                     # 10 Compose UI tests
└── fakes/                                 # in-memory mutable fakes for UI
    ├── FakeAppCatalog.kt
    ├── FakeUsageStats.kt
    └── FakeAddedAppsRepository.kt
```

The default `ExampleUnitTest` and `ExampleInstrumentedTest` scaffolding are kept — they validate the toolchain.

Conventions:

- Test class name = `<Subject>Test`, same package as production code
- Test method names use Kotlin backticked descriptions for JVM tests (`fun \`excludes apps with zero avg daily usage\`()`); camelCase for instrumented tests works fine too
- Fakes live in a `fakes/` subpackage of the source set, one file per interface
- Domain tests stay JVM-only — no Android framework, no Robolectric
- Compose UI tests use `createAndroidComposeRule<ComponentActivity>()` and wrap subjects in `ReclaimTheme { ... }`

UI test patterns that work in this codebase:

- **Extract a `*Content` composable.** A screen wrapped in `ModalBottomSheet` (or a `Scaffold`) is hard to test directly. Expose an inner `*Content` composable that takes the same parameters and call it from the test with `composeRule.setContent`. The wrapper composable still calls it in production.
- **Place `contentDescription` on the leaf node, not the container.** A `BasicTextField` inside a `Row` only receives focus if the semantics are on the field itself. `Modifier.semantics { contentDescription = "Search installed apps" }` goes on the `BasicTextField`, not the surrounding `Row`.
- **Use `onAllNodesWithText(text).assertCountEquals(0)`** to assert absence. `assertDoesNotExist` exists in newer Compose UI test versions but isn't in the current BOM.
- **Pass test-only state as default-valued parameters.** Stepper bound tests use `initialQuota = 7.hours + 45.minutes` so the test reaches the upper bound in one click instead of 28.
