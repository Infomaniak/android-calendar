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
package com.infomaniak.calendar.ui.screen.settings

import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatFullDayName
import com.infomaniak.calendar.model.settings.SettingThemeOption
import com.infomaniak.calendar.ui.screen.settings.components.SettingItemSwitch
import com.infomaniak.calendar.ui.screen.settings.components.SettingScaffold
import com.infomaniak.calendar.ui.screen.settings.components.SettingSection
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import kotlinx.datetime.DayOfWeek
import com.infomaniak.core.common.R as RCore

@Composable
fun SettingFirstDayOfWeekScreen(
    goBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    firstDayOfWeekViewModel: FirstDayOfWeekViewModel = viewModel(),
) {
    val firstDayOfWeek by firstDayOfWeekViewModel.firstDayOfWeek.collectAsStateWithLifecycle()

    Content(
        goBack = goBack,
        modifier = modifier,
        firstDayOfWeek = { firstDayOfWeek },
        onFirstDayOfWeekClick = { firstDayOfWeekViewModel.onFirstDayOfWeekClick(it) },
    )
}

@Composable
private fun Content(
    firstDayOfWeek: () -> DayOfWeek?,
    onFirstDayOfWeekClick: (DayOfWeek) -> Unit,
    goBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    SettingScaffold(
        title = stringResource(R.string.generalSettingsStartOfWeekLabel),
        goBack = goBack,
        modifier = modifier,
    ) {
        SettingSection {
            val currentFirstDayOfWeek = firstDayOfWeek()
            DayOfWeek.entries.forEachIndexed { index, option ->
                val shapes = ListItemDefaults.segmentedShapes(index, DayOfWeek.entries.size)
                FirstDayOfWeekItem(shapes, option, currentFirstDayOfWeek, onFirstDayOfWeekClick)
            }
        }
    }
}

@Composable
private fun FirstDayOfWeekItem(
    shapes: ListItemShapes,
    option: DayOfWeek,
    currentFirstDayOfWeek: DayOfWeek?,
    onFirstDayOfWeekClick: (DayOfWeek) -> Unit,
) {
    SettingItemSwitch(
        shapes = shapes,
        text = option.formatFullDayName(),
        checked = { currentFirstDayOfWeek == option },
        onCheckedChange = { onFirstDayOfWeekClick(option) },
    )
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        Content(goBack = {}, firstDayOfWeek = { DayOfWeek.MONDAY }, onFirstDayOfWeekClick = {})
    }
}
