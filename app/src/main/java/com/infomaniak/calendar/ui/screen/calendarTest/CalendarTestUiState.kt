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
package com.infomaniak.calendar.ui.screen.calendarTest

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

sealed interface CalendarTestUiState {

    data object Loading : CalendarTestUiState

    data class Success(val message: String) : CalendarTestUiState

    data class Error(val message: String) : CalendarTestUiState

    companion object {
        internal class CalendarTestUiStatePreviewProvider : PreviewParameterProvider<CalendarTestUiState> {
            override val values = sequenceOf(Loading, Success, Error)

            companion object {
                val Success = Success("Hello world")
                val Error = Error("Something went wrong")
            }
        }
    }
}


