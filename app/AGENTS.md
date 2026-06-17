# AGENTS.md - Infomaniak Calendar App

> For the multi-module / submodule overview, see the root `AGENTS.md`. The shared business logic lives in the
> `multiplatform-calendar/` Git submodule (separate repository).

## Project Summary

**Infomaniak Calendar** is an Android calendar application built by Infomaniak Network SA. The UI is implemented entirely
with Jetpack Compose and Material 3, while shared cross-platform models and logic come from the `multiplatform-calendar`
Kotlin Multiplatform library.

### High-Level Tech Stack

- **Language**: Kotlin — JVM toolchain resolved via the Foojay resolver convention plugin (declared in `settings.gradle.kts`)
- **Platform**: Android — `minSdk`, `targetSdk`, and `compileSdk` are set in `app/build.gradle.kts`
- **Build System**: Gradle with Kotlin DSL, version catalog (`gradle/libs.versions.toml`)
- **UI Framework**: Jetpack Compose (Material 3), Navigation 3
- **Architecture**: Single-Activity + Compose, with shared logic delegated to the KMP module
- **Shared Logic**: Consumed from `multiplatform-calendar` composite build via `libs.infomaniak.multiplaform.calendar.core`
- **Testing**: JUnit 4 (unit), Espresso + Compose UI Test (instrumented)

## Context Map

```
app/src/main/java/com/infomaniak/calendar/
├── MainApplication.kt              # Application class, initialises Metro AppGraph, Sentry and Matomo
├── MainActivity.kt                 # Single Activity, hosts Compose content
├── MatomoCalendar.kt               # Matomo tracker (implements Core's Matomo interface)
├── di/
│   ├── AppGraph.kt                 # Metro @DependencyGraph (AppScope) — inherits CalendarCoreGraph + multibinding
│   ├── MetroViewModelFactory.kt    # ViewModelProvider.Factory backed by the ViewModel multibinding map
│   └── ViewModelKey.kt             # @MapKey annotation for ViewModel multibinding
└── ui/
    ├── navigation/
    │   ├── MainNavHost.kt          # Top-level NavDisplay with entryProvider
    │   └── NavDestination.kt       # Sealed NavKey hierarchy (Onboarding, Home, CalendarTest, EventDetail)
    ├── screen/
    │   ├── calendarTest/
    │   │   ├── CalendarTestScreen.kt       # CalendarTest entry (exposes onNavigateToEventDetail callback)
    │   │   ├── CalendarTestUiState.kt      # Sealed interface (Loading / Loaded / Error)
    │   │   ├── CalendarTestAction.kt       # User actions (OnClickDisconnect, OnScroll)
    │   │   ├── CalendarTestViewModel.kt    # ViewModel: observes planning, triggers CalDAV sync
    │   │   ├── composable/
    │   │   │   ├── Planning.kt             # Loaded state — lazy planning list (exposes onEventClick)
    │   │   │   ├── EventCard.kt            # Single event card (clickable)
    │   │   │   ├── Error.kt / Loading.kt / Detail.kt / EventHeader.kt / ...
    │   │   ├── model/                      # EventUi, PlanningDayUi, PlanningWeekUi
    │   │   ├── paging/                     # PlanningPager + ScrollInfo
    │   │   ├── previewParameter/
    │   │   └── utils/                      # EventExt.kt, toPlanningWeeks.kt
    │   └── eventDetail/
    │       ├── EventDetailScreen.kt        # EventDetail NavKey + entry (onNavigateBack, onNavigateToEdit)
    │       ├── EventDetailUiState.kt       # Sealed interface (Loading / Loaded / Error)
    │       ├── EventDetailAction.kt        # User actions (OnClickEdit / OnClickDelete / OnClickBack)
    │       ├── EventDetailViewModel.kt     # ViewModel: loads event from NavKey, handles edit/delete
    │       ├── composable/
    │       │   ├── Content.kt              # Loaded state — full detail view + delete button
    │       │   ├── Loading.kt              # Loading state Composable
    │       │   └── Error.kt               # Error state Composable
    │       └── previewParameter/
    │           └── EventDetailUiStatePreviewProvider.kt
    │   └── eventFormTest/                  # Test screen: edit (eventId set) / create (eventId null) — MVI
    │       ├── EventFormTestScreen.kt      # EventFormTest NavKey + entry (onNavigateBack); TopAppBar save
    │       ├── EventFormUiState.kt         # Sealed (Loading / Editing(form, calendars, isSaving) / Error)
    │       ├── EventFormAction.kt          # Field changes + OnClickSave / OnClickBack
    │       ├── EventFormTestViewModel.kt   # Assisted on eventId?; prefill via observeEvent/observeCalendars; updateEvent
    │       ├── composable/                 # EventFormFields (text fields, all-day, calendar dropdown), DateTimeField (pickers)
    │       ├── model/                      # EventFormData, CalendarChoice
    │       └── utils/                      # EventFormMapper (Event ↔ form ↔ EventEditData)
    └── theme/
        ├── Theme.kt                # CalendarTheme Composable (Material 3 color schemes)
        ├── Color.kt                # Color tokens
        └── Type.kt                 # Typography tokens

app/src/main/res/
├── drawable/                       # Vector drawables (icons, launcher)
├── mipmap-*/                       # Launcher icons (per density + adaptive)
├── values/                         # colors.xml, strings.xml, themes.xml (default, English)
├── values-night/                   # Dark theme overrides
├── values-<lang>/                  # Per-language string translations (da, de, el, es, fi, fr, it, nb, nl, pl, pt, sv)
└── xml/                            # backup_rules.xml, data_extraction_rules.xml

app/src/test/                       # JUnit unit tests (e.g., ExampleUnitTest.kt)
app/src/androidTest/                # Instrumented / Compose UI tests (e.g., ExampleInstrumentedTest.kt)

app/
├── build.gradle.kts                # App module build script (Android application + Compose plugins)
├── proguard-rules.pro              # ProGuard / R8 rules for release builds
└── AGENTS.md                       # This file
```

