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
package com.infomaniak.calendar.ui.screen.settings.defaultEventDuration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDuration
import com.infomaniak.calendar.ui.screen.settings.components.SettingChoices
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun SettingDefaultEventDurationScreen(
    goBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    settingDefaultEventDurationViewModel: SettingDefaultEventDurationViewModel = viewModel(),
) {
    val defaultEventDuration by settingDefaultEventDurationViewModel.defaultEventDuration.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    SettingDefaultEventDurationScreen(
        defaultEventDuration = defaultEventDuration,
        goBack = goBack,
        onItemClick = {
            scope.launch {
                settingDefaultEventDurationViewModel.setDefaultEventDuration(it.duration)
                goBack?.invoke()
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun SettingDefaultEventDurationScreen(
    defaultEventDuration: Duration?,
    goBack: (() -> Unit)?,
    onItemClick: (EventDuration) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingChoices(
        title = stringResource(R.string.generalSettingsDefaultEventDurationLabel),
        items = EventDuration.entries,
        selectedItem = { EventDuration.entries.find { it.duration == defaultEventDuration } },
        itemLabel = { it.duration.formatDuration() },
        onItemClick = {
            onItemClick(it)
            goBack?.invoke()
        },
        goBack = goBack,
        modifier = modifier,
    )
}

private enum class EventDuration(val duration: Duration) {
    Minutes15(15.minutes),
    Minutes30(30.minutes),
    Minutes45(45.minutes),
    Hour1(1.hours),
    Hour1Minutes15(1.hours + 15.minutes),
    Hour1Minutes30(1.hours + 30.minutes),
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        SettingDefaultEventDurationScreen(
            defaultEventDuration = 15.minutes,
            goBack = {},
            onItemClick = {},
        )
    }
}
