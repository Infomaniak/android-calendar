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
package com.infomaniak.calendar.components.eventdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.component.AttendeesButton
import com.infomaniak.calendar.components.eventdetail.component.ClickableItem
import com.infomaniak.calendar.components.eventdetail.component.DateAndTime
import com.infomaniak.calendar.components.eventdetail.component.LIST_ITEM_HORIZONTAL_PADDING
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

@Composable
fun EventDetail(
    eventDetail: EventDetailUi,
    onKMeetClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val horizontalContentPadding = contentPadding.onlyHorizontal()

    Column(
        modifier = modifier.padding(top = contentPadding.calculateTopPadding(), bottom = contentPadding.calculateBottomPadding()),
    ) {
        with(eventDetail) {
            Title(eventColor, title, Modifier.padding(horizontalContentPadding))
            DateAndTime(start, end, isAllDay, Modifier.padding(horizontalContentPadding))

            Section(contentPadding = horizontalContentPadding) {
                if (eventDetail.attendees.all.isNotEmpty()) {
                    AttendeesButton(eventDetail.attendees.all, onClick = {}, contentPadding = horizontalContentPadding)
                }

                if (kMeetUrl != null) KMeetButton(onClick = onKMeetClick, contentPadding = horizontalContentPadding)
            }
        }
    }
}

@Composable
private fun Title(color: Color, title: String, modifier: Modifier = Modifier) {
    ListItem(
        headlineContent = { Text(text = title, style = MaterialTheme.typography.titleLargeEmphasized) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun KMeetButton(onClick: () -> Unit, modifier: Modifier = Modifier, contentPadding: PaddingValues = PaddingValues()) {
    ClickableItem(
        text = stringResource(id = R.string.participateKMeetTitle),
        leadingIconRes = R.drawable.ic_product_kmeet,
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Margin.Mini)) {
                Text(stringResource(R.string.buttonJoin), style = MaterialTheme.typography.labelLarge)
                Icon(painterResource(R.drawable.ic_squares_stacked), contentDescription = null, modifier = Modifier.size(20.dp))
            }
        },
        onClick = onClick,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

/**
 * Automatically shows or hides divider based on if any content is composed or not. This layout acts like a column.
 */
@Composable
private fun Section(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable () -> Unit,
) {
    Layout(
        contents = listOf({ Divider(modifier = Modifier.padding(contentPadding)) }, content),
        modifier = modifier,
    ) { (dividerMeasurables, contentMeasurables), constraints ->
        if (contentMeasurables.isEmpty()) return@Layout layout(0, 0) {}

        val childConstraints = constraints.copy(minHeight = 0)
        val placeables = (dividerMeasurables + contentMeasurables).map { it.measure(childConstraints) }

        val width = placeables.maxOf { it.width }.coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = placeables.sumOf { it.height }.coerceIn(constraints.minHeight, constraints.maxHeight)

        layout(width, height) {
            var y = 0
            placeables.forEach { placeable ->
                placeable.place(0, y)
                y += placeable.height
            }
        }
    }
}

@Composable
private fun Divider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.padding(LIST_ITEM_HORIZONTAL_PADDING))
}

@Preview
@Composable
private fun PreviewEventDetail() {
    val eventDetail = EventDetailUi(
        eventColor = Color.Red,
        calendarColor = Color.Blue,
        title = "Event Title",
        start = EventDetailTiming.Precise(Instant.parse("2026-05-20T08:00:00Z"), TimeZone.of("Europe/Paris")),
        end = EventDetailTiming.Precise(Instant.parse("2026-05-20T09:00:00Z"), TimeZone.of("Europe/Paris")),
        isAllDay = false,
        attendees = Attendees(all = previewAttendees, me = null),
        kMeetUrl = "test urlc",
        location = "Location",
        room = null,
        urlLink = null,
        description = "Description",
        files = emptyList(),
        notifications = emptyList(),
    )

    MaterialTheme {
        Surface {
            EventDetail(eventDetail, onKMeetClick = {}, contentPadding = PaddingValues(horizontal = Margin.Small))
        }
    }
}
