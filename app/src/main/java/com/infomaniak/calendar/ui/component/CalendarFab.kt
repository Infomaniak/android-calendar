/*
 * Infomaniak Authenticator - Android
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
package com.infomaniak.calendar.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.ui.theme.CalendarTheme

@Composable
fun CalendarFab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        elevation = FloatingActionButtonDefaults.elevation(0.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add",
        )
    }
}

@Preview
@Composable
private fun CalendarFabPreview() {
    CalendarTheme {
        CalendarFab(onClick = {})
    }
}
