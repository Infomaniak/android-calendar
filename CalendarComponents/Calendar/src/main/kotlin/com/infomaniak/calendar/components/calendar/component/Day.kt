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
package com.infomaniak.calendar.components.calendar.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.component.DateState
import com.infomaniak.calendar.components.foundation.component.DayCircle
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.preview.LocalEventColorsUiFactory
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Clock

private const val MAX_DOTS = 3
private val DOT_SIZE = 6.dp

@Composable
internal fun Day(
    date: LocalDate,
    dateState: DateState,
    onClick: () -> Unit,
    dotsFor: () -> List<EventColorsUi>,
    modifier: Modifier = Modifier,
) {
    val locale = LocalLocale.current.platformLocale
    val fullDate = remember(date, locale) {
        date.toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale))
    }

    val dots = dotsFor()
    val eventsDescription = if (dots.isEmpty()) {
        null
    } else {
        pluralStringResource(R.plurals.contentDescriptionEventCount, dots.size, dots.size)
    }
    val dayDescription = remember(fullDate, eventsDescription) {
        listOfNotNull(fullDate, eventsDescription).joinToString()
    }

    val stateDescriptionToday = if (dateState == DateState.Today) stringResource(R.string.contentDescriptionToday) else null
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(LocalViewConfiguration.current.minimumTouchTargetSize.height)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
            ) { onClick() }
            .clearAndSetSemantics {
                contentDescription = dayDescription
                selected = dateState == DateState.Selected
                stateDescriptionToday?.let { stateDescription = it }
            }
            .padding(Margin.Micro),
        contentAlignment = Alignment.Center,
    ) {
        DayCircle(
            state = dateState,
            modifier = Modifier
                .fillMaxHeight()
                .then(
                    if (dateState == DateState.Selected || dateState == DateState.Today) Modifier.aspectRatio(
                        1f,
                        matchHeightConstraintsFirst = true,
                    ) else Modifier,
                ),

            ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Spacer(modifier = Modifier.height(DOT_SIZE))
                Text(text = date.day.toString())

                if (dateState == DateState.None) {
                    EventDots(
                        dots = dotsFor(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Margin.Micro),
                    )
                } else {
                    Spacer(modifier = Modifier.height(DOT_SIZE))
                }
            }
        }
    }
}

@Composable
private fun EventDots(dots: List<EventColorsUi>, modifier: Modifier = Modifier) {
    val hasOverflow = dots.size > MAX_DOTS
    val visibleDots = dots.take(if (hasOverflow) MAX_DOTS - 1 else MAX_DOTS)

    Row(
        modifier = modifier.height(DOT_SIZE),
        horizontalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        visibleDots.forEach { eventColor ->
            EventDot(color = eventColor.sourceColor)
        }
        if (hasOverflow) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plus),
                contentDescription = null,
                modifier = Modifier.size(DOT_SIZE),
            )
        }
    }
}

@Composable
private fun EventDot(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(DOT_SIZE)
            .clip(CircleShape)
            .background(color),
    )
}

/**
 * Builds a [Modifier] that turns a day cell into a shared element keyed by its [date], so the same day
 * translates gracefully between the collapsed week and the expanded month during the expand/collapse
 * transition. Days that only exist on one side (e.g. days from other weeks of the month) have no match
 * and simply fade in/out with the [AnimatedVisibilityScope].
 *
 * Returns [Modifier] unchanged when [enabled] is false or when the transition scopes are unavailable
 * so the day renders normally without participating in any shared transition.
 */
@Composable
internal fun Modifier.daySharedElement(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    date: LocalDate,
    enabled: Boolean,
): Modifier {
    if (!enabled || sharedTransitionScope == null || animatedVisibilityScope == null) return this

    return with(sharedTransitionScope) {
        sharedElement(rememberSharedContentState(key = date), animatedVisibilityScope)
    }
}

@Composable
@Preview
private fun DayPreview() {
    val today = Clock.today()
    val previewDaySize = 48.dp
    val eventColors = LocalEventColorsUiFactory.current.create(0xFF6750A4.toInt())

    Surface {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DateState.entries.forEach { dateState ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(dateState.name, style = MaterialTheme.typography.labelSmall)
                    Day(
                        dateState = dateState,
                        date = today,
                        onClick = {},
                        dotsFor = { listOf(eventColors, eventColors, eventColors) },
                        modifier = Modifier.size(previewDaySize),
                    )
                }
            }
        }
    }
}
