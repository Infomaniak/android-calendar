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
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi

/**
 * An [event] drawn in a week row, over [span] consecutive columns from [startColumn], on its [lane].
 *
 * An event spanning several days is one segment per run of consecutive days within a week, so [event] is the run's
 * first day slice.
 */
@Immutable
data class MonthEventSegmentUi(
    val event: EventSummaryUi.Normal,
    val startColumn: Int,
    val span: Int,
    val lane: Int,
) {
    val columns: IntRange get() = startColumn until startColumn + span
}
