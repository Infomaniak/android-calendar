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
package com.infomaniak.calendar.ui.component.topAppBar

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons.BackButton
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons.DrawerIconButton
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons.InboxButton
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons.SearchButton
import com.infomaniak.calendar.ui.navigation.state.LocalDrawerState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import kotlinx.coroutines.launch

object TopAppBarButtons {
    @Composable
    fun InboxButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(R.drawable.ic_inbox),
                contentDescription = stringResource(R.string.contentDescriptionInbox),
                modifier = modifier,
            )
        }
    }

    @Composable
    fun SearchButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(R.drawable.ic_magnifying_glass),
                contentDescription = stringResource(R.string.contentDescriptionSearch),
                modifier = modifier,
            )
        }
    }

    @Composable
    fun DrawerIconButton(modifier: Modifier = Modifier) {
        val scope = rememberCoroutineScope()
        val calendarDrawerState = LocalDrawerState.current

        IconButton(
            onClick = { scope.launch { calendarDrawerState?.open() } },
            modifier = modifier,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_list),
                contentDescription = stringResource(R.string.contentDescriptionMenuDrawer),
            )
        }
    }

    @Composable
    fun BackButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
        IconButton(onClick = onClick, modifier = modifier) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.contentDescriptionBack),
            )
        }
    }
}

@Preview
@Composable
private fun SearchButtonPreview() {
    CalendarThemeForPreview {
        SearchButton(onClick = {})
    }
}

@Preview
@Composable
private fun InboxButtonPreview() {
    CalendarThemeForPreview {
        InboxButton(onClick = {})
    }
}

@Preview
@Composable
private fun DrawerIconButtonPreview() {
    CalendarThemeForPreview {
        DrawerIconButton()
    }
}

@Preview
@Composable
private fun BackButtonPreview() {
    CalendarThemeForPreview {
        BackButton(onClick = {})
    }
}
