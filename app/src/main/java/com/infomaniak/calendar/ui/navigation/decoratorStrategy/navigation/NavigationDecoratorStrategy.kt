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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import com.infomaniak.calendar.ui.modifier.sharedElement
import com.infomaniak.calendar.ui.navigation.state.LocalSharedSnackbarHostState

private const val SNACKBAR_KEY = "SNACKBAR_HOST"

class NavigationDecoratorStrategy<T : NavKey>(
    private val floatingToolbar: @Composable () -> Unit,
) : SceneDecoratorStrategy<T> {
    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> {
        val shouldShowNavigation = (scene.metadata[SHOULD_SHOW_FLOATING_TOOLBAR_WITH_FAB] as? Boolean) ?: false

        return object : Scene<T> by scene {
            override val content: @Composable () -> Unit = {
                when {
                    shouldShowNavigation -> DisplayToolbar()
                    else -> DisplayContentOnly()
                }
            }

            @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
            @Composable
            private fun DisplayContentOnly() {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = LocalSharedSnackbarHostState.current?.snackbarHostState ?: return@Scaffold,
                            snackbar = { Snackbar(it, modifier = Modifier.sharedElement(SNACKBAR_KEY)) },
                        )
                    },
                ) { _ ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                    ) {
                        scene.content()
                    }
                }
            }

            @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
            @Composable
            private fun DisplayToolbar() {
                Scaffold(
                    bottomBar = { floatingToolbar() },
                    snackbarHost = {
                        SnackbarHost(
                            hostState = LocalSharedSnackbarHostState.current?.snackbarHostState ?: return@Scaffold,
                            snackbar = { Snackbar(it, modifier = Modifier.sharedElement(SNACKBAR_KEY)) },
                        )
                    },
                    contentWindowInsets = DrawerDefaults.windowInsets.only(WindowInsetsSides.Bottom),
                ) { _ ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        scene.content()
                    }
                }
            }
        }
    }
}
