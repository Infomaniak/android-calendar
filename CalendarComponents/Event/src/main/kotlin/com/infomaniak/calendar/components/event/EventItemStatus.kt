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
package com.infomaniak.calendar.components.event

import android.content.res.Configuration
import androidx.annotation.FloatRange
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.EventStatus
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.foundation.preview.LocalEventColorsUiFactory
import com.infomaniak.core.ui.compose.theme.LocalIsThemeDarkMode
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

internal fun EventUi.toEventItemStatus(): EventItemStatus {
    if (status == EventStatus.Cancelled) return EventItemStatus.Declined(colors)
    val me = attendees.me ?: return EventItemStatus.Default(colors)

    return when (me.status) {
        ParticipationStatus.Accepted -> EventItemStatus.Default(colors)
        ParticipationStatus.Declined -> EventItemStatus.Declined(colors)
        ParticipationStatus.Tentative -> EventItemStatus.Maybe(colors)
        ParticipationStatus.NeedsAction -> EventItemStatus.Pending(colors)
    }
}

@Immutable
sealed class EventItemStatus(
    val cardColors: @Composable () -> CardColors,
    val cardBorder: @Composable () -> BorderStroke? = { null },
    val stripesColor: @Composable () -> Color? = { null },
    val textDecoration: TextDecoration? = null,
) {
    abstract val eventColors: EventColorsUi

    data class Default(override val eventColors: EventColorsUi) : EventItemStatus(
        cardColors = { containerColors(eventColors) },
    )

    data class Maybe(override val eventColors: EventColorsUi) : EventItemStatus(
        cardColors = { containerColors(eventColors) },
        stripesColor = { eventColors.onDatavizContainerVariant.copy(alpha = 0.1f) },
    )

    data class Declined(override val eventColors: EventColorsUi) : EventItemStatus(
        cardColors = { containerColors(eventColors, contentAlpha = 0.5f) },
        textDecoration = TextDecoration.LineThrough,
    )

    data class Pending(override val eventColors: EventColorsUi) : EventItemStatus(
        cardColors = { containerVariantColors(eventColors) },
        cardBorder = { BorderStroke(2.dp, eventColors.onDatavizContainer) },
    )

    companion object {
        @Composable
        private fun containerVariantColors(eventColors: EventColorsUi): CardColors = CardDefaults.cardColors(
            containerColor = eventColors.datavizContainer,
            contentColor = eventColors.onDatavizContainer,
        )

        @Composable
        private fun containerColors(
            eventColors: EventColorsUi,
            @FloatRange(0.0, 1.0) contentAlpha: Float = 1f,
        ): CardColors = CardDefaults.cardColors(
            containerColor = eventColors.datavizContainerVariant,
            contentColor = eventColors.onDatavizContainerVariant.copyIfNeeded(contentAlpha),
        )

        /**
         * Skips instantiations caused by copy if we're modifying alpha to be the same value as it already was. Useful for all
         * common cases where we set alpha to 1.
         */
        private fun Color.copyIfNeeded(@FloatRange(0.0, 1.0) alpha: Float): Color {
            return if (alpha == this.alpha) this else copy(alpha)
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    @Composable
    fun EventItemForStatus(status: EventItemStatus, modifier: Modifier = Modifier) {
        EventItem(
            start = Clock.System.now(),
            end = Clock.System.now() + 15.minutes,
            isAllDay = false,
            title = "Event title",
            status = status,
            trailingIcons = EventIcons.entries.toSet(),
            modifier = modifier,
        )
    }

    CompositionLocalProvider(LocalIsThemeDarkMode provides isSystemInDarkTheme()) {
        MaterialTheme(if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
            Surface {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
                    val eventColors = LocalEventColorsUiFactory.current.create(0x0)

                    EventItemForStatus(EventItemStatus.Default(eventColors))
                    EventItemForStatus(EventItemStatus.Maybe(eventColors))
                    EventItemForStatus(EventItemStatus.Declined(eventColors))
                    EventItemForStatus(EventItemStatus.Pending(eventColors))
                }
            }
        }
    }
}
