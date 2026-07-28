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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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

@Composable
fun HorizontalMonthSelector(
    selectedDate: () -> LocalDate,
    onMonthSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalLocale.current.platformLocale
    val listState = rememberLazyListState()

    val months = remember {
        val selected = selectedDate()
        (-12..12).map { offset ->
            selected.plus(offset, DateTimeUnit.MONTH)
        }
    }

    LaunchedEffect(selectedDate()) {
        snapshotFlow { selectedDate() }.collectLatest { newDate ->
            val currentMonthIndex = months.indexOfFirst {
                it.year == newDate.year && it.month == newDate.month
            }
            if (currentMonthIndex >= 0) {
                listState.animateScrollToItem(currentMonthIndex)
            }
        }
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = Margin.Large),
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val itemsToDisplay = buildList {
            for (i in months.indices) {
                val month = months[i]
                if (i > 0 && months[i - 1].year != month.year) {
                    add(MonthDisplayItem.YearSeparator(month.year))
                }
                add(MonthDisplayItem.MonthItem(month))
            }
        }

        items(itemsToDisplay.size) { index ->
            when (val item = itemsToDisplay[index]) {
                is MonthDisplayItem.MonthItem -> {
                    val month = item.date
                    val isSelected = month.year == selectedDate().year && month.month == selectedDate().month

                    MonthButton(
                        month = month,
                        isSelected = isSelected,
                        locale = locale,
                        onMonthClick = { onMonthSelected(month) },
                    )
                }
                is MonthDisplayItem.YearSeparator -> {
                    YearSeparator(item.year)
                }
            }
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
            .padding(horizontal = Margin.Medium, vertical = Margin.Mini),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
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

private sealed class MonthDisplayItem {
    data class MonthItem(val date: LocalDate) : MonthDisplayItem()
    data class YearSeparator(val year: Int) : MonthDisplayItem()
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
