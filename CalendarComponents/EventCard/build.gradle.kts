import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.kotlin.android)
    alias(core.plugins.compose.compiler)
}

val appCompileSdk: Int by rootProject.extra
val appMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.calendar.components.eventcard"
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

    flavorDimensions += "distribution"
    productFlavors {
        create("standard") {
            isDefault = true
        }
        create("fdroid")
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

    implementation(core.infomaniak.core.avatar)
    implementation(core.infomaniak.core.ui.compose.margin)

    implementation(platform(core.compose.bom))
    implementation(core.compose.foundation)
    implementation(core.compose.ui)
    implementation(core.compose.ui.android)
    implementation(core.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(core.compose.ui.tooling)

    implementation(libs.haze)
    implementation(libs.haze.blur)
    implementation(libs.haze.materials)
}
