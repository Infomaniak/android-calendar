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
import java.util.Properties

plugins {
    alias(core.plugins.android.application)
    alias(core.plugins.compose.compiler)
    alias(core.plugins.kotlin.android)
    alias(core.plugins.kotlin.parcelize)
    alias(core.plugins.kotlin.serialization)
    alias(core.plugins.sentry.plugin)
    alias(kmpCalendar.plugins.metro)
}

val appCompileSdk: Int by rootProject.extra
val appMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    compileSdk = appCompileSdk
    namespace = "com.infomaniak.calendar"

    defaultConfig {
        applicationId = "com.infomaniak.calendar"
        minSdk = appMinSdk
        targetSdk = appCompileSdk
        versionCode = 2
        versionName = "0.1-dev"

        setProperty("archivesBaseName", "calendar-$versionName ($versionCode)")

        buildConfigField("String", "CLIENT_ID", "\"019ED5E7-47D9-7C02-A0C0-F5EF862DB5A1\"")

        androidResources {
            localeFilters += listOf("en", "de", "es", "fr", "it", "da", "el", "fi", "nb", "nl", "pl", "pt", "sv")
            generateLocaleConfig = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("standard") {
            dimension = "distribution"
            isDefault = true
        }
        create("fdroid") {
            dimension = "distribution"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    composeCompiler {
        stabilityConfigurationFiles = listOf(rootProject.layout.projectDirectory.file("stability_config.conf"))
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}
kotlin {
    compilerOptions {
        freeCompilerArgs.add("-XXLanguage:+ExplicitBackingFields")
    }
}

val isRelease = gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }

val envProperties = rootProject.file("env.properties")
    .takeIf { it.exists() }
    ?.let { file -> Properties().also { props -> file.reader().use(props::load) } }

val useCalendarCoreCompositeBuild = gradle.extra["useCalendarCoreCompositeBuild"] as Boolean

val sentryAuthToken = envProperties?.getProperty("sentryAuthToken")
    .takeUnless { it.isNullOrBlank() }
    ?: if (isRelease) error("The `sentryAuthToken` property in `env.properties` must be specified (see `env.example.properties`).") else ""

configurations.configureEach {
    // The Matomo SDK logs network issues to Timber, and the Sentry plugin detects the Timber dependency,
    // and adds its integration, which generates noise.
    // Since we're not using Timber for anything else, it's safe to completely disable it,
    // as specified in Sentry's documentation: https://docs.sentry.io/platforms/android/integrations/timber/#disable
    exclude(group = "io.sentry", module = "sentry-android-timber")
}

sentry {
    autoInstallation.sentryVersion.set(core.versions.sentry)
    org = "sentry"
    projectName = "calendar-android"
    authToken = sentryAuthToken
    url = "https://sentry-mobile.infomaniak.com"
    includeDependenciesReport = false
    includeSourceContext = isRelease

    // Enables or disables the automatic upload of mapping files during a build.
    // If you disable this, you'll need to manually upload the mapping files with sentry-cli when you do a release.
    // Default is enabled.
    autoUploadProguardMapping = isRelease

    // Disables or enables the automatic configuration of Native Symbols for Sentry.
    // This executes sentry-cli automatically so you don't need to do it manually.
    // Default is disabled.
    uploadNativeSymbols = isRelease

    // Does or doesn't include the source code of native code for Sentry.
    // This executes sentry-cli with the --include-sources param. automatically so you don't need to do it manually.
    // Default is disabled.
    includeNativeSources = isRelease
}

dependencies {
    implementation(project(":CalendarComponents:Calendar"))
    implementation(project(":CalendarComponents:Planning"))
    implementation(project(":CalendarComponents:Foundation"))

    if (useCalendarCoreCompositeBuild) {
        implementation(libs.infomaniak.multiplatform.calendar.core.submodule)
    } else {
        implementation(libs.infomaniak.multiplatform.calendar.core)
    }

    implementation(core.infomaniak.core.auth)
    implementation(core.infomaniak.core.avatar)
    implementation(core.infomaniak.core.common)
    implementation(core.infomaniak.core.crossapplogin.front)
    implementation(core.infomaniak.core.matomo)
    implementation(core.infomaniak.core.network)
    implementation(core.infomaniak.core.onboarding)
    implementation(core.infomaniak.core.datavalue)
    implementation(core.infomaniak.core.sentry)
    implementation(core.infomaniak.core.ui.compose.margin)
    implementation(core.infomaniak.core.ui.compose.preview)
    implementation(core.infomaniak.core.ui.compose.theme)
    implementation(libs.infomaniak.designsystem.theme.calendar)

    implementation(core.kotlinx.serialization.json)
    implementation(kmpCalendar.androidx.room.runtime)

    implementation(platform(core.compose.bom))
    implementation(core.activity.compose)
    implementation(core.androidx.core.ktx)
    implementation(core.androidx.lifecycle.runtime.ktx)
    implementation(core.appcompat)
    implementation(core.compose.ui)
    implementation(core.compose.ui.graphics)
    implementation(core.compose.ui.tooling.preview)
    implementation(core.androidx.adaptive)
    implementation(core.material)
    implementation(core.okhttp)
    implementation(libs.compose.material3)
    implementation(libs.metrox.viewmodel.compose)

    testImplementation(core.junit)
    androidTestImplementation(platform(core.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(core.androidx.espresso.core)
    androidTestImplementation(core.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(core.compose.ui.tooling)
    implementation(kmpCalendar.kotlinx.datetime)

    implementation(libs.haze)
    implementation(libs.haze.blur)
    implementation(libs.haze.materials)

    // Navigation 3
    implementation(core.androidx.lifecycle.viewmodel.navigation3)
    implementation(core.androidx.navigation3.runtime)
    implementation(core.androidx.navigation3.ui)
}
