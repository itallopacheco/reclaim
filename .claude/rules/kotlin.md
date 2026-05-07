---
description: Kotlin + Jetpack Compose conventions for the Reclaim app
paths:
  - "app/src/main/java/**/*.kt"
---

# Kotlin / Compose conventions

## Composable structure

- A screen file exposes **one public `@Composable`** taking callbacks. The rest of the file is `private @Composable` section helpers (TopBar, Hero, FooterCta, etc.).
- Screens never receive `NavController` or `NavHostController`. Navigation is a callback the caller wires up in `navigation/ReclaimNavHost.kt`.
- Every screen file ends with a `@Preview` showing the screen with safe defaults. Mark the preview function `private`.

## State

- Screen-local state: `var x by remember { mutableStateOf(...) }`. No ViewModel layer in the codebase yet — don't introduce one without a reason.
- Lists for UI lookup tables (e.g. habit definitions) go in a `private data class` + `remember { listOf(...) }` inside the screen. Inline data is fine until the third reuse.
- For business logic that doesn't fit in a screen (filtering, ordering, validation), reach into `domain/` use cases. Construct them inline for now. When DI becomes worth it, that's a separate decision — don't preempt.

## Theme and design tokens

- Import colors from `com.example.reclaim.ui.theme.*` (`ReclaimTeal`, `ReclaimInk`, `ReclaimBg`, `ReclaimLine`, `ReclaimAmber`, ...). **Never hardcode hex in a screen.**
- `ReclaimTheme` maps tokens onto Material3 slots, so `MaterialTheme.colorScheme.primary` resolves to `ReclaimTeal`. Either form is fine; prefer the explicit token when the mapping isn't obvious.
- Theme is locked to a light scheme with no dynamic color. Don't reintroduce `dynamicLightColorScheme` / `isSystemInDarkTheme()`.

## Icons

- Lucide icons in the source design map to Material Icons Extended. Common mappings:
  - `chevron-left` → `Icons.Filled.ChevronLeft`
  - `lock` → `Icons.Filled.Lock`
  - `check` → `Icons.Filled.Check`
  - `sparkles` → `Icons.Filled.AutoAwesome`
  - `target` → `Icons.Filled.GpsFixed`
  - `book-open` → `Icons.AutoMirrored.Filled.MenuBook`
  - `dumbbell` → `Icons.Filled.FitnessCenter`
- Use `Icons.AutoMirrored.*` for directional icons (back arrows, lists with indent).

## Sizing

- All dimensions in `dp`, font sizes in `sp`. Translate CSS pixel values directly: `text-[14.5px]` → `14.5.sp`, `width:84px` → `84.dp`.
- Match the HTML export's spacing 1:1. Don't round to "nice" Material 8dp grid values when the design specifies otherwise.

## Imports

- Prefer specific imports over wildcards (matches the existing files).
- Keep `androidx.compose.*` imports grouped with project imports below.
