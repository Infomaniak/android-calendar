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
package com.infomaniak.calendar.ui.component.drawer

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.CircleUser
import com.infomaniak.designsystem.icons.symbols.Cog
import com.infomaniak.designsystem.icons.symbols.Symbol

@Composable
fun MenuList(menuOptions: List<MenuOption>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
        menuOptions.forEachIndexed { index, menuOption ->
            MenuItem(menuOption = menuOption, index = index, count = menuOptions.size)
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun MenuListPreview() {
    CalendarThemeForPreview {
        MenuList(
            menuOptions = listOf(
                MenuOption(R.string.accountsTitle, EsdsIcons.CircleUser, {}),
                MenuOption(R.string.settingsTitle, EsdsIcons.Cog, {}),
            ),
        )
    }
}

data class MenuOption(
    @StringRes val itemNameRes: Int,
    val itemIcon: Symbol,
    val itemAction: () -> Unit,
)
