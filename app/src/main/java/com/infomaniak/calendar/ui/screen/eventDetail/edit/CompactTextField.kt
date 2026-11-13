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
package com.infomaniak.calendar.ui.screen.eventDetail.edit

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Check
import com.infomaniak.designsystem.icons.symbols.CircleCross
import com.infomaniak.designsystem.icons.symbols.CircleInfo
import com.infomaniak.designsystem.icons.symbols.Clock
import com.infomaniak.designsystem.icons.symbols.Cross

@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        disabledBorderColor = Color.Transparent,
        errorBorderColor = Color.Transparent,
    ),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val textColor = when {
        textStyle.color != Color.Unspecified -> textStyle.color
        focused -> colors.focusedTextColor
        else -> colors.unfocusedTextColor
    }

    val contentPadding = OutlinedTextFieldDefaults.contentPadding(
        start = 16.dp,
        top = 0.dp,
        end = 8.dp,
        bottom = 0.dp,
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.defaultMinSize(
            minWidth = TextFieldDefaults.MinWidth,
        ),
        textStyle = textStyle.copy(color = textColor),
        singleLine = singleLine,
        maxLines = maxLines,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(
            if (isError) colors.errorCursorColor else colors.cursorColor,
        ),
        decorationBox = { innerTextField ->
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 24.dp) {
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = {
                        Box(
                            modifier = if (leadingIcon != null || trailingIcon != null) {
                                Modifier.heightIn(min = 32.dp)
                            } else {
                                Modifier
                            },
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            innerTextField()
                        }
                    },
                    enabled = true,
                    singleLine = singleLine,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    isError = isError,
                    label = label,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon?.let { icon ->
                        {
                            Box(
                                modifier = Modifier.padding(end = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                icon()
                            }
                        }
                    },
                    supportingText = supportingText?.let { content ->
                        {
                            Box(Modifier.padding(bottom = 8.dp)) {
                                content()
                            }
                        }
                    },
                    colors = colors,
                    contentPadding = contentPadding,
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = true,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = shape,
                            focusedBorderThickness = 0.dp,
                            unfocusedBorderThickness = 0.dp,
                        )
                    },
                )
            }
        },
    )
}

@Preview
@Composable
private fun PreviewCompactTextField() {
    Surface {
        CompactTextField(
            value = "Preview",
            onValueChange = {},
            isError = false,
        )
    }
}

@Preview
@Composable
private fun PreviewCompactTextFieldWithInfo() {
    Surface {
        CompactTextField(
            value = "Preview",
            onValueChange = {},
            isError = false,
            trailingIcon = { Icon(EsdsIcons.CircleInfo.Small, contentDescription = "Clock") },
        )
    }
}


@Preview
@Composable
private fun PreviewCompactTextFieldError() {
    Surface {
        CompactTextField(
            value = "Preview",
            onValueChange = {},
            isError = true,
            supportingText = { Text("This is an error") },
        )
    }
}

@Preview
@Composable
private fun PreviewCompactTextFieldErrorAndTrailingIcon() {
    Surface {
        CompactTextField(
            value = "Preview",
            onValueChange = {},
            isError = true,
            supportingText = { Text("This is an error") },
            trailingIcon = { Icon(EsdsIcons.CircleCross.Small, contentDescription = "Clock") },
        )
    }
}
