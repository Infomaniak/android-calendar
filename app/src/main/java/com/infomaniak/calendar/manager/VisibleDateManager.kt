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

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
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

    private var isInitialized = false

    @OptIn(SavedStateHandleSaveableApi::class)
    fun makeSaveableTo(savedStateHandle: SavedStateHandle) {
        val saver = Saver<VisibleDateManager, String>(
            save = { it.visibleDate.value.toString() },
            restore = { savedValue ->
                restoreVisibleDate(LocalDate.parse(savedValue))
                return@Saver this
            },
        )
        savedStateHandle.saveable("visibleDay", saver = saver) {
            restoreVisibleDate(visibleDate.value)
            return@saveable this
        }
    }

    private fun restoreVisibleDate(date: LocalDate) {
        if (isInitialized) return

        isInitialized = true
        visibleDate.value = date
    }
}
