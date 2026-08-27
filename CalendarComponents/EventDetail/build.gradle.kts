import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.compose.compiler)
    alias(core.plugins.kotlin.android)
}

val appCompileSdk: Int by rootProject.extra
val appMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.calendar.components.eventdetail"
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
    implementation(project(":CalendarComponents:Foundation"))
    implementation(project(":CalendarComponents:Resources"))

    implementation(core.infomaniak.core.filetypes)
    implementation(core.infomaniak.core.ui.compose.basics)
    implementation(core.infomaniak.core.ui.compose.margin)

    implementation(platform(core.compose.bom))
    implementation(core.compose.foundation)
    implementation(core.infomaniak.core.common)
    implementation(core.compose.ui.android)
    implementation(core.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(core.compose.ui.tooling)

    implementation(kmpCalendar.kotlinx.datetime)
}
