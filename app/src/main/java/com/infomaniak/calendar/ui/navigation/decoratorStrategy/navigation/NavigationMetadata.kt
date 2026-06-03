package com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation

enum class MetadataSceneStrategy(val metadata: Map<String, Any>) {
    FloatingToolbarWithFab(metadata = mapOf(SHOULD_SHOW_FLOATING_TOOLBAR_WITH_FAB to true)),
}

fun metaDataOf(vararg strategies: MetadataSceneStrategy): Map<String, Any> {
    return buildMap { strategies.forEach { putAll(it.metadata) } }
}

const val SHOULD_SHOW_FLOATING_TOOLBAR_WITH_FAB = "ShouldShowNavigationWithFabKey"
