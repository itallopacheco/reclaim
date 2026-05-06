# Reclaim

Android screen-time app. Early-stage PoC. UI is being translated from a Claude Design HTML export — see `~/Downloads/Reclaim _standalone_.html` for the source designs.

## Stack

- Kotlin 2.0.21, Android Gradle Plugin 8.13.2
- Jetpack Compose (BOM 2024.09.00), Material3
- Navigation Compose 2.8.4
- Material Icons Extended (from BOM)
- compileSdk 36, targetSdk 36, minSdk 34, JVM target 11
- Single module: `:app`, namespace `com.example.reclaim`

Version catalog: `gradle/libs.versions.toml`. Add new deps there, then reference via `libs.*` in `app/build.gradle.kts`.

## Commands

```bash
./gradlew :app:compileDebugKotlin   # fast compile check
./gradlew :app:assembleDebug        # build debug APK
./gradlew :app:installDebug         # install to connected device/emulator
./gradlew test                      # unit tests (JVM)
./gradlew connectedAndroidTest      # instrumented tests (needs device)
./gradlew lint                      # Android lint
./gradlew clean
```

Launch after install: `adb shell am start -n com.example.reclaim/.MainActivity`.

For UI iteration prefer Android Studio's `@Preview` over running the app — every screen has a preview at the bottom of its file.

## Package layout

```
app/src/main/java/com/example/reclaim/
├── MainActivity.kt              # entry point, hosts ReclaimNavHost under ReclaimTheme
├── navigation/
│   ├── Destinations.kt          # sealed Destination(route) routes
│   └── ReclaimNavHost.kt        # NavHost wiring; screens get callbacks, not NavController
└── ui/
    ├── screen/                  # one file per screen, public @Composable
    │   ├── LockScreen.kt        # PoC translated from Claude Design export
    │   ├── HomeScreen.kt
    │   ├── DetailScreen.kt
    │   └── SettingsScreen.kt
    └── theme/
        ├── Color.kt             # Reclaim* design tokens (16 values)
        ├── Theme.kt             # ReclaimTheme, light-only, no dynamic color
        └── Type.kt
```

Start destination is `Destination.Lock` while the lock screen is the PoC focus.

## Conventions

- **Screen composables** are public top-level functions taking callbacks (`onBack`, `onGoToHabits`, ...). They never receive `NavController`. Wiring lives in `ReclaimNavHost.kt`.
- **Section composables** within a screen (TopBar, Hero, FooterCta, ...) are `private @Composable` in the same file. Don't extract to siblings unless reused.
- **Screen-local state** uses `remember { mutableStateOf(...) }`. No ViewModel layer yet.
- **Colors come from `ui/theme/Color.kt`** (`ReclaimTeal`, `ReclaimInk`, `ReclaimBg`, etc.). Never hardcode hex in screens. `ReclaimTheme` maps tokens onto Material3 slots; using `MaterialTheme.colorScheme.primary` resolves to `ReclaimTeal`.
- **No dynamic color.** `ReclaimTheme` is locked to a static light scheme so the design palette renders consistently.
- **Sizes in dp**, font sizes in sp. The HTML uses `text-[14.5px]` style — translate as `14.5.sp`.
- **Icons:** Lucide icons in the design map to closest Material Icons Extended equivalents (e.g. `lucide:sparkles` → `Icons.Filled.AutoAwesome`, `lucide:target` → `Icons.Filled.GpsFixed`, `lucide:book-open` → `Icons.AutoMirrored.Filled.MenuBook`). Document the mapping inline if non-obvious.

## Design translation flow

1. Source: `~/Downloads/Reclaim _standalone_.html` (Claude Design export). Real markup is in a JSON-stringified blob inside a JS bundler envelope — extract with Python before grepping.
2. The export defines CSS variables at the top (`--bg`, `--ink`, `--teal`, etc.). All are mirrored in `ui/theme/Color.kt` as `Reclaim*` constants.
3. When implementing a new screen: extract its template from the HTML, translate 1:1 to Compose, reuse existing tokens, add `@Preview` at the bottom.

## Tests

Currently only the default Android Studio scaffolding exists (`ExampleUnitTest`, `ExampleInstrumentedTest`). No domain logic to test yet. JUnit4 + `androidx.compose.ui:ui-test-junit4` are wired up.
