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
package com.infomaniak.calendar.ui.screen.settings.firstDayOfWeek

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatFullDayName
import com.infomaniak.calendar.ui.screen.settings.components.SettingChoices
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import kotlinx.datetime.DayOfWeek

@Composable
fun SettingFirstDayOfWeekScreen(
    goBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    firstDayOfWeekViewModel: SettingFirstDayOfWeekViewModel = viewModel(),
) {
    val firstDayOfWeek by firstDayOfWeekViewModel.firstDayOfWeek.collectAsStateWithLifecycle()

    Content(
        firstDayOfWeek = firstDayOfWeek,
        goBack = goBack,
        onItemClick = { firstDayOfWeekViewModel.setFirstDayOfWeekClick(it) },
        modifier = modifier,
    )
}

@Composable
private fun Content(
    firstDayOfWeek: DayOfWeek?,
    goBack: (() -> Unit)?,
    onItemClick: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingChoices(
        title = stringResource(R.string.generalSettingsStartOfWeekLabel),
        items = DayOfWeek.entries,
        selectedItem = { firstDayOfWeek },
        itemLabel = { it.formatFullDayName() },
        onItemClick = {
            onItemClick(it)
            goBack?.invoke()
        },
        goBack = goBack,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        Content(
            firstDayOfWeek = DayOfWeek.MONDAY,
            goBack = {},
            onItemClick = {},
        )
    }
}
