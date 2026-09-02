import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.compose.compiler)
    alias(core.plugins.kotlin.parcelize)
}

val appCompileSdk: Int by rootProject.extra
val appMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.calendar.components.foundation"
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
    implementation(core.infomaniak.core.common)
    implementation(core.infomaniak.core.ui.compose.theme)

    implementation(kmpCalendar.kotlinx.datetime)

    implementation(platform(core.compose.bom))
    implementation(core.compose.foundation)
    implementation(core.compose.ui.android)
    implementation(core.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(core.compose.ui.tooling)

    testImplementation(core.junit)
    testImplementation(core.robolectric)
}
