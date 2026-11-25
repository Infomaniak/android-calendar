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
package com.infomaniak.calendar.components.month.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.YearMonth

/**
 * A [month] laid out as the weeks it touches, from the week holding its first day to the week holding its last one.
 */
@Immutable
data class MonthUi(val month: YearMonth, val weeks: List<MonthWeekUi>) {
    init {
        require(weeks.isNotEmpty()) { "$month must touch at least one week" }
    }
}
