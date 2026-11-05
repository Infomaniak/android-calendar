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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.CircleUser
import com.infomaniak.designsystem.icons.symbols.Cog
import com.infomaniak.designsystem.icons.symbols.Headset
import com.infomaniak.designsystem.icons.symbols.Symbol

fun LazyListScope.menuListItems(menuOptions: List<MenuOption>) {
    menuOptions.forEachIndexed { index, menuOption ->
        item(key = menuOption.itemNameRes, contentType = "menuOption") {
            MenuItem(
                menuOption = menuOption,
                index = index,
                count = menuOptions.size,
                modifier = Modifier
                    .animateItem()
                    .padding(horizontal = Margin.Medium)
                    .padding(
                        top = if (index == 0) Margin.Huge else ListItemDefaults.SegmentedGap,
                        bottom = if (index == menuOptions.lastIndex) Margin.Large else 0.dp,
                    ),
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun MenuListPreview() {
    CalendarThemeForPreview {
        LazyColumn {
            menuListItems(
                menuOptions = listOf(
                    MenuOption(R.string.accountsTitle, EsdsIcons.CircleUser, {}),
                    MenuOption(R.string.settingsTitle, EsdsIcons.Cog, {}),
                    MenuOption(R.string.helpTitle, EsdsIcons.Headset, {}),
                ),
            )
        }
    }
}

data class MenuOption(
    @StringRes val itemNameRes: Int,
    val itemIcon: Symbol,
    val itemAction: () -> Unit,
)
