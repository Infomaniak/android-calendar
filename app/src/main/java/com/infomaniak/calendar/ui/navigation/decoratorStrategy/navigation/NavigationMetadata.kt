/*
 * Infomaniak Calendar - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation

enum class MetadataSceneStrategy(override val metadata: Map<String, Any>) : NavigationMetadata {
    FloatingToolbarWithFab(metadata = mapOf(SHOULD_SHOW_FLOATING_TOOLBAR_WITH_FAB to true)),
    Drawer(metadata = mapOf(SHOULD_SHOW_DRAWER to true)),
    ResponsiveDialog(metadata = mapOf(SHOULD_SHOW_RESPONSIVE_DIALOG to true)),
}

fun metaDataOf(vararg items: NavigationMetadata): Map<String, Any> = buildMap {
    items.forEach {
        with(it) { applyMetadata() }
    }
}

interface NavigationMetadata {
    val metadata: Map<String, Any>
    fun MutableMap<String, Any>.applyMetadata() = putAll(metadata)
}

const val SHOULD_SHOW_RESPONSIVE_DIALOG = "ShouldShowResponsiveDialog"
const val SHOULD_SHOW_FLOATING_TOOLBAR_WITH_FAB = "ShouldShowNavigationWithFabKey"
const val SHOULD_SHOW_DRAWER = "ShouldShowDrawer"
