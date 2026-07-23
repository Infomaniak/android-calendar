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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Keeps the days-of-week header horizontally in sync with the currently displayed calendar pager.
 *
 * Each pager owns its own slot ([setExpandedOffsetSource] / [setCollapsedOffsetSource]) so they never
 * clobber each other during a transition, and the header reads the slot matching the current expansion
 * state via [offset]. The read is lazy (pull-based) so it can be sampled during the draw phase (e.g. inside
 * `graphicsLayer`) and stay in sync with the scrolling day columns, without the one-frame lag a pushed value
 * would introduce.
 */
@Stable
internal class CalendarHeaderState {
    private var expandedOffset: () -> Float by mutableStateOf({ 0f })
    private var collapsedOffset: () -> Float by mutableStateOf({ 0f })

    fun offset(isExpanded: Boolean): Float = if (isExpanded) expandedOffset() else collapsedOffset()

    fun setExpandedOffsetSource(source: () -> Float) {
        expandedOffset = source
    }

    fun setCollapsedOffsetSource(source: () -> Float) {
        collapsedOffset = source
    }
}

@Composable
internal fun rememberCalendarHeaderState(): CalendarHeaderState = remember { CalendarHeaderState() }
