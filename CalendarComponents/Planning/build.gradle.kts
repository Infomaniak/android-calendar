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
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.compose.compiler)
    alias(core.plugins.kotlin.android)
    alias(core.plugins.kotlin.parcelize)
}

val appCompileSdk: Int by rootProject.extra
val appMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.calendar.components.planning"
    compileSdk = appCompileSdk

    defaultConfig {
        minSdk = appMinSdk
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    buildFeatures {
        compose = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        }
    }
}

dependencies {
    api(project(":CalendarComponents:Foundation"))
    implementation(project(":CalendarComponents:Event"))
    implementation(project(":CalendarComponents:Resources"))

    implementation(core.infomaniak.core.common)
    implementation(core.infomaniak.core.ui.compose.margin)
    implementation(libs.infomaniak.designsystem.theme.calendar)

    implementation(platform(core.compose.bom))
    implementation(core.compose.foundation)
    implementation(core.compose.ui.android)
    implementation(core.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(core.compose.ui.tooling)

    implementation(libs.paging.compose)

    implementation(kmpCalendar.kotlinx.datetime)
}