## Local Norms

### Architecture & Design

- **Single Activity**: `MainActivity` hosts all Compose content via `setContent { CalendarTheme { Surface { MainNavHost() } } }`.
- **Compose-only UI**: No XML layouts / ViewBinding; use Material 3 components.
- **Navigation**: Uses Jetpack Navigation 3 (`NavDisplay` + `entryProvider`). Each screen defines its own `NavKey`
  data object and an `EntryProviderScope<NavKey>` extension (e.g., `home()`). Top-level wiring lives in `MainNavHost`.
- **MVI**: Test screens follow an MVI loop. The UI sends `…Action`s to the ViewModel via a single
  `processAction(action)` entry point. The ViewModel mutates a `StateFlow<…UiState>` for rendering and emits one-shot
  `…UiEvent`s (navigation, etc.) through a `Channel(BUFFERED).receiveAsFlow()` — collected once in the screen's entry
  via `LaunchedEffect`. Use a `Channel` (not a `SharedFlow`) so each event is consumed exactly once with no replay.
- **DI**: Metro `@DependencyGraph` (`AppGraph`) scoped to `AppScope`. ViewModels are auto-registered
  via multibinding (`@ContributesIntoMap` + `@ViewModelKey`) and resolved through `MetroViewModelFactory`,
  set as `defaultViewModelProviderFactory` in `MainActivity`. In Composables, use the standard
  `viewModel<MyViewModel>()` from `androidx.lifecycle.viewmodel.compose`. When several instances of the same screen can
  coexist (e.g. multiple `EventDetail` entries), pass a distinct `viewModel(key = …)` so each one gets its own instance
  instead of sharing the Activity-scoped ViewModel.
