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
package com.infomaniak.calendar.data

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.infomaniak.core.datavalue.DataValues
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Moon
import com.infomaniak.designsystem.icons.symbols.Smartphone
import com.infomaniak.designsystem.icons.symbols.Sun
import com.infomaniak.designsystem.icons.symbols.Symbol
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Duration.Companion.hours
import com.infomaniak.core.common.R as RCore

@SingleIn(AppScope::class)
class SettingsDataValues @Inject constructor(appContext: Context) : DataValues(appContext, name = "settingsData") {
    val theme = dataValue(key = "theme", defaultValue = SettingThemeOption.System)
    val defaultEventDuration = dataValue(key = "defaultEventDuration", defaultValue = 1.hours)
}

enum class SettingThemeOption(@StringRes private val titleRes: Int, val symbol: Symbol) {
    System(RCore.string.themeSystem, EsdsIcons.Smartphone),
    Light(RCore.string.themeLight, EsdsIcons.Sun),
    Dark(RCore.string.themeDark, EsdsIcons.Moon);

    val title: String @Composable get() = stringResource(titleRes)
}
