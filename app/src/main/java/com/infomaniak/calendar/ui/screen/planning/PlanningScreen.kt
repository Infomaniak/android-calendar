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
package com.infomaniak.calendar.ui.screen.planning

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.planning.Planning
import com.infomaniak.calendar.ui.previewparameter.EventsByWeekAndDayPreviewParameter
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin

@Composable
fun PlanningScreen(modifier: Modifier = Modifier, viewModel: PlanningViewModel = viewModel()) {
    val weekEvents: EventsByWeekAndDay by viewModel.weekEvents.collectAsStateWithLifecycle()
    PlanningScreen(weekEvents = { weekEvents }, modifier = modifier)
}

@Composable
private fun PlanningScreen(weekEvents: () -> EventsByWeekAndDay, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("PlanningScreen") }) },
        modifier = modifier,
    ) { paddingValues ->
        Column {
            Planning(
                weekEvents = weekEvents,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Margin.Medium)
                    .fillMaxWidth(),
                contentPadding = paddingValues,
            )
        }
    }
}

@Preview
@Composable
private fun Preview(@PreviewParameter(EventsByWeekAndDayPreviewParameter::class) weekEvents: EventsByWeekAndDay) {
    CalendarThemeForPreview {
        PlanningScreen(weekEvents = { weekEvents })
    }
}
