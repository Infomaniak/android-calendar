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
package com.infomaniak.calendar.components.eventdetail.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.resources.R

@Composable
internal fun OccupiedStatus(isOccupied: Boolean, modifier: Modifier = Modifier) {
    ListItem(
        content = { Text(text = stringResource(if (isOccupied) R.string.occupiedLabel else R.string.availableLabel)) },
        leadingContent = { OccupiedIcon() },
        modifier = modifier,
    )
}

@Composable
internal fun OccupiedStatusSwitch(
    checked: () -> Boolean,
    onCheckedChange: (isChecked: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    shapes: ListItemShapes = ClickableItemDefaults.ClickableItemShapes,
) {
    ListItemSwitch(checked, onCheckedChange, modifier, shapes, leadingContent = { OccupiedIcon() }) {
        Text(text = stringResource(R.string.occupiedLabel))
    }
}

@Composable
private fun OccupiedIcon() {
    ItemIcon(painterResource(R.drawable.ic_briefcase))
}

@Preview
@Composable
private fun PreviewOccupiedStatus() {
    MaterialTheme {
        Surface {
            Column {
                OccupiedStatus(isOccupied = true)
                OccupiedStatus(isOccupied = false)
            }
        }
    }
}

@Preview
@Composable
private fun PreviewSwitch() {
    MaterialTheme {
        Surface {
            Column {
                OccupiedStatusSwitch(checked = { true }, onCheckedChange = {})
                OccupiedStatusSwitch(checked = { false }, onCheckedChange = {})
            }
        }
    }
}
