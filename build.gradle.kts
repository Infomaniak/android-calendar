// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply {
        set("appCompileSdk", 36)
        set("appMinSdk", 27)
        set("javaVersion", JavaVersion.VERSION_21)
    }
}

plugins {
    alias(core.plugins.android.application) apply false
    alias(core.plugins.compose.compiler) apply false
    alias(core.plugins.kotlin.android) apply false
    alias(kmpCalendar.plugins.android.library) apply false
    alias(kmpCalendar.plugins.kotlin.multiplatform) apply false
    alias(kmpCalendar.plugins.kotlin.serialization) apply false
    alias(kmpCalendar.plugins.metro) apply false
    alias(core.plugins.compose.lint)
}
