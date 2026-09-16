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

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.resources.R

@Composable
internal fun ClassificationStatus(classification: EventDetailUi.Classification, modifier: Modifier = Modifier) {
    ListItem(
        headlineContent = { Text(text = stringResource(classification.label)) },
        leadingContent = { Icon(painter = painterResource(R.drawable.ic_lock), contentDescription = null) },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun PreviewClassificationStatus() {
    MaterialTheme {
        Surface {
            Column {
                EventDetailUi.Classification.entries.forEach { ClassificationStatus(classification = it) }
            }
        }
    }
}
