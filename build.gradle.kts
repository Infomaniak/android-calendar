import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply {
        set("appCompileSdk", 37)
        set("appMinSdk", 27)
        set("javaVersion", JavaVersion.VERSION_21)
    }
}

plugins {
    alias(core.plugins.android.application) apply false
    alias(core.plugins.android.library) apply false
    alias(core.plugins.compose.compiler) apply false
    alias(kmpCalendar.plugins.android.kmp.library) apply false
    alias(kmpCalendar.plugins.kotlin.multiplatform) apply false
    alias(kmpCalendar.plugins.kotlin.serialization) apply false
    alias(kmpCalendar.plugins.metro) apply false
    alias(core.plugins.compose.lint)
}

// Every module compiled with Compose shares the app's stability configuration, so that a type declared stable there, is
// stable in the CalendarComponents too.
subprojects {
    plugins.withId("org.jetbrains.kotlin.plugin.compose") {
        configure<ComposeCompilerGradlePluginExtension> {
            stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_config.conf"))
        }
    }
}
