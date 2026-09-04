package com.infomaniak.calendar.components.eventdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.component.DateAndTime
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

@Composable
fun EventDetail(
    eventDetail: EventDetailUi,
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

@Preview
@Composable
private fun PreviewEventDetail() {
    val eventDetail = EventDetailUi(
        eventColor = Color.Red,
        calendarColor = Color.Blue,
        title = "Event Title",
        start = EventDetailTiming.Precised(Instant.parse("2026-05-20T08:00:00Z"), TimeZone.of("Europe/Paris")),
        end = EventDetailTiming.Precised(Instant.parse("2026-05-20T09:00:00Z"), TimeZone.of("Europe/Paris")),
        isAllDay = false,
        attendees = Attendees(emptyList(), null),
        kMeetUrl = null,
        location = "Location",
        room = null,
        urlLink = null,
        description = "Description",
        files = emptyList(),
        notifications = emptyList(),
    )

    MaterialTheme {
        Surface {
            EventDetail(eventDetail)
        }
    }
}
