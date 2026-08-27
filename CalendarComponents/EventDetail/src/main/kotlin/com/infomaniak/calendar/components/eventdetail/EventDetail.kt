package com.infomaniak.calendar.components.eventdetail

import android.app.Notification
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.foundation.models.Room
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDateRange
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDateTimeRange
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.filetypes.FileType
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Immutable
data class EventDetail(
    val eventColor: Color,
    val calendarColor: Color,
    val title: String,
    val start: Instant,
    val end: Instant,
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
        Title2(color = eventDetail.eventColor, title = eventDetail.title)
        DateAndTime(start = eventDetail.start, end = eventDetail.end, isAllDay = eventDetail.isAllDay)
    }
}

@Composable
private fun Title(color: Color, title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .padding(2.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLargeEmphasized,
        )
    }
}

@Composable
private fun Title2(color: Color, title: String, modifier: Modifier = Modifier) {
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

@Composable
private fun DateAndTime(start: Instant, end: Instant, isAllDay: Boolean, modifier: Modifier = Modifier) {
    val timeZone = TimeZone.currentSystemDefault()

    ListItem(
        headlineContent = {
            Text(
                text = if (isAllDay) {
                    formatDateRange(start.toLocalDateTime(timeZone).date, end.toLocalDateTime(timeZone).date)
                } else {
                    formatDateTimeRange(start, end, timeZone)
                },
                style = MaterialTheme.typography.bodyMedium,
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
        start = Instant.parse("2026-05-20T08:00:00Z"),
        end = Instant.parse("2026-05-20T09:00:00Z"),
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
