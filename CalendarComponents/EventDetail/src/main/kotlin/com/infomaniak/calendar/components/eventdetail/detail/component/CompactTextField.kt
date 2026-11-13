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
package com.infomaniak.calendar.components.eventdetail.detail.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProvideTextStyle
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.CircleCross
import com.infomaniak.designsystem.icons.symbols.CircleInfo

@Composable
fun CompactTextField(
    state: TextFieldState,
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
        start = 0.dp,
        top = 0.dp,
        end = 0.dp,
        bottom = 0.dp,
    )

    BasicTextField(
        state = state,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minWidth = TextFieldDefaults.MinWidth),
        textStyle = textStyle.copy(color = textColor),
        lineLimits = if (singleLine) {
            TextFieldLineLimits.SingleLine
        } else {
            TextFieldLineLimits.MultiLine(maxHeightInLines = maxLines)
        },
        decorator = { innerTextField ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    propagateMinConstraints = true,
                ) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 24.dp) {
                        OutlinedTextFieldDefaults.DecorationBox(
                            value = state.text.toString(),
                            innerTextField = { innerTextField() },
                            enabled = true,
                            singleLine = singleLine,
                            visualTransformation = VisualTransformation.None,
                            interactionSource = interactionSource,
                            isError = isError,
                            label = label,
                            placeholder = placeholder,
                            leadingIcon = leadingIcon,
                            trailingIcon = trailingIcon?.let { icon ->
                                { Box(contentAlignment = Alignment.Center) { icon() } }
                            },
                            supportingText = null,
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
                }

                if (supportingText != null) {
                    val supportingColor = when {
                        isError -> colors.errorSupportingTextColor
                        focused -> colors.focusedSupportingTextColor
                        else -> colors.unfocusedSupportingTextColor
                    }

                    CompositionLocalProvider(LocalContentColor provides supportingColor) {
                        ProvideTextStyle(MaterialTheme.typography.bodySmall) {
                            Box(Modifier.padding(start = 0.dp, top = 4.dp, end = 0.dp, bottom = 8.dp)) {
                                supportingText()
                            }
                        }
                    }
                }
            }

        },
    )
}

@Preview
@Composable
private fun PreviewCompactTextField() {
    Surface {
        CompactTextField(
            state = rememberTextFieldState("Preview"),
            isError = false,
        )
    }
}

@Preview
@Composable
private fun PreviewCompactTextFieldWithInfo() {
    Surface {
        CompactTextField(
            state = rememberTextFieldState("Preview"),
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
            state = rememberTextFieldState("Preview"),
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
            state = rememberTextFieldState("Preview"),
            isError = true,
            supportingText = { Text("This is an error") },
            trailingIcon = { Icon(EsdsIcons.CircleCross.Small, contentDescription = "Clock") },
        )
    }
}
