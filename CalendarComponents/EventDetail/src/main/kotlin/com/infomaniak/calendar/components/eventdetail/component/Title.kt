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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.designsystem.core.theme.EsdsTheme

@Composable
internal fun Title(color: Color, title: String, modifier: Modifier = Modifier) {
    ListItem(
        content = { Text(text = title, style = MaterialTheme.typography.titleLargeEmphasized) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(EsdsTheme.icon.sizeMd)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        },
        modifier = modifier,
    )
}

@Composable
internal fun TitleEditable(color: Color, title: String, modifier: Modifier = Modifier) {
    val textFieldState = rememberTextFieldState(title)

    ListItem(
        content = {
            BasicTextField(
                state = textFieldState,
                textStyle = MaterialTheme.typography.titleLargeEmphasized,
                lineLimits = TextFieldLineLimits.SingleLine,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
                decorator = { innerTextField ->
                    Box {
                        if (textFieldState.text.isEmpty()) {
                            Text(
                                text = "TODO Placeholder",
                                style = MaterialTheme.typography.titleLargeEmphasized,
                                color = OutlinedTextFieldDefaults.colors().unfocusedPlaceholderColor,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(EsdsTheme.icon.sizeMd)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            Title(color = Color.Red, title = "Title")
        }
    }
}

@Preview
@Composable
private fun PreviewEditable() {
    MaterialTheme {
        Surface {
            TitleEditable(color = Color.Red, title = "Title")
        }
    }
}
