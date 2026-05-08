# PRD: Home — Top Apps Section (Slice 2 of 3)

**Date:** 2026-05-08
**Status:** Draft
**Slice:** 2 of 3 (Home screen)

## Problem

After Slice 1, the user knows the **total** screen time today vs the daily limit. They don't yet know **which apps** are eating the budget. To act ("I should put my phone down" / "I should close TikTok"), they need a per-app breakdown of today's usage against each app's quota.

The Apps tab already shows added apps with their quotas. The Home needs the same list framed differently: ranked by today's usage, with a progress bar that turns warm as quota is approached or exceeded.

## Background

Slice 2 covers the middle section of the Home design: a "Top apps" card listing the added apps with today's usage, today's quota, and a progress bar. The section header has a "See all" link that switches the user to the Apps tab.

The data source is what Slice 1 introduced (`UsageStats.usageToday()`) plus the existing `AddedAppsRepository` and `AppCatalog`. No new domain interfaces are required, but a small use case is appropriate to encapsulate ranking and the per-app status decision (red/amber/green).

The Apps tab shows added apps in the order they were added. The Home shows them ranked: highest usage today first. The two views co-exist; they tell different stories.

The "See all" link routes to the existing Apps tab via the `switchTab` helper already in `ReclaimNavHost`. No new destinations.

Source design: `~/Downloads/Reclaim _standalone_.html`, screen 3, "Top apps" section.

## Requirements

### Must Have

- **Section header**: title *"Top apps"* on the left; *"See all"* text button on the right that switches the bottom navigation to the Apps tab.
- **List**: one row per added app, ranked by **today's usage**, descending. Ties broken by display name ascending.
- **Row content**:
  - App icon on the left. If the launcher icon is unavailable, a colored circle with the app's two-letter initials (matching the `AppsScreen` fallback if one exists today; otherwise a simple ink-on-canvas badge).
  - App display name (semibold).
  - Right-aligned `usage / quota` label, e.g. `1h 45m / 2h`. Uses the same `Xh Ym` formatting as Slice 1.
  - Horizontal progress bar below the name: fraction `today / quota`, clamped to `[0, 1]`.
- **Status colors** (per row), driven by `today / quota`:
  - **Red** (`ReclaimRed`): `today > quota`. Bar fills 100%, label and bar both red.
  - **Amber** (`ReclaimAmber`): `today / quota >= 0.8` and not exceeded. Bar shows the actual fraction in amber, label in amber.
  - **Green** (`ReclaimGreen`): below 80%. Bar shows the actual fraction in green, label in default `ReclaimInk2`.
- **Empty state — no apps added**: section is hidden entirely. The user already saw the "Add apps to set your daily limit" hint in the hero ring (Slice 1).
- **Permission state — no Usage Access**: section is hidden entirely. The hero ring already prompts for the grant (Slice 1). Showing zeroed-out rows would be misleading.
- **Refresh on resume**: same behavior as the ring — the list reflects the latest `usageToday()` when the user returns to Home.

### Should Have

- A new domain use case `RankAppsForHomeUseCase` (or similar name) that takes `addedApps()`, `installedApps()`, and `usageToday()` and returns a sorted list of view-friendly rows: app, today's usage, quota, status (`OK`/`WARN`/`OVER`). Keeps the screen free of business rules and gives Slice 2 a JVM unit-test surface.
- Top-N cap is **not** required for now (added apps are user-curated and rarely exceed a handful). If the list grows long, the page scrolls. Caps can be added later without breaking the contract.

### Out of Scope

- Tap-to-open-app on a row.
- Tap-to-edit-quota on a row (Apps tab handles that).
- Showing apps the user has *not* added.
- Per-app trends, sparklines, week-over-week deltas.
- Any reorder UI or filter chips.
- Habits today section (Slice 3).

## Constraints

- The status decision (`OK` / `WARN` / `OVER`) lives in the domain use case, not in the composable. The screen renders, the domain decides.
- Reuse the same `Xh Ym` time formatter from Slice 1. If Slice 1 placed it in the screen file, extract it to a small shared helper at this point — but not before (per the project's "wait for the third occurrence" rule, this is the second; defer extraction unless it lands in three places by the time Slice 3 ships).
- Progress bar height and corner radius match the design (`pbar` style: short, rounded). Use existing `ReclaimLine` as the track.
- All colors via tokens. The amber threshold (80%) is a constant in the use case's `private companion object`.
- One row composable, kept private to `HomeScreen.kt`. Don't pre-extract a shared `AppRow` component until Slice 3 confirms shape.

## Acceptance Criteria

### Ranking

- Given three added apps with today's usage of 2h, 30m, and 0m respectively, when Home renders, then the rows appear in that order from top to bottom.
- Given two added apps with identical today's usage, when Home renders, then they appear ordered alphabetically by display name.

### Status — green

- Given an app with quota 2h and today's usage 30m, when Home renders, then the row shows `30m / 2h`, the bar is ~25% filled in `ReclaimGreen`, and the label uses `ReclaimInk2`.

### Status — amber

- Given an app with quota 2h and today's usage 1h 45m (87.5%), when Home renders, then the row shows `1h 45m / 2h`, the bar is ~88% filled in `ReclaimAmber`, and the label is `ReclaimAmber`.

### Status — red (exceeded)

- Given an app with quota 2h and today's usage 2h 14m, when Home renders, then the row shows `2h 14m / 2h`, the bar is fully filled in `ReclaimRed`, and the label is `ReclaimRed`.

### Empty and permission states

- Given the user has no added apps, when Home renders, then the "Top apps" section is not visible at all (no header, no card, no spacer).
- Given Usage Access is not granted, when Home renders, then the "Top apps" section is not visible at all.

### See all

- Given the user is on Home and has at least one added app, when they tap "See all", then the bottom navigation switches to the Apps tab and the Apps screen is shown.

### Refresh

- Given the user is on Home with one added app at 1h today, opens that app outside Reclaim for 5 more minutes, and returns to Home, when Home is shown, then the row reflects the new total (~1h 5m).
