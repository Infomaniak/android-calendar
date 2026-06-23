/*
 * Infomaniak Calendar - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("Core/build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("com.infomaniak.core.composite")
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://jitpack.io")
            content {
                includeModule("com.github.lottiefiles", "dotlottie-android")
                includeModule("com.github.matomo-org", "matomo-sdk-android")
                includeModule("com.github.AppDevNext.Logcat", "LogcatCoreLib")
            }
        }
    }

    versionCatalogs {
        create("kmpCalendar") { from(files("multiplatform-calendar/gradle/kmpCalendar.versions.toml")) }
        create("core") { from(files("Core/gradle/core.versions.toml")) }
    }
}

rootProject.name = "Calendar"
include(":app")

// Read local.properties first (git-ignored), then fall back to gradle.properties.
// Set useCalendarCoreCompositeBuild=true in local.properties to use the local submodule source
// instead of the published AAR artifacts from Maven Local / Maven Central.
val localProperties = java.util.Properties().also { props ->
    val localPropertiesFile = file("local.properties")
    if (localPropertiesFile.exists()) localPropertiesFile.inputStream().use { props.load(it) }
}
val useCalendarCoreCompositeBuild = (localProperties.getProperty("useCalendarCoreCompositeBuild")
    ?: providers.gradleProperty("useCalendarCoreCompositeBuild").orNull)
    ?.toBoolean() ?: false

if (useCalendarCoreCompositeBuild) {
    includeBuild("multiplatform-calendar") {
        dependencySubstitution {
            substitute(module("com.infomaniak.multiplaform-calendar:Core")).using(project(":Core"))
            substitute(module("com.infomaniak.multiplaform-calendar:multiplatform-calendar")).using(project(":kmpdav"))
        }
    }
}
