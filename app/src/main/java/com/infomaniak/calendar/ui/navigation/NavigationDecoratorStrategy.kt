package com.infomaniak.calendar.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import com.infomaniak.core.ui.compose.navigation.NavigationType

class NavigationDecoratorStrategy<T : NavKey>(
    private val navigationType: NavigationType,
    private val navBarContent: @Composable () -> Unit,
    private val navRailContent: @Composable () -> Unit,
) : SceneDecoratorStrategy<T> {
    private fun shouldDecorate(scene: Scene<T>): Boolean {
        return when (val key = scene.key) {
            is BottomBarNavigation -> true
            is String -> {
                key.contains("Planning") || key.contains("Day") || key.contains("Week") || key.contains("Month")
            }
            else -> false
        }
    }

    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> {
        if (!shouldDecorate(scene)) return scene

        return object : Scene<T> by scene {
            override val content: @Composable () -> Unit = {
                if (navigationType == NavigationType.Rail || navigationType == NavigationType.Drawer) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        navRailContent()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                        ) { scene.content() }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                        ) { scene.content() }
                        navBarContent()
                    }
                }
            }
        }
    }
}
