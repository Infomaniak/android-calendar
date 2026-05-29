package com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import com.infomaniak.calendar.ui.navigation.LocalGlobalPadding
import com.infomaniak.calendar.ui.navigation.LocalGlobalSnackbar
import com.infomaniak.calendar.ui.navigation.component.sharedNavigation
import com.infomaniak.core.ui.compose.navigation.NavigationType

private const val SNACKBAR_HOST_KEY = "SNACKBAR_HOST"

class NavigationDecoratorStrategy<T : NavKey>(
    private val navigationType: NavigationType,
    private val navBarContent: @Composable () -> Unit,
    private val navRailContent: @Composable (floatingActionButton: (@Composable () -> Unit)?) -> Unit,
    private val floatingActionButton: @Composable () -> Unit,
) : SceneDecoratorStrategy<T> {
    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> {
        val shouldShowNavigation = (scene.metadata[SHOULD_SHOW_NAVIGATION_KEY] as? Boolean) ?: false
        val shouldShowFab = (scene.metadata[SHOULD_DISPLAY_FAB] as? Boolean) ?: false

        return object : Scene<T> by scene {
            override val content: @Composable () -> Unit = {
                when {
                    shouldShowNavigation -> DisplayNavigation(showFab = shouldShowFab)
                    shouldShowFab -> DisplayFabOnly()
                    else -> DisplayContentOnly()
                }
            }

            @Composable
            private fun DisplayContentOnly() {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = LocalGlobalSnackbar.current,
                            snackbar = { Snackbar(it, modifier = Modifier.sharedNavigation(SNACKBAR_HOST_KEY)) }
                        )
                    }
                ) { paddingValues ->
                    CompositionLocalProvider(LocalGlobalPadding provides paddingValues) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            scene.content()
                        }
                    }
                }
            }

            @Composable
            private fun DisplayFabOnly() {
                Scaffold(
                    floatingActionButton = { floatingActionButton() },
                    snackbarHost = {
                        SnackbarHost(
                            hostState = LocalGlobalSnackbar.current,
                            snackbar = { Snackbar(it, modifier = Modifier.sharedNavigation(SNACKBAR_HOST_KEY)) }
                        )
                    }
                ) { paddingValues ->
                    CompositionLocalProvider(LocalGlobalPadding provides paddingValues) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            scene.content()
                        }
                    }
                }
            }

            @Composable
            private fun DisplayNavigation(showFab: Boolean) {
                if (navigationType == NavigationType.Rail || navigationType == NavigationType.Drawer) {
                    RailLayout(showFab)
                } else {
                    BarLayout(showFab)
                }
            }

            @Composable
            private fun RailLayout(showFab: Boolean) {
                Row(modifier = Modifier.fillMaxSize()) {
                    navRailContent(if (showFab) floatingActionButton else null)
                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(
                                hostState = LocalGlobalSnackbar.current,
                                snackbar = { Snackbar(it, modifier = Modifier.sharedNavigation(SNACKBAR_HOST_KEY)) }
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                    ) { paddingValues ->
                        CompositionLocalProvider(LocalGlobalPadding provides paddingValues) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                scene.content()
                            }
                        }
                    }
                }
            }

            @Composable
            private fun BarLayout(showFab: Boolean) {
                Scaffold(
                    bottomBar = { navBarContent() },
                    floatingActionButton = { if (showFab) floatingActionButton() },
                    snackbarHost = {
                        SnackbarHost(
                            hostState = LocalGlobalSnackbar.current,
                            snackbar = { Snackbar(it, modifier = Modifier.sharedNavigation(SNACKBAR_HOST_KEY)) }
                        )
                    },
                    contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                ) { paddingValues ->
                    CompositionLocalProvider(LocalGlobalPadding provides paddingValues) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            scene.content()
                        }
                    }
                }
            }
        }
    }
}