- **Edge-to-edge**: Call `enableEdgeToEdge()` in `onCreate` before `setContent` (already wired in `MainActivity`).
- **Shared logic**: Prefer reusing models / logic from `com.infomaniak.multiplatform_calendar.*` instead of duplicating
  Android-only equivalents.
- **KISS / SOLID**: Keep Composables focused; extract reusable pieces into small `@Composable` functions.

### Commands

```bash
# Build debug
./gradlew :app:assembleDebug

# Build release
./gradlew :app:assembleRelease

# Run unit tests
./gradlew :app:testDebugUnitTest

# Run instrumented / Compose UI tests (device or emulator required)
./gradlew :app:connectedDebugAndroidTest

# Install on a connected device
./gradlew :app:installDebug

# Clean
./gradlew clean
```

### Code Style

**General:**

- Official Kotlin code style enforced through `kotlin.code.style=official` in `gradle.properties`.
- Use Android Studio's default Kotlin formatter; no extra linter is configured at this time — keep diffs clean.

**Line Length:**

- Aim for **130 characters** per line maximum for Kotlin files.
- Exceptions: long string literals, URLs, and import statements.

**Blank Lines:**

- Never use more than 1 consecutive blank line.
- Add 1 blank line after an early `return` statement.

**Copyright Headers:**

- Required in **all** Kotlin source files (and any non-generated XML where applicable).
- Format used across the codebase (see `MainActivity.kt`, `app/build.gradle.kts`):
  ```
  /*
   * Infomaniak Calendar - Android
   * Copyright (C) YYYY Infomaniak Network SA
   *
   * This program is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as published by
   * the Free Software Foundation, either version 3 of the License, or
   * (at your option) any later version.
   *
   * ...full GPLv3 header...
   */
  ```
- Use a range when a file spans multiple years: `Copyright (C) 2026-2027 Infomaniak Network SA`.
- **No blank line between the copyright block and the `package` declaration.**

**Naming:**

- Classes / Composables / enums: PascalCase (`MainActivity`, `CalendarTheme`, `HomeViewModel`).
- Functions / properties: camelCase (`enableEdgeToEdge`, `viewModelFactory`).
- Packages: single word if possible  (`com.infomaniak.calendar.ui.theme`) or camelCase if not possible in one word.
- Enum entries: PascalCase for new enums (`Home`, `Favorites`). Existing entries that are persisted (e.g., to
  `SharedPreferences`) must not be renamed.
- Resource IDs: snake_case (`ic_home`, `ic_account_box`).

**Control Flow:**

```kotlin
// Trivial statements: prefer one-line (under 130 chars)
if (condition) return result

// Trivial if/else: prefer one-line
val color = if (isDark) darkColor else lightColor

// Non-trivial: always use braces + newlines
if (condition) {
    doSomething()
    doAnotherThing()
}
```

**Jetpack Compose:**

```kotlin
// One parameter: one line (if under 130 chars)
@Composable
fun MyComponent(name: String) {
    Text("Hello $name!")
}

// Multiple parameters: each on its own line with trailing comma
@Composable
fun MyComponent(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(text = "Hello $name!", modifier = modifier)
}
```

- Always accept `modifier: Modifier = Modifier` as the first optional parameter for reusable Composables.
- Prefer `@Preview` Composables next to the Composable they preview.
- Place theme tokens in `ui/theme/` and use `CalendarTheme { ... }` as the outermost wrapper in every entry point and
  preview.

**Resources (XML):**

- Remove `fillColor="#00000000"` entries (invisible colors auto-added by Figma exports).
- Run Android Studio's "Reformat Code" on `res/drawable/` before committing icon changes.
- Keep `values/` and `values-night/` in sync for any new color / theme attribute.
- Localization: supported app languages are limited via `androidResources.localeFilters` in `app/build.gradle.kts`, and
  `generateLocaleConfig = true` produces the locale config so users can pick a per-app language (Android 13+ system
  setting). When adding or removing a `values-<lang>/` folder, update `localeFilters` accordingly.

