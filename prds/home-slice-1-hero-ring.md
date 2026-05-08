# PRD: Home — Hero Ring (Slice 1 of 3)

**Date:** 2026-05-08
**Status:** Draft
**Slice:** 1 of 3 (Home screen)

## Problem

When a user opens Reclaim, the Home tab today shows nothing useful. It's a stub that says "(design pending)". The user has no anchor for "how am I doing today" — the central promise of the app. They added apps with quotas in the Apps tab, but the home screen offers no daily snapshot.

The Home is the screen the user is supposed to glance at multiple times per day. It needs to answer one question fast: **am I within my screen-time budget today, or not?**

## Background

Slice 1 covers the top half of the Home design: the date/greeting header and the hero ring. The ring is the visual centerpiece — a circular progress indicator showing today's screen time against the daily limit, with the time large in the middle.

Two product decisions already made with the user:

- **Daily limit** = sum of the daily quotas across all added apps. No separate "global limit" entity. Adding/editing/removing an app changes the limit immediately.
- **Screen time today** = sum of *today's* foreground time across the *added apps only*. Apps the user did not add do not count. This is consistent with the rest of Reclaim ("you choose what to monitor").

The "+ X min earned today" teal callout in the design depends on the habits domain, which doesn't exist yet. It is **deferred to Slice 3** and hidden in Slice 1.

The greeting line in the design reads "Good afternoon, Maya." There is no profile system yet. The greeting is rendered as **"Good afternoon"** (no name, no time-of-day branching). When a profile lands, this line evolves.

The bell icon top-right is decorative for now (no notifications inbox).

Source design: `~/Downloads/Reclaim _standalone_.html`, screen 3 ("03 Home · Dashboard"), top section through the earned callout.

## Requirements

### Must Have

- **Header**: today's date in the form "Tuesday, May 5" (uppercase tracking-wider), and a greeting line "Good afternoon" below it. A bell icon sits to the right (visual only, no action).
- **Hero ring**: a circular progress indicator showing how much of today's daily limit has been used.
  - Center label "SCREEN TIME TODAY" (small, uppercase).
  - Center number: today's total screen time formatted as `Xh Ym` (e.g. `2h 47m`, `47m`, `0h 0m`).
  - Caption below the number: "of your `Xh` daily limit" (the limit formatted the same way).
  - Track color: `ReclaimLine`. Progress arc color: `ReclaimTeal`. Progress is the fraction `today / limit`, clamped to `[0, 1]`.
- **Empty state — no apps added**: ring shows zero progress. Center number reads `0h 0m`. The caption is replaced with the copy *"Add apps to set your daily limit"* in the muted ink color.
- **Permission state — no Usage Access**: ring shows zero progress. Center number reads `—` (em dash). Below the ring, a small text button reads *"Grant usage access"* and opens the system Usage Access settings screen on tap. (Reuse the same intent the onboarding screen uses.)
- **Exceeded state**: when today's screen time is greater than the daily limit, the ring arc remains visually full (clamped at 100%) and the center number switches to `ReclaimRed`. The caption still shows "of your `Xh` daily limit."
- **Live refresh on resume**: when the user returns to Home from another screen or from the system, today's number reflects the latest usage. (Re-querying on every recomposition is fine for now; no caching.)
- **Domain extension**: `UsageStats` gains a new method that returns today's foreground time per package. The real adapter (`UsageStatsManagerStats`) implements it by querying from the start of today (00:00 local time) to now.

### Should Have

- The earned callout's vertical space is **not** reserved in Slice 1. Layout should let Slice 3 add the callout without rework, but the absence of the callout should not leave an awkward empty band.

### Out of Scope

- Top apps list (Slice 2).
- Habits today section (Slice 3).
- The "+ X min earned today" callout (Slice 3, depends on habits).
- Profile / personalized greeting / time-of-day greeting variants.
- Bell icon behavior.
- Multi-day views, weekly summaries, history.
- A ViewModel layer. Screen pulls from domain interfaces directly, like the rest of the app.
- Background refresh, push updates, work managers.

## Constraints

- Pure Compose, no ViewModel. Screen takes callbacks for navigation and reads from `AppCatalog`, `UsageStats`, `AddedAppsRepository` — same pattern as `AppsScreen`.
- The `UsageStats` interface stays in `domain/` and pure-JVM. Time-of-day boundaries (start of today) must be computed without `android.*` imports — use `java.time` (already JVM standard).
- The "no Usage Access" CTA must reuse the same Settings intent the onboarding screen uses (`Settings.ACTION_USAGE_ACCESS_SETTINGS`). Wire it through `ReclaimNavHost` so the screen does not import Android intents.
- All colors from `ui/theme/Color.kt` tokens. No hex in the screen file.
- Screen file contains one public `@Composable` plus private section helpers. Extract a `HomeScreenContent` composable so the ring and states are testable without launching the full screen.

## Acceptance Criteria

### Hero ring — within limit

- Given the user has 2 added apps with quotas summing to 3h, and `UsageStats.usageToday()` reports 1h 30m of combined foreground time across those apps, when Home renders, then the ring shows ~50% progress, the center reads `1h 30m` in default ink, and the caption reads "of your 3h daily limit".

### Hero ring — exceeded

- Given the daily limit is 2h and today's combined usage of added apps is 2h 14m, when Home renders, then the ring arc is fully filled, the center reads `2h 14m` in `ReclaimRed`, and the caption still reads "of your 2h daily limit".

### Empty state — no apps added

- Given the user has not added any apps, when Home renders, then the ring shows zero progress, the center reads `0h 0m`, and the caption is replaced with *"Add apps to set your daily limit"*.

### Permission state — no Usage Access

- Given Usage Access is not granted, when Home renders, then the ring shows zero progress, the center reads `—`, and a *"Grant usage access"* text button is visible below the ring.
- When the user taps the button, then the Android Usage Access settings screen opens.
- Given the user grants Usage Access in Settings and returns to the app, when Home is shown again, then the ring reflects today's actual usage (no relaunch required).

### Header

- Given today is Tuesday May 5, when Home renders, then the header reads "TUESDAY, MAY 5" (uppercase, tracking) on the first line and "Good afternoon" on the second line. The bell icon is visible top-right.

### Domain — usage today

- Given today's usage data covers multiple sessions for the same package, when `UsageStats.usageToday()` is called, then it returns one entry per package whose value is the total foreground time since 00:00 local today.
- Given Usage Access is not granted, when `UsageStats.usageToday()` is called, then it returns an empty map (same convention as `avgDailyUsageLast7Days()`).

### Refresh

- Given the user navigates from Home to Apps, opens an app outside Reclaim, and returns to Home, when Home is shown, then the center number reflects the new total.
