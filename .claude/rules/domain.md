---
description: Domain layer conventions — pure-JVM use cases, interfaces, fakes
paths:
  - "app/src/main/java/com/example/reclaim/domain/**/*.kt"
  - "app/src/test/java/com/example/reclaim/domain/**/*.kt"
---

# Domain layer

Pure Kotlin/JVM. No Android framework imports anywhere under `domain/`. If a file needs `android.*` it belongs in `data/`, not here.

## Shape of a use case

```kotlin
class SuggestAppsUseCase(
    private val catalog: AppCatalog,
    private val usageStats: UsageStats,
    private val addedApps: AddedAppsRepository,
) {
    fun invoke(): List<SuggestedApp> = ...
}
```

- Constructor-injected dependencies, all interfaces.
- A single `fun invoke(...)` is the public surface. Plain `fun`, not `operator fun`. The test calls `useCase.invoke(...)` explicitly.
- Use cases compose pure transformations on lists (`.filter`, `.map`, `.sortedByDescending`, `.take`). Single-responsibility; the use case decides the *order* of the operations, not the operations themselves.
- Constants like `MAX_SUGGESTIONS = 10` live in a `private companion object` inside the use case.

## Entities

- Always `data class` for entities (`App`, `SuggestedApp`, `AddedApp`). `assertEquals` on lists relies on structural equality.
- One file per type, even when small.
- Fields are immutable `val`. No mutation in the domain.

Current entities:

```kotlin
// apps
data class App(val packageName: String, val displayName: String, val isLauncherApp: Boolean)
data class SuggestedApp(val app: App, val avgDaily: Duration)
data class AddedApp(val packageName: String, val dailyQuota: Duration)
data class HomeAppRow(val app: App, val today: Duration, val quota: Duration,
                      val status: HomeAppStatus, val isBlockingNow: Boolean = false)
enum class HomeAppStatus { OK, WARN, OVER }

// habits
data class Habit(val id: Long, val name: String, val icon: HabitIcon, val reward: Duration)
data class HabitsTodaySummary(val pending: List<Habit>, val completed: List<Habit>,
                              val totalReward: Duration)
```

The rewards feature has no entity — the daily balance is exposed as a plain `kotlin.time.Duration` from `RewardsRepository.currentBalance()`.

## Interfaces

- One method per interface where possible, named for what it returns (`installedApps()`, `avgDailyUsageLast7Days()`, `addedApps()`). No `get` prefix — Kotlin convention.
- Return types are read-only collections (`List`, `Set`, `Map`).
- The interface lives next to the use cases that consume it. Move only if a second consumer appears.

Current interfaces:

```kotlin
// apps
interface AppCatalog { fun installedApps(): List<App> }

interface UsageStats {
    fun avgDailyUsageLast7Days(): Map<String, Duration>
    fun usageToday(): Map<String, Duration>
}

interface AddedAppsRepository {
    fun addedApps(): List<AddedApp>
    fun add(addedApp: AddedApp)
    fun delete(packageName: String)
}

// blocking
interface BlockingDecision { fun isBlocked(packageName: String): Boolean }
interface ForegroundAppMonitor { fun currentForegroundPackage(): String? }

// habits
interface HabitsRepository {
    fun habits(): List<Habit>
    fun add(habit: Habit); fun update(habit: Habit); fun delete(id: Long)
    fun completionsToday(): Map<Long, Int>
    fun markCompleteToday(id: Long, atMinuteOfDay: Int)
    fun unmarkToday(id: Long)
}

// rewards
interface RewardsRepository {
    fun currentBalance(): Duration
    fun addReward(amount: Duration)
    fun subtractReward(amount: Duration)  // allows negative
    fun spend(amount: Duration)            // clamps at zero
}
```

Write-bearing interfaces (`AddedAppsRepository`, `HabitsRepository`, `RewardsRepository`) are idempotent by primary key — `add` replaces a same-key entry, `delete`/`unmarkToday` on an absent key is a no-op. `RewardsRepository` mutations always succeed — the only contract is that `spend` never drives the balance below zero, while `subtractReward` may. The behavioral contract belongs to implementations in `data/`, not the domain itself.

Current use cases:

