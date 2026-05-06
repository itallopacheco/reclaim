---
description: Translating Claude Design HTML exports into Compose screens
paths:
  - "app/src/main/java/com/example/reclaim/ui/**/*.kt"
---

# Design translation

## Source of truth

`~/Downloads/Reclaim _standalone_.html` is the design export. The real markup is JSON-encoded inside a JS bundler envelope at the bottom of the file. Extract before grepping:

```bash
python3 -c "import re,json; s=open('/Users/oicapivara/Downloads/Reclaim _standalone_.html').read(); m=re.search(r'\"<!DOCTYPE html.*?\"', s, re.DOTALL); print(json.loads(m.group(0)))" > /tmp/reclaim.html
```

Each screen lives in a `<template id="screen-N">` block with a `data-screen-label` attribute.

## Translation rules

- **One screen per file** under `ui/screen/`. Public `@Composable` named after the screen (`LockScreen`, `HomeScreen`, ...).
- **Match layout 1:1** with the HTML — same paddings, same order of elements, same hierarchy. Don't "improve" the design during translation.
- **Use existing design tokens.** All CSS variables (`--bg`, `--ink`, `--teal`, etc.) are already in `ui/theme/Color.kt` as `Reclaim*` constants. If a screen needs a token that isn't there, add it to `Color.kt` first.
- **Tailwind utility values translate literally.** `px-6` → `24.dp` horizontal padding, `mt-7` → `28.dp` top spacer, `text-[14.5px]` → `fontSize = 14.5.sp`, `rounded-[14px]` → `RoundedCornerShape(14.dp)`.
- **CSS filters are approximations.** `filter: grayscale(0.7); opacity: 0.55` on a brand color is approximated by `color.copy(alpha = 0.55f).compositeOver(ReclaimInk3)`. Document the approximation with a comment.
- **Pills, cards, badges are inline `Box(Modifier.clip(...).background(...))`** — don't extract a `ReclaimPill` component until the third occurrence.

## Anti-patterns

- Hardcoding a hex value in a screen file. Add a token to `Color.kt` instead.
- Replacing the design's amber reward badge with `MaterialTheme.colorScheme.tertiary` "to be more Material." The design wins.
- Wrapping a screen in `Scaffold` with `TopAppBar` when the design has a custom top bar (most Reclaim screens do). Build the top bar manually in the screen's first `Row`.
- Adding ripple-less clickables silently. Use `Modifier.clickable(onClick = ...)` and accept the default ripple unless the design explicitly suppresses it.

## Previews

Every screen file ends with:

```kotlin
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun MyScreenPreview() {
    ReclaimTheme { MyScreen(...) }
}
```

390x844 matches the iPhone frame in the design export, which is what the layouts were tuned for.
