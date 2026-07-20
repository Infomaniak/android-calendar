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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalViewConfiguration
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
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Clock

@Composable
internal fun Day(
    date: LocalDate,
    dateState: DateState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalLocale.current.platformLocale
    val fullDate = remember(date, locale) {
        date.toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale))
    }
    val stateDescriptionToday = if (dateState == DateState.Today) stringResource(R.string.contentDescriptionToday) else null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(LocalViewConfiguration.current.minimumTouchTargetSize.height)
            .padding(Margin.Micro),
        contentAlignment = Alignment.Center,
    ) {
        DayCircle(
            state = dateState,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f, matchHeightConstraintsFirst = true)
                .clip(CircleShape)
                .clickable(role = Role.Button) { onClick() }
                .clearAndSetSemantics {
                    contentDescription = fullDate
                    selected = dateState == DateState.Selected
                    stateDescriptionToday?.let { stateDescription = it }
                },
        ) {
            Text(text = date.day.toString())
        }
    }
}

@Composable
@Preview
private fun DayPreview() {
    val today = Clock.today()
    val previewDaySize = 32.dp

    Surface {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(DateState.Selected.name, style = MaterialTheme.typography.labelSmall)
                Day(
                    dateState = DateState.Selected,
                    date = today,
                    onClick = {},
                    modifier = Modifier.size(previewDaySize),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(DateState.Today.name, style = MaterialTheme.typography.labelSmall)
                Day(
                    dateState = DateState.Today,
                    date = today,
                    onClick = {},
                    modifier = Modifier.size(previewDaySize),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(DateState.None.name, style = MaterialTheme.typography.labelSmall)
                Day(
                    dateState = DateState.None,
                    date = today,
                    onClick = {},
                    modifier = Modifier.size(previewDaySize),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(DateState.NotMonth.name, style = MaterialTheme.typography.labelSmall)
                Day(
                    dateState = DateState.NotMonth,
                    date = today,
                    onClick = {},
                    modifier = Modifier.size(previewDaySize),
                )
            }
        }
    }
}
