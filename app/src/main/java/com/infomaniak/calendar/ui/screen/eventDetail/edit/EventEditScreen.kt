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

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
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
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.infomaniak.calendar.components.eventdetail.form.EventForm
import com.infomaniak.calendar.ui.modifier.LocalSharedTransitionScope
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.theme.CalendarTheme
import com.infomaniak.calendar.ui.theme.Dimens
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId

@Composable
fun EventEditScreen(
    occurrenceId: OccurrenceId,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventEditViewModel = viewModel(),
) {
    val uiState by viewModel.eventDetail.collectAsStateWithLifecycle()

    LaunchedEffect(occurrenceId) {
        viewModel.setOccurrenceId(occurrenceId)
    }

    EventEditScreen(
        uiState = { uiState },
        goBack = goBack,
        modifier = modifier,
        sharedTransitionScope = LocalSharedTransitionScope.current,
        animatedVisibilityScope = LocalNavAnimatedContentScope.current,
    )
}

@Composable
private fun EventEditScreen(
    uiState: () -> EventDetailUiState,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    Scaffold(
        topBar = { TopAppBar(title = {}) },
    ) { contentPadding ->
        when (val state = uiState()) {
            // Coming from the detail screen, the shared flow is already warm, so this is only hit when entering edit directly or
            // recreating the activity, but it still will be loaded from disk which is fast enough.
            EventDetailUiState.Loading -> Unit
            is EventDetailUiState.Success -> EventForm(
                eventColor = state.eventDetail.eventColor,
                title = state.eventDetail.title,
                modifier = modifier,
                contentPadding = contentPadding + Dimens.EventDetailScreensHorizontalPadding,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
            EventDetailUiState.Unavailable -> LaunchedEffect(Unit) { goBack() }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    CalendarTheme {
        Surface {
            EventEditScreen(
                uiState = { EventDetailUiState.Loading },
                goBack = {},
            )
        }
    }
}
