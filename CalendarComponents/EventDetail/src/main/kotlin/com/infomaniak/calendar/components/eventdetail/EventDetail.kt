package com.infomaniak.calendar.components.eventdetail

import android.app.Notification
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.foundation.models.Room
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.filetypes.FileType
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
fun EventDetail(eventDetail: EventDetail, modifier: Modifier = Modifier) {

}
