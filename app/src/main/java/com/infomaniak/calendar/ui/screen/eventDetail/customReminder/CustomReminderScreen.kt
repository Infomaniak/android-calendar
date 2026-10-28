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
package com.infomaniak.calendar.ui.screen.eventDetail.customReminder

import androidx.compose.foundation.layout.plus
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.eventdetail.form.CustomReminder
import com.infomaniak.calendar.components.eventdetail.form.rememberCustomReminderState
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.ui.component.topAppBar.SubmitTopAppBar
import com.infomaniak.calendar.ui.navigation.LocalSharedViewModelStoreOwner
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.ui.theme.Dimens
import com.infomaniak.calendar.components.resources.R as RComponents

@Composable
fun CustomReminderScreen(
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    customReminderResultViewModel: CustomReminderResultViewModel = viewModel(LocalSharedViewModelStoreOwner.current), // shared from parent
) {
    CustomReminderScreen(
        onSubmit = { notification ->
            customReminderResultViewModel.addReminder(notification)
            goBack()
        },
        goBack = goBack,
        modifier = modifier,
    )
}

@Composable
private fun CustomReminderScreen(
    onSubmit: (EventDetailUi.Notification) -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberCustomReminderState()

    Scaffold(
        topBar = {
            SubmitTopAppBar(
                goBack = goBack,
                onSubmit = { state.toNotification()?.let(onSubmit) },
                isSubmitEnabled = { state.amount != null },
                title = { Text(stringResource(RComponents.string.reminderTitle)) },
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        CustomReminder(state = state, contentPadding = contentPadding + Dimens.EventDetailScreensHorizontalPadding)
    }
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        CustomReminderScreen(onSubmit = {}, goBack = {})
    }
}
