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
package com.infomaniak.calendar.ui.screen.eventDetail.timeZoneSearch

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.ui.component.TimeZoneSearchScreenContent
import com.infomaniak.calendar.ui.navigation.LocalSharedViewModelStoreOwner
import com.infomaniak.calendar.ui.screen.eventDetail.form.EventFormViewModel
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview

@Composable
fun TimeZoneSearchScreen(
    isStart: Boolean,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    eventFormViewModel: EventFormViewModel = viewModel(LocalSharedViewModelStoreOwner.current), // shared from parent
) {
    TimeZoneSearchScreenContent(
        onTimeZoneSelected = { timeZone ->
            eventFormViewModel.formState?.dateAndTimeState?.run { (if (isStart) start else end).pickTimeZone(timeZone) }
            goBack()
        },
        goBack = goBack,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        TimeZoneSearchScreenContent(onTimeZoneSelected = {}, goBack = {})
    }
}
