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
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.items.ClickableItemDefaults
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIcon
import com.infomaniak.calendar.components.eventdetail.component.items.ListItemSwitch
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Lock

@Composable
internal fun ClassificationStatus(classification: EventDetailUi.Classification, modifier: Modifier = Modifier) {
    ListItem(
        content = { Text(text = stringResource(classification.label)) },
        leadingContent = { ClassificationIcon() },
        modifier = modifier,
    )
}

@Composable
internal fun ClassificationStatusSwitch(
    checked: () -> Boolean,
    onCheckedChange: (isChecked: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    shapes: ListItemShapes = ClickableItemDefaults.ClickableItemShapes,
) {
    ListItemSwitch(checked, onCheckedChange, modifier, shapes, leadingContent = { ClassificationIcon() }) {
        Text(text = stringResource(EventDetailUi.Classification.Public.label))
    }
}

@Composable
private fun ClassificationIcon() {
    ItemIcon(EsdsIcons.Lock)
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

@Preview
@Composable
private fun PreviewClassificationStatusSwitch() {
    MaterialTheme {
        Surface {
            Column {
                ClassificationStatusSwitch(checked = { true }, onCheckedChange = {})
                ClassificationStatusSwitch(checked = { false }, onCheckedChange = {})
            }
        }
    }
}
