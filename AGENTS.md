# AGENTS.md - Infomaniak Calendar (Top Level)

> **Navigation Guide**: This file describes the composite structure. For app-specific norms, see `app/AGENTS.md`. For the
> shared Kotlin Multiplatform calendar library, see the `multiplatform-calendar/` Git submodule (its own repository).

## Repository Structure

This is a **multi-module Gradle build** with a Git submodule for the shared Kotlin Multiplatform business logic:

```
android-calendar/
├── app/                            # Android Calendar application (see app/AGENTS.md)
│   ├── src/main/java/...           # App source code (Kotlin + Jetpack Compose)
│   ├── src/main/res/               # Android resources
│   ├── src/test/                   # Unit tests (JUnit)
│   └── src/androidTest/            # Instrumented / Compose UI tests
├── multiplatform-calendar/         # Git submodule - KMP shared library
│   └── gradle/kmpCalendar.versions.toml   # Version catalog consumed by the root build
├── gradle/
│   ├── libs.versions.toml          # Android app version catalog
│   └── wrapper/                    # Gradle wrapper
├── build.gradle.kts                # Root build script
├── settings.gradle.kts             # Includes :app; uses includeBuild("multiplatform-calendar") for composite build
└── AGENTS.md                       # This file (top-level overview)
```

## Quick Summary

| Component                    | Location                  | AGENTS.md                          | Purpose                                                          |
|------------------------------|---------------------------|------------------------------------|------------------------------------------------------------------|
| **App**                      | `app/`                    | `app/AGENTS.md`                    | Android Calendar app (Jetpack Compose UI, navigation, lifecycle) |
| **Multiplatform Calendar**   | `multiplatform-calendar/` | submodule repo (separate)          | Shared KMP business/data logic (models, networking, etc.)        |
| **Root**                     | `./`                      | `AGENTS.md`                        | This file - multi-module build overview                          |

## Build Layout Explained

- **`multiplatform-calendar` is a Git submodule**: It lives in the separate
  [Infomaniak/multiplatform-calendar](https://github.com/Infomaniak/multiplatform-calendar) repository and is consumed here
  through Gradle. Changes inside it are tracked in that repo, not this one.
- **Two version catalogs**:
    - `gradle/libs.versions.toml` — Android app dependencies (default `libs` catalog).
    - `multiplatform-calendar/gradle/kmpCalendar.versions.toml` — KMP-side dependencies, exposed in `settings.gradle.kts`
      as the `kmpCalendar` catalog.
- **Project inclusion**: `settings.gradle.kts` includes `:app` and wires `multiplatform-calendar` as a composite build
  via `includeBuild("multiplatform-calendar")` with a `dependencySubstitution` block that maps two Maven coordinates:
    - `com.infomaniak.multiplaform-calendar:Core` → `:Core` project
    - `com.infomaniak.multiplaform-calendar:multiplatform-calendar` → `:kmpdav` project (internal bridge module)
  The app depends on both via `libs.infomaniak.multiplatform.calendar` and `libs.infomaniak.multiplatform.calendar.core`.
- **Impact**: Editing `multiplatform-calendar/` affects every consumer of that library - changes belong in its own repo
  and PR.

## Submodule Workflow

```bash
# Initial clone of this repo
git clone --recurse-submodules <repo-url>

# Or after a plain clone
git submodule update --init --recursive

# Pull latest submodule revision pointed to by this repo
git submodule update --remote multiplatform-calendar
```

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
- **Apple artifact**: The public `KmpCalendar.xcframework` is produced by the Core module (`:Core`) and exports the
  internal `:kmpdav` bridge module. `CalendarSDKProvider.shared.sdk` is the Apple entry point exposed by Core.
- **Rust/UniFFI**: The Rust crate lives in `multiplatform-calendar/kmpdav/rust/caldav_bridge`; `:kmpdav/build.gradle.kts`
  wires Gobley (`dev.gobley.cargo` / `dev.gobley.uniffi`) to compile it and generate `uniffi.caldav_bridge.*` bindings.
- **Plugin aliases**: Module `build.gradle.kts` files register the Android and Kotlin Multiplatform plugins from both
  catalogs (`libs.plugins.*` and `kmpCalendar.plugins.*`).

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

# Clean
./gradlew clean
```

## Important Rules

1. **Editing the submodule**: Do not modify files under `multiplatform-calendar/` from this repository. Open a PR in the
   `Infomaniak/multiplatform-calendar` repo, then bump the submodule pointer here.
2. **Norm separation**:
    - App norms → `app/AGENTS.md`
    - Multi-module / build / submodule norms → this file
    - KMP library norms → submodule repository
3. **When working on**:
    - `app/src/...` → read `app/AGENTS.md`
    - Build files / submodule pointer → read this file
    - KMP shared code → switch to the `multiplatform-calendar` repository
4. **Keep AGENTS.md up to date**: For every change to the architecture, build layout, module structure, conventions,
   commands, or anything else that should be reflected in AGENTS.md, you **must** update the relevant AGENTS.md file
   (this one for cross-cutting/build/submodule changes, `app/AGENTS.md` for app-specific changes) as part of the same
   change. If a change is worth notifying agents about, it belongs in AGENTS.md.

## Self-correction

1. **Stale map**: Update this file when new top-level modules, catalogs, or submodules are added.
2. **New norms**: Add cross-cutting build/submodule conventions here as they appear.
3. **Reference app**: For any change touching `app/`, also consult `app/AGENTS.md`.
