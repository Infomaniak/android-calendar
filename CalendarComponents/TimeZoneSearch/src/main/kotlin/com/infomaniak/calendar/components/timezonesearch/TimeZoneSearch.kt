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
package com.infomaniak.calendar.components.timezonesearch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.component.EmptyState
import com.infomaniak.calendar.components.foundation.component.SearchBar
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.calendar.components.timezonesearch.component.TimeZoneItem
import com.infomaniak.calendar.components.timezonesearch.models.TimeZoneUi
import com.infomaniak.calendar.components.timezonesearch.models.availableTimeZones
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone

@Composable
fun TimeZoneSearch(onTimeZoneClick: (TimeZone) -> Unit, modifier: Modifier = Modifier) {
    val locale = LocalLocale.current.platformLocale
    val searchQuery = rememberTextFieldState()

    val searchResults by produceState<List<TimeZoneUi>?>(initialValue = null, locale) {
        val timeZones = withContext(Dispatchers.Default) { availableTimeZones() }
        snapshotFlow { searchQuery.text.toString().trim() }.collectLatest { query ->
            value = timeZones.filter { it.matches(query) }
        }
    }

    Column(modifier = modifier) {
        SearchBar(searchState = searchQuery, placeholder = stringResource(android.R.string.search_go))

        when (val results = searchResults) {
            null -> Unit // Still loading
            emptyList<TimeZoneUi>() -> EmptyState(text = stringResource(R.string.emptyStateNoTimeZone))
            else -> TimeZoneResults(results, searchQuery.text.toString().trim(), onTimeZoneClick)
        }
    }
}

@Composable
private fun TimeZoneResults(results: List<TimeZoneUi>, query: String, onTimeZoneClick: (TimeZone) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = EsdsTheme.spacing.md, vertical = EsdsTheme.spacing.sm),
    ) {
        items(results, key = { it.timeZone.id }) { timeZone ->
            TimeZoneItem(timeZone, query, onClick = { onTimeZoneClick(timeZone.timeZone) })
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            TimeZoneSearch(onTimeZoneClick = {})
        }
    }
}
