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
plugins {
    alias(core.plugins.android.application)
    alias(core.plugins.compose.compiler)
    alias(core.plugins.kotlin.android)
    alias(core.plugins.kotlin.serialization)
    alias(libs.plugins.metro)
}

android {
    compileSdk { version = release(36) }
    namespace = "com.infomaniak.calendar"

    defaultConfig {
        applicationId = "com.infomaniak.calendar"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // TODO[login]: Replace this placeholder with the real Calendar OAuth client id once available.
        buildConfigField("String", "CLIENT_ID", "\"17EE3471-9843-4FB9-AD95-CB8C41BAD624\"")
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
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.infomaniak.multiplatform.calendar.core)

    implementation(core.infomaniak.core.auth)
    implementation(core.infomaniak.core.common)
    implementation(core.infomaniak.core.crossapplogin.front)
    implementation(core.infomaniak.core.network)
    implementation(core.infomaniak.core.onboarding)
    implementation(core.infomaniak.core.ui.compose.margin)

    implementation(platform(core.compose.bom))
    implementation(core.activity.compose)
    implementation(core.androidx.core.ktx)
    implementation(core.androidx.lifecycle.runtime.ktx)
    implementation(core.appcompat)
    implementation(core.compose.material3)
    implementation(core.compose.ui)
    implementation(core.compose.ui.graphics)
    implementation(core.compose.ui.tooling.preview)
    implementation(core.material)

    testImplementation(core.junit)
    androidTestImplementation(platform(core.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(core.androidx.espresso.core)
    androidTestImplementation(core.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(core.compose.ui.tooling)

    // Navigation 3
    implementation(core.androidx.lifecycle.viewmodel.navigation3)
    implementation(core.androidx.navigation3.runtime)
    implementation(core.androidx.navigation3.ui)
}
