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
package com.infomaniak.calendar.ui.screen.settings.components

import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatFullDayName
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.designsystem.icons.symbols.Symbol
import kotlinx.datetime.DayOfWeek

@Composable
fun <T> SettingChoices(
    title: String,
    items: Collection<T>,
    selectedItem: () -> T?,
    itemLabel: @Composable (T) -> String,
    onItemClick: (T) -> Unit,
    goBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    itemSymbol: @Composable ((T) -> Symbol)? = null,
) {
    SettingScaffold(title, goBack, modifier) {
        SettingSection(title = subtitle) {
            val currentItem = selectedItem()
            items.forEachIndexed { index, option ->
                val shapes = ListItemDefaults.segmentedShapes(index, items.size)
                Item(shapes, option, currentItem, onItemClick, itemLabel, itemSymbol)
            }
        }
    }
}

@Composable
private fun <T> Item(
    shapes: ListItemShapes,
    option: T,
    currentItem: T?,
    onItemClick: (T) -> Unit,
    itemLabel: @Composable (T) -> String,
    itemSymbol: @Composable ((T) -> Symbol)?,
) {
    SettingItemSwitch(
        shapes = shapes,
        text = itemLabel(option),
        symbol = itemSymbol?.invoke(option),
        checked = { currentItem == option },
        onCheckedChange = { onItemClick(option) },
    )
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        SettingChoices(
            items = DayOfWeek.entries,
            selectedItem = { DayOfWeek.MONDAY },
            itemLabel = { it.formatFullDayName() },
            onItemClick = {},
            goBack = {},
            title = stringResource(R.string.generalSettingsStartOfWeekLabel),
        )
    }
}
