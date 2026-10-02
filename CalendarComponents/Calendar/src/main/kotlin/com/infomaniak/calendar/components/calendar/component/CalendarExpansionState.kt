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
package com.infomaniak.calendar.components.calendar.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

internal const val COLLAPSED = 0f
internal const val EXPANDED = 1f
private const val SETTLE_THRESHOLD = 0.5f

/** Past this speed a release reads as a flick, and lands in the direction it was thrown at whatever height. */
private val FLICK_VELOCITY = 200.dp

private val SettleSpec = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)

/**
 * Drives the calendar between the collapsed week and the expanded month.
 *
 * The expansion is a [progress] fraction rather than a boolean so the same state can be animated by a tap on
 * the title, or dragged frame by frame by a scroll coming from the content below.
 *
 * [progress] is a plain snapshot float, not an `Animatable`: a drag writes it straight from the scroll
 * callback it arrives in, where suspending is not an option, or the calendar would trail the finger.
 * Everything that follows the expansion reads it from a layout or draw lambda, so a drag re-measures and
 * redraws the calendar without ever recomposing it.
 */
@Stable
class CalendarExpansionState(private val _isExpanded: MutableState<Boolean>, private val coroutineScope: CoroutineScope) {

    private val _progress = mutableFloatStateOf(if (_isExpanded.value) EXPANDED else COLLAPSED)

    var progress: Float
        get() = _progress.floatValue
        private set(value) {
            _progress.floatValue = value.coerceIn(COLLAPSED, EXPANDED)
        }

    val isExpanded: Boolean by _isExpanded

    private var dragRange = 0f
    private var flickVelocity: Float? = null

    private var settling: Job? = null

    /** Animates to the state the calendar is not in, for callers driving it by something other than a drag. */
    fun toggle() = animateTo(expanded = !isExpanded, initialVelocity = 0f)

    internal fun updateMetrics(dragRange: Float, density: Density) {
        this.dragRange = dragRange
        flickVelocity = with(density) { FLICK_VELOCITY.toPx() }
    }

    private fun drag(delta: Float): Float {
        if (dragRange <= 0f) return 0f

        settling?.cancel()

        val from = progress
        val to = (from + delta / dragRange).coerceIn(COLLAPSED, EXPANDED)
        progress = to

        // A drag that goes all the way settles itself, and there is no release left to record where it landed.
        if (to == COLLAPSED || to == EXPANDED) _isExpanded.value = to == EXPANDED

        return (to - from) * dragRange
    }

    private fun settle(velocity: Float) {
        val isFlick = flickVelocity?.let { abs(velocity) >= it } == true
        val expanded = if (isFlick) velocity > 0f else progress > SETTLE_THRESHOLD

        animateTo(expanded, initialVelocity = velocity)
    }

    private fun animateTo(expanded: Boolean, initialVelocity: Float) {
        _isExpanded.value = expanded
        settling?.cancel()
        settling = coroutineScope.launch {
            animate(
                initialValue = progress,
                targetValue = if (expanded) EXPANDED else COLLAPSED,
                initialVelocity = if (dragRange > 0f) initialVelocity / dragRange else 0f,
                animationSpec = SettleSpec,
            ) { value, _ -> progress = value }
        }
    }

    internal val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (source != NestedScrollSource.UserInput || available.y >= 0f) return Offset.Zero

            return Offset(x = 0f, y = drag(available.y))
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (source != NestedScrollSource.UserInput || available.y <= 0f) return Offset.Zero

            return Offset(x = 0f, y = drag(available.y))
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if (progress != COLLAPSED && progress != EXPANDED) settle(available.y)

            return Velocity.Zero
        }
    }
}

fun Modifier.collapseCalendarOnScroll(expansionState: CalendarExpansionState): Modifier {
    return nestedScroll(expansionState.nestedScrollConnection)
}

@Composable
fun rememberCalendarExpansionState(initiallyExpanded: Boolean = false): CalendarExpansionState {
    val isExpanded = rememberSaveable { mutableStateOf(initiallyExpanded) }
    val coroutineScope = rememberCoroutineScope()

    return remember { CalendarExpansionState(isExpanded, coroutineScope) }
}
