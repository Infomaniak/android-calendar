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
package com.infomaniak.calendar.components.eventdetail.modifier

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Helper based on [ProvideEventSharedTransition]. Mimics the standard compose sharedBounds modifier.
 */
@Composable
internal fun Modifier.eventSharedBounds(element: EventSharedElement): Modifier {
    val transition = LocalEventSharedTransition.current ?: return this

    return with(transition.sharedTransitionScope) {
        sharedBounds(
            sharedContentState = rememberSharedContentState(element),
            animatedVisibilityScope = transition.animatedVisibilityScope,
        )
    }
}

/**
 * Helper based on [ProvideEventSharedTransition]. Mimics the standard compose sharedElement modifier.
 */
@Composable
internal fun Modifier.eventSharedElement(element: EventSharedElement): Modifier {
    val transition = LocalEventSharedTransition.current ?: return this

    return with(transition.sharedTransitionScope) {
        sharedElement(
            sharedContentState = rememberSharedContentState(element),
            animatedVisibilityScope = transition.animatedVisibilityScope,
        )
    }
}

/**
 * Enables [eventSharedBounds] and [eventSharedElement]. Nothing animates unless both scopes are provided, which is the case for
 * previews and for screens that don't take part in a transition.
 */
@Composable
internal fun ProvideEventSharedTransition(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    content: @Composable () -> Unit,
) {
    val transition = if (sharedTransitionScope == null || animatedVisibilityScope == null) {
        null
    } else {
        remember(sharedTransitionScope, animatedVisibilityScope) {
            EventSharedTransition(sharedTransitionScope, animatedVisibilityScope)
        }
    }

    CompositionLocalProvider(LocalEventSharedTransition provides transition, content = content)
}

internal enum class EventSharedElement {
    Title,
}

private data class EventSharedTransition(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope,
)

private val LocalEventSharedTransition = staticCompositionLocalOf<EventSharedTransition?> { null }
