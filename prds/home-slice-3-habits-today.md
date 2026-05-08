# PRD: Home — Habits Today + Earned Callout (Slice 3 of 3)

**Date:** 2026-05-08
**Status:** Draft
**Slice:** 3 of 3 (Home screen)

## Problem

Reclaim's promise is two-sided: limit the apps that drain you, and **earn back time by building habits**. After Slices 1 and 2, the user can see how much screen time they used and which apps drove it, but the "earn back" half of the story is invisible. Today, the Habits tab is a stub and there is no concept of a habit anywhere in the codebase.

This slice is the largest of the three. It introduces the habits domain from scratch and wires the first user-facing surface for it: the "Habits today" card on Home and the "+ X min earned today" callout under the hero ring.

## Background

Slice 3 covers the bottom of the Home design above the tab bar:

- **Earned callout** (placed under the hero ring, deferred from Slice 1): a small teal pill reading "+ X min earned today" with a sparkles icon. The number is the sum of rewards from habits the user completed today.
- **Habits today** card: a "X / Y" counter and a preview of one pending habit, with the habit name, status, and reward badge.

The full Habits tab (list, create, edit, delete, mark complete) is outside this slice. Slice 3 only delivers the **read-side** surfaces on Home plus enough domain and data to make completion meaningful. A minimal way to mark a habit complete is required so the surfaces aren't empty in practice — see Must Have.

