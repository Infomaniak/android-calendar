package com.infomaniak.calendar.components.eventdetail

import android.app.Notification
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.component.DateAndTime
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.foundation.models.Room
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.filetypes.FileType
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

@Immutable
data class EventDetail(
    val eventColor: Color,
    val calendarColor: Color,
    val title: String,
    val start: EventDetailTiming,
    val end: EventDetailTiming,
    val isAllDay: Boolean,
    val attendees: Attendees,
    val kMeetUrl: String?,
    val location: String?,
    val room: Room?,
    val urlLink: String?,
    val description: String?,
    val files: List<EventFile>,
    val notifications: List<Notification>,
)

@Immutable
data class EventFile(val name: String) {
    val fileType: FileType by lazy { FileType.guessFromFileName(name) }
}

@Immutable
data class Notification(
    val notificationType: NotificationType,
    val executionTime: Instant,
)

@Immutable
enum class NotificationType(@DrawableRes val icon: Int, @StringRes val label: Int) {
    Email(R.drawable.ic_bell, R.string.notificationTypeEmail),
    Push(R.drawable.ic_bubble_top_right_circle, R.string.notificationTypePush),
}

@Composable
fun EventDetail(
    eventDetail: EventDetail,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val horizontalContentPadding = contentPadding.onlyHorizontal()

    Column(
        modifier = modifier.padding(top = contentPadding.calculateTopPadding(), bottom = contentPadding.calculateBottomPadding()),
    ) {
        Title(color = eventDetail.eventColor, title = eventDetail.title)

        DateAndTime(
            start = eventDetail.start.toDateTimeInput(),
            end = eventDetail.end.toDateTimeInput(),
            isAllDay = eventDetail.isAllDay,
        )
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
    val eventDetail = EventDetail(
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

    Surface {
        EventDetail(eventDetail, contentPadding = PaddingValues(horizontal = Margin.Large))
    }
}
