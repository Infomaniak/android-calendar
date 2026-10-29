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
package com.infomaniak.calendar.utils

import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState

private const val ATTENDEES_SEARCH_STATE_KEY = "attendeesSearchState"

@OptIn(SavedStateHandleSaveableApi::class)
fun SavedStateHandle.saveableAttendeesState(): SnapshotStateSet<AttendeeUi> {
    return saveable(key = ATTENDEES_SEARCH_STATE_KEY, saver = AttendeesSaver) { mutableStateSetOf() }
}

// TODO: Use autoSaver once on Compose 1.13+, SnapshotStateSet only parcels its first element before (b/537461421)
val AttendeesSaver = Saver<SnapshotStateSet<AttendeeUi>, ArrayList<AttendeeUi>>(
    save = { ArrayList(it) },
    restore = { mutableStateSetOf<AttendeeUi>().apply { addAll(it) } },
)
