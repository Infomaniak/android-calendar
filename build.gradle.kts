// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(kmpCalendar.plugins.android.kmp.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(kmpCalendar.plugins.kotlin.multiplatform) apply false
    alias(kmpCalendar.plugins.kotlin.serialization) apply false
}
