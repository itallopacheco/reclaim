# PRD: Habits CRUD

**Date:** 2026-05-08
**Status:** Draft

## Problem

Reclaim's value proposition is two-sided. The Apps tab gives the user a way to set limits on draining apps. The other half is supposed to let them **earn time back by building habits**. Today the Habits tab is a stub. The user has no way to define a habit, mark it done, or see progress. The whole "earn" half of the app is invisible.

This PRD covers the foundational CRUD surface for habits: define them, see them, complete them, edit them, delete them. Without this, the planned "Habits today" section on the Home (Slice 3, issue #11) has nothing to render.

## Background

Slice 3 of the Home screen was deliberately blocked because the habits domain doesn't exist anywhere in the codebase. There is no `Habit` entity, no repository, no use cases. The Habits tab (`HabitsScreen.kt`) and the Add Habit modal (`AddHabitSheet.kt`) are placeholder files with hardcoded text.

The design has been finalized for two screens:

- **Screen 6 — Habits list**: a header with "Today / Habits" stacked, a teal "Earned today" summary card, two grouped sections ("Pending · N", "Completed · N"), and a floating action button that opens the Add Habit modal.
- **Screen 7 — Add Habit modal**: a sheet with name input, icon picker (10 fixed icons), and a row of 5 fixed time-reward chips. The original design also showed Frequency and Reminder cards, but those are explicitly stripped from this PRD per agreement with the user.

The persistence and architecture pattern follows what's already proven in the Apps CRUD: a `DataStoreHabitsRepository` mirrors `DataStoreAddedAppsRepository`, and an `EditHabitSheet` mirrors `EditAppSheet` (load existing, save changes, or open a confirm dialog to delete).

The "Earned today" card at the top of screen 6 displays the user's progress in minutes earned, completion count, and minutes still available. It's computed live from the habits + today's completions. This is the **first** place in the app where the user sees the "earn back" concept made concrete. Slice 3 of the Home will later surface a smaller version of the same story.

## Requirements

### Must Have

#### Habit definition

- **Create a habit** via the Add Habit modal:
  - **Name** (single-line text input, required, non-empty after trim).
  - **Icon** picked from a fixed set of 10 (book-open, dumbbell, footprints, sun, pen-line, droplet, utensils, moon, music, bike). One must be selected; default to the first.
  - **Time reward** picked from 5 fixed chips: 5, 10, 15, 30, 45 minutes. One must be selected; default to 15 minutes.
  - "Save" creates the habit and dismisses the modal. "Cancel" discards.
  - Save is disabled when the name is empty.
- Habits are **persisted across app restarts**.

#### Habits list (screen 6)

- **Header**: small uppercase "Today" tag and a large "Habits" title. (No date in the design's header; date display is the Home's job.)
- **Earned today card** (teal background, teal border, ink text):
  - Title: "EARNED TODAY"
  - Big number: total minutes earned today (sum of `reward` across habits completed today). Format: `+Xh Ym` or `+X min`.
  - Right column: `N of M done` (completed count / total habits) and `+X min available` (sum of `reward` for habits not yet completed today, formatted as minutes).
- **Pending section**: header `Pending · N`, then a card per pending habit. Each card has:
  - An empty check-circle on the left (tap to complete).
  - A 36dp rounded teal-tinted icon tile.
  - The habit name (semibold).
  - A subtitle "Daily" (no streak — explicitly out of scope).
  - A teal-on-teal-bg "+X min" reward badge on the right.
- **Completed section**: header `Completed · N`, then one muted card per completed habit. The check-circle is filled, the icon tile uses the muted (`ReclaimLine`) background, and the subtitle reads `Done at H:MM AM/PM` (the local time the completion was recorded).
- **Empty state**: when the user has zero habits, the "Earned today" card is hidden, both list sections are hidden, and the screen shows a friendly hint inviting the user to create their first habit. The FAB is still visible.
- **FAB**: bottom-right floating action button with a `+` icon. Tap opens the Add Habit modal.
- **Tap a row** (anywhere except the check-circle): opens the Edit Habit modal for that habit.
- **Tap the check-circle**: toggles "completed today" for that habit. Idempotent — tapping a completed habit's filled circle marks it back to pending, removing the time stamp and excluding it from "Earned today" again.

#### Edit Habit modal

- Mirrors the AddHabit layout: name input, icon picker, reward chips. All three are pre-filled with the habit's current values.
- "Save" persists the changes and dismisses. "Cancel" discards.
- Below the form (or in a header position consistent with `EditAppSheet`), a destructive "Remove habit" action that opens a confirmation dialog. Confirming removes the habit and its completion record (if any) for today.

#### Persistence and date model

- Habits and today's completions persist across app restarts, force-stops, and process death.
- "Today" is the **device's local date**. A habit completed at 23:59 Monday and re-checked at 00:01 Tuesday is not completed today on Tuesday.
- The Earned today card and the Pending/Completed split refresh on resume so the user sees a clean slate after midnight.

### Should Have

- **Stable ordering** of habits in both lists. Default: order of creation (oldest first). The user notices when the list reshuffles unexpectedly; pick one rule and keep it.
- **Test coverage**:
  - JVM unit tests for use cases (create, complete, summary computation, date boundary).
  - Robolectric tests for `DataStoreHabitsRepository`.
  - Compose UI tests covering the empty state, a populated list with one pending and one completed habit, a check-circle tap moving a row across sections, and the Add and Edit modals' Save/Cancel/Delete paths.

### Out of Scope

- **Reminders / notifications.** No reminder card in the modal, no scheduling, no `POST_NOTIFICATIONS` permission. The design's Frequency/Reminder cards are stripped from screen 7.
- **Frequencies other than daily.** No "3× per week", "weekdays only", or custom schedules.
- **Streaks.** The design shows streak counts in the row subtitle. Subtitle reads only "Daily" in this PRD.
- **Slice 3 of the Home** ("Habits today" preview + earned callout under the hero ring). That ships as its own feature once this CRUD lands.
- **Earned minutes feeding back into anything.** No raising the daily limit, no unlocking blocked apps, no "App Blocked — Earn back" screen (screen 8). Earned minutes are display-only here.
- **Calendar / history view**, the calendar icon top-right of screen 6 is decorative and non-functional.
- **Habit reordering UI.**

## Constraints

- **Architecture mirrors the Apps CRUD.** The work splits across `domain/habits/` (pure JVM, new package next to `domain/apps/`), `data/` (a `DataStoreHabitsRepository` next to `DataStoreAddedAppsRepository`), and `ui/screen/` (rewrite `HabitsScreen.kt`, `AddHabitSheet.kt`, new `EditHabitSheet.kt`).
- **Domain layer is pure Kotlin/JVM**, no `android.*` imports. Local-date handling uses `java.time` (already standard JVM).
- **DI through `ReclaimApplication`.** New lazy properties for the repository and use case getters, same shape as the existing `addedApps`, `todayScreenTime`, `rankAppsForHome`.
- **Navigation via callbacks.** `HabitsScreen` does not see `NavController`. The existing `AddHabit` modal route stays; add `EditHabit` with a habit-id arg, mirroring `EditApp.routeFor(packageName)`.
- **Theme tokens only.** No hex in screens. The "Earned today" card uses `ReclaimTeal2` (background) and `ReclaimTeal3` (border), which exist in `Color.kt`.
- **Icons** are the fixed 10 from screen 7. The domain stores an icon identifier (enum or stable string), not an Android drawable resource. The screen maps each identifier to a Material Icons Extended composable per the project's icon mapping rules.
- **Reward** is one of `{5, 10, 15, 30, 45}` minutes. Stored as a `Duration`. The chip set is the only way to set it.
- **Single feature branch.** Implementable in one PR like the previous slices.

## Acceptance Criteria

### Create a habit

- Given the user is on the Habits tab and has zero habits, when they tap the FAB, then the Add Habit modal opens with an empty name field, the first icon selected, and the 15-minute reward chip selected.
- Given the user opened the Add Habit modal and the name is empty, when they look at the Save button, then it is disabled.
- Given the user typed a name, picked an icon, and tapped a reward chip, when they tap Save, then the modal dismisses and the habit appears at the bottom of the Pending list with the chosen icon, name, and reward badge.

### Complete and uncomplete

- Given the user has a pending habit "Read 20 minutes" with reward 30 min, when they tap its check-circle, then the row moves to the Completed section with the timestamp `Done at H:MM AM/PM`, the Earned today number increases by 30 min, the `done` counter increments, and the available number decreases by 30 min.
- Given the user has a completed habit, when they tap its filled check-circle, then the row moves back to Pending, the timestamp clears, and Earned today decreases by the habit's reward.

### Earned today summary

- Given the user has 4 habits with rewards 30, 45, 15, 10 (total 100 min) and has completed the 30 and 15 today, when the screen renders, then the card shows `+45 min` earned, `2 of 4 done`, and `+55 min available`.
- Given the user has habits but none completed today, when the screen renders, then the card shows `+0 min` earned and the available number equals the sum of all rewards.

### Empty state

- Given the user has zero habits, when the Habits tab renders, then the Earned today card and both list sections are hidden, a friendly hint encourages creating the first habit, and the FAB is visible.
- Given the user creates their first habit, when the modal closes, then the empty hint disappears and the new habit appears in Pending with the Earned today card showing `+0 min` of the new habit's reward as available.

### Edit a habit

- Given a habit "Walk outside" with the footprints icon and a 15-minute reward, when the user taps the row body (not the check-circle), then the Edit modal opens with all three fields pre-filled.
- Given the user changes the name to "Walk after lunch" and taps Save, when the modal dismisses, then the row in the list reflects the new name immediately.
- Given the user taps Cancel after editing, when the modal dismisses, then no change is persisted.

### Delete a habit

- Given the user is in the Edit modal, when they tap "Remove habit" and confirm in the dialog, then the modal dismisses and the habit no longer appears in either list.
- Given the deleted habit had been completed today, when it is removed, then Earned today decreases by that habit's reward and the `of M` total in the counter decreases by 1.

### Date boundary

- Given the user marked a habit complete yesterday, when the Habits tab renders today, then the habit appears in Pending and Earned today does not include yesterday's reward.

### Persistence

- Given the user creates a habit, completes it, and force-stops the app, when they reopen Reclaim later the same day, then the habit is still listed and still marked completed with the original timestamp, and the Earned today total is unchanged.
- Given the user creates a habit on day 1, when day 2 begins (local time), then the habit is back in Pending with no completion record and Earned today resets to zero.

### Navigation

- Given the user is on the Habits tab, when they tap the FAB, then the Add Habit modal opens via the existing `AddHabit` route.
- Given the user is on the Habits tab, when they tap a habit row, then the Edit Habit modal opens via a new `EditHabit` route carrying the habit's id.
- Given the user dismisses either modal, when control returns, then they land back on the Habits tab.
