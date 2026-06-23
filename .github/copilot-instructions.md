# Copilot Coding Agent Onboarding — android-calendar

> **Read `AGENTS.md` and `app/AGENTS.md` first** for architecture and conventions. This file covers build, CI, and validation.

## Overview
Infomaniak Calendar for Android. Pure Jetpack Compose + Navigation 3, Metro DI (no Hilt), multiplatform business logic via the `multiplatform-calendar` KMP submodule. Two flavors: `standard` and `fdroid`.

## ⚠️ Critical: Rust Toolchain Required
The `multiplatform-calendar` submodule includes a Rust/UniFFI module (`:kmpdav`). Without the Rust toolchain and Android targets, the build fails:
```bash
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
```

## One-Time Environment Setup
```bash
git submodule update --init --recursive   # Core + multiplatform-calendar submodules
./gradlew --stop   # stop stale daemon after Rust toolchain setup
```

## Build & Test (CI: `.github/workflows/android.yml`)
```bash
./gradlew clean
./gradlew build
./gradlew testStandardDebugUnitTest --stacktrace
./gradlew testFdroidDebugUnitTest --stacktrace
```
CI also runs Android Lint via the shared `infomaniak/.github` reusable workflow.

## Project Layout
```
app/                       # Main Android Calendar app
CalendarComponents/        # Portable Compose UI library (Foundation, Event, Planning, Resources)
Core/                      # Git submodule — Infomaniak Core
multiplatform-calendar/    # Git submodule — KMP business logic (includes Rust/UniFFI :kmpdav)
gradle/libs.versions.toml
```

## PR Review Instructions

- Ensure strings are localized via `strings.xml` resources.
- Ensure UI is written in Jetpack Compose using Material3 components.
- DI is **Metro** (not Hilt): use `@Inject`, `@Component`, `@Module` from `dev.zacsweers.metro` — do not introduce Hilt.
- `standard` flavor only: Firebase, Google services — fdroid builds must compile without them.
- When adding/removing a runtime dependency, update `LICENSES.md` at the repo root.
