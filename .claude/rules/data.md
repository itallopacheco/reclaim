---
description: Data layer conventions — adapters that implement domain interfaces
paths:
  - "app/src/main/java/com/example/reclaim/data/**/*.kt"
---

# Data layer

The `data/` package holds adapters that implement the interfaces declared in `domain/apps/`. This is where Android framework code (`PackageManager`, `UsageStatsManager`, DataStore) belongs. The domain stays free of `android.*` imports; the data layer earns the right to import them.

## Current state

- `DemoAppCatalog` — static list of 10 popular apps (Instagram, WhatsApp, TikTok, etc.). Bootstrap so the modal renders before fatia C arrives.
- `DemoUsageStats` — static avg-usage map matching the demo catalog. Same role.
- `InMemoryAddedAppsRepository` — process-lifetime store backed by a `mutableListOf<AddedApp>`. Lost on app restart.

All three are `object` singletons. No DI framework yet, so the NavHost and the screens reach them by name.

## Shape of an adapter

Match the domain interface's contract exactly. No additional public methods.

```kotlin
object InMemoryAddedAppsRepository : AddedAppsRepository {
    private val apps = mutableListOf<AddedApp>()

    override fun addedApps(): List<AddedApp> = apps.toList()

    override fun add(addedApp: AddedApp) {
        apps.removeAll { it.packageName == addedApp.packageName }
        apps.add(addedApp)
    }
}
```

`addedApps()` returns a defensive copy. `add()` is **idempotent by `packageName`** — calling it twice with the same package replaces the entry, never accumulates.

## Filters that depend on real-world knowledge

The "exclude Reclaim's own package" rule does **not** belong in the domain. When fatia C introduces `PackageManagerAppCatalog`, that adapter filters `com.example.reclaim` at the source. Same for "exclude system apps without a launcher icon" — the data adapter classifies; the domain reads.

## Replacement plan (fatia C)

| Today (fatia B) | Fatia C |
|---|---|
| `DemoAppCatalog` | `PackageManagerAppCatalog` (queries `Intent.ACTION_MAIN` + `CATEGORY_LAUNCHER`) |
| `DemoUsageStats` | `UsageStatsManagerStats` (`UsageStatsManager.queryUsageStats(INTERVAL_DAILY)` over last 7 days) |
| `InMemoryAddedAppsRepository` | `DataStoreAddedAppsRepository` (Jetpack DataStore Preferences) |

When the swap happens, the `Demo*` files go away. `InMemoryAddedAppsRepository` stays only if it's useful for testing or previews; otherwise delete it too.

## Anti-patterns

- Adding domain logic to a data adapter. Sorting, top-N, "exclude already-added" — those live in the use cases. Adapters return raw data plus the structural filters that the framework dictates (launcher activity present, app op granted, etc.).
- Coroutines that swallow errors. When fatia C introduces `suspend`, propagate failures so the use case (and its callers) decide what to do. No silent `try { ... } catch { emptyList() }`.
- Importing `android.*` from domain code via the adapter. The adapter is one-way: it imports from `domain/`, never the reverse.
- Caching reads inside an adapter without a clear invalidation story. `DemoAppCatalog` is fine because it's static. A future cache around `PackageManagerAppCatalog` needs to be invalidated when the user grants/revokes permissions or returns from Settings.
