package com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation

import androidx.compose.animation.AnimatedContentScope
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
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.infomaniak.core.ui.compose.navigation.NavigationType

class NavigationDecoratorStrategy<T : NavKey>(
    private val navigationType: NavigationType,
    private val navBarContent: @Composable (AnimatedContentScope) -> Unit,
    private val navRailContent: @Composable (AnimatedContentScope) -> Unit,
) : SceneDecoratorStrategy<T> {

    private fun shouldDecorate(scene: Scene<T>): Boolean {
        return (scene.metadata[NavigationMetadata.SHOULD_SHOW_NAVIGATION_KEY] as? Boolean) ?: false
    }

    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> {
        if (!shouldDecorate(scene)) return scene

        return object : Scene<T> by scene {
            override val content: @Composable () -> Unit = {
                val animatedContentScope = LocalNavAnimatedContentScope.current

                if (navigationType == NavigationType.Rail || navigationType == NavigationType.Drawer) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        navRailContent(animatedContentScope)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                        ) {
                            scene.content()
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                        ) {
                            scene.content()
                        }
                        navBarContent(animatedContentScope)
                    }
                }
            }
        }
    }
}