- `SuggestAppsUseCase(catalog, usageStats, addedApps)` — top-N apps by avg daily usage, excluding already-added.
- `SearchAppsUseCase(catalog, addedApps)` — substring search over installed apps, excluding already-added.
- `TodayScreenTimeUseCase(addedApps, usageStats)` — sums `usageToday()` across the user's added packages. Drives the Home hero ring.
- `RankAppsForHomeUseCase(addedApps, usageStats, catalog, blockingDecision)` — returns ranked `HomeAppRow`s for Home/Apps; computes `HomeAppStatus` (OK / WARN at ≥80% / OVER) and a separate `isBlockingNow` flag.
- `ShouldBlockAppUseCase(addedApps, usageStats, rewards)` — *implements* `BlockingDecision`. Returns `true` when `usage >= dailyQuota AND rewards.currentBalance() <= 0`. A use case being its own interface implementation is intentional: the public `invoke()` is for screens that want imperative phrasing, and `isBlocked()` is what `RankAppsForHomeUseCase` and `BlockingService` consume through the abstraction.
- `HabitsTodaySummaryUseCase(habits)` — partitions habits by today's completion count and folds rewards into `HabitsTodaySummary`.
- `CurrentRewardBalanceUseCase(rewards)` — read-through wrapper. Drives the Home `AvailablePill` and the foreground service's notification text.
- `ApplyRewardSpendUseCase(rewards)` — used by `BlockingService.pollOnce` to debit the pool by the elapsed wall-clock between ticks; relies on `spend`'s clamp-at-zero contract.
- `ApplyHabitRewardUseCase(rewards)` / `ApplyHabitUnrewardUseCase(rewards)` — invoked by the `RewardingHabitsRepository` decorator on `markCompleteToday` / `unmarkToday`.

## What the domain does NOT decide

- **No knowledge of real-world identifiers.** The domain doesn't know that the Reclaim app's package is `com.example.reclaim` or that some apps are "system apps". Filtering Reclaim itself happens in the `data/` adapter (`PackageManagerAppCatalog`), not here.
- **No persistence concerns.** The repository interface returns the current state. How it got there (DataStore, in-memory, network) is not the domain's business.
- **No coroutines yet.** Use cases are synchronous. When real I/O lands in `data/`, that layer will introduce `suspend` and the domain will follow.

## Defaults and missing data

- `Map[key]` returning `null` for an unmapped key is the JVM default. The use case decides what to do:
  - When the test guarantees every key is mapped, use `usage.getValue(packageName)` (throws on absence — fail loud).
  - When a future test introduces a missing key, that test dictates the behavior. Don't pre-emptively add `?: Duration.ZERO` defaults.

## Tests

Tests mirror production:

```
app/src/test/java/com/example/reclaim/domain/
├── apps/
│   ├── SuggestAppsUseCaseTest.kt, SearchAppsUseCaseTest.kt
│   ├── TodayScreenTimeUseCaseTest.kt, RankAppsForHomeUseCaseTest.kt
│   └── fakes/  (FakeAppCatalog, FakeUsageStats, FakeAddedAppsRepository)
├── blocking/
│   ├── ShouldBlockAppUseCaseTest.kt
│   └── fakes/  (FakeBlockingDecision)
├── habits/
│   ├── HabitsTodaySummaryUseCaseTest.kt
│   └── fakes/  (FakeHabitsRepository)
└── rewards/
    ├── CurrentRewardBalanceUseCaseTest.kt, ApplyRewardSpendUseCaseTest.kt
    ├── ApplyHabitRewardUseCaseTest.kt, ApplyHabitUnrewardUseCaseTest.kt
    └── fakes/  (FakeRewardsRepository — mutable since the contract mutates)
```

- One test class per use case, named `<UseCase>Test`.
- Test method names use Kotlin backticks: `fun \`excludes apps with zero avg daily usage\`()`.
- One behavior per test, one `assertEquals` per test.
- Fakes are constructor-injected, immutable, in-memory:
  ```kotlin
  class FakeAppCatalog(private val apps: List<App>) : AppCatalog {
      override fun installedApps() = apps
  }
  ```
- No mocking framework. No Robolectric in the domain tests.
- Run a single test class during TDD:
  ```bash
  ./gradlew :app:testDebugUnitTest --tests "com.example.reclaim.domain.apps.SuggestAppsUseCaseTest"
  ```

## Anti-patterns

- Adding a feature to a use case before a failing test demands it (top-N cap, exclusion rule, default value).
- Returning a richer type "for the UI" without a test asserting on the extra field. The test that needs it adds it.
- Catching exceptions to provide fallbacks. Let it throw; the test that exercises the missing case decides the contract.
- Importing anything from `android.*`, `androidx.*`, `kotlinx.coroutines.*`. If it's needed, this code probably belongs in `data/`.
