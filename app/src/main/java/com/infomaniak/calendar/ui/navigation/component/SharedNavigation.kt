package com.infomaniak.calendar.ui.navigation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.infomaniak.calendar.ui.navigation.LocalSharedTransitionScope

@Composable
fun Modifier.sharedNavigation(key: String): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this

    return with(sharedScope) {
        val animatedContentScope = LocalNavAnimatedContentScope.current

        this@sharedNavigation.then(
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key),
                animatedVisibilityScope = animatedContentScope,
            ),
        )
    }
}
