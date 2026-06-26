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

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope

class DrawerDecoratorStrategy<T : NavKey>(
    private val drawer: @Composable (content: @Composable () -> Unit) -> Unit,
) : SceneDecoratorStrategy<T> {
    fun shouldDecorate(scene: Scene<T>): Boolean {
        return scene.metadata[SHOULD_SHOW_DRAWER] as? Boolean == true
    }

    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> {
        if (!shouldDecorate(scene)) return scene

        return object : Scene<T> by scene {
            @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
            override val content: @Composable () -> Unit = {
                drawer {
                    Scaffold { _ ->
                        scene.content()
                    }
                }
            }
        }
    }
}
