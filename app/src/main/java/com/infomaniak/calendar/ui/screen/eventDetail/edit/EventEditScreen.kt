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
package com.infomaniak.calendar.ui.screen.eventDetail.edit

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.plus
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.eventdetail.edit.EventEdit
import com.infomaniak.calendar.ui.theme.CalendarTheme
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId

@Composable
fun EventEditScreen(
    occurrenceId: OccurrenceId,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventEditViewModel = viewModel(),
) {
    val uiState by viewModel.eventDetail.collectAsStateWithLifecycle(null)

    LaunchedEffect(occurrenceId) {
        viewModel.setOccurrenceId(occurrenceId)
    }

    EventEditScreen(
        uiState = { uiState },
        goBack = goBack,
        modifier = modifier,
    )
}

@Composable
private fun EventEditScreen(uiState: () -> Unit, goBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { TopAppBar(title = {}) },
    ) { scaffoldContentPadding ->
        EventEdit(
            modifier = modifier,
            contentPadding = scaffoldContentPadding + PaddingValues(horizontal = Margin.Small),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    CalendarTheme {
        Surface {
            EventEditScreen(
                uiState = {},
                goBack = {},
            )
        }
    }
}
