# android-calendar

## Table of Contents

- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Build Modes](#build-modes)
- [Contributing](#contributing)
- [License](#license)

## Prerequisites

Before you begin, ensure you have met the following requirements:

- You are using a Linux, macOS, or Windows machine.
- You have installed Java Development Kit (JDK) 17 or later.
- You have Android Studio installed.
- You have an active internet connection to download project dependencies.

## Getting Started

Clone the repository with its submodules:

```bash
git clone --recurse-submodules <repo-url>
```

Or, after a plain clone:

```bash
git submodule update --init --recursive
```

Then open the project in Android Studio and let Gradle sync.

## Build Modes

The app depends on the `multiplatform-calendar` KMP library. Two modes are available:

### Default — Published AARs (Maven Local / Maven Central)

No configuration needed. Gradle resolves the library from Maven Local or Maven Central automatically.

### Local source — Composite build (for KMP development)

When you need to work on the `multiplatform-calendar` submodule alongside the app, you can tell Gradle
to compile the submodule from source and substitute the published AARs at build time.

Add the following line to your `local.properties` (this file is git-ignored and never committed):

```properties
useCalendarCoreCompositeBuild=true
```

With this flag set, Gradle will:
1. Include `multiplatform-calendar/` as a composite build.
2. Substitute `com.infomaniak.multiplaform-calendar:Core` → local `:Core` project.
3. Substitute `com.infomaniak.multiplaform-calendar:multiplatform-calendar` → local `:kmpdav` project.

Remove the line (or set it to `false`) to switch back to the published AARs.

## Contributing

If you see a bug or an enhancement point, feel free to create an issue, so that we can discuss it. Once approved, we or you (
depending on the priority of the bug/improvement) will take care of the issue and apply a merge request. Please, don't do a merge
request before creating an issue.

## License

This project is under GPLv3 license. See the LICENSE file for more details.
