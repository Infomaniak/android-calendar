package com.infomaniak.calendar.components.eventcard

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.HourFormatter.formatHours
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.models.AvatarColors
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.ui.compose.margin.Margin
import java.time.LocalDateTime
import kotlin.math.roundToInt

private val AVATAR_SPACING = (-8).dp
private val MAX_AVATAR_COUNT = 3

@Composable
fun rememberEventCardState(): EventCardState {
    return remember { EventCardState() }
}

class EventCardState {
    var collapsedHeight by mutableStateOf<Int?>(null)
    var expandedHeight by mutableStateOf<Int?>(null)

    fun recordHeights(collapsedHeight: Int, expandedHeight: Int) {
        this.collapsedHeight = collapsedHeight
        this.expandedHeight = expandedHeight
    }

    // fun getProgress(delta: Int): Float {
    //     val collapsed = collapsedHeight ?: return 0f
    //     val expanded = expandedHeight ?: return 1f
    //     return (delta - collapsed).toFloat() / (expanded - collapsed)
    // }
}

/**
 * A seekable event card that continuously morphs between a collapsed and an expanded state.
 *
 * [progress] is a `0f..1f` value (`0f` = fully collapsed, `1f` = fully expanded) that is meant to be
 * driven by an external gesture (e.g. a scroll offset). It is intentionally exposed as a lambda so
 * every frame is resolved through deferred reads inside `graphicsLayer` / layout blocks: changing
 * [progress] only re-runs the draw and layout phases, never recomposition. This keeps the transition
 * cheap even when many cards are laid out in a scrolling list.
 */
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
    eventCardState: EventCardState = rememberEventCardState(),
) {
    Layout(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(CardDefaults.cardColors().containerColor)
            .padding(Margin.Medium),
        content = {
            FadingContent(alpha = { collapsedAlpha(progress()) }) {
                CollapsedEventCardContent(title = title, startDate = startDate, endDate = endDate, action = action)
            }
            FadingContent(alpha = { expandedAlpha(progress()) }) {
                ExpandedEventCardContent(
                    timeUntilEvent = timeUntilEvent,
                    title = title,
                    startDate = startDate,
                    endDate = endDate,
                    location = location,
                    attendees = attendees,
                    action = action,
                )
            }
        },
    ) { measurables, constraints ->
        val childConstraints = Constraints(
            minWidth = 0,
            maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else Constraints.Infinity,
            minHeight = 0,
            maxHeight = Constraints.Infinity,
        )

        val collapsed = measurables[0].measure(childConstraints)
        val expanded = measurables[1].measure(childConstraints)

        val width = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            maxOf(collapsed.width, expanded.width)
        }
        val progress = progress().coerceIn(0f, 1f)
        val height = collapsed.height + ((expanded.height - collapsed.height) * progress).roundToInt()

        eventCardState.recordHeights(collapsed.height, expanded.height)

        layout(width, height) {
            collapsed.place(0, 0)
            expanded.place(0, 0)
        }
    }
}

/**
 * A lightweight [androidx.compose.foundation.layout.Box] replacement that applies a deferred [alpha]
 * without recomposing when the alpha value changes.
 */
@Composable
private fun FadingContent(alpha: () -> Float, content: @Composable () -> Unit) {
    Layout(
        modifier = Modifier.graphicsLayer { this.alpha = alpha() },
        content = content,
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val width = placeables.maxOfOrNull { it.width } ?: 0
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(width, height) {
            placeables.forEach { it.place(0, 0) }
        }
    }
}

private fun collapsedAlpha(progress: Float): Float = (1f - progress / 0.45f).coerceIn(0f, 1f)

private fun expandedAlpha(progress: Float): Float = ((progress - 0.4f) / 0.6f).coerceIn(0f, 1f)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpandedEventCardContent(
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

                if (attendees.isNotEmpty()) AttendeesAvatars(attendees)
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
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
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
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AVATAR_SPACING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        attendees.take(MAX_AVATAR_COUNT).forEach { avatarType ->
            Avatar(avatarType, modifier = Modifier.size(24.dp))
        }
        if (attendees.size > MAX_AVATAR_COUNT) {
            val extraAttendeeSize = attendees.size - MAX_AVATAR_COUNT
            val extraAttendeesText = pluralStringResource(R.plurals.moreParticipantsLabel, extraAttendeeSize, extraAttendeeSize)
            Text(
                extraAttendeesText,
                style = MaterialTheme.typography.bodySmallEmphasized,
                modifier = Modifier.padding(start = -AVATAR_SPACING + Margin.Mini),
            )
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
                progress = { 1f },
            )
        }
    }
}
