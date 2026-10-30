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
package com.infomaniak.calendar.ui.screen.eventDetail.customRecurrence

import androidx.compose.foundation.layout.plus
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.eventdetail.form.CustomRecurrence
import com.infomaniak.calendar.components.eventdetail.form.rememberCustomRecurrenceState
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi
import com.infomaniak.calendar.ui.component.topAppBar.SubmitTopAppBar
import com.infomaniak.calendar.ui.navigation.LocalSharedViewModelStoreOwner
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.ui.theme.Dimens
import kotlinx.datetime.LocalDate
import com.infomaniak.calendar.components.resources.R as RComponents

@Composable
fun CustomRecurrenceScreen(
    start: LocalDate,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    customRecurrenceSharedDataViewModel: CustomRecurrenceSharedDataViewModel = viewModel(LocalSharedViewModelStoreOwner.current), // shared from parent
) {
    CustomRecurrenceScreen(
        start = start,
        initialRecurrence = customRecurrenceSharedDataViewModel.initialRecurrence,
        onSubmit = { recurrence ->
            customRecurrenceSharedDataViewModel.pickRecurrence(recurrence)
            goBack()
        },
        goBack = goBack,
        modifier = modifier,
    )
}

@Composable
private fun CustomRecurrenceScreen(
    start: LocalDate,
    initialRecurrence: RecurrenceUi,
    onSubmit: (RecurrenceUi.Rule) -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberCustomRecurrenceState(start, initialRecurrence)

    Scaffold(
        topBar = {
            SubmitTopAppBar(
                goBack = goBack,
                onSubmit = { state.toRecurrence()?.let(onSubmit) },
                isSubmitEnabled = { state.toRecurrence() != null },
                title = { Text(stringResource(RComponents.string.customRecurrenceTitle)) },
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        CustomRecurrence(state = state, contentPadding = contentPadding + Dimens.EventDetailScreensHorizontalPadding)
    }
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        CustomRecurrenceScreen(start = LocalDate(2026, 12, 21), initialRecurrence = RecurrenceUi.None, onSubmit = {}, goBack = {})
    }
}
