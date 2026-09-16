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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

data class ResponsiveDialogSceneStrategy<T : Any>(val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!windowSizeClass.isAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND, HEIGHT_DP_MEDIUM_LOWER_BOUND)) return null

        val lastEntry = entries.lastOrNull() ?: return null
        if (lastEntry.metadata[SHOULD_SHOW_RESPONSIVE_DIALOG] as? Boolean != true) return null

        val overlaidEntries = entries.dropLast(1)
        if (overlaidEntries.isEmpty()) return null

        return DialogScene(
            key = lastEntry.contentKey,
            entry = lastEntry,
            previousEntries = overlaidEntries,
            overlaidEntries = overlaidEntries,
            onBack = onBack,
        )
    }
}

private class DialogScene<T : Any>(
    override val key: Any,
    private val entry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val onBack: () -> Unit,
) : OverlayScene<T> {
    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable () -> Unit = {
        val lifecycleOwner = rememberLifecycleOwner()

        Dialog(onDismissRequest = onBack) {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                Box(modifier = Modifier.clip(AlertDialogDefaults.shape)) { entry.Content() }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DialogScene<*>

        if (key != other.key) return false
        if (entry != other.entry) return false
        if (previousEntries != other.previousEntries) return false
        if (overlaidEntries != other.overlaidEntries) return false
        if (onBack != other.onBack) return false
        if (entries != other.entries) return false
        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + entry.hashCode()
        result = 31 * result + previousEntries.hashCode()
        result = 31 * result + overlaidEntries.hashCode()
        result = 31 * result + onBack.hashCode()
        result = 31 * result + entries.hashCode()
        result = 31 * result + content.hashCode()
        return result
    }
}
