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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.CircleCrossFilled

@Composable
fun CompactTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current,
    isError: Boolean = false,
    colors: TextFieldColors = CompactTextFieldDefaults.colors(),
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    supportingText: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    // If color is not provided via the text style, use content color as a default
    val textColor =
        textStyle.color.takeOrElse {
            val focused = interactionSource.collectIsFocusedAsState().value
            colors.textColor(enabled, isError, focused)
        }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    val isSingleLine = lineLimits is TextFieldLineLimits.SingleLine
    val verticalAlignment = if (isSingleLine) {
        Alignment.CenterVertically
    } else {
        Alignment.Top
    }
    val contentAlignment = if (isSingleLine) Alignment.CenterStart else Alignment.TopStart

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            state = state,
            lineLimits = lineLimits,
            textStyle = mergedTextStyle,
            enabled = enabled,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(colors.focusedTextColor),
            modifier = modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {},
            decorator = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (isSingleLine) 0.dp else EsdsTheme.spacing.sm)
                        .minimumInteractiveComponentSize(),
                    verticalAlignment = verticalAlignment,
                    horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xl),
                ) {
                    CompositionLocalProvider(LocalContentColor provides colors.focusedTextColor) {
                        leadingIcon?.invoke()

                        Column(modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = contentAlignment) {
                                if (state.text.isEmpty()) {
                                    placeholder?.invoke()
                                }
                                innerTextField()
                            }

                            AnimatedVisibility(
                                visible = isError,
                                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                            ) {
                                supportingText?.invoke()
                            }
                        }

                        trailingIcon?.invoke()
                    }
                }
            },
        )
    }
}

internal object CompactTextFieldDefaults {
    @Composable
    fun colors(
        focusedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        focusedPlaceholderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = EsdsTheme.opacity.strong),
        unfocusedPlaceholderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = EsdsTheme.opacity.strong),
        errorSupportingTextColor: Color = MaterialTheme.colorScheme.error,
    ): TextFieldColors {
        return TextFieldDefaults.colors(
            focusedTextColor = focusedTextColor,
            unfocusedTextColor = unfocusedTextColor,
            errorSupportingTextColor = errorSupportingTextColor,
            focusedPlaceholderColor = focusedPlaceholderColor,
            unfocusedPlaceholderColor = unfocusedPlaceholderColor,
        )
    }
}

@Preview
@Composable
private fun PreviewCompactTextField() {
    Surface {
        CompactTextField(
            state = rememberTextFieldState(stringResource(R.string.descriptionTitle)),
            isError = false,
        )
    }
}

@Preview
@Composable
private fun PreviewCompactTextFieldWithTrailingIcon() {
    Surface {
        CompactTextField(
            state = rememberTextFieldState(stringResource(R.string.descriptionTitle)),
            isError = false,
            trailingIcon = { Icon(EsdsIcons.CircleCrossFilled.ExtraSmall, contentDescription = "Clock") },
        )
    }
}

@Preview
@Composable
private fun PreviewCompactTextFieldMultilineWithTrailingIcon() {
    Surface {
        CompactTextField(
            state = rememberTextFieldState(stringResource(R.string.descriptionTitle)),
            isError = false,
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 3),
            trailingIcon = { Icon(EsdsIcons.CircleCrossFilled.ExtraSmall, contentDescription = "Clock") },
        )
    }
}


@Preview
@Composable
private fun PreviewCompactTextFieldErrorAndTrailingIcon() {
    Surface {
        CompactTextField(
            state = rememberTextFieldState(stringResource(R.string.descriptionTitle)),
            isError = true,
            supportingText = { Text(stringResource(R.string.eventValidationCharacterLimitExceeded)) },
            trailingIcon = { Icon(EsdsIcons.CircleCrossFilled.ExtraSmall, contentDescription = "Clock") },
        )
    }
}
