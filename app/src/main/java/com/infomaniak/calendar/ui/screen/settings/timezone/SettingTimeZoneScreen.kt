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
package com.infomaniak.calendar.ui.screen.settings.timezone

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.model.settings.SettingTimeZone
import com.infomaniak.calendar.ui.component.TimeZoneSearchScreenContent
import com.infomaniak.calendar.ui.theme.CalendarTheme
import kotlinx.coroutines.launch

@Composable
fun SettingTimeZoneScreen(
    goBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: SettingTimeZoneViewModel = viewModel(),
) {
    val scope = rememberCoroutineScope()

    TimeZoneSearchScreenContent(
        onTimeZoneSelected = { timeZone ->
            scope.launch {
                viewModel.setTimeZoneSuspend(SettingTimeZone.Custom(timeZone.id))
                goBack?.invoke()
            }
        },
        goBack = goBack,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    CalendarTheme {
        Surface {
            SettingTimeZoneScreen(goBack = {})
        }
    }
}
