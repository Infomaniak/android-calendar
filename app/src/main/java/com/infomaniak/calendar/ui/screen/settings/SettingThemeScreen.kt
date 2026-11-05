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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.data.SettingThemeOption
import com.infomaniak.calendar.ui.screen.settings.components.SettingItemSwitch
import com.infomaniak.calendar.ui.screen.settings.components.SettingSection
import com.infomaniak.calendar.ui.screen.settings.components.SettingShape
import com.infomaniak.calendar.ui.screen.settings.components.SettingScaffold
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Moon
import com.infomaniak.designsystem.icons.symbols.Smartphone
import com.infomaniak.designsystem.icons.symbols.Sun
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
            val theme = theme()
            SettingItemSwitch(
                shapes = SettingShape.First,
                text = stringResource(RCore.string.themeSystem),
                symbol = EsdsIcons.Smartphone,
                checked = { theme == SettingThemeOption.System },
                onCheckedChange = { onThemeClick(SettingThemeOption.System) },
            )
            SettingItemSwitch(
                shapes = SettingShape.Middle,
                text = stringResource(RCore.string.themeLight),
                symbol = EsdsIcons.Sun,
                checked = { theme == SettingThemeOption.Light },
                onCheckedChange = { onThemeClick(SettingThemeOption.Light) },
            )
            SettingItemSwitch(
                shapes = SettingShape.Last,
                text = stringResource(RCore.string.themeDark),
                symbol = EsdsIcons.Moon,
                checked = { theme == SettingThemeOption.Dark },
                onCheckedChange = { onThemeClick(SettingThemeOption.Dark) },
            )
        }
    }
}

@Preview
@Composable
private fun SettingThemePreview() {
    CalendarThemeForPreview {
        SettingThemeContent(goBack = {}, theme = { SettingThemeOption.System }, onThemeClick = {})
    }
}
