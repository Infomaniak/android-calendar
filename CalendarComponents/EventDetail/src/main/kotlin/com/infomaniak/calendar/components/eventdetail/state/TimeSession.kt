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
package com.infomaniak.calendar.components.eventdetail.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalTime

/**
 * Consecutive time picks on [entry], all corrected from the start and duration the session began with. A wheel picks every
 * row it passes, so the duration would otherwise already be zero once one entry goes past the other.
 *
 * It begins again when the range changed since its last pick (a date or time zone picked meanwhile), but not when its own
 * corrections move a date.
 */
internal class TimeSession(private val state: DateAndTimeState, private val entry: DateAndTimeState.Entry) {
    private var initialRange = state.range()
    private var lastRange = initialRange

    fun pickTime(time: LocalTime) = with(state) {
        if (range() != lastRange) initialRange = range()
        val (initialStart, initialEnd) = initialRange
        entry.edit(initialStart, initialEnd - initialStart) { entry.time = time }
        lastRange = range()
    }
}

/** A session lasts while [entry]'s time picker stays open ([isOpen]). */
@Composable
internal fun DateAndTimeState.rememberTimeSession(entry: DateAndTimeState.Entry, isOpen: Boolean): TimeSession {
    return remember(this, entry, isOpen) { TimeSession(this, entry) }
}

private fun DateAndTimeState.range() = start.instant to end.instant
