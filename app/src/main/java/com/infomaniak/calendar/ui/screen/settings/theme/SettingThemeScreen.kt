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
package com.infomaniak.calendar.ui.screen.settings.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.model.settings.SettingThemeOption
import com.infomaniak.calendar.ui.screen.settings.components.SettingChoices
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.common.R as RCore

@Composable
fun SettingThemeScreen(
    goBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    settingThemeViewModel: SettingThemeViewModel = viewModel(),
) {
    val theme by settingThemeViewModel.theme.collectAsStateWithLifecycle()

    Content(
        goBack = goBack,
        modifier = modifier,
        theme = { theme },
        onThemeClick = { settingThemeViewModel.onThemeClick(it) },
    )
}

@Composable
private fun Content(
    theme: () -> SettingThemeOption?,
    onThemeClick: (SettingThemeOption) -> Unit,
    goBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    SettingChoices(
        title = stringResource(RCore.string.themeTitle),
        items = SettingThemeOption.entries,
        selectedItem = { theme() },
        itemLabel = { it.title },
        itemSymbol = { it.symbol },
        onItemClick = { onThemeClick(it) },
        goBack = goBack,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        Content(goBack = {}, theme = { SettingThemeOption.System }, onThemeClick = {})
    }
}
