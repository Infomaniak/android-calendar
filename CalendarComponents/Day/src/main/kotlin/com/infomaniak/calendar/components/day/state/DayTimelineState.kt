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
package com.infomaniak.calendar.components.day.state

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.infomaniak.calendar.components.day.DayTimelineDefaults
import com.infomaniak.calendar.components.day.model.HOURS_PER_DAY
import com.infomaniak.calendar.components.day.model.MINUTES_PER_HOUR
import kotlinx.coroutines.flow.first

/** The hour a day opens on: early enough to have the morning in view, late enough to skip the night. */
private const val INITIAL_VISIBLE_HOUR = 8

/**
 * The timeline opens on [INITIAL_VISIBLE_HOUR], unless [scrollState] was restored to a position the
 * user had already scrolled to.
 *
 * [initialHourHeight] is read once, when the state is built, and is deliberately not a key of the
 * remember: the state owns its height from then on, so a pinch in progress survives a recomposition
 * rather than being reset to the height the caller first asked for.
 */
@Composable
fun rememberDayTimelineState(
    initialHourHeight: Dp = DayTimelineDefaults.HourHeight,
    scrollState: ScrollState = rememberScrollState(),
): DayTimelineState {
    val state = remember(scrollState) { DayTimelineState(initialHourHeight, scrollState) }
    val density = LocalDensity.current

    LaunchedEffect(state) {
        if (scrollState.value != 0) return@LaunchedEffect

        // Scrolling before the timeline has been measured would be clamped to a scroll range of zero.
        snapshotFlow { scrollState.maxValue }.first { it > 0 }
        state.scrollToMinuteOfDay(INITIAL_VISIBLE_HOUR * MINUTES_PER_HOUR, density)
    }

    // A new scroll range is the timeline reporting the height a zoom asked it for, which is the
    // first moment the zoom's own scroll can be honoured in full.
    LaunchedEffect(state, density) {
        snapshotFlow { scrollState.maxValue }.collect { state.reanchorAfterZoom(density) }
    }

    return state
}

/**
 * Vertical geometry of the day view: how tall an hour is, where the timeline is scrolled, and the
 * translation between a minute of the day and a vertical offset.
 *
 * A single instance is shared by every page of the day pager, so swiping to another day keeps the
 * same zoom level and the same scroll position.
 */
@Stable
class DayTimelineState(initialHourHeight: Dp, val scrollState: ScrollState) {

    private var clampedHourHeight by mutableStateOf(initialHourHeight.coerceToAllowedRange())

    var isPinching: Boolean by mutableStateOf(false)
        internal set

    /** The anchor of the zoom that is still waiting for the timeline to be measured at its new height. */
    private var heldAnchor: PinchAnchor? = null

    var hourHeight: Dp
        get() = clampedHourHeight
        set(value) {
            clampedHourHeight = value.coerceToAllowedRange()
        }

    val timelineHeight: Dp get() = hourHeight * HOURS_PER_DAY

    fun verticalOffsetOf(minuteOfDay: Int): Dp = hourHeight * (minuteOfDay / MINUTES_PER_HOUR.toFloat())

    internal fun anchorAt(contentY: Float, density: Density): PinchAnchor = PinchAnchor(
        hourOfDay = contentY / with(density) { hourHeight.toPx() },
        viewportY = contentY - scrollState.value,
    )

    internal fun zoomAround(anchor: PinchAnchor, zoom: Float, density: Density) {
        hourHeight *= zoom
        heldAnchor = anchor

        holdAnchor(anchor, density)
    }

    /**
     * Puts the anchored hour back under the fingers once the timeline has been measured at its new
     * height.
     *
     * Zooming only asks for that measure, so the scroll range is still the previous, shorter one
     * while the pinch is being handled: near the end of the day a scroll towards it is clipped to
     * that range, and what was clipped off would never be asked for again.
     */
    internal fun reanchorAfterZoom(density: Density) {
        val anchor = heldAnchor ?: return
        heldAnchor = null

        holdAnchor(anchor, density)
    }

    private fun holdAnchor(anchor: PinchAnchor, density: Density) {
        val anchorY = anchor.hourOfDay * with(density) { hourHeight.toPx() }

        scrollState.dispatchRawDelta(anchorY - anchor.viewportY - scrollState.value)
    }

    suspend fun scrollToMinuteOfDay(minuteOfDay: Int, density: Density) {
        val target = verticalOffsetOf(minuteOfDay) - hourHeight

        scrollState.scrollTo(with(density) { target.roundToPx() }.coerceAtLeast(0))
    }

    private fun Dp.coerceToAllowedRange(): Dp {
        return coerceIn(DayTimelineDefaults.MinHourHeight, DayTimelineDefaults.MaxHourHeight)
    }
}

@Immutable
internal data class PinchAnchor(val hourOfDay: Float, val viewportY: Float)
