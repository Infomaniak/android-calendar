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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme

private const val MAX_LINES = Int.MAX_VALUE

@Composable
internal fun Title(color: Color, title: String, modifier: Modifier = Modifier) {
    ListItem(
        leadingContent = { EventColorDot(color) },
        content = { Text(text = title, style = MaterialTheme.typography.titleLargeEmphasized, maxLines = MAX_LINES) },
        modifier = modifier,
    )
}

@Composable
internal fun TitleEditable(color: Color, title: String, modifier: Modifier = Modifier) {
    val textFieldState = rememberTextFieldState(title)

    ListItem(
        leadingContent = { EventColorDot(color) },
        content = {
            BasicTextField(
                state = textFieldState,
                textStyle = MaterialTheme.typography.titleLargeEmphasized,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = MAX_LINES),
                modifier = Modifier.fillMaxWidth(),
                decorator = { innerTextField ->
                    Box {
                        if (textFieldState.text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.eventTitle),
                                style = MaterialTheme.typography.titleLargeEmphasized,
                                color = OutlinedTextFieldDefaults.colors().unfocusedPlaceholderColor,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun EventColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(EsdsTheme.icon.sizeMd)
            .padding(2.dp)
            .clip(CircleShape)
            .background(color),
    )
}

private const val PREVIEW_TITLE = "End of the year vacations with Santa Claus"

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            Title(color = Color.Red, title = PREVIEW_TITLE)
        }
    }
}

@Preview
@Composable
private fun PreviewEditable() {
    MaterialTheme {
        Surface {
            TitleEditable(color = Color.Red, title = PREVIEW_TITLE)
        }
    }
}
