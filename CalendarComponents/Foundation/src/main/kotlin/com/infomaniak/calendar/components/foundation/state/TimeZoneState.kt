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
import kotlinx.datetime.TimeZone

/**
 * Remembers the current timezone and emits a new value on each timezone update.
 *
 * The returned State holds the current device timezone and is intended to update automatically
 * whenever the user updates it, so that composables reading it recompose with the new date.
 *
 * Note: the automatic update logic is not yet implemented. For now this simply holds the
 * timezone captured at first composition and never changes.
 */
@Composable
fun rememberCurrentTimeZone(): State<TimeZone> {
    return remember { mutableStateOf(TimeZone.currentSystemDefault()) }
}
