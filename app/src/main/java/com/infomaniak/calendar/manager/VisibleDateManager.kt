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
package com.infomaniak.calendar.manager

import android.os.Bundle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import com.infomaniak.core.common.utils.today
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

@Inject
@SingleIn(AppScope::class)
class VisibleDateManager {
    val visibleDate = mutableStateOf(Clock.today())
    val visibleDateFlow = snapshotFlow { visibleDate.value }

    fun bindTo(savedStateHandle: SavedStateHandle) {
        savedStateHandle.get<Bundle>(KEY)?.getString(KEY)?.let { visibleDate.value = LocalDate.parse(it) }
        savedStateHandle.setSavedStateProvider(KEY) { bundleOf(KEY to visibleDate.value.toString()) }
    }

    companion object {
        private const val KEY = "visibleDate"
    }
}
