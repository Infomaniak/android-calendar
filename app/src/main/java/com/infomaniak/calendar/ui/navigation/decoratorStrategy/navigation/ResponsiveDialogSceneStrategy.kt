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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.infomaniak.calendar.ui.modifier.LocalSharedTransitionScope
import com.infomaniak.calendar.utils.NavigationTransition

/**
 * Displays every trailing entry flagged with [SHOULD_SHOW_RESPONSIVE_DIALOG] inside a single [Dialog], as long as the window is
 * big enough for it.
 */
class ResponsiveDialogSceneStrategy<T : Any>(private val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {

    private var ongoingScene: ResponsiveDialogScene<T>? = null

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!windowSizeClass.isAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND, HEIGHT_DP_MEDIUM_LOWER_BOUND)) return null

        val dialogEntries = entries.takeLastWhile { it.metadata[SHOULD_SHOW_RESPONSIVE_DIALOG] as? Boolean == true }
        if (dialogEntries.isEmpty()) return null

        val overlaidEntries = entries.dropLast(dialogEntries.size)
        if (overlaidEntries.isEmpty()) return null

        val firstDialogEntryKey = dialogEntries.first().contentKey
        val scene = ongoingScene?.takeIf { it.key == firstDialogEntryKey } ?: ResponsiveDialogScene(firstDialogEntryKey, onBack)

        return scene.also {
            it.update(dialogEntries, overlaidEntries)
            ongoingScene = it
        }
    }
}

/**
 * An [OverlayScene] rendering its entries inside a single [Dialog], animating from one to the next.
 *
 * A [Dialog] lives in its own window, so it has a layout hierarchy of its own. Shared elements can only be animated within one
 * hierarchy, which is why this scene hosts its own [SharedTransitionLayout] and [AnimatedContent] rather than relying on the
 * ones of the [androidx.navigation3.ui.NavDisplay], which belong to the main window.
 *
 * For that to work the window must outlive a navigation between two dialog entries, and NavDisplay renders overlay scenes
 * inside a [androidx.compose.runtime.key] of the scene itself. So this scene is a single mutable instance reused across
 * calculations, rather than a new immutable one per back stack change, which would recreate the window and lose the animation.
 */
private class ResponsiveDialogScene<T : Any>(override val key: Any, private val onBack: () -> Unit) : OverlayScene<T> {

    override var entries: List<NavEntry<T>> by mutableStateOf(emptyList())
        private set

    override var overlaidEntries: List<NavEntry<T>> by mutableStateOf(emptyList())
        private set

    // Every dialog entry is rendered by this scene, so whatever we go back to, we land on the entries below the dialog.
    override val previousEntries: List<NavEntry<T>> get() = overlaidEntries

    fun update(entries: List<NavEntry<T>>, overlaidEntries: List<NavEntry<T>>) {
        this.entries = entries
        this.overlaidEntries = overlaidEntries
    }

    override val content: @Composable () -> Unit = {
        val lifecycleOwner = rememberLifecycleOwner()

        Dialog(onDismissRequest = onBack) {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                SharedTransitionLayout {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this@SharedTransitionLayout) {
                        AnimatedContent(
                            targetState = entries.last(),
                            modifier = Modifier.clip(AlertDialogDefaults.shape),
                            transitionSpec = { NavigationTransition.contentTransform },
                            contentKey = { it.contentKey },
                        ) { animatedEntry ->
                            // AnimatedContent keeps the instance it was given when the content key showed up, but entries are
                            // recreated on every back stack change. Popped entries are gone from the list while they animate
                            // out, in which case the one we've been given is the only one left.
                            val entry = entries.firstOrNull { it.contentKey == animatedEntry.contentKey } ?: animatedEntry

                            CompositionLocalProvider(LocalNavAnimatedContentScope provides this@AnimatedContent) {
                                entry.Content()
                            }
                        }
                    }
                }
            }
        }
    }
}
