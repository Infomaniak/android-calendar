# Copilot Coding Agent Onboarding — android-calendar

> **Read `AGENTS.md` and `app/AGENTS.md` first** for architecture and conventions. This file covers build, CI, and validation.

## Overview
Infomaniak Calendar for Android. Pure Jetpack Compose + Navigation 3, Metro DI (no Hilt), multiplatform business logic via the `multiplatform-calendar` KMP submodule. Two flavors: `standard` and `fdroid`.

## ⚠️ Critical: Rust Toolchain Required
The `multiplatform-calendar` submodule includes a Rust/UniFFI module (`:kmpdav`). Without the Rust toolchain and Android targets, the build fails:
```bash
# Install Rust + Android targets (once per machine)
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
```

## One-Time Environment Setup
```bash
git submodule update --init --recursive   # Core + multiplatform-calendar submodules
# Stop stale daemon after Rust toolchain setup so it picks up updated PATH:
./gradlew --stop
```

## Build & Test (CI: `.github/workflows/android.yml`)
CI runs on every non-draft PR:
```bash
./gradlew clean
./gradlew build
./gradlew testStandardDebugUnitTest --stacktrace
./gradlew testFdroidDebugUnitTest --stacktrace
```
CI also runs Android Lint via the shared `infomaniak/.github` reusable workflow.

## Project Layout
```
app/                       # Main Android Calendar app (see app/AGENTS.md)
CalendarComponents/        # Portable Compose UI library (Foundation, Event, Planning, Resources)
Core/                      # Git submodule — Infomaniak Core library
multiplatform-calendar/    # Git submodule — KMP business logic (includes Rust/UniFFI :kmpdav)
gradle/libs.versions.toml  # Repo-level version catalog
settings.gradle.kts        # Composite build config
```

## Key Conventions
- DI is **Metro** (not Hilt): `@Inject`, `@Component`, `@Module` from `dev.zacsweers.metro`.
- Navigation uses **AndroidX Navigation 3** (beta API — may change).
- `standard` flavor only: Firebase, Google Play services (never reference in `fdroid` code paths).
- All strings in `res/values/strings.xml`.
- When adding/removing a runtime dependency, update `LICENSES.md` at the repo root.
