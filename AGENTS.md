# AGENTS.md - Infomaniak Calendar (Top Level)

> **Navigation Guide**: This file describes the composite structure. For app-specific norms, see `app/AGENTS.md`. For the
> shared Kotlin Multiplatform calendar library, see the `multiplatform-calendar/` Git submodule (its own repository).

## Overview
Infomaniak Calendar for Android. Pure Jetpack Compose + Navigation 3, Metro DI (no Hilt), multiplatform business logic via the `multiplatform-calendar` KMP submodule. Two flavors: `standard` and `fdroid`.

## Repository Structure

This is a **multi-module Gradle build** with two Git submodules:

```
android-calendar/
├── app/                            # Android Calendar application (see app/AGENTS.md)
│   ├── src/main/java/...           # App source code (Kotlin + Jetpack Compose)
│   ├── src/main/res/               # Android resources
│   ├── src/test/                   # Unit tests (JUnit)
│   └── src/androidTest/            # Instrumented / Compose UI tests
├── CalendarComponents/             # Portable Android UI component library (this repo)
│   ├── Foundation/                 # Shared models + base Compose components
│   ├── Event/                      # EventItem Composable
│   ├── Day/                        # Day Composable (hour grid)
│   ├── Planning/                   # Planning Composable (week/day/event list)
│   ├── Calendar/                   # Calendar Composable (expanded, unexpanded)
│   └── Resources/                  # Centralised string resources (no code)
├── Core/                           # Git submodule - Infomaniak shared Android library
│   ├── build-logic/                # Composite build: provides com.infomaniak.core.composite plugin
│   └── gradle/core.versions.toml  # Core version catalog consumed by the root build
├── multiplatform-calendar/         # Git submodule - KMP shared library
│   └── gradle/kmpCalendar.versions.toml   # Version catalog consumed by the root build
├── gradle/
│   ├── libs.versions.toml          # Android app version catalog
│   └── wrapper/                    # Gradle wrapper
├── build.gradle.kts                # Root build script
├── settings.gradle.kts             # Includes :app + :CalendarComponents:*; composite builds for Core and multiplatform-calendar
└── AGENTS.md                       # This file (top-level overview)
```

## Quick Summary

| Component                    | Location                  | AGENTS.md                          | Purpose                                                                   |
|------------------------------|---------------------------|------------------------------------|---------------------------------------------------------------------------|
| **App**                      | `app/`                    | `app/AGENTS.md`                    | Android Calendar app (Jetpack Compose UI, navigation, lifecycle)          |
| **CalendarComponents**       | `CalendarComponents/`     | this file (see section below)      | Portable Compose UI component library; intended for future cross-app reuse|
| **Core**                     | `Core/`                   | `Core/AGENTS.md`                   | Git submodule — Infomaniak shared Android library (auth, network, UI, …)  |
| **Multiplatform Calendar**   | `multiplatform-calendar/` | submodule repo (separate)          | Git submodule — Shared KMP business/data logic (models, networking, etc.) |
| **Root**                     | `./`                      | `AGENTS.md`                        | This file - multi-module build overview                                   |

## Build Layout Explained

