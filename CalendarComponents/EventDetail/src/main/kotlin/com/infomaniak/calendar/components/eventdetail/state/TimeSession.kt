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
 */
internal class TimeSession(private val state: DateAndTimeState, private val entry: DateAndTimeState.Entry) {
    private val initialStart = state.start.instant
    private val initialInterval = state.end.instant - initialStart

    fun pickTime(time: LocalTime) = with(state) { entry.edit(initialStart, initialInterval) { entry.time = time } }
}

/**
 * A session lasts while [entry]'s time picker stays open ([isOpen]), and restarts if a date or time zone is picked meanwhile.
 */
@Composable
internal fun DateAndTimeState.rememberTimeSession(entry: DateAndTimeState.Entry, isOpen: Boolean): TimeSession {
    return remember(this, entry, isOpen, start.date, start.timeZone, end.date, end.timeZone) { TimeSession(this, entry) }
}
