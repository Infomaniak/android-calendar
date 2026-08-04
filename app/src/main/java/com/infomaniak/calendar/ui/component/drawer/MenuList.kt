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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.designsystem.core.theme.EsdsTheme

@Composable
fun MenuList(
    menuOptions: List<MenuOption>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = Margin.Medium, vertical = Margin.Mini)
            .clip(shape = EsdsTheme.radius.twoXl)
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = Margin.Mini, horizontal = Margin.Micro),
    ) {
        menuOptions.forEach { menuOption ->
            MenuItem(menuOption = menuOption)
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun MenuListPreview() {
    CalendarThemeForPreview {
        MenuList(
            menuOptions = listOf(
                MenuOption(R.string.accountManagement, R.drawable.ic_circle_user, {}),
                MenuOption(R.string.settingsTitle, R.drawable.ic_cog, {}),
                MenuOption(R.string.helpTitle, R.drawable.ic_headset, {}),
            ),
        )
    }
}

data class MenuOption(
    @StringRes val itemNameRes: Int,
    @DrawableRes val itemIcon: Int,
    val itemAction: () -> Unit,
)
