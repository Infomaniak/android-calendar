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
package com.infomaniak.calendar.components.eventdetail.form.component.reminder

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.form.component.OptionsDialog
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDurationOffset
import com.infomaniak.calendar.components.resources.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private val reminderOffsetPresets = listOf(
    null,
    Duration.ZERO,
    (-5).minutes,
    (-10).minutes,
    (-15).minutes,
    (-30).minutes,
    (-1).hours,
    (-1).days,
)

@Composable
internal fun ReminderOffsetDialog(
    selectedOffset: Duration?,
    onOffsetSelect: (offset: Duration?) -> Unit,
    onCustomClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    OptionsDialog(
        options = reminderOffsetPresets,
        selectedOption = selectedOffset,
        onOptionSelect = onOffsetSelect,
        customLabel = stringResource(R.string.customAlarmLabel),
        onCustomClick = onCustomClick,
        onDismissRequest = onDismissRequest,
    ) { offset ->
        offset?.let { Text(it.formatDurationOffset()) } ?: Text(stringResource(R.string.alarmOffsetNone))
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        ReminderOffsetDialog(selectedOffset = null, onOffsetSelect = {}, onCustomClick = {}, onDismissRequest = {})
    }
}
