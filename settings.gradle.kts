pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    versionCatalogs {
        create("kmpCalendar") { from(files("multiplatform-calendar/gradle/kmpCalendar.versions.toml")) }
        create("core") { from(files("Core/gradle/core.versions.toml")) }
    }
}

rootProject.name = "Calendar"
include(":app")

includeBuild("multiplatform-calendar") {
    dependencySubstitution {
        substitute(module("com.infomaniak.multiplaform-calendar:core")).using(project(":Core"))
    }
}
