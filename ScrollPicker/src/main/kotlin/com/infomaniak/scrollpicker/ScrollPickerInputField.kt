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
package com.infomaniak.scrollpicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.Channel

@Composable
internal fun <T> ScrollPickerInputField(
    items: List<T>,
    labels: List<String>,
    state: ScrollPickerState,
    onInput: (String) -> T?,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    textStyle: TextStyle,
    horizontalAlignment: Alignment.Horizontal,
    cursorColor: Color,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var value by remember { mutableStateOf(TextFieldValue()) }
    val inputCompletion = remember { Channel<Boolean>(Channel.CONFLATED) }

    // Apply the new value to the field before moving focus, so blur cannot report its previous value.
    SideEffect {
        if (inputCompletion.tryReceive().getOrNull() == true) focusManager.performImeAction(keyboardOptions.imeAction)
    }

    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            val text = newValue.text
            if (text == value.text) {
                value = newValue
                return@BasicTextField
            }
            val index = onInput(text)?.let(items::indexOf)?.takeIf { it >= 0 }
            val isPrefix = labels.anyLongerStartingWith(text)
            if (text.isNotEmpty() && index == null && !isPrefix) return@BasicTextField

            value = newValue
            if (index != null) {
                state.select(index)
                inputCompletion.trySend(!isPrefix)
            }
        },
        modifier = modifier.onFocusChanged {
            if (it.isFocused) {
                val label = labels.getOrElse(state.selectedIndex) { "" }
                value = TextFieldValue(label, TextRange(0, label.length))
            }
        },
        enabled = enabled,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(onAny = { focusManager.performImeAction(keyboardOptions.imeAction) }),
        singleLine = true,
        cursorBrush = SolidColor(cursorColor),
        decorationBox = { innerTextField ->
            Column(Modifier.fillMaxSize(), Arrangement.Center, horizontalAlignment) {
                // The inner field fills the width it's given, so shrink it to its text for the alignment to apply.
                Box(Modifier.width(IntrinsicSize.Max)) { innerTextField() }
            }
        },
    )
}

private fun FocusManager.performImeAction(imeAction: ImeAction) {
    when (imeAction) {
        ImeAction.Next -> moveFocus(FocusDirection.Next)
        ImeAction.Previous -> moveFocus(FocusDirection.Previous)
        ImeAction.None -> Unit
        else -> clearFocus()
    }
}

private fun List<String>.anyLongerStartingWith(text: String): Boolean {
    return any { it.length > text.length && it.startsWith(text, ignoreCase = true) }
}

@Preview
@Composable
private fun ScrollPickerInputFieldPreview() {
    val items = remember { (0..23).toList() }
    val labels = remember(items) { items.map { it.toString().padStart(2, '0') } }
    val state = remember(items) { ScrollPickerState(initialIndex = 10).apply { setItems(items.size, loop = false) } }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }

    MaterialTheme {
        Surface {
            val colors = ScrollPickerDefaults.colors()
            Box(Modifier.padding(16.dp)) {
                ScrollPickerInputField(
                    items = items,
                    labels = labels,
                    state = state,
                    onInput = String::toIntOrNull,
                    enabled = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    textStyle = ScrollPickerDefaults.textStyle.copy(color = colors.activeSelectedTextColor),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    cursorColor = colors.cursorColor,
                    modifier = Modifier
                        .width(100.dp)
                        .height(ScrollPickerDefaults.ItemHeight)
                        .focusRequester(focusRequester),
                )
            }
        }
    }
}