### UI Development

- **Compose-first**: All new screens are Compose. Do not introduce XML layouts or ViewBinding.
- **Material 3**: Use `androidx.compose.material3.*` components.
- **Theming**: Wrap content in `CalendarTheme`. Add new tokens to `ui/theme/Color.kt`, `Type.kt`, and `Theme.kt`.
- **Edge-to-edge & insets**: Honor `Scaffold` inner padding (see `Modifier.padding(innerPadding)`); do not hardcode
  system bar insets.

### Testing

- **Unit Tests**: `app/src/test/java/com/infomaniak/calendar/`
    - JUnit 4 (`libs.junit`). Add MockK or other tools through the version catalog if needed.
- **UI / Instrumented Tests**: `app/src/androidTest/java/com/infomaniak/calendar/`
    - Compose UI Test (`androidx-compose-ui-test-junit4`) and Espresso (`androidx-espresso-core`).
    - Use `createAndroidComposeRule` / `createComposeRule` for Composable tests.
- **Naming**: `*Test.kt` for unit tests, `*Test.kt` / `*InstrumentedTest.kt` for androidTest.
- Keep tests deterministic — avoid relying on real time/network; mock collaborators from the KMP module when possible.

### Dependencies & Version Catalogs

- App dependencies are declared in `gradle/libs.versions.toml` (accessed via the `libs` accessor).
- The KMP submodule exposes `multiplatform-calendar/gradle/kmpCalendar.versions.toml` as the `kmpCalendar` catalog
  (see root `settings.gradle.kts`). Use it for plugin/library coordinates shared with the KMP world.
- The `multiplatform-calendar` library is consumed as a composite build; its `:Core` project is substituted for
  the `com.infomaniak.multiplaform-calendar:Core` Maven coordinate and its `:kmpdav` bridge project for the
  `com.infomaniak.multiplaform-calendar:multiplatform-calendar` coordinate (declared in `gradle/libs.versions.toml`
  as `infomaniak-multiplaform-calendar-core` / `infomaniak-multiplaform-calendar` and referenced via
  `libs.infomaniak.multiplaform.calendar.core` / `libs.infomaniak.multiplaform.calendar`).
- When adding a dependency:
    1. Add the version to `[versions]`, the coordinate to `[libraries]` (or `[plugins]`), in `libs.versions.toml`.
    2. Reference it as `libs.<group>.<name>` in `app/build.gradle.kts`.
    3. Avoid hard-coded versions in module build files.

### Environment

- **JDK**: 17 or later (see root `README.md`).
- **Gradle**: Wrapper is committed; always invoke `./gradlew`.
- **Configuration cache**: Enabled (`org.gradle.configuration-cache=true`). Keep build scripts cache-compatible (no
  `Task.project` access at execution time, etc.).
- **`local.properties`**: Auto-generated by Android Studio (SDK path). Never commit.
- **`env.properties`**: Holds the `sentryAuthToken` used by the Sentry Gradle plugin (see `env.example.properties`). Git-ignored;
  required for `release` builds (the build fails fast otherwise), optional for debug builds.
