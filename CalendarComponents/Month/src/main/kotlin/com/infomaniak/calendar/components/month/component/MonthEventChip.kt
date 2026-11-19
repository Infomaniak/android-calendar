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
package com.infomaniak.calendar.components.month.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.event.EventItemCard
import com.infomaniak.calendar.components.event.toEventItemStatus
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.calendar.components.month.layout.MonthDefaults
import com.infomaniak.calendar.components.month.preview.previewCancelledEvent
import com.infomaniak.calendar.components.month.preview.previewDeclinedEvent
import com.infomaniak.calendar.components.month.preview.previewEvent
import com.infomaniak.calendar.components.month.preview.previewPendingEvent
import com.infomaniak.calendar.components.month.preview.previewTentativeEvent
import com.infomaniak.designsystem.core.theme.EsdsTheme

/** An event in a lane of the month grid: the card of the other views, holding only its title. */
@Composable
internal fun MonthEventChip(event: EventSummaryUi.Normal, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val status = event.toEventItemStatus()

    EventItemCard(status, onClick, modifier, contentPadding = MonthDefaults.EventTitlePadding) {
        Text(
            text = event.title,
            style = MonthDefaults.EventTitleStyle,
            textDecoration = status.textDecoration,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            Column {
                listOf(previewEvent, previewCancelledEvent, previewDeclinedEvent, previewTentativeEvent, previewPendingEvent)
                    .forEach { event ->
                        MonthEventChip(event, onClick = {}, Modifier.size(width = 96.dp, height = MonthDefaults.LaneHeight))
                        Spacer(Modifier.size(EsdsTheme.spacing.twoXs))
                    }
            }
        }
    }
}
