package com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation

enum class MetadataSceneStrategy(val metadata: Map<String, Any>) {
    FloatingToolbar(metadata = mapOf(SHOULD_SHOW_FLOATING_TOOLBAR to true)),
    Fab(metadata = mapOf(SHOULD_DISPLAY_FAB to true)),
}

fun metaDataOf(vararg strategies: MetadataSceneStrategy): Map<String, Any> {
    return buildMap { strategies.forEach { putAll(it.metadata) } }
}

const val SHOULD_SHOW_FLOATING_TOOLBAR = "ShouldShowNavigationKey"
const val SHOULD_DISPLAY_FAB = "ShouldDisplayFabKey"
