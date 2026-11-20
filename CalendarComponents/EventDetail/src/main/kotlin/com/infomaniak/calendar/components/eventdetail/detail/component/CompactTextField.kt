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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.CircleCrossFilled
import kotlin.math.roundToInt
import com.infomaniak.core.common.R as RCommon

/**
 * A compact text field component that supports single-line and multi-line input,
 * with customizable colors, icons, and supporting text that is close to the input.
 */
@Composable
fun CompactTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current,
    colors: TextFieldColors = CompactTextFieldDefaults.colors(),
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    isError: Boolean = false,
    errorText: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    // If color is not provided via the text style, use content color as a default
    val focused = interactionSource.collectIsFocusedAsState().value
    val textColor = textStyle.color.takeOrElse { colors.textColor(enabled, isError, focused) }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    val isSingleLine = lineLimits is TextFieldLineLimits.SingleLine
    val verticalAlignment = if (isSingleLine) {
        Alignment.CenterVertically
    } else {
        Alignment.Top
    }

    var firstLineHeightPx by remember { mutableIntStateOf(0) }
    val firstLineHeight = with(LocalDensity.current) { firstLineHeightPx.toDp() }

    // In multiline, icons are centered on the first line (or the placeholder) instead of the top of the field
    val alignedIcon: @Composable (@Composable () -> Unit) -> Unit = { icon ->
        if (isSingleLine) {
            icon()
        } else {
            Box(modifier = Modifier.heightIn(min = firstLineHeight), contentAlignment = Alignment.Center) { icon() }
        }
    }

    val invalidInputMessage = stringResource(RCommon.string.invalidInput)

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            state = state,
            lineLimits = lineLimits,
            textStyle = mergedTextStyle,
            enabled = enabled,
            onTextLayout = { getResult ->
                getResult()?.takeIf { it.lineCount > 0 }?.let {
                    firstLineHeightPx = (it.getLineBottom(0) - it.getLineTop(0)).roundToInt()
                }
            },
            interactionSource = interactionSource,
            cursorBrush = SolidColor(colors.cursorColor(isError)),
            modifier = modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { if (isError) error(invalidInputMessage) },
            decorator = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isSingleLine) Modifier.minimumInteractiveComponentSize() else Modifier),
                    verticalAlignment = verticalAlignment,
                    horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xl),
                ) {
                    leadingIcon?.let {
                        CompositionLocalProvider(
                            LocalContentColor provides colors.leadingIconColor(enabled, isError, focused),
                        ) { alignedIcon(it) }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (state.text.isEmpty()) {
                                placeholder?.let {
                                    CompositionLocalProvider(
                                        LocalContentColor provides colors.placeholderColor(enabled, isError, focused),
                                    ) { it() }
                                }
                            }
                            innerTextField()
                        }

                        AnimatedVisibility(
                            visible = isError,
                            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                        ) {
                            CompositionLocalProvider(
                                LocalContentColor provides colors.supportingTextColor(enabled, isError, focused),
                                LocalTextStyle provides MaterialTheme.typography.bodySmall,
                            ) {
                                errorText?.invoke()
                            }

                        }
                    }

                    trailingIcon?.let {
                        CompositionLocalProvider(
                            LocalContentColor provides colors.trailingIconColor(enabled, isError, focused),
                        ) { it() }
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
        focusedTrailingIconColor: Color = focusedTextColor,
        unfocusedTrailingIconColor: Color = unfocusedTextColor,
        errorTrailingIconColor: Color = MaterialTheme.colorScheme.error,
        cursorColor: Color = focusedTextColor,
        errorCursorColor: Color = MaterialTheme.colorScheme.error,
        focusedLeadingIconColor: Color = focusedTextColor,
        unfocusedLeadingIconColor: Color = unfocusedTextColor,
        errorLeadingIconColor: Color = focusedTextColor,
    ): TextFieldColors {
        return TextFieldDefaults.colors(
            focusedTextColor = focusedTextColor,
            unfocusedTextColor = unfocusedTextColor,
            errorSupportingTextColor = errorSupportingTextColor,
            focusedPlaceholderColor = focusedPlaceholderColor,
            unfocusedPlaceholderColor = unfocusedPlaceholderColor,
            focusedTrailingIconColor = focusedTrailingIconColor,
            unfocusedTrailingIconColor = unfocusedTrailingIconColor,
            errorTrailingIconColor = errorTrailingIconColor,
            cursorColor = cursorColor,
            errorCursorColor = errorCursorColor,
            focusedLeadingIconColor = focusedLeadingIconColor,
            unfocusedLeadingIconColor = unfocusedLeadingIconColor,
            errorLeadingIconColor = errorLeadingIconColor,
        )
    }
}

@Preview
@Composable
private fun PreviewCompactTextField() {
    Surface {
        CompactTextField(
            state = rememberTextFieldState(stringResource(R.string.descriptionTitle)),
        )
    }
}

@Preview
@Composable
private fun PreviewCompactTextFieldWithTrailingIcon() {
    Surface {
        CompactTextField(
            state = rememberTextFieldState(stringResource(R.string.descriptionTitle)),
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
            errorText = { Text(stringResource(R.string.eventValidationCharacterLimitExceeded)) },
            trailingIcon = { Icon(EsdsIcons.CircleCrossFilled.ExtraSmall, contentDescription = "Clock") },
        )
    }
}