- **Analytics & crash reporting**: Sentry (`io.sentry.dsn` + `auto-init=false` in the manifest, initialised via Core's
  `configureSentry` in `MainApplication`) and Matomo (`MatomoCalendar`, built on Core's `Matomo` module).
- **Submodule**: Run `git submodule update --init --recursive` after a fresh clone before building.

## Learned Preferences

*Add project-specific corrections here as they occur.*

**ViewModel DI pattern:**

Every new ViewModel needs three annotations — no changes to `AppGraph` required:

```kotlin
@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(MyViewModel::class)
class MyViewModel(...) : ViewModel()
```

In Composables, use the standard `viewModel<MyViewModel>()`.
`MetroViewModelFactory` (set as `defaultViewModelProviderFactory` in `MainActivity`) resolves the VM
from the multibinding map automatically. Because the current `LocalViewModelStoreOwner` is the Activity
(no per-`NavEntry` ViewModelStore decorator is installed), the Metro factory is picked up without passing it
explicitly. To get a distinct instance per screen occurrence, pass `viewModel(key = …)`.

**ViewModel with a constructor argument (navigation args):**

When a ViewModel needs a navigation arg **and** Metro-injected dependencies, use Metro **assisted injection**:
the arg is `@Assisted`, the dependencies are injected normally. Expose an `@AssistedFactory` and build the VM
with the standard Compose `viewModel(key = ...) { factory.create(arg) }` initializer overload (no
`SavedStateHandle`/`CreationExtras` plumbing). The composable pulls the *factory* from the graph — never the
injected dependencies themselves.

```kotlin
@AssistedInject
class MyViewModel(
  @Assisted private val id: SomeId,
  private val repo: SomeRepo, // injected by Metro
) : ViewModel() {
  // ...
  @AssistedFactory
  fun interface Factory {
    fun create(id: SomeId): MyViewModel
  }
}

// Expose the factory on AppGraph: `val myViewModelFactory: MyViewModel.Factory`
// In the screen entry:
val factory = ComposeAppGraph.myViewModelFactory
val viewModel = viewModel(key = destination.id.value) { factory.create(destination.id) }
```

The distinct `key` gives each occurrence its own instance (fixes "wrong detail shown" bugs).

> **Future simplification**: When upgrading to Metro 0.12.0+ (requires Kotlin 2.3.20+),
> `@ViewModelKey` will support `implicitClassKey` (no need to repeat the class name), and the
> `metrox-viewmodel` artifact will replace our hand-written `MetroViewModelFactory`, `ViewModelKey`,
> and `@Multibinds` declarations.

**Kotlin Control Flow:**

- One-line `if` / `if/else` for trivial expressions within the line-length limit.
- Braces + newlines for any non-trivial branch.

**Jetpack Compose:**

- Single-parameter Composables may stay on one line when within the line limit.
- Always thread `Modifier` through reusable Composables.

**Resources (XML):**

- Strip Figma-generated `fillColor="#00000000"` attributes.
- Reformat drawables with Android Studio before committing.

**Navigation with screen data (EventDetail pattern):**

When a detail screen needs data from the previous screen (e.g., EventDetail), encode all pre-formatted fields
directly in the `NavDestination` data class as primitive/nullable-String fields (no cross-screen model imports).
The entry screen's extension function accepts a navigation callback (e.g., `onNavigateToEventDetail`) and the
ViewModel is initialized via `LaunchedEffect(destination) { viewModel.load(destination) }`.
The ViewModel guards re-initialization with `if (uiState.value !is Loading) return`.
Post-action navigation (e.g., after delete) is signalled by an `isDeleted: Boolean` flag in `Loaded` state,
collected via `LaunchedEffect(state)` in the composable.

**Material Icons dependency:**

`androidx.compose.material:material-icons-core` is a separate dependency from Material 3.
It is declared as `core.compose.material.icons` in the Core catalog and must be added to
`app/build.gradle.kts` to use `Icons.Default.*` in Composables.

## Self-correction

1. **Stale Map**: Update the Context Map when new packages, screens, or top-level files appear under `app/`.
2. **New Norms**: Add user corrections to "Learned Preferences" immediately.
3. **Reference Submodule**: When touching imports from `com.infomaniak.multiplatform_calendar.*`, remember the source
   lives in the `multiplatform-calendar` repository — coordinate changes there, not in `app/`.
4. **Always document architecture changes here**: Any change to the app's architecture, module layout, navigation,
   theming approach, conventions, commands, testing setup, or anything else that future agents should know about
   **must** be reflected in this `app/AGENTS.md` (or the root `AGENTS.md` if it is cross-cutting) as part of the same
   change. Treat AGENTS.md as part of the deliverable, not as optional documentation.