- **`Core` is a Git submodule** ([Infomaniak/android-core](https://github.com/Infomaniak/android-core)): Provides shared
  Android infrastructure (auth, network, Compose UI primitives, utilities) consumed across all Infomaniak Android apps.
  It is wired into the build in two ways:
    - `pluginManagement { includeBuild("Core/build-logic") }` — exposes the `com.infomaniak.core.composite` settings
      plugin and all `core.plugins.*` Gradle plugin aliases used in module `build.gradle.kts` files.
    - `Core/gradle/core.versions.toml` — exposed as the `core` version catalog.
  Do **not** modify files under `Core/` from this repository; changes belong in the `Infomaniak/android-core` repo.
- **`multiplatform-calendar` is a Git submodule**: It lives in the separate
  [Infomaniak/multiplatform-calendar](https://github.com/Infomaniak/multiplatform-calendar) repository and is consumed here
  through Gradle. Changes inside it are tracked in that repo, not this one.
- **Three version catalogs**:
    - `gradle/libs.versions.toml` — Android app + CalendarComponents dependencies (default `libs` catalog).
    - `multiplatform-calendar/gradle/kmpCalendar.versions.toml` — KMP-side dependencies, exposed in `settings.gradle.kts`
      as the `kmpCalendar` catalog (e.g. `kotlinx-datetime` used by Foundation and Planning).
    - `Core/gradle/core.versions.toml` — Core library dependencies and plugin aliases, exposed as the `core` catalog
      (e.g. `core.plugins.android.library`, `core.compose.bom`, `core.infomaniak.core.ui.compose.margin`).
- **Project inclusion**: `settings.gradle.kts` includes `:app` plus the four `:CalendarComponents:*` subprojects, and
  wires `multiplatform-calendar` as a composite build via `includeBuild("multiplatform-calendar")` with a
  `dependencySubstitution` block that maps two Maven coordinates:
    - `com.infomaniak.multiplaform-calendar:Core` → `:Core` project
    - `com.infomaniak.multiplaform-calendar:multiplatform-calendar` → `:kmpdav` project (internal bridge module)
  The app depends on both via `libs.infomaniak.multiplatform.calendar` and `libs.infomaniak.multiplatform.calendar.core`.
- **Impact**: Editing `multiplatform-calendar/` or `Core/` affects every consumer of those libraries — changes belong in
  their own repos and PRs.

## Submodule Workflow

```bash
# Initial clone of this repo
git clone --recurse-submodules <repo-url>

# Or after a plain clone
git submodule update --init --recursive

# Pull latest submodule revision pointed to by this repo
git submodule update --remote multiplatform-calendar
git submodule update --remote Core
```

## CalendarComponents

`CalendarComponents/` is a group of Android library modules that live **in this repository** and are included as regular
Gradle subprojects (not a composite build). They hold all calendar-specific Compose UI that could eventually be
extracted and shared with other Infomaniak apps.

### Design intent

The group is intentionally **self-contained**: no dependency on `:app`, no DI framework, no KMP types leaking in.
String resources follow the single-module pattern — all strings consumed by any CalendarComponents module are declared in 
`:CalendarComponents:Resources` so consumers never have to manage per-module string tags.

### Modules

| Gradle path                       | Package                                          | Purpose                                                                                                                   |
|-----------------------------------|--------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| `:CalendarComponents:Foundation`  | `com.infomaniak.calendar.components.foundation`  | Shared models (`EventUi`, `YearWeek`, `WeekNumbering`) and base Compose components (`DayCircle`, `DateState`) used by all other CalendarComponents modules. |
| `:CalendarComponents:Resources`   | `com.infomaniak.calendar.components.resources`   | String-only module: `res/values/strings.xml` (+ translations). No Kotlin code, no Compose. Centralises all CalendarComponents string resources. |
| `:CalendarComponents:Event`       | `com.infomaniak.calendar.components.event`       | `EventItem` Composable — renders a single event row. Re-exports Foundation via `api`. |
| `:CalendarComponents:Planning`    | `com.infomaniak.calendar.components.planning`    | `Planning` Composable — a `LazyColumn` with ISO week headers and per-day event lists. Also provides the `stickyWithinItem` `Modifier` extension. Re-exports Event, Foundation, and Resources via `api`. Week header design is a **placeholder**. |
| `:CalendarComponents:Day`         | `com.infomaniak.calendar.components.day`         | Day view — the scrollable hour grid a single day is drawn on. Holds `resolveOverlaps`, the pure-Kotlin solver placing concurrent events, ported from the [Eventually](https://github.com/claustrofob/Eventually) SwiftUI layout the iOS calendar uses so both platforms arrange a day identically. Reusable by the future 3-day / week views. Re-exports Foundation via `api`. |

### Dependency graph

```
Planning ──api──► Event ──api──► Foundation
    │                 ▲               ▲
    ├──api────────────┼───────────────┤
    └──api──► Resources               │
                      │               │
Day ──────────────────┴───api─────────┘
```

`Foundation` is the only module with no CalendarComponents dependency. `Planning` and `Day` are the two top-level
entry points for consumers, one per calendar view; each transitively brings in the stack it needs.

### What is final vs placeholder

| Component                             | Status                        |
|---------------------------------------|-------------------------------|
| `DayCircle` + `DateState` (Foundation)| ✅ Final UI                   |
| `DayIndicator` (Planning)             | ✅ Final UI                   |
| `EventItem` (Event)                   | ✅ Final UI                   |
| Week header in `Planning`             | 🚧 Placeholder — design TBD   |

### External dependencies used

- `core.compose.bom` / `core.compose.*` — Compose BOM and UI primitives (from `core` catalog).
- `core.infomaniak.common` — `KotlinDateUtils` date helpers (`Clock.today()`, `LocalDate.isToday()`, …) (from `core` catalog).
- `core.infomaniak.core.ui.compose.margin` — `Margin` spacing constants (from `core` catalog, Planning only).
- `kmpCalendar.kotlinx.datetime` — `kotlinx-datetime` types (`LocalDate`, `TimeZone`, …) (Foundation + Planning).
- `libs.compose.material3` — Material 3 Compose (from `libs` catalog).
- `libs.infomaniak.designsystem.theme.calendar` — Infomaniak Design System.

### Ownership

All CalendarComponents source lives **in this repository**. Changes to these modules belong in PRs against
`Infomaniak/android-calendar`. Do **not** edit them from a consuming app or the submodule repositories.

## Key Integration Points

- **Two KMP modules consumed by the app**: The submodule contains two consumable Gradle projects (plus a pure-aggregator
  root project `:` with no sources):
    - **kmpdav module** (`:kmpdav`) — internal bridge module: Rust/UniFFI CalDAV bridge, remote CalDAV models/client, `CaldavClientModule`.
    - **Core module** (`:Core`) — public API module: domain models, Room database, repositories, `AccountManager`, `CalendarManager`, Apple `CalendarSDK`.
- **Shared models / business logic**: The app imports from `com.infomaniak.multiplatform_calendar.core.*` (e.g.,
  `com.infomaniak.multiplatform_calendar.core.domain.model.calendar.Color`) and the bridge from
  `com.infomaniak.multiplatform_calendar.data.remote.caldav.*` (e.g., `DavAccount`).
- **Dependency wiring**: Declared in `app/build.gradle.kts` via two dependencies:
    - `implementation(libs.infomaniak.multiplatform.calendar)` — substituted with the `:kmpdav` project.
    - `implementation(libs.infomaniak.multiplatform.calendar.core)` — substituted with `:Core`.
- **DI**: The app uses Metro's `@DependencyGraph` (`AppGraph`) which picks up `@ContributesTo` modules from both
  the `:kmpdav` and Core modules (e.g., `CalendarCoreGraph`, `AndroidDatabaseModule`, `DatabaseModule`, `CaldavClientModule`).
  `CalendarCoreGraph` (in Core `commonMain`) defines the shared accessors (`accountManager`, `calendarManager`)
  and is automatically merged into `AppGraph` (Android). On Apple, `CalendarSDK` lives in Core `appleMain` and explicitly
  inherits `:kmpdav`'s `CaldavClientModule` while also receiving Core's contributed bindings.
- **Apple artifact**: The public `KmpCalendar.xcframework` is produced by the Core module (`:Core`). `:kmpdav` is a
  plain `implementation` dependency and is **not** exported: the public Apple API exposes only Core-owned types (e.g.
  credentials are passed as Core's `DavCredentials`, mapped to `:kmpdav`'s `DavAccount` at the repository boundary).
  `CalendarSDKProvider.shared.sdk` is the Apple entry point exposed by Core.
- **Rust/UniFFI**: The Rust crate lives in `multiplatform-calendar/kmpdav/rust/caldav_bridge`; `:kmpdav/build.gradle.kts`
  wires Gobley (`dev.gobley.cargo` / `dev.gobley.uniffi`) to compile it and generate `uniffi.caldav_bridge.*` bindings.
- **Plugin aliases**: Module `build.gradle.kts` files reference plugins from all three catalogs:
  `libs.plugins.*` (app), `kmpCalendar.plugins.*` (KMP), and `core.plugins.*` (Core build-logic).

## Quick Commands

```bash
# Build the Android app (debug)
./gradlew :app:assembleDebug

# Build the Android app (release)
./gradlew :app:assembleRelease

# Run unit tests
./gradlew :app:testDebugUnitTest

# Run instrumented / Compose UI tests (requires a connected device or emulator)
./gradlew :app:connectedDebugAndroidTest

# Build all CalendarComponents modules
./gradlew :CalendarComponents:Planning:assembleDebug

# Clean
./gradlew clean
```

## Important Rules

1. **Editing submodules**: Do not modify files under `multiplatform-calendar/` or `Core/` from this repository. Open a
   PR in the respective repo (`Infomaniak/multiplatform-calendar` or `Infomaniak/android-core`), then bump the submodule
   pointer here. The `multiplatform-calendar` submodule pointer and the `calendarCore` version in
   `gradle/libs.versions.toml` must always be bumped together in the same PR — this is enforced by the
   [`calendar-core-sync-check.yml`](.github/workflows/calendar-core-sync-check.yml) CI workflow.
2. **CalendarComponents ownership**: All `CalendarComponents/` source lives in **this** repository. Changes go here, not
   in a consuming app or a submodule. The group is designed to be portable — keep it free of `:app` dependencies and DI
   framework references so it can be extracted in the future.
3. **Norm separation**:
    - App norms → `app/AGENTS.md`
    - Multi-module / build / submodule / CalendarComponents norms → this file
    - KMP library norms → `multiplatform-calendar` submodule repository
    - Core library norms → `Core` submodule repository (`Core/AGENTS.md`)
4. **When working on**:
    - `app/src/...` → read `app/AGENTS.md`
    - `CalendarComponents/...` → read the CalendarComponents section in this file
    - Build files / submodule pointer → read this file
    - KMP shared code → switch to the `multiplatform-calendar` repository
    - Core shared code → switch to the `Core` (`android-core`) repository
5. **Keep AGENTS.md up to date**: For every change to the architecture, build layout, module structure, conventions,
   commands, or anything else that should be reflected in AGENTS.md, you **must** update the relevant AGENTS.md file
   (this one for cross-cutting/build/submodule changes, `app/AGENTS.md` for app-specific changes) as part of the same
   change. If a change is worth notifying agents about, it belongs in AGENTS.md.

## Self-correction

1. **Stale map**: Update this file when new top-level modules, catalogs, or submodules are added.
2. **CalendarComponents growth**: When a new module is added under `CalendarComponents/`, add it to the table and
   dependency graph in the CalendarComponents section. When placeholder UI is finalised, update the status table.
3. **New norms**: Add cross-cutting build/submodule conventions here as they appear.
4. **Reference app**: For any change touching `app/`, also consult `app/AGENTS.md`.
