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
package com.infomaniak.calendar.components.day.highlight

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import com.infomaniak.calendar.components.day.model.TimedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val HIGHLIGHT_DAMPING_RATIO = 0.8f

@Composable
internal fun rememberEventHighlightState(): EventHighlightState {
    val scope = rememberCoroutineScope()
    return remember(scope) { EventHighlightState(scope) }
}

/**
 * The event a long press holds up, unfolded over the days so it can be read in full.
 *
 * The card grows from where it sat in its column, [startBounds], to the whole width of [areaBounds],
 * as [progress] goes from 0 to 1, and shrinks back once the finger lifts. Both rectangles, like
 * [fingerY], are expressed in the coordinates of the overlay drawing it, [overlayCoordinates].
 */
@Stable
internal class EventHighlightState(private val scope: CoroutineScope) {

    var highlighted: TimedEvent? by mutableStateOf(null)
        private set

    /** Whether the finger still holds the event up: the timeline stays put until it lifts. */
    var isHeld: Boolean by mutableStateOf(false)
        private set

    var startBounds: Rect by mutableStateOf(Rect.Zero)
        private set

    var areaBounds: Rect by mutableStateOf(Rect.Zero)
        private set

    /** Where the finger pressed, so the unfolded card can open above it rather than under it. */
    var fingerY: Float by mutableFloatStateOf(0f)
        private set

    /** Set while laid out, and only read once a long press needs to know where its card is. */
    var overlayCoordinates: LayoutCoordinates? = null

    private val progressAnimatable = Animatable(0f)
    val progress: Float get() = progressAnimatable.value

    private var animation: Job? = null

    fun show(timedEvent: TimedEvent, startBounds: Rect, fingerY: Float, areaBounds: Rect) {
        val isAnotherEvent = highlighted != timedEvent
        highlighted = timedEvent
        this.startBounds = startBounds
        this.areaBounds = areaBounds
        this.fingerY = fingerY
        isHeld = true

        animation?.cancel()
        animation = scope.launch {
            // The same event, pressed again while it folds back, unfolds from where it is.
            if (isAnotherEvent) progressAnimatable.snapTo(0f)
            progressAnimatable.animateTo(1f, highlightSpring)
        }
    }

    fun hide() {
        if (!isHeld) return
        isHeld = false

        animation?.cancel()
        animation = scope.launch {
            progressAnimatable.animateTo(0f, highlightSpring)
            highlighted = null
        }
    }

    private companion object {
        val highlightSpring = spring<Float>(dampingRatio = HIGHLIGHT_DAMPING_RATIO, stiffness = Spring.StiffnessMediumLow)
    }
}
