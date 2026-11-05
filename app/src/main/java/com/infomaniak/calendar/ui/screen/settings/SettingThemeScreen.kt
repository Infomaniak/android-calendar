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

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.common.R as RCore

@Composable
fun SettingThemeScreen(
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    settingThemeViewModel: SettingThemeViewModel = viewModel(),
) {
    SettingThemeContent(onBack = goBack, modifier = modifier)
}

@Composable
private fun SettingThemeContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(RCore.string.themeTitle)) },
                navigationIcon = { TopAppBarButtons.BackButton(onClick = onBack) },
            )
        },
        modifier = modifier,
    ) { _ -> }
}

@Preview
@Composable
private fun SettingThemePreview() {
    CalendarThemeForPreview {
        SettingThemeContent(onBack = {})
    }
}
