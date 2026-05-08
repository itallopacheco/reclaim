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
├── ReclaimApplication.kt        # DI root; lazy adapters + use cases, reached via context.reclaimApplication()
├── navigation/
│   ├── Destinations.kt          # sealed Destination(route) + TabDestination enum
│   └── ReclaimNavHost.kt        # NavHost wiring; screens get callbacks, not NavController
├── domain/                      # pure-JVM, no android.* imports
│   ├── apps/                    # added-apps feature
│   │   ├── App.kt, SuggestedApp.kt, AddedApp.kt, HomeAppRow.kt, HomeAppStatus.kt
│   │   ├── AppCatalog.kt, UsageStats.kt, AddedAppsRepository.kt   # interfaces
│   │   ├── SuggestAppsUseCase.kt, SearchAppsUseCase.kt
│   │   ├── TodayScreenTimeUseCase.kt        # sums today's usage across added apps (Home hero ring)
│   │   └── RankAppsForHomeUseCase.kt        # ranked HomeAppRow list, injects BlockingDecision for the badge
│   ├── blocking/                # app-blocking feature
│   │   ├── BlockingDecision.kt              # interface: isBlocked(packageName)
│   │   ├── ForegroundAppMonitor.kt          # interface: currentForegroundPackage()
│   │   └── ShouldBlockAppUseCase.kt         # implements BlockingDecision: today >= dailyQuota
│   ├── habits/
│   │   ├── Habit.kt, HabitIcon.kt, HabitsTodaySummary.kt
│   │   ├── HabitsRepository.kt              # CRUD + completionsToday()/markCompleteToday/unmarkToday
│   │   └── HabitsTodaySummaryUseCase.kt
│   └── rewards/                # daily reward pool (grows with habits, spent in over-quota apps)
│       ├── RewardsRepository.kt             # currentBalance / addReward / subtractReward / spend
│       ├── CurrentRewardBalanceUseCase.kt
│       ├── ApplyRewardSpendUseCase.kt       # clamps at zero
│       ├── ApplyHabitRewardUseCase.kt
│       └── ApplyHabitUnrewardUseCase.kt     # may go negative
├── data/                        # adapters that implement the domain interfaces
│   ├── PackageManagerAppCatalog.kt          # Intent.ACTION_MAIN + CATEGORY_LAUNCHER, excludes Reclaim
│   ├── UsageStatsManagerStats.kt            # queryUsageStats(INTERVAL_DAILY) for 7-day avg + today
│   ├── UsageEventsForegroundAppMonitor.kt   # queryEvents w/ 10s lookback, last ACTIVITY_RESUMED
│   ├── DataStoreAddedAppsRepository.kt      # AddedApp persistence via DataStore Preferences
│   ├── DataStoreHabitsRepository.kt         # Habits + per-day completion counts via DataStore
│   ├── DataStoreRewardsRepository.kt        # reward balance (Long seconds) + day rollover marker
│   ├── RewardingHabitsRepository.kt         # decorator: applies reward side-effects on mark/unmark
│   ├── BlockingService.kt                   # foreground service, polls foreground app every 1s
│   ├── BlockingServiceController.kt         # start/stop hooks for the service
│   ├── BootCompletedReceiver.kt             # restarts BlockingService after reboot
│   └── Permissions.kt                       # usage-access + canDrawOverlays helpers
└── ui/
    ├── main/
    │   └── MainScaffold.kt      # tab bar host (Home / Apps / Habits) + FAB
    ├── screen/                  # one file per screen, public @Composable
    │   ├── OnboardingValueScreen.kt, OnboardingPermissionsScreen.kt
    │   ├── HomeScreen.kt, AppsScreen.kt, HabitsScreen.kt
    │   ├── AddAppSheet.kt, EditAppSheet.kt
    │   ├── AddHabitSheet.kt, EditHabitSheet.kt
    │   ├── BlockingActivity.kt, BlockedAppScreen.kt    # overlay shown when an app exceeds its quota
    │   ├── BlockingNowBadge.kt                          # shared badge used on Home + Apps cards
    │   └── LockScreen.kt
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
- **`data/`** — adapters implementing the domain interfaces (`PackageManagerAppCatalog`, `UsageStatsManagerStats`, `UsageEventsForegroundAppMonitor`, `DataStoreAddedAppsRepository`, `DataStoreHabitsRepository`). Filters that depend on real-world knowledge (e.g. excluding Reclaim's own package) belong here, **not** in the domain.

### App blocking

`BlockingService` is a foreground service started from `MainActivity`. Every second it asks `ForegroundAppMonitor.currentForegroundPackage()`, runs `ShouldBlockAppUseCase.invoke(pkg)`, and — if blocked and `Permissions.canDrawOverlays()` — launches `BlockingActivity` on top of the offending app. `BootCompletedReceiver` restarts the service after a reboot. The service stops itself when the user has no added apps left.

`RankAppsForHomeUseCase` injects `BlockingDecision` so each `HomeAppRow` carries `isBlockingNow`, surfaced as `BlockingNowBadge` on Home and Apps cards.

### Rewards

A single daily reward pool ("saldo bônus") grows when habits are completed and is consumed in real time while the user stays in apps that already exhausted their quota. The pool is a `Duration` stored as `Long` seconds, with a `rewards_day` marker that resets the balance on a local-date change. Negative balances are allowed when the user undoes a habit they already spent.

- `ShouldBlockAppUseCase` is the only place the rule lives: it returns `true` only when `usage >= dailyQuota AND balance <= 0`. Both `BlockingService` and `RankAppsForHomeUseCase` consume the unchanged `BlockingDecision` interface.
- `RewardingHabitsRepository` wraps `DataStoreHabitsRepository` so that every `markCompleteToday` adds `Habit.reward` to the pool and every `unmarkToday` subtracts it. Each habit completion adds another reward — multiple completions per day stack.
- `BlockingService.pollOnce` decrements the balance (using `SystemClock.elapsedRealtime()` deltas) before the block check whenever the foreground app is added and over its quota. The service notification's content text reflects state per tick: `Saldo bônus: X min restantes` while spending, `Reclaim está protegendo seus limites` otherwise.
- `HomeScreen` renders two pills above the hero ring (`EarnedPill` + `AvailablePill`), each independently hidden when its value is `<= 0`. The outer `HomeScreen` polls `CurrentRewardBalanceUseCase` every 1s via `LaunchedEffect` so the available value follows the spend in near-real time.

`ReclaimApplication` is the DI root — it owns the lazy adapter instances and exposes use cases as properties. Screens reach it via `context.reclaimApplication()` and receive the dependencies they need through `ReclaimNavHost`. No DI framework, no ViewModels.

Domain code is tested in pure JVM with hand-rolled fakes under `app/src/test/.../domain/<feature>/fakes/`. Data adapters are tested with Robolectric in `app/src/test/.../data/`.

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
├── domain/
│   ├── apps/
│   │   ├── SuggestAppsUseCaseTest.kt
│   │   ├── SearchAppsUseCaseTest.kt
│   │   ├── TodayScreenTimeUseCaseTest.kt
│   │   ├── RankAppsForHomeUseCaseTest.kt
│   │   └── fakes/  (FakeAppCatalog, FakeUsageStats, FakeAddedAppsRepository)
│   ├── blocking/
│   │   ├── ShouldBlockAppUseCaseTest.kt
│   │   └── fakes/  (FakeBlockingDecision)
│   ├── habits/
│   │   ├── HabitsTodaySummaryUseCaseTest.kt
│   │   └── fakes/  (FakeHabitsRepository)
│   └── rewards/
│       ├── CurrentRewardBalanceUseCaseTest.kt, ApplyRewardSpendUseCaseTest.kt
│       ├── ApplyHabitRewardUseCaseTest.kt, ApplyHabitUnrewardUseCaseTest.kt
│       └── fakes/  (FakeRewardsRepository)
└── data/                                   # Robolectric-backed adapter tests
    ├── PackageManagerAppCatalogTest.kt
    ├── UsageStatsManagerStatsTest.kt
    ├── UsageEventsForegroundAppMonitorTest.kt
    ├── DataStoreAddedAppsRepositoryTest.kt
    ├── DataStoreHabitsRepositoryTest.kt
    ├── DataStoreRewardsRepositoryTest.kt
    └── RewardingHabitsRepositoryTest.kt

app/src/androidTest/java/com/example/reclaim/ui/screen/
├── AddAppSheetTest.kt, EditAppSheetTest.kt
├── AddHabitSheetTest.kt, EditHabitSheetTest.kt
├── HomeScreenTest.kt, AppsScreenTest.kt, HabitsScreenTest.kt
├── BlockedAppScreenTest.kt
├── OnboardingPermissionsScreenTest.kt
└── fakes/                                 # mutable in-memory fakes for UI tests
    ├── FakeAppCatalog.kt, FakeUsageStats.kt, FakeAddedAppsRepository.kt
    └── FakeHabitsRepository.kt
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
