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
package com.infomaniak.calendar.ui.state

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

val LocalVisibleDayState = staticCompositionLocalOf<VisibleDayState?> { null }

@Composable
@SuppressLint("ComposeMutableParameters")
fun rememberVisibleDayState(visibleDate: MutableState<LocalDate> = rememberSaveable { mutableStateOfToday() }): VisibleDayState {
    return remember { VisibleDayState(_visibleDate = visibleDate) }
}

private fun mutableStateOfToday(): MutableState<LocalDate> = mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()))

@Stable
class VisibleDayState(private val _visibleDate: MutableState<LocalDate>) {
    val visibleDate by _visibleDate

    private val _scrollCommand = Channel<LocalDate>(Channel.CONFLATED)
    val scrollCommand: ReceiveChannel<LocalDate> = _scrollCommand

    fun onVisibleDateChanged(date: LocalDate) {
        _visibleDate.value = date
    }

    fun jumpTo(date: LocalDate) = _scrollCommand.trySend(date)
}
