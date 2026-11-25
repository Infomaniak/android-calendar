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
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.math.sqrt

private const val HIGHLIGHT_DAMPING_RATIO = 0.8f
private const val BOUNCE_DAMPING_RATIO = 0.45f

/** How much of the finger's way the unfolded card follows before it comes loose. */
private const val HELD_RESISTANCE = 0.35f

/**
 * @param onEventDropped Called when a held event is dragged and dropped on an [eventDropTarget]. Without it,
 * held events can't be dragged.
 */
@Composable
fun rememberEventHighlightState(onEventDropped: ((EventSummaryUi.Normal, EventDrop) -> Unit)? = null): EventHighlightState {
    val scope = rememberCoroutineScope()
    val currentOnEventDropped = rememberUpdatedState(onEventDropped)
    return remember(scope) { EventHighlightState(scope, currentOnEventDropped) }
}

/**
 * The [EventHighlight] a long press holds up, unfolded as [progress] goes from 0 to 1 and folded back once released.
 *
 * Moving the finger first pulls the unfolded card along with some resistance, springing after it. Pulled far
 * enough, it comes loose: it folds back to its own size and follows the finger, shifted by [dragOffset], until
 * it is dropped on the [eventDropTarget] under the finger.
 */
@Stable
class EventHighlightState internal constructor(
    private val scope: CoroutineScope,
    private val onEventDropped: State<((EventSummaryUi.Normal, EventDrop) -> Unit)?>,
) {

    private val highlightState = mutableStateOf<EventHighlight?>(null)
    internal val highlight: EventHighlight? by highlightState

    private val isHeldState = mutableStateOf(false)
    /** Whether the finger still holds the event up: the timeline stays put until it lifts. */
    val isHeld: Boolean by isHeldState

    private val isDraggingState = mutableStateOf(false)
    /** Whether the held card came loose and follows the finger. */
    val isDragging: Boolean by isDraggingState

    internal val canDrag: Boolean get() = onEventDropped.value != null

    /** Set while laid out, and only read once a long press needs to know where its card is. */
    internal var overlayCoordinates: LayoutCoordinates? = null

    /** Where an event can be dropped, each knowing its date. */
    internal val dropTargets = mutableSetOf<EventDropTargetNode>()

    /** The day the held event was picked up on. */
    private var fromDate: LocalDate? = null

    private val progressAnimatable = Animatable(0f)
    internal val progress: Float get() = progressAnimatable.value

    private val dragOffsetAnimatable = Animatable(Offset.Zero, Offset.VectorConverter)
    internal val dragOffset: Offset get() = dragOffsetAnimatable.value

    private var fingerTarget = Offset.Zero
    // Snaps the offset every frame, so it is cancelled by hand before any other animation of the offset.
    private var fingerFollowing: Job? = null

    // Each animation cancels the running one, so a cancelled fold-back never clears the highlight.
    internal fun show(highlight: EventHighlight, finger: Offset) {
        val isAnotherEvent = this.highlight?.key != highlight.key
        highlightState.value = highlight
        isHeldState.value = true
        isDraggingState.value = false
        fromDate = dropTargetAt(finger)?.date

        fingerFollowing?.cancel()
        fingerTarget = Offset.Zero
        scope.launch {
            // The same event, pressed again while it folds back, unfolds from where it is.
            if (isAnotherEvent) progressAnimatable.snapTo(0f)
            progressAnimatable.animateTo(1f, highlightSpring)
        }
        scope.launch { dragOffsetAnimatable.snapTo(Offset.Zero) }
    }

    /** The finger moved by [delta] from where it pressed, while the card is still unfolded. */
    internal fun pull(delta: Offset) {
        if (isHeld && !isDragging) followFinger(delta * HELD_RESISTANCE)
    }

    /** The card comes loose: it folds back to its own size and catches up with the finger. */
    internal fun startDrag(delta: Offset) {
        if (!isHeld || isDragging) return
        isDraggingState.value = true

        scope.launch { progressAnimatable.animateTo(0f, highlightSpring) }
        followFinger(delta)
    }

    /** The finger moved by [delta] from where it pressed, while the card follows it. */
    internal fun dragTo(delta: Offset) {
        if (isDragging) followFinger(delta)
    }

    /**
     * Lets go of the card under [finger], [delta] away from where it was pressed, and reports where it lands.
     * The card then goes back to its place.
     */
    internal fun drop(finger: Offset, delta: Offset) {
        val highlight = highlight?.takeIf { isHeld && isDragging } ?: return
        val overlay = overlayCoordinates?.takeIf { it.isAttached }
        val target = dropTargetAt(finger)
        if (overlay != null && target != null) {
            val cardBounds = highlight.startBounds.translate(delta)
            val drop = EventDrop(
                fromDate = fromDate ?: target.date,
                date = target.date,
                time = target.timeAt(overlay, cardBounds.top, cardBounds.height),
            )
            onEventDropped.value?.invoke(highlight.event, drop)
        }
        hide()
    }

    internal fun hide() {
        if (!isHeld) return
        isHeldState.value = false

        fingerFollowing?.cancel()
        scope.launch {
            val goingBack = launch { dragOffsetAnimatable.animateTo(Offset.Zero, highlightOffsetSpring) }
            progressAnimatable.animateTo(0f, highlightSpring)
            goingBack.join()
            isDraggingState.value = false
            highlightState.value = null
        }
    }

    /** Whether the card [key] identifies is the one held up, which the overlay draws in its place. */
    fun isHighlighting(key: Any): Boolean = highlight?.key == key

    /** The drop target under [finger], or the nearest one: the finger can stray off them, like over a timeline's gutter. */
    private fun dropTargetAt(finger: Offset): EventDropTargetNode? {
        val overlay = overlayCoordinates?.takeIf { it.isAttached } ?: return null
        return dropTargets
            .mapNotNull { target -> target.distanceTo(overlay, finger)?.let { target to it } }
            .minByOrNull { (_, distance) -> distance }
            ?.first
    }

    /**
     * Springs [dragOffset] toward [target]. The finger moves every frame, faster than an animation restarted on
     * each move could ever get going, so a single spring keeps chasing the latest target instead.
     */
    private fun followFinger(target: Offset) {
        fingerTarget = target
        if (fingerFollowing?.isActive == true) return
        fingerFollowing = scope.launch {
            var velocity = Offset.Zero
            var lastFrame = withFrameNanos { it }
            while (true) {
                val frame = withFrameNanos { it }
                var remaining = ((frame - lastFrame) / NANOS_PER_SECOND).coerceAtMost(MAX_FRAME_SECONDS)
                lastFrame = frame
                var offset = dragOffsetAnimatable.value
                while (remaining > 0f) {
                    val step = remaining.coerceAtMost(SPRING_STEP_SECONDS)
                    val acceleration = (fingerTarget - offset) * BOUNCE_STIFFNESS - velocity * bounceDamping
                    velocity += acceleration * step
                    offset += velocity * step
                    remaining -= step
                }
                val isSettled = (fingerTarget - offset).getDistance() < SETTLED_DISTANCE &&
                    velocity.getDistance() < SETTLED_DISTANCE
                dragOffsetAnimatable.snapTo(if (isSettled) fingerTarget else offset)
                if (isSettled) break
            }
        }
    }

    private companion object {
        const val NANOS_PER_SECOND = 1_000_000_000f
        const val MAX_FRAME_SECONDS = 1f / 30f
        const val SPRING_STEP_SECONDS = 1f / 240f
        const val SETTLED_DISTANCE = 0.5f
        const val BOUNCE_STIFFNESS = Spring.StiffnessMedium
        val bounceDamping = 2f * BOUNCE_DAMPING_RATIO * sqrt(BOUNCE_STIFFNESS)
        val highlightSpring = spring<Float>(dampingRatio = HIGHLIGHT_DAMPING_RATIO, stiffness = Spring.StiffnessMediumLow)
        val highlightOffsetSpring = spring<Offset>(dampingRatio = HIGHLIGHT_DAMPING_RATIO, stiffness = Spring.StiffnessMediumLow)
    }
}
