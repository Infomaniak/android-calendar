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
package com.infomaniak.calendar.components.event.highlight

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.LayoutCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val HIGHLIGHT_DAMPING_RATIO = 0.8f

@Composable
fun rememberEventHighlightState(): EventHighlightState {
    val scope = rememberCoroutineScope()
    return remember(scope) { EventHighlightState(scope) }
}

/** The [EventHighlight] a long press holds up, unfolded as [progress] goes from 0 to 1 and folded back once released. */
@Stable
class EventHighlightState internal constructor(private val scope: CoroutineScope) {

    private val highlightState = mutableStateOf<EventHighlight?>(null)
    internal val highlight: EventHighlight? by highlightState

    private val isHeldState = mutableStateOf(false)
    /** Whether the finger still holds the event up: the timeline stays put until it lifts. */
    val isHeld: Boolean by isHeldState

    /** Set while laid out, and only read once a long press needs to know where its card is. */
    internal var overlayCoordinates: LayoutCoordinates? = null

    private val progressAnimatable = Animatable(0f)
    internal val progress: Float get() = progressAnimatable.value

    // Each animation cancels the running one, so a cancelled fold-back never clears the highlight.
    internal fun show(highlight: EventHighlight) {
        val isAnotherEvent = this.highlight?.key != highlight.key
        highlightState.value = highlight
        isHeldState.value = true

        scope.launch {
            // The same event, pressed again while it folds back, unfolds from where it is.
            if (isAnotherEvent) progressAnimatable.snapTo(0f)
            progressAnimatable.animateTo(1f, highlightSpring)
        }
    }

    internal fun hide() {
        if (!isHeld) return
        isHeldState.value = false

        scope.launch {
            progressAnimatable.animateTo(0f, highlightSpring)
            highlightState.value = null
        }
    }

    /** Whether the card [key] identifies is the one held up, which the overlay draws in its place. */
    fun isHighlighting(key: Any): Boolean = highlight?.key == key

    private companion object {
        val highlightSpring = spring<Float>(dampingRatio = HIGHLIGHT_DAMPING_RATIO, stiffness = Spring.StiffnessMediumLow)
    }
}
