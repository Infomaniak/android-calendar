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
package com.infomaniak.calendar.ui.component.topAppBar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

private const val DURATION_TWEEN = 150

@Composable
fun AnimatedMonthYearText(date: LocalDate, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val currentYear = rememberCurrentYear()

    AnimatedContent(
        modifier = modifier,
        targetState = date,
        contentKey = { it.year to it.month },
        transitionSpec = { verticalRoll() },
        label = "MonthYearAnimation",
    ) { targetDate ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = targetDate.monthYearLabel(locale, currentYear))
            Icon(painter = painterResource(R.drawable.ic_chevron), contentDescription = null)
        }
    }
}

@Composable
private fun rememberCurrentYear(): Int {
    return remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).year }
}

private fun <T : Comparable<T>> AnimatedContentTransitionScope<T>.verticalRoll(): ContentTransform {
    val direction = if (targetState > initialState) 1 else -1

    return (
            slideInVertically(animationSpec = tween(DURATION_TWEEN)) { height -> direction * height } + fadeIn(
                animationSpec = tween(DURATION_TWEEN),
            )
            )
        .togetherWith(
            slideOutVertically(animationSpec = tween(DURATION_TWEEN)) { height -> -direction * height } + fadeOut(
                animationSpec = tween(DURATION_TWEEN),
            ),
        )
}

@Preview
@Composable
private fun AnimatedMonthYearTextPreview() {
    CalendarThemeForPreview {
        AnimatedMonthYearText(date = LocalDate(2026, 7, 8))
    }
}

@Preview
@Composable
private fun AnimatedMonthYearTextPastYearPreview() {
    CalendarThemeForPreview {
        AnimatedMonthYearText(date = LocalDate(2025, 12, 25))
    }
}
