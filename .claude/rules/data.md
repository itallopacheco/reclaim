---
description: Data layer conventions — adapters that implement domain interfaces
paths:
  - "app/src/main/java/com/example/reclaim/data/**/*.kt"
---

# Data layer

The `data/` package holds adapters that implement the interfaces declared in `domain/apps/`. This is where Android framework code (`PackageManager`, `UsageStatsManager`, DataStore) belongs. The domain stays free of `android.*` imports; the data layer earns the right to import them.

## Current state

- `PackageManagerAppCatalog(packageManager, ownPackageName)` — queries `Intent.ACTION_MAIN` + `CATEGORY_LAUNCHER`, excludes Reclaim's own package, caches the result with an `invalidate()` hook.
- `UsageStatsManagerStats(usageStatsManager, appOpsManager, packageName)` — `queryUsageStats(INTERVAL_DAILY, ...)` for both the 7-day average and today (since 00:00 local). Returns `emptyMap()` when usage access is denied. Exposes `hasUsageAccess()` for callers that need to gate UI.
- `DataStoreAddedAppsRepository(dataStore: DataStore<Preferences>)` — persists `AddedApp` entries via Jetpack DataStore Preferences. Idempotent `add()` by `packageName`; `delete()` by `packageName`.

All three are constructed once per process in `ReclaimApplication` as `by lazy` properties. No DI framework, no singletons.

## Shape of an adapter

Match the domain interface's contract exactly. No additional public methods unless the call site needs framework-specific affordances (`hasUsageAccess()` on `UsageStatsManagerStats`, `invalidate()` on `PackageManagerAppCatalog`).

`addedApps()` returns a defensive copy. `add()` is **idempotent by `packageName`** — calling it twice with the same package replaces the entry, never accumulates. `delete()` is also idempotent: deleting an absent package is a no-op.

## Filters that depend on real-world knowledge

The "exclude Reclaim's own package" rule lives in `PackageManagerAppCatalog`, **not** the domain. Same for "exclude apps without a launcher activity." The data adapter classifies; the domain reads.

## Caching

`PackageManagerAppCatalog` caches `installedApps()` to avoid hitting `PackageManager` on every recomposition. The cache is invalidated explicitly via `invalidate()` — `ReclaimNavHost` calls it from the AddApp modal's `OnResume` so the user picks up newly-installed apps without a relaunch. Any future cache (e.g. wrapping `UsageStatsManagerStats`) needs the same explicit invalidation story tied to a real lifecycle event.

## Tests

Adapter tests live at `app/src/test/java/com/example/reclaim/data/` and use Robolectric. Domain fakes (`FakeAppCatalog`, `FakeUsageStats`, `FakeAddedAppsRepository`) are not for adapter tests — they exist to drive use case tests in pure JVM.

## Anti-patterns

- Adding domain logic to a data adapter. Sorting, top-N, "exclude already-added" — those live in the use cases. Adapters return raw data plus the structural filters that the framework dictates (launcher activity present, app op granted, etc.).
- Coroutines that swallow errors. Propagate failures so the use case (and its callers) decide what to do. No silent `try { ... } catch { emptyList() }`. The exception is permission-denied paths where the contract is "return empty"; those are explicit, not catch-alls.
- Importing `android.*` from domain code via the adapter. The adapter is one-way: it imports from `domain/`, never the reverse.
- Caching reads inside an adapter without an invalidation story. `PackageManagerAppCatalog.invalidate()` is the precedent — wire any new cache to a lifecycle event the caller controls.
