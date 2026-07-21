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
package com.infomaniak.calendar.components.foundation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

/**
 * Remembers the current date and emits a new value on each day change.
 *
 * The returned [State] holds today's date and is intended to update automatically
 * whenever the day rolls over, so that composables reading it recompose with the new date.
 *
 * Note: the automatic update logic is not yet implemented. For now this simply holds the
 * date captured at first composition and never changes.
 */
@Composable
fun rememberToday(): State<LocalDate> {
    return remember { mutableStateOf(Clock.today()) }
}
