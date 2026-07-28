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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.utils.firstDayOfMonth
import com.infomaniak.calendar.components.foundation.utils.getMonthYearLabel
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.monthYearLabel
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.plus
import java.time.Month
import java.time.format.TextStyle
import kotlin.time.Clock

private const val CENTER_INDEX = Int.MAX_VALUE / 2

private fun monthAt(anchorDate: LocalDate, index: Int): LocalDate =
    anchorDate.plus(index - CENTER_INDEX, DateTimeUnit.MONTH)

private fun monthDistance(from: LocalDate, to: LocalDate): Int =
    (to.year - from.year) * 12 + (to.month.number - from.month.number)

@Composable
internal fun HorizontalMonthSelector(
    selectedDate: () -> LocalDate,
    onMonthSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalLocale.current.platformLocale
    val anchorDate = remember { selectedDate() }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = CENTER_INDEX)

    LaunchedEffect(anchorDate) {
        snapshotFlow { selectedDate() }.collectLatest { date ->
            val target = CENTER_INDEX + monthDistance(anchorDate, date)
            listState.animateScrollToItem(target.coerceIn(0, Int.MAX_VALUE - 1))
        }
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = Margin.Large),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        items(count = Int.MAX_VALUE) { index ->
            val month = monthAt(anchorDate, index)
            val isSelected = month.year == selectedDate().year && month.month == selectedDate().month

            if (month.month.number == 1) {
                YearSeparator(month.year)
            }

            MonthButton(
                month = month,
                isSelected = isSelected,
                locale = locale,
                onMonthClick = { onMonthSelected(month) },
            )
        }
    }
}

@Composable
private fun MonthButton(
    month: LocalDate,
    isSelected: Boolean,
    locale: java.util.Locale,
    onMonthClick: () -> Unit,
) {
    val monthName = Month.of(month.month.number).getDisplayName(TextStyle.SHORT, locale)

    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .padding(horizontal = Margin.Mini, vertical = Margin.Medium)
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Button,
            ) { onMonthClick() }
            .clearAndSetSemantics {
                contentDescription = month.getMonthYearLabel(locale)
                selected = isSelected
            }
            .padding(horizontal = Margin.Medium, vertical = Margin.Mini),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = monthName,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            color = contentColor,
        )
    }
}

@Composable
private fun YearSeparator(year: Int) {
    Box(
        modifier = Modifier
            .padding(horizontal = Margin.Small, vertical = Margin.Micro),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
@Preview
private fun HorizontalMonthSelectorPreview() {
    Surface {
        HorizontalMonthSelector(
            selectedDate = { Clock.today() },
            onMonthSelected = {},
        )
    }
}
