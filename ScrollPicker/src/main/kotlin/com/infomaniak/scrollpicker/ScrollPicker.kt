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

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp

/**
 * A single wheel picker, scrolled vertically to select one of [items].
 *
 * @param items the values to pick from, numbers, text or anything [itemLabel] can render.
 * @param state hoisted selection and scroll state, see [rememberScrollPickerState].
 * @param loop whether the wheel wraps around infinitely instead of stopping at the first and last item.
 * @param itemLabel the text displayed for an item.
 * @param onInput parses the text typed after clicking the selected item, returning `null` to reject it.
 * When `null`, the selected item isn't clickable and values can only be picked by scrolling.
 * @param keyboardOptions keyboard used for manual input, e.g. [androidx.compose.ui.text.input.KeyboardType.Number].
 */
@Composable
fun <T> ScrollPicker(
    items: List<T>,
    state: ScrollPickerState,
    modifier: Modifier = Modifier,
    loop: Boolean = false,
    enabled: Boolean = true,
    itemLabel: (T) -> String = { it.toString() },
    onInput: ((String) -> T?)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visibleItemCount: Int = ScrollPickerDefaults.VisibleItemCount,
    itemHeight: Dp = ScrollPickerDefaults.ItemHeight,
    textStyle: TextStyle = ScrollPickerDefaults.textStyle,
    colors: ScrollPickerColors = ScrollPickerDefaults.colors(),
) {
    // TODO: LazyColumn over state.lazyListState, snapping, loop index mapping, fading, dividers.
    // TODO: Switch to ScrollPickerInputField when the selected item is clicked and onInput != null.
}

@Composable
private fun ScrollPickerItem(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    textStyle: TextStyle,
    colors: ScrollPickerColors,
    modifier: Modifier = Modifier,
) {
    // TODO
}

@Composable
private fun ScrollPickerInputField(
    initialText: String,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
    keyboardOptions: KeyboardOptions,
    textStyle: TextStyle,
    colors: ScrollPickerColors,
    modifier: Modifier = Modifier,
) {
    // TODO
}

@Preview
@Composable
private fun NumbersPreview() {
    ScrollPicker(items = (0..59).toList(), state = rememberScrollPickerState(), loop = true, onInput = String::toIntOrNull)
}

@Preview
@Composable
private fun TextPreview() {
    ScrollPicker(items = listOf("Minutes", "Hours", "Days", "Weeks"), state = rememberScrollPickerState())
}
