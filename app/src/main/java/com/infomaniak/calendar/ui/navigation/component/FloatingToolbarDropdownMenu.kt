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
package com.infomaniak.calendar.ui.navigation.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorPosition
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.core.ui.compose.margin.Margin

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingToolbarDropdownMenu(
    isExpanded: Boolean,
    onMenuExpanded: (Boolean) -> Unit,
    onNavigationButtonClicked: (NavDestination.CalendarView) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenuPopup(
        expanded = isExpanded,
        onDismissRequest = { onMenuExpanded(false) },
        popupPositionProvider = MenuDefaults.rememberDropdownMenuPopupPositionProvider(
            dropdownMenuAnchorPosition = MenuAnchorPosition.Above,
        ),
        modifier = modifier.padding(bottom = Margin.Medium),
        properties = PopupProperties(clippingEnabled = false, focusable = true),
    ) {
        DropdownMenuGroup(shapes = MenuDefaults.groupShape(index = 0, count = 1)) {
            DateSelectionItems.entries.forEachIndexed { index, item ->
                DropdownMenuItem(
                    shape = MenuDefaults.itemShape(index, DateSelectionItems.entries.count()).shape,
                    text = { Text(stringResource(item.labelRessourceId)) },
                    leadingIcon = { Icon(painter = painterResource(item.icon), contentDescription = null) },
                    onClick = {
                        onMenuExpanded(false)
                        onNavigationButtonClicked(item.destination)
                    },
                    modifier = Modifier.widthIn(min = 180.dp),
                )
            }
        }
    }
}

private enum class DateSelectionItems(
    @param:StringRes val labelRessourceId: Int,
    @DrawableRes val icon: Int,
    val destination: NavDestination.CalendarView,
) {
    Day(
        labelRessourceId = R.string.dayTitle,
        icon = R.drawable.ic_rows_two,
        destination = NavDestination.CalendarView.Day,
    ),
    ThreeDays(
        labelRessourceId = R.string.threeDaysTitle,
        icon = R.drawable.ic_columns_three,
        destination = NavDestination.CalendarView.ThreeDays,
    ),
    Week(
        labelRessourceId = R.string.weekTitle,
        icon = R.drawable.ic_columns_four,
        destination = NavDestination.CalendarView.Week,
    ),
    Month(
        labelRessourceId = R.string.monthTitle,
        icon = R.drawable.ic_grid_three_two,
        destination = NavDestination.CalendarView.Month,
    ),
    Planning(
        labelRessourceId = R.string.planningTitle,
        icon = R.drawable.ic_overline_rectangle_underline,
        destination = NavDestination.CalendarView.Planning,
    )
}
