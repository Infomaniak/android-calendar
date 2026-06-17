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
    namespace = "com.infomaniak.calendar.components.event"
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
    implementation(kmpCalendar.kotlinx.datetime)

    implementation(platform(core.compose.bom))
    implementation(core.compose.foundation)
    implementation(core.compose.ui.android)
    implementation(libs.compose.material3)
}
