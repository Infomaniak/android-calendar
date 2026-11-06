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
import com.infomaniak.calendar.data.SettingThemeOption
import com.infomaniak.calendar.ui.screen.settings.components.SettingItemSwitch
import com.infomaniak.calendar.ui.screen.settings.components.SettingScaffold
import com.infomaniak.calendar.ui.screen.settings.components.SettingSection
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.common.R as RCore

@Composable
fun SettingThemeScreen(
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    settingThemeViewModel: SettingThemeViewModel = viewModel(),
) {
    val theme by settingThemeViewModel.theme.collectAsStateWithLifecycle()

    SettingThemeContent(
        goBack = goBack,
        modifier = modifier,
        theme = { theme },
        onThemeClick = { settingThemeViewModel.onThemeClick(it) },
    )
}

@Composable
private fun SettingThemeContent(
    theme: () -> SettingThemeOption?,
    onThemeClick: (SettingThemeOption) -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingScaffold(
        title = stringResource(RCore.string.themeTitle),
        goBack = goBack,
        modifier = modifier,
    ) {
        SettingSection {
            val currentTheme = theme()
            SettingThemeOption.entries.forEachIndexed { index, option ->
                val shapes = ListItemDefaults.segmentedShapes(index, SettingThemeOption.entries.size)
                ThemeItem(shapes, option, currentTheme, onThemeClick)
            }
        }
    }
}

@Composable
private fun ThemeItem(
    shapes: ListItemShapes,
    option: SettingThemeOption,
    currentTheme: SettingThemeOption?,
    onThemeClick: (SettingThemeOption) -> Unit,
) {
    SettingItemSwitch(
        shapes = shapes,
        text = option.title,
        symbol = option.symbol,
        checked = { currentTheme == option },
        onCheckedChange = { onThemeClick(option) },
    )
}

@Preview
@Composable
private fun SettingThemePreview() {
    CalendarThemeForPreview {
        SettingThemeContent(goBack = {}, theme = { SettingThemeOption.System }, onThemeClick = {})
    }
}
