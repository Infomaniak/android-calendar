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
package com.infomaniak.calendar.components.day.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.infomaniak.calendar.components.day.DayTimelineDefaults
import com.infomaniak.calendar.components.day.model.TimedEvent
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.event.EventIcons
import com.infomaniak.calendar.components.event.component.cardStripes
import com.infomaniak.calendar.components.event.toEventIcons
import com.infomaniak.calendar.components.event.toEventItemStatus
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.HourFormatter.formatHours
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlin.math.roundToInt

private val MinTitleSize = 11.sp
private val ReadableTitleSize = 13.sp

private val MinTitleHeight = 16.dp
private val ReadableTitleHeight = 20.dp

/**
 * A title is rounded to this before it is written. Its size is the one thing a card cannot change
 * without composing again and laying its paragraph out again, so it is the one thing that may not
 * follow a pinch frame by frame. Half a point is far below what the eye catches, and turns a whole
 * zoom into a handful of sizes.
 */
private val TitleSizeStep = 0.5.sp
private const val MinTitleWrapEms = 2f
private const val MaxDetailLines = 2
private const val ELLIPSIS = "…"

@Composable
internal fun TimedEventCard(
    timedEvent: TimedEvent,
    titleSizing: TitleSizing,
    visibleHeight: () -> Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = timedEvent.event.toEventItemStatus()

    Card(
        onClick = onClick,
        colors = status.cardColors(),
        border = status.cardBorder(),
        shape = EsdsTheme.radius.sm,
        modifier = modifier,
    ) {
        Row(modifier = Modifier
            .cardStripes(status)
            .fillMaxSize()) {
            EventAccentBar(status.accentBarColor())

            EventDetails(
                event = timedEvent.event,
                titleSizing = titleSizing,
                visibleHeight = visibleHeight,
                textDecoration = status.textDecoration,
                modifier = Modifier.padding(horizontal = EsdsTheme.spacing.lg),
            )
        }
    }
}

/**
 * Stacks the title over its details and settles, while measuring, everything that has to follow the
 * zoom exactly: how many lines fit under the title, and the padding left over once they have.
 *
 * The details are always composed, never gated on a height read too early. One that does not fit is
 * measured and dropped, which costs nothing after the first frame: its constraints never change, so
 * Compose hands back the paragraph it already laid out.
 */
@Composable
private fun EventDetails(
    event: EventUi.Normal,
    titleSizing: TitleSizing,
    visibleHeight: () -> Float,
    textDecoration: TextDecoration?,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val spacing = with(density) { EsdsTheme.spacing.twoXs.roundToPx() }
    val maxPadding = with(density) { EsdsTheme.spacing.lg.toPx() }

    val textMeasurer = rememberTextMeasurer()
    val detailStyle = MaterialTheme.typography.bodySmall
    val hours = "${event.start.formatHours()} - ${event.end.formatHours()}"

    Layout(
        modifier = modifier,
        contents = listOf(
            {
                TitleRow(
                    title = event.title,
                    icons = event.toEventIcons(),
                    titleSizing = titleSizing,
                    textDecoration = textDecoration,
                )
            },
            { event.location?.let { DetailLine(it, textDecoration) } },
            { DetailLine(hours, textDecoration) },
        ),
    ) { (titleMeasurables, locationMeasurables, timeMeasurables), constraints ->
        val available = visibleHeight()
        val childConstraints = Constraints(maxWidth = constraints.maxWidth)

        // The width a line has is only known here, once the accent bar and the padding have taken
        // their share of the card.
        fun isTextDisplayed(text: String?, style: TextStyle): Boolean {
            return text != null && textMeasurer.hasRoomForText(constraints.maxWidth, text, style)
        }

        val lines = ArrayList<Placeable>(1 + MaxDetailLines)
        var linesHeight = 0f

        if (isTextDisplayed(event.title, titleSizing.style)) {
            val title = titleMeasurables.single().measure(childConstraints)
            lines.add(title)
            linesHeight = title.height.toFloat()
        }

        for ((detailMeasurables, text) in listOf(locationMeasurables to event.location, timeMeasurables to hours)) {
            if (!isTextDisplayed(text, detailStyle)) continue
            val detail = detailMeasurables.firstOrNull()?.measure(childConstraints) ?: continue

            val gap = if (lines.isEmpty()) 0 else spacing
            if (linesHeight + gap + detail.height > available) break

            linesHeight += gap + detail.height
            lines.add(detail)
        }

        // Padding is what the lines leave over, never what they are charged: a card with the height
        // for one more line spends it on that line, and only a card with height to spare breathes.
        val padding = ((available - linesHeight) / 2f).coerceIn(0f, maxPadding).roundToInt()

        layout(constraints.maxWidth, linesHeight.roundToInt() + padding * 2) {
            var y = padding

            lines.forEach { line ->
                line.place(x = 0, y = y)
                y += line.height + spacing
            }
        }
    }
}

/**
 * Whether [text] has room to show anything in [maxWidth] pixels: all of it, or a first letter with
 * the ellipsis trailing it.
 *
 * A card too narrow for that writes nothing at all. An ellipsis on its own names no event and reads
 * as content that was never there, where the bare card at least reads as the block of time it is.
 */
private fun TextMeasurer.hasRoomForText(maxWidth: Int, text: String, style: TextStyle): Boolean {
    if (text.isEmpty() || maxWidth <= 0) return false

    // By code point, or a title opening on an emoji would be measured on half of one.
    val firstCharacter = text.substring(0, text.offsetByCodePoints(0, 1))

    val letterAndEllipsis = measure(firstCharacter + ELLIPSIS, style, maxLines = 1).size.width
    if (letterAndEllipsis <= maxWidth) return true

    // Only a text not longer than the ellipsis it would be cut down to can still fit whole here.
    return measure(text, style, maxLines = 1).size.width <= maxWidth
}

