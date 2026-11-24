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

import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.infomaniak.calendar.components.resources.R

@Composable
fun MaxCharacterTextField(
    state: TextFieldState,
    maxAmountCharacters: Int,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    allowsLineBreak: Boolean = true,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val isTooLong = state.text.length > maxAmountCharacters
    val extraCharacters = maxAmountCharacters - state.text.length
    val errorMessage = stringResource(R.string.eventValidationCharacterLimitExceeded) + " $extraCharacters"

    CompactTextField(
        state = state,
        isError = isTooLong,
        placeholder = placeholder,
        textStyle = textStyle,
        errorText = { Text(text = errorMessage, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        lineLimits = lineLimits,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        allowsLineBreak = allowsLineBreak,
        modifier = modifier,
    )
}
