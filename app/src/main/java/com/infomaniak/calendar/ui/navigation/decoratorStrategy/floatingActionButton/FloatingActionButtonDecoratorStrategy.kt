package com.infomaniak.calendar.ui.navigation.decoratorStrategy.floatingActionButton

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope

class FloatingActionButtonDecoratorStrategy<T : NavKey>(
    private val floatingActionButton: @Composable () -> Unit,
) : SceneDecoratorStrategy<T> {

    private fun shouldDecorate(scene: Scene<T>): Boolean {
        return true
    }

    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> {
        if (!shouldDecorate(scene)) return scene

        return object : Scene<T> by scene {
            override val content: @Composable () -> Unit = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                    floatingActionButton()
                }
            }
        }
    }
}
