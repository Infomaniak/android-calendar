package com.infomaniak.calendar.components.eventcard

import android.R.attr.action
import android.R.attr.label
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.HourFormatter.formatHours
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.models.AvatarColors
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.ui.compose.margin.Margin
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EventCard(
    timeUntilEvent: String,
    title: String,
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    location: String?,
    attendees: List<AvatarType>,
    action: EventCardAction,
    progress: () -> Float,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.large) {
        EventCardContent(
            timeUntilEvent = timeUntilEvent,
            title = title,
            startDate = startDate,
            endDate = endDate,
            location = location,
            attendees = attendees,
            action = action,
            modifier = Modifier.padding(Margin.Medium),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EventCardContent(
    timeUntilEvent: String,
    title: String,
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    location: String?,
    attendees: List<AvatarType>,
    action: EventCardAction,
    modifier: Modifier = Modifier,
) {
    Column(horizontalAlignment = Alignment.Start, modifier = modifier) {
        Text(
            timeUntilEvent.uppercase(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmallEmphasized,
        )
        Spacer(modifier = Modifier.height(Margin.Micro))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(Margin.Mini))

        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Margin.Mini)) {
                IconItem(painterResource(R.drawable.ic_clock), null, startDate.formatRangeTo(endDate))
                if (location != null) {
                    IconItem(painterResource(R.drawable.ic_door_open), null, location)
                }
                if (attendees.isNotEmpty()) {
                    AttendeesAvatars(attendees)
                }
            }

            ActionButton(action)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CollapsedEventCardContent(
    title: String,
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    action: EventCardAction,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            IconItem(painterResource(R.drawable.ic_clock), null, startDate.formatRangeTo(endDate))
        }

        ActionButton(action)
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ActionButton(action: EventCardAction) {
    when (action) {
        EventCardAction.None -> Unit
        is EventCardAction.Button -> {
            Button(action.onClick, shapes = ButtonDefaults.shapes(ButtonDefaults.squareShape)) {
                Icon(painterResource(action.iconRes), null, tint = LocalContentColor.current)
                Spacer(modifier = Modifier.width(Margin.Mini))
                Text(action.label)
            }
        }
    }
}

@Composable
private fun AttendeesAvatars(attendees: List<AvatarType>, modifier: Modifier = Modifier) {
    DynamicOverflowRow(
        overflowIndicator = { Text("+$it") },
        spacing = (-8).dp,
        modifier = modifier,
    ) {
        attendees.forEach {
            Avatar(it, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun IconItem(painter: Painter, contentDescription: String?, text: String) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
        Row(horizontalArrangement = Arrangement.spacedBy(Margin.Mini)) {
            Icon(painter, contentDescription, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun LocalDateTime.formatRangeTo(end: LocalDateTime): String = "${formatHours()} - ${end.formatHours()}"

sealed interface EventCardAction {
    data object None : EventCardAction
    sealed class Button(@DrawableRes val iconRes: Int, val label: String, val onClick: () -> Unit) : EventCardAction {
        class JoinMeeting(onClick: () -> Unit) : Button(R.drawable.ic_product_kmeet, "Join", onClick)
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            EventCard(
                timeUntilEvent = "In 5 minutes",
                title = "Calendar meeting",
                startDate = LocalDateTime.of(2026, 6, 19, 8, 0),
                endDate = LocalDateTime.of(2026, 6, 19, 16, 0),
                location = "Japan room",
                attendees = List(9) { AvatarType.WithInitials.Initials("AB", AvatarColors(Color.Gray, Color.White)) },
                action = EventCardAction.Button.JoinMeeting {},
            )
        }
    }
}
