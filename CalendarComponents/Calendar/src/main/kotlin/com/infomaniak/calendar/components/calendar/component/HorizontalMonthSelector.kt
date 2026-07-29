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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.monthDisplayName
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.monthYearLabel
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth
import java.util.Locale
import kotlin.time.Clock

private const val CENTER_INDEX = Int.MAX_VALUE / 2

private fun monthAt(anchorDate: LocalDate, index: Int): LocalDate {
    return anchorDate.plus(index - CENTER_INDEX, DateTimeUnit.MONTH)
}

private fun monthDistance(from: LocalDate, to: LocalDate): Int = from.yearMonth.monthsUntil(to.yearMonth)

@Composable
fun HorizontalMonthSelector(
    selectedDate: () -> LocalDate,
    onMonthSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalLocale.current.platformLocale
    val anchorDate = remember { selectedDate() }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = CENTER_INDEX)

    LaunchedEffect(anchorDate) {
        snapshotFlow { selectedDate() }
            .drop(1)
            .distinctUntilChanged()
            .collectLatest { selectedDate ->
                val target = CENTER_INDEX + monthDistance(anchorDate, selectedDate)
                val layoutInfo = listState.layoutInfo
                val targetItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == target }

                val isFullyVisible = targetItem != null &&
                        targetItem.offset >= layoutInfo.viewportStartOffset &&
                        targetItem.offset + targetItem.size <= layoutInfo.viewportEndOffset

                if (!isFullyVisible) {
                    listState.animateScrollToItem(target)
                }
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
            val isSelected = month.yearMonth == selectedDate().yearMonth

            if (month.month.number == 1) {
                YearSeparator(month.year)
            }

            MonthButton(
                month = month,
                isSelected = isSelected,
                locale = locale,
                onMonthClick = { onMonthSelected(month) },
                modifier = Modifier.padding(horizontal = Margin.Medium, vertical = Margin.Large),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MonthButton(
    month: LocalDate,
    isSelected: Boolean,
    locale: Locale,
    onMonthClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthName = month.monthDisplayName(locale)

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

    TextButton(
        onClick = onMonthClick,
        shape = RoundedCornerShape(50),
        modifier = modifier
            .clearAndSetSemantics {
                contentDescription = month.monthYearLabel(locale)
                selected = isSelected
            },
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
        ),
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
private fun YearSeparator(year: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
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
