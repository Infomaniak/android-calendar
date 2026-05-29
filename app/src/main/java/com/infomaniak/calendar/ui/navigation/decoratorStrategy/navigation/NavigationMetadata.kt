package com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation

enum class MetadataSceneStrategy(val metadata: Map<String, Any>) {
    NavigationBar(metadata = mapOf(SHOULD_SHOW_NAVIGATION_KEY to true)),
    Fab(metadata = mapOf(SHOULD_DISPLAY_FAB to true)),
}

fun metaDataOf(vararg strategies: MetadataSceneStrategy): Map<String, Any> {
    return buildMap { strategies.forEach { putAll(it.metadata) } }
}

const val SHOULD_SHOW_NAVIGATION_KEY = "ShouldShowNavigationKey"
const val SHOULD_DISPLAY_FAB = "ShouldDisplayFabKey"
