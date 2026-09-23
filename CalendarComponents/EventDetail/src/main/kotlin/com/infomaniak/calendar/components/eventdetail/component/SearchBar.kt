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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.ui.compose.margin.Margin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchQuery: () -> String,
    onSearchQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchBarDefaults.InputField(
        query = searchQuery(),
        onQueryChange = onSearchQueryChanged,
        onSearch = {},
        expanded = false,
        onExpandedChange = {},
        placeholder = { Text(stringResource(R.string.searchForAttendees)) },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_magnifying_glass),
                contentDescription = stringResource(R.string.contentDescriptionSearch),
            )
        },
        modifier = modifier
            .padding(horizontal = Margin.Small, vertical = Margin.Small)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.extraLargeIncreased,
            )
            .fillMaxWidth(),
    )
}

@Preview
@Composable
private fun SearchBarPreview(){
    SearchBar(
        searchQuery = { "" },
        onSearchQueryChanged = {},
    )
}
