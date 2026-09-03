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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.component.DateAndTime
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.foundation.models.Room
import com.infomaniak.calendar.components.foundation.state.rememberCurrentTimeZone
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.filetypes.FileType
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Immutable
sealed interface Timing {
    @get:Composable
    val instant: Instant

    @Immutable
    data class Precised(private val _instant: Instant, val timeZone: TimeZone) : Timing {
        override val instant: Instant @Composable get() = _instant
    }

    @Immutable
    class Floating(val date: LocalDateTime) : Timing {
        override val instant: Instant @Composable get() = date.toInstant(rememberCurrentTimeZone().value)
    }
}

@Immutable
data class EventDetail(
    val eventColor: Color,
    val calendarColor: Color,
    val title: String,
    val startAtTimeZone: LocalDateTime,
    val startTimeZone: TimeZone?,
    val endAtTimeZone: LocalDateTime,
    val endTimeZone: TimeZone?,
    val start: Timing,
    val end: Timing,
    val isAllDay: Boolean,
    val attendees: Attendees,
    val kMeetUrl: String?,
    val location: String?,
    val room: Room?,
    val urlLink: String?,
    val description: String?,
    val files: List<EventFile>,
    val notifications: List<Notification>,
) {
    @get:Composable
    val startAtLocale: LocalDateTime
        get() {
            val currentSystemTimeZone by rememberCurrentTimeZone()
            return startAtTimeZone.toInstant(startTimeZone ?: currentSystemTimeZone).toLocalDateTime(currentSystemTimeZone)
        }

    @get:Composable
    val endAtLocale: LocalDateTime
        get() {
            val currentSystemTimeZone by rememberCurrentTimeZone()
            return endAtTimeZone.toInstant(endTimeZone ?: currentSystemTimeZone).toLocalDateTime(currentSystemTimeZone)
        }
}

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

        val currentTimeZone by rememberCurrentTimeZone()
        DateAndTime(
            start = DateTimeInput(
                atLocale = { eventDetail.startAtLocale },
                utcOffsetAtLocale = { (eventDetail.start as? Timing.Precised)?.let { currentTimeZone.offsetAt(it.instant) } },
                atTimeZone = eventDetail.startAtTimeZone,
                utcOffsetAtTimeZone = { (eventDetail.start as? Timing.Precised)?.let { it.timeZone.offsetAt(it.instant) } },
            ),
            end = DateTimeInput(
                atLocale = { eventDetail.endAtLocale },
                utcOffsetAtLocale = { (eventDetail.end as? Timing.Precised)?.let { currentTimeZone.offsetAt(it.instant) } },
                atTimeZone = eventDetail.endAtTimeZone,
                utcOffsetAtTimeZone = { (eventDetail.end as? Timing.Precised)?.let { it.timeZone.offsetAt(it.instant) } },
            ),
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
        startAtTimeZone = LocalDateTime.parse("2026-05-20T08:00:00"),
        startTimeZone = TimeZone.of("Europe/Paris"),
        endAtTimeZone = LocalDateTime.parse("2026-05-20T09:00:00"),
        endTimeZone = TimeZone.of("Europe/Paris"),
        start = Timing.Precised(Instant.parse("2026-05-20T08:00:00"), TimeZone.of("Europe/Paris")),
        end = Timing.Precised(Instant.parse("2026-05-20T09:00:00"), TimeZone.of("Europe/Paris")),
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
