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
package com.infomaniak.calendar

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import kotlinx.serialization.Serializable

public class CustomDialogSceneStrategy<T : Any>(val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {

    val s = DialogSceneStrategy<T>()

    public override fun SceneStrategyScope<T>.calculateScene(
        entries: List<NavEntry<T>>,
    ): Scene<T>? {
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            return null
        }

        return with(s) {
            this@calculateScene.calculateScene(entries)
        }
    }
}

@Serializable
private data object RouteA : NavKey

@Serializable
private data class RouteB(val id: String) : NavKey

fun ComponentActivity.setEdgeToEdgeConfig() {
    enableEdgeToEdge()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Force the 3-button navigation bar to be transparent
        // See: https://developer.android.com/develop/ui/views/layout/edge-to-edge#create-transparent
        window.isNavigationBarContrastEnforced = false
    }
}

class DialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = rememberNavBackStack(RouteA)
            val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
            val dialogStrategy = remember { CustomDialogSceneStrategy<NavKey>(windowSizeClass) }

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategies = listOf(dialogStrategy),
                entryProvider = entryProvider {
                    entry<RouteA> {
                        ContentGreen("Welcome to Nav3") {
                            Button(
                                onClick = dropUnlessResumed {
                                    backStack.add(RouteB("123"))
                                },
                            ) {
                                Text("Click to open dialog")
                            }
                        }
                    }
                    entry<RouteB>(
                        metadata = DialogSceneStrategy.dialog(
                            DialogProperties(windowTitle = "Route B dialog"),
                        ),
                    ) { key ->
                        ContentBlue(
                            title = "Route id: ${key.id}",
                            modifier = Modifier.clip(
                                shape = RoundedCornerShape(16.dp),
                            ),
                        )
                    }
                },
            )
        }
    }
}
