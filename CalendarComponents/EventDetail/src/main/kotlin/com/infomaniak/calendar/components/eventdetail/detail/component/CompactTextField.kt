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
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.CircleCrossFilled

@Composable
fun CompactTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    supportingText: String = "",
    isError: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = colors.onSurface,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )

    BasicTextField(
        state = state,
        lineLimits = TextFieldLineLimits.SingleLine,
        textStyle = textStyle,
        cursorBrush = SolidColor(colors.primary),
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                if (isError && supportingText.isNotBlank()) {
                    error(supportingText)
                }
            },
        decorator = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides colors.onSurfaceVariant,
                ) {
                    leadingIcon?.invoke()

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (state.text.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = textStyle,
                                    color = colors.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            innerTextField()
                        }

                        AnimatedVisibility(
                            visible = isError && supportingText.isNotBlank(),
                            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                        ) {
                            Text(
                                text = supportingText,
                                color = colors.error,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }

                    trailingIcon?.invoke()
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
private fun PreviewCompactTextFieldWithTrailingIcon() {
    Surface {
        CompactTextField(
            state = rememberTextFieldState("Preview"),
            isError = false,
            trailingIcon = { Icon(EsdsIcons.CircleCrossFilled.ExtraSmall, contentDescription = "Clock") },
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
            supportingText = "Error message",
            trailingIcon = { Icon(EsdsIcons.CircleCrossFilled.ExtraSmall, contentDescription = "Clock") },
        )
    }
}
