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
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi

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
 * [eventSharedBounds] for a reminder, matched through its stored alarm. Reminders added in the form have none, and aren't
 * shared with the detail anyway.
 */
@Composable
internal fun Modifier.reminderSharedBounds(notification: EventDetailUi.Notification): Modifier {
    val sourceId = notification.sourceId ?: return this
    return eventSharedBounds(EventSharedElement.Reminder(sourceId))
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
 * Enables [eventSharedBounds] and [eventSharedElement]. Nothing animates unless both scopes are provided. Previews don't provide
 * such scopes.
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

internal sealed interface EventSharedElement {
    data object Title : EventSharedElement
    data object DateAndTime : EventSharedElement
    data object Attendees : EventSharedElement
    data object KMeet : EventSharedElement
    data object Room : EventSharedElement
    data object Place : EventSharedElement
    data object OccupiedStatus : EventSharedElement
    data object ClassificationStatus : EventSharedElement
    data object Calendar : EventSharedElement
    data object Divider1 : EventSharedElement
    data object Divider2 : EventSharedElement
    data object Divider3 : EventSharedElement
    data object Divider4 : EventSharedElement

    data class Reminder(val id: String) : EventSharedElement
}

private data class EventSharedTransition(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope,
)

private val LocalEventSharedTransition = staticCompositionLocalOf<EventSharedTransition?> { null }
