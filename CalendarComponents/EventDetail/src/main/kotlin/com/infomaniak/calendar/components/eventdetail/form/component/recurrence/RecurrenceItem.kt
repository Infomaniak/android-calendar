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
package com.infomaniak.calendar.components.eventdetail.form.component.recurrence

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.items.ClickableItem
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.CircularArrowsClockwise
import kotlinx.datetime.LocalDateTime

@Composable
internal fun RecurrenceItem(text: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ClickableItem(
        text = text.orEmpty(),
        leadingIcon = EsdsIcons.CircularArrowsClockwise,
        onClick = onClick,
        modifier = modifier,
        trailingContent = {},
    )
}

@Composable
internal fun recurrenceText(
    recurrence: RecurrenceUi,
    start: LocalDateTime,
    getRecurrenceText: suspend (RecurrenceUi, start: LocalDateTime) -> String?,
): String? {
    val noneText = stringResource(R.string.recurrenceNone)
    val text by produceState(if (recurrence == RecurrenceUi.None) noneText else null, recurrence, start) {
        value = if (recurrence == RecurrenceUi.None) noneText else getRecurrenceText(recurrence, start)
    }

    return text
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            RecurrenceItem(text = "Every week on Monday", onClick = {})
        }
    }
}
