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

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatCityAndCountry
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDuration
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatFullDayName
import com.infomaniak.calendar.model.settings.SettingThemeOption
import com.infomaniak.calendar.model.settings.SettingTimeZone
import com.infomaniak.calendar.model.settings.toTimeZone
import com.infomaniak.calendar.ui.screen.settings.components.SettingItemClickable
import com.infomaniak.calendar.ui.screen.settings.components.SettingItemOpen
import com.infomaniak.calendar.ui.screen.settings.components.SettingItemSwitch
import com.infomaniak.calendar.ui.screen.settings.components.SettingScaffold
import com.infomaniak.calendar.ui.screen.settings.components.SettingSection
import com.infomaniak.calendar.ui.screen.settings.components.SettingShape
import com.infomaniak.calendar.ui.screen.settings.components.SwitchType
import com.infomaniak.calendar.ui.screen.settings.defaultEventDuration.SettingDefaultEventDurationViewModel
import com.infomaniak.calendar.ui.screen.settings.firstDayOfWeek.SettingFirstDayOfWeekViewModel
import com.infomaniak.calendar.ui.screen.settings.theme.SettingThemeViewModel
import com.infomaniak.calendar.ui.screen.settings.timezone.SettingTimeZoneViewModel
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.common.extensions.openUrl
import com.infomaniak.core.network.SUPPORT_URL
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Bell
import com.infomaniak.designsystem.icons.symbols.BubbleTopRightCircle
import com.infomaniak.designsystem.icons.symbols.CalendarDots
import com.infomaniak.designsystem.icons.symbols.Globe
import com.infomaniak.designsystem.icons.symbols.Headset
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import com.infomaniak.calendar.components.resources.R as RComponents
import com.infomaniak.core.common.R as RCore

@Composable
fun SettingsScreen(
    goBack: () -> Unit,
    goToTheme: () -> Unit,
    goToFirstDayOfWeek: () -> Unit,
    goToDefaultEventDuration: () -> Unit,
    goToTimeZone: () -> Unit,
    modifier: Modifier = Modifier,
    settingThemeViewModel: SettingThemeViewModel = viewModel(),
    firstDayOfWeekViewModel: SettingFirstDayOfWeekViewModel = viewModel(),
    defaultEventDurationViewModel: SettingDefaultEventDurationViewModel = viewModel(),
    settingTimeZoneViewModel: SettingTimeZoneViewModel = viewModel(),
) {
    val theme by settingThemeViewModel.theme.collectAsStateWithLifecycle()
    val firstDayOfWeek by firstDayOfWeekViewModel.firstDayOfWeek.collectAsStateWithLifecycle()
    val defaultEventDuration by defaultEventDurationViewModel.defaultEventDuration.collectAsStateWithLifecycle()
    val timeZone by settingTimeZoneViewModel.timeZone.collectAsStateWithLifecycle()

    SettingsScreen(
        theme = { theme },
        firstDayOfWeek = { firstDayOfWeek },
        defaultEventDuration = { defaultEventDuration },
        timeZone = { timeZone },
        goBack = goBack,
        goToTheme = goToTheme,
        goToFirstDayOfWeek = goToFirstDayOfWeek,
        goToDefaultEventDuration = goToDefaultEventDuration,
        onTimeZoneChange = settingTimeZoneViewModel::setTimeZone,
        goToTimeZone = goToTimeZone,
        modifier = modifier,
    )
}

@Composable
private fun SettingsScreen(
    theme: () -> SettingThemeOption?,
    firstDayOfWeek: () -> DayOfWeek?,
    defaultEventDuration: () -> Duration?,
    timeZone: () -> SettingTimeZone?,
    goBack: () -> Unit,
    goToTheme: () -> Unit,
    goToFirstDayOfWeek: () -> Unit,
    goToDefaultEventDuration: () -> Unit,
    onTimeZoneChange: (SettingTimeZone) -> Unit,
    goToTimeZone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    SettingScaffold(stringResource(R.string.settingsTitle), goBack, modifier) {
        SettingSection(title = stringResource(R.string.generalSettingsDisplayLabel)) {
            SettingItemClickable(
                shapes = SettingShape.First,
                text = stringResource(RCore.string.themeTitle),
                onClick = goToTheme,
                symbol = theme()?.symbol,
                supportingText = theme()?.title,
            )
            SettingItemClickable(
                shapes = SettingShape.Last,
                text = stringResource(R.string.generalSettingsStartOfWeekLabel),
                onClick = goToFirstDayOfWeek,
                symbol = EsdsIcons.CalendarDots,
                supportingText = firstDayOfWeek()?.formatFullDayName(),
            )
        }

        SettingSection(title = stringResource(R.string.generalSettingsEventLabel)) {
            SettingItemClickable(
                shapes = SettingShape.First,
                text = stringResource(R.string.generalSettingsDefaultEventDurationLabel),
                onClick = goToDefaultEventDuration,
                symbol = EsdsIcons.BubbleTopRightCircle,
                supportingText = defaultEventDuration()?.formatDuration(),
            )

            // This is the only place where we can actual reference the device default time zone instead of the setting one.
            // TODO[timeZone]: Observe device time zone updates to notify consumers so they can update data that depend on it.
            val deviceTimeZone = remember { mutableStateOf(TimeZone.currentSystemDefault()) }
            SettingItemSwitch(
                shapes = SettingShape.Middle,
                text = stringResource(R.string.generalSettingsUseDeviceTimeZoneLabel),
                checked = { timeZone() == SettingTimeZone.Automatic },
                onCheckedChange = { isAutomatic ->
                    onTimeZoneChange(
                        if (isAutomatic) SettingTimeZone.Automatic else SettingTimeZone.Custom(deviceTimeZone.value.id),
                    )
                },
                symbol = EsdsIcons.Globe,
                switchType = SwitchType.Switch,
            )
            SettingItemClickable(
                shapes = SettingShape.Last,
                text = stringResource(RComponents.string.timeZoneLabel),
                onClick = goToTimeZone,
                supportingText = timeZone()?.toTimeZone()?.formatCityAndCountry(),
                enabled = { timeZone() is SettingTimeZone.Custom },
            )
        }

        SettingSection(title = stringResource(R.string.generalSettingsAuthorizationsLabel)) {
            SettingItemOpen(
                shapes = SettingShape.Single,
                text = stringResource(R.string.generalSettingsNotificationsLabel),
                onClick = { goToSystemNotificationScreen(context) },
                symbol = EsdsIcons.Bell,
            )
        }

        SettingSection(title = stringResource(R.string.generalSettingsHelpAndPrivacyLabel)) {
            SettingItemOpen(
                shapes = SettingShape.Single,
                text = stringResource(R.string.generalSettingsContactSupportLabel),
                onClick = { context.openUrl(SUPPORT_URL) },
                symbol = EsdsIcons.Headset,
            )
        }
    }
}

private fun goToSystemNotificationScreen(context: Context) {
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .also(context::startActivity)
}

@Preview
@Composable
private fun SettingScreenPreview() {
    CalendarThemeForPreview {
        SettingsScreen(
            theme = { SettingThemeOption.System },
            firstDayOfWeek = { DayOfWeek.MONDAY },
            defaultEventDuration = { 1.hours },
            timeZone = { SettingTimeZone.Custom("Europe/Zurich") },
            goBack = {},
            goToTheme = {},
            goToFirstDayOfWeek = {},
            goToDefaultEventDuration = {},
            onTimeZoneChange = {},
            goToTimeZone = {},
        )
    }
}
