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
import kotlinx.datetime.LocalDate

/**
 * A row of the month grid: its 7 consecutive [dates], one per column, and the [segments] drawn over them.
 *
 * Columns only make sense against the dates they were packed with, which is why a week carries both: whoever packs
 * the segments also decides the grid, so the two cannot disagree. Segments only cover dates of the month, and never
 * share a lane on a column.
 */
@Immutable
data class MonthWeekUi(val dates: List<LocalDate>, val segments: List<MonthEventSegmentUi>)
