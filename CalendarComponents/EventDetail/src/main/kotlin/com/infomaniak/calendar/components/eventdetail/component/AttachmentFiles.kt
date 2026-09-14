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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.core.ui.compose.theme.LocalIsThemeDarkMode
import com.infomaniak.designsystem.core.theme.EsdsTheme

@Composable
internal fun AttachmentFiles(
    files: List<EventDetailUi.File>,
    onFileClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    files.forEach { file ->
        ClickableItem(
            text = file.name,
            leadingContent = {
                val fileType = file.fileType
                Icon(
                    imageVector = fileType.icon,
                    contentDescription = null,
                    modifier = Modifier.size(EsdsTheme.icon.sizeSm),
                    tint = fileType.color(LocalIsThemeDarkMode.current),
                )
            },
            trailingContent = {},
            onClick = { onFileClick(file.id) },
            contentPadding = contentPadding,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun PreviewAttachmentFiles() {
    MaterialTheme {
        Surface {
            AttachmentFiles(
                files = listOf(
                    EventDetailUi.File("1", "How to not get fired.pdf"),
                    EventDetailUi.File("2", "Bob.txt"),
                    EventDetailUi.File("3", "Next loto results.png"),
                ),
                onFileClick = {},
            )
        }
    }
}
