---
description: Data layer conventions — adapters that implement domain interfaces
paths:
  - "app/src/main/java/com/example/reclaim/data/**/*.kt"
---

# Data layer

The `data/` package holds adapters that implement interfaces declared anywhere under `domain/` (`apps/`, `blocking/`, `habits/`). This is where Android framework code (`PackageManager`, `UsageStatsManager`, DataStore, foreground services, broadcast receivers) belongs. The domain stays free of `android.*` imports; the data layer earns the right to import them.

## Current state

**Read-only adapters**

- `PackageManagerAppCatalog(packageManager, ownPackageName)` — queries `Intent.ACTION_MAIN` + `CATEGORY_LAUNCHER`, excludes Reclaim's own package, caches the result with an `invalidate()` hook.
- `UsageStatsManagerStats(usageStatsManager, appOpsManager, packageName)` — `queryUsageStats(INTERVAL_DAILY, ...)` for both the 7-day average and today (since 00:00 local). Returns `emptyMap()` when usage access is denied. Exposes `hasUsageAccess()` for callers that need to gate UI.
- `UsageEventsForegroundAppMonitor(usageStatsManager)` — implements `ForegroundAppMonitor`. Calls `queryEvents(now - 10s, now)` and returns the package of the latest `ACTIVITY_RESUMED` event, or `null`. The 10-second lookback is a `private companion object` constant; raise it only if a test demands it.

**Write-bearing adapters**

- `DataStoreAddedAppsRepository(dataStore)` — persists `AddedApp` entries. `add` is idempotent by `packageName`, `delete` is a no-op for absent packages.
- `DataStoreHabitsRepository(dataStore)` — persists habits and per-day completion counts. Same idempotency contract; `markCompleteToday` increments, `unmarkToday` clears. Day-rollover is handled lazily via a `completion_day` marker — the first read after a local-date change clears yesterday's completions.
- `DataStoreRewardsRepository(dataStore)` — persists the daily reward pool as `rewards_balance_seconds: Long` plus a `rewards_day` marker. `addReward` / `subtractReward` mutate freely (negative balances allowed); `spend` clamps at zero. Day rollover mirrors the habits pattern: on a new local date the balance reads zero and the marker is rewritten.
- `RewardingHabitsRepository(inner: HabitsRepository, rewards: RewardsRepository)` — Kotlin-`by`-delegation decorator that overrides only `markCompleteToday` (adds `Habit.reward` to the pool) and `unmarkToday` (subtracts when a completion existed). `ReclaimApplication.habits` exposes the *wrapped* instance, so any caller of `markCompleteToday`/`unmarkToday` automatically credits or debits the pool. Unknown habit ids are a no-op on both sides.

**Blocking runtime**

- `BlockingService` — foreground service (`FOREGROUND_SERVICE_TYPE_SPECIAL_USE`). Polls `foregroundAppMonitor.currentForegroundPackage()` every `POLL_INTERVAL = 1.seconds` via `Handler(Looper.getMainLooper()).postDelayed(...)` and dispatches `BlockingActivity` once per blocked package (deduped by `lastBlockedDispatched`). Each tick first calls `applyRewardSpend.invoke(elapsed)` (using `SystemClock.elapsedRealtime()` deltas) when the foreground app is added and over its quota — *before* asking `shouldBlockApp.invoke(...)` — so a positive balance can mask the block until it ticks down to zero. Notification text is updated per tick to `Saldo bônus: X min restantes` while spending and reverts to `Reclaim está protegendo seus limites` otherwise; `notify` is only re-issued when the text actually changes. Skips overlay dispatch if `Permissions.canDrawOverlays()` is false. Calls `stopSelf()` when no apps are added. `companion object` exposes `start(ctx)` / `stop(ctx)` — those are the only entry points; never `Intent`-construct the service from elsewhere.
- `BlockingServiceController` — the orchestrator that decides when to call `BlockingService.start/stop`.
- `BootCompletedReceiver` — re-arms `BlockingService` after `BOOT_COMPLETED`.
- `Permissions` — pure helpers: `hasUsageAccess(context)`, `canDrawOverlays(context)`. No DI; called directly where needed.

All adapters are constructed once per process in `ReclaimApplication` as `by lazy` properties. No DI framework, no singletons.

## Shape of an adapter

Match the domain interface's contract exactly. No additional public methods unless the call site needs framework-specific affordances (`hasUsageAccess()` on `UsageStatsManagerStats`, `invalidate()` on `PackageManagerAppCatalog`).

`addedApps()` returns a defensive copy. `add()` is **idempotent by `packageName`** — calling it twice with the same package replaces the entry, never accumulates. `delete()` is also idempotent: deleting an absent package is a no-op.

## Decorator adapters

`RewardingHabitsRepository` is a precedent for layering cross-feature side-effects in `data/`: a class that implements one domain interface (`HabitsRepository`), takes another as a collaborator (`RewardsRepository`), delegates the bulk via `: HabitsRepository by inner`, and overrides only the methods that need the side-effect. The wiring in `ReclaimApplication` swaps the lazy property to expose the decorated instance — callers see the original interface. Use this pattern instead of either (a) calling the secondary use case from every UI site that mutates the primary repository, or (b) folding cross-feature logic into the primary adapter.

## Filters that depend on real-world knowledge

The "exclude Reclaim's own package" rule lives in `PackageManagerAppCatalog`, **not** the domain. Same for "exclude apps without a launcher activity." The data adapter classifies; the domain reads.

## Caching

`PackageManagerAppCatalog` caches `installedApps()` to avoid hitting `PackageManager` on every recomposition. The cache is invalidated explicitly via `invalidate()` — `ReclaimNavHost` calls it from the AddApp modal's `OnResume` so the user picks up newly-installed apps without a relaunch. Any future cache (e.g. wrapping `UsageStatsManagerStats`) needs the same explicit invalidation story tied to a real lifecycle event.

## Polling and threading

`BlockingService` runs its tick on the main looper. The work each tick does is cheap (a DataStore-backed list read and a synchronous `queryEvents` call), so a `Handler` schedule is enough — don't reach for coroutines or a worker thread without a measurement that says you need them. Wrap the tick in `try/finally` so `postDelayed` always re-arms even if the call throws (precedent: `BlockingService.tick`).

## Tests

Adapter tests live at `app/src/test/java/com/example/reclaim/data/` and use Robolectric. Domain fakes (`FakeAppCatalog`, `FakeUsageStats`, `FakeAddedAppsRepository`, `FakeBlockingDecision`, `FakeHabitsRepository`) are not for adapter tests — they exist to drive use case tests in pure JVM.

## Anti-patterns

- Adding domain logic to a data adapter. Sorting, top-N, "exclude already-added" — those live in the use cases. Adapters return raw data plus the structural filters that the framework dictates (launcher activity present, app op granted, etc.).
- Coroutines that swallow errors. Propagate failures so the use case (and its callers) decide what to do. No silent `try { ... } catch { emptyList() }`. The exception is permission-denied paths where the contract is "return empty"; those are explicit, not catch-alls.
- Importing `android.*` from domain code via the adapter. The adapter is one-way: it imports from `domain/`, never the reverse.
- Caching reads inside an adapter without an invalidation story. `PackageManagerAppCatalog.invalidate()` is the precedent — wire any new cache to a lifecycle event the caller controls.
