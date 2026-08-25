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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MenuItem(menuOption: MenuOption, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
    ) {
        SegmentedListItem(
            onClick = menuOption.itemAction,
            shapes = ListItemDefaults.segmentedShapes(
                index = menuOption.index,
                count = 3
            ),
            leadingContent = {
                Icon(
                    painter = painterResource(menuOption.itemIcon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            trailingContent = {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                )
            },
            verticalAlignment = Alignment.CenterVertically,
            colors = ListItemDefaults.colors(MaterialTheme.colorScheme.surfaceContainerLowest),
        ) {
            Text(
                text = stringResource(menuOption.itemNameRes),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun MenuItemPreview() {
    CalendarThemeForPreview {
        MenuItem(
            menuOption = MenuOption(
                itemNameRes = R.string.accountsTitle,
                itemIcon = R.drawable.ic_circle_user,
                itemAction = {},
                index = 1,
            ),
        )
    }
}