@Composable
private fun DetailLine(text: String, textDecoration: TextDecoration?) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        textDecoration = textDecoration,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun TitleRow(
    title: String,
    icons: Set<EventIcons>,
    titleSizing: TitleSizing,
    textDecoration: TextDecoration?,
    modifier: Modifier = Modifier,
) {
    val iconsSpacing = with(LocalDensity.current) { EsdsTheme.spacing.xs.roundToPx() }

    Layout(
        modifier = modifier,
        contents = listOf(
            {
                Text(
                    text = title,
                    style = titleSizing.style,
                    fontWeight = FontWeight.Medium,
                    textDecoration = textDecoration,
                    maxLines = titleSizing.maxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            { TrailingIcons(icons, titleSizing.iconSize) },
        ),
    ) { (titleMeasurables, iconsMeasurables), constraints ->
        val iconsPlaceable = iconsMeasurables.single().measure(constraints.copy(minWidth = 0, minHeight = 0))
        val titleMeasurable = titleMeasurables.single()

        val widthLeftForTitle = constraints.maxWidth - iconsPlaceable.width - iconsSpacing
        val showsIcons = iconsPlaceable.width > 0 &&
                titleMeasurable.maxIntrinsicWidth(constraints.maxHeight) <= widthLeftForTitle

        val titlePlaceable = titleMeasurable.measure(
            constraints.copy(
                minWidth = 0,
                minHeight = 0,
                maxWidth = if (showsIcons) widthLeftForTitle else constraints.maxWidth,
            ),
        )

        val height = if (showsIcons) maxOf(titlePlaceable.height, iconsPlaceable.height) else titlePlaceable.height

        layout(constraints.maxWidth, height) {
            titlePlaceable.place(x = 0, y = 0)
            if (showsIcons) {
                iconsPlaceable.place(
                    x = constraints.maxWidth - iconsPlaceable.width,
                    y = (height - iconsPlaceable.height) / 2,
                )
            }
        }
    }
}

@Composable
private fun TrailingIcons(icons: Set<EventIcons>, size: Dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.twoXs)) {
        EventIcons.entries.forEach { if (it in icons) it.TrailingIcon(size = size) }
    }
}

/**
 * How a card writes its title. Everything here is rounded, so that it stays equal from one frame of
 * a pinch to the next and the card composes again only when it truly changes shape.
 */
@Immutable
internal data class TitleSizing(val style: TextStyle, val maxLines: Int, val iconSize: Dp)

/**
 * The title rises with the height its card can give it, evenly rather than by steps, between
 * [MinTitleSize] at [MinTitleHeight] and the size the style asks for once the card can hold that
 * size and its padding. [TitleSizeStep] then rounds it to something the eye cannot follow.
 */
@Composable
internal fun titleSizingFor(visibleHeight: Dp, width: Dp): TitleSizing = with(LocalDensity.current) {
    val style = MaterialTheme.typography.bodyMediumEmphasized
    val padding = EsdsTheme.spacing.lg
    val lineHeight = style.lineHeight.toDp()
    val fullHeight = padding * 2 + lineHeight

    val size = when {
        visibleHeight >= fullHeight -> style.fontSize
        visibleHeight >= ReadableTitleHeight -> lerp(
            start = ReadableTitleSize,
            stop = style.fontSize,
            fraction = (visibleHeight - ReadableTitleHeight) / (fullHeight - ReadableTitleHeight),
        )
        visibleHeight >= MinTitleHeight -> lerp(
            start = MinTitleSize,
            stop = ReadableTitleSize,
            fraction = (visibleHeight - MinTitleHeight) / (ReadableTitleHeight - MinTitleHeight),
        )
        else -> MinTitleSize
    }.roundedToStep()

    // Rounding up must not overshoot the style: the top of the ladder is the style's own size.
    val scale = (size.toDp() / style.fontSize.toDp()).coerceIn(0f, 1f)
    val scaledLineHeight = lineHeight * scale

    // A title may take every line of its card, since the details under it only get what it leaves.
    // Measured against the whole height and not what padding would spare: padding is the leftover.
    val wraps = (width - padding * 2) >= size.toDp() * MinTitleWrapEms
    val maxLines = if (wraps) (visibleHeight / scaledLineHeight).toInt() else 1

    TitleSizing(
        style = if (scale == 1f) style else style.copy(fontSize = style.fontSize * scale, lineHeight = style.lineHeight * scale),
        maxLines = maxLines.coerceAtLeast(1),
        iconSize = EventIcons.TrailingIconSize * scale,
    )
}

private fun TextUnit.roundedToStep(): TextUnit = ((value / TitleSizeStep.value).roundToInt() * TitleSizeStep.value).sp

/**
 * A card at the height of a one hour event. Its title size and the lines it shows both follow that
 * height, so the preview settles it once and hands the same value to both.
 */
@Preview(widthDp = 220)
@Composable
private fun TimedEventCardPreview() {
    Surface {
        val cardHeight = DayTimelineDefaults.HourHeight
        val cardHeightPx = with(LocalDensity.current) { cardHeight.toPx() }

        TimedEventCard(
            timedEvent = previewDayEvents.timed.first(),
            titleSizing = titleSizingFor(visibleHeight = cardHeight, width = 220.dp),
            visibleHeight = { cardHeightPx },
            onClick = {},
            modifier = Modifier.height(cardHeight),
        )
    }
}
