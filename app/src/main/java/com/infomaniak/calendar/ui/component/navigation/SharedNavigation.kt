package com.infomaniak.calendar.ui.component.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Modifier.sharedNavigation(
    key: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope?,
): Modifier {
    if (animatedContentScope == null) return this

    return with(sharedTransitionScope) {
        this@sharedNavigation.then(
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key),
                animatedVisibilityScope = animatedContentScope,
            )
        )
    }
}
