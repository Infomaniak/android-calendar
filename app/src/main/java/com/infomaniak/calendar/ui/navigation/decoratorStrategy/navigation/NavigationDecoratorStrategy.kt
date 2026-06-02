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
import com.infomaniak.calendar.ui.navigation.component.sharedNavigation
import com.infomaniak.calendar.ui.navigation.scroll.LocalSnackbarHostState

private const val SNACKBAR_KEY = "SNACKBAR_HOST"

class NavigationDecoratorStrategy<T : NavKey>(
    private val floatingToolbar: @Composable (floatingActionButton: (@Composable () -> Unit)?) -> Unit,
    private val floatingActionButton: @Composable () -> Unit,
) : SceneDecoratorStrategy<T> {
    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> {
        val shouldShowNavigation = (scene.metadata[SHOULD_SHOW_FLOATING_TOOLBAR] as? Boolean) ?: false
        val shouldShowFab = (scene.metadata[SHOULD_DISPLAY_FAB] as? Boolean) ?: false

        return object : Scene<T> by scene {
            override val content: @Composable () -> Unit = {
                when {
                    shouldShowNavigation -> DisplayNavigation(showFab = shouldShowFab)
                    shouldShowFab -> DisplayFabOnly()
                    else -> DisplayContentOnly()
                }
            }

            @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
            @Composable
            private fun DisplayContentOnly() {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = LocalSnackbarHostState.current?.snackbarHostState ?: return@Scaffold,
                            snackbar = { Snackbar(it, modifier = Modifier.sharedNavigation(SNACKBAR_KEY)) },
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
            private fun DisplayFabOnly() {
                Scaffold(
                    floatingActionButton = { floatingActionButton() },
                    snackbarHost = {
                        SnackbarHost(
                            hostState = LocalSnackbarHostState.current?.snackbarHostState ?: return@Scaffold,
                            snackbar = { Snackbar(it, modifier = Modifier.sharedNavigation(SNACKBAR_KEY)) },
                        )
                    },
                ) { _ ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        scene.content()
                    }
                }
            }

            @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
            @Composable
            private fun DisplayNavigation(showFab: Boolean) {

                Scaffold(
                    bottomBar = {
                        floatingToolbar(if (showFab) @Composable { -> floatingActionButton() } else null)
                    },
                    snackbarHost = {
                        SnackbarHost(
                            hostState = LocalSnackbarHostState.current?.snackbarHostState ?: return@Scaffold,
                            snackbar = { Snackbar(it, modifier = Modifier.sharedNavigation(SNACKBAR_KEY)) },
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
