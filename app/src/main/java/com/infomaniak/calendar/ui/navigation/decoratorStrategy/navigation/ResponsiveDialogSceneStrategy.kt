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

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

data class ResponsiveDialogSceneStrategy<T : Any>(val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {
    private val dialogSceneStrategy = DialogSceneStrategy<T>()

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) return null

        return with(dialogSceneStrategy) {
            this@calculateScene.calculateScene(entries.withRoundedLastEntry())
        }
    }

    private fun List<NavEntry<T>>.withRoundedLastEntry(): List<NavEntry<T>> {
        val lastEntry = lastOrNull() ?: return this

        return dropLast(1) + NavEntry(navEntry = lastEntry) {
            Box(modifier = Modifier.clip(AlertDialogDefaults.shape)) { lastEntry.Content() }
        }
    }
}