The reward concept: every habit has a *reward in minutes*. Completing the habit today adds those minutes to "earned today". The earned minutes do not yet feed back into the daily limit (that's a future product decision: do earned minutes raise the limit, unlock blocked apps, or just count for show?). For Slice 3, earned minutes are **displayed only**, not enforced.

Source design: `~/Downloads/Reclaim _standalone_.html`, screen 3, lower section ("Habits today" card and the teal "+ X min earned today" pill above it).

## Requirements

### Must Have

#### Domain

- New entity `Habit` with at minimum: id, display name, reward in minutes, an icon hint (a small enum mapping to `book-open`, `dumbbell`, `target`, etc., consistent with the icon mapping rules in `CLAUDE.md`).
- New entity `HabitCompletion` capturing that a given habit was completed on a given local date.
- New domain interface `HabitsRepository` exposing:
  - `habits()` — the list of habits the user has defined.
  - `completionsToday()` — the set of habit ids completed today (local date).
  - `markCompleteToday(habitId)` — idempotent for the current local date.
  - `unmarkToday(habitId)` — undo, idempotent.
- New use case `HabitsTodaySummaryUseCase` that returns a small summary view: total habit count, completed count, the next pending habit (if any), and total minutes earned today.

#### Data

- A `DataStoreHabitsRepository` (Jetpack DataStore Preferences, same approach as `DataStoreAddedAppsRepository`) for habits and completions. Bootstrap data: a small fixed seed of 2–3 example habits on first launch is acceptable so the screen has content out of the box. The seed is a one-time migration; the user can add/remove habits later (Habits tab work is its own future PRD).
- Wire the new repository in `ReclaimApplication` alongside the existing adapters.

#### Home UI

- **Earned callout** under the hero ring: a teal pill with sparkles icon and the text "+ Xh Ym earned today" using the same time formatter as Slices 1 and 2. The pill is **hidden** when zero minutes have been earned today.
- **Habits today** section: a header reading *"Habits today"* with a right-aligned `completed / total` counter (e.g. `2 / 4`).
- Below the header, a single card showing **the next pending habit** for today:
  - Square icon tile in `ReclaimTeal2` background with the habit's icon in `ReclaimTeal`.
  - Habit name (semibold).
  - Status line: "Pending" (small, muted).
  - Reward badge on the right ("+30 min") in the design's amber pill style.
- Tapping the card **marks the habit complete for today**. The card animates/transitions out of the pending slot, the counter increments, and the earned callout under the hero ring updates immediately. Re-tapping is a no-op for today (idempotent).
- **All habits done state**: when `completed == total > 0`, the card is replaced with a small celebratory line (e.g. *"All habits done today. Nice work."*) in muted ink.
- **No habits state**: when the user has zero defined habits, the entire section is hidden (header, counter, card). This won't happen in practice once the seed runs, but the UI handles it.
- **Refresh on resume**: returning to Home re-reads `completionsToday()` so any change made elsewhere (future Habits tab) is reflected.

### Should Have

- The next-pending habit selection is deterministic: pick by habit creation order. The design only shows one habit; the rule should not surprise the user across sessions.
- Compose UI tests cover: card visible when pending, tap marks complete, "all done" copy appears when last pending is completed.

### Out of Scope

- The full Habits tab (list, add, edit, delete, history). That's a separate PRD.
- Streaks, weekly views, habit reminders, notifications.
- Any feedback loop where earned minutes raise the daily limit, unlock apps, or affect the hero ring's progress. The earned number is **display only** in this slice.
- Habit categories, tags, ordering UI.
- Multi-completion per day (counting more than once). One completion per habit per day.

## Constraints

- The new domain code (`Habit`, `HabitCompletion`, `HabitsRepository`, `HabitsTodaySummaryUseCase`) is pure JVM. No `android.*` imports in `domain/`.
- "Today" is the **local date** (device timezone). A habit completed at 23:59 Monday and re-checked at 00:01 Tuesday is *not* completed today on Tuesday.
- The DataStore migration that seeds initial habits runs **once** on first launch of a build that ships this slice. Subsequent launches don't re-seed, even if the user has deleted the seeded habits.
- All colors via tokens. The amber reward badge uses `ReclaimAmberBg` background with `ReclaimAmber` text (verify in `Color.kt`; if a token is missing, add it before the screen consumes it).
- Icon mapping per `CLAUDE.md` (`book-open` → `Icons.AutoMirrored.Filled.MenuBook`, `target` → `Icons.Filled.GpsFixed`, `dumbbell` → `Icons.Filled.FitnessCenter`).
- Tests: domain layer in JUnit4 with hand-rolled fakes (`FakeHabitsRepository`); Compose UI tests on a real device using the `*Content` extraction pattern.

## Acceptance Criteria

### Earned callout

- Given the user has completed two habits today with rewards of 15 min and 10 min, when Home renders, then a teal pill under the hero ring reads "+ 25m earned today" with a sparkles icon.
- Given the user has completed no habits today, when Home renders, then no earned callout is shown.

### Habits today — counter

- Given the user has 4 habits and has completed 2 today, when Home renders, then the section header shows the counter `2 / 4`.

### Habits today — pending card

- Given the user has at least one pending habit today, when Home renders, then a single card shows the next pending habit's icon, name, the word *"Pending"*, and the reward badge (e.g. `+30 min`).
- Given the user taps the pending card, when the tap completes, then the counter increments, the card transitions to the next pending habit (or the all-done state), and the earned callout updates to include this habit's reward.
- Given the user taps the same card twice in quick succession, when the tap completes, then the habit is recorded as completed exactly once for today.

### Habits today — all done

- Given the user has completed every habit today (`completed == total > 0`), when Home renders, then the card area is replaced with the line *"All habits done today. Nice work."*

### Habits today — empty

- Given the user has zero defined habits, when Home renders, then the entire "Habits today" section is hidden.

### Date boundary

- Given the user marked a habit complete yesterday, when Home renders today, then that habit appears in today's pending list and the earned callout does not include yesterday's reward.

### Persistence

- Given the user marks a habit complete and force-stops the app, when they reopen Reclaim later the same day, then the habit remains marked complete and the earned callout still shows the correct total.
- Given the user installs the app for the first time, when Home renders, then the seeded habits appear in the section (counter `0 / N` with a pending card visible).
