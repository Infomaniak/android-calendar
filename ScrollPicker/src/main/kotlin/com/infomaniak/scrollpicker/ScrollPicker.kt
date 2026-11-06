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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A single wheel picker, scrolled vertically to select one of [items].
 *
 * Clicking the selected item starts manual input when [onInput] is given. Every keystroke is parsed and selected right
 * away, so [ScrollPickerState.selectedIndex] always holds the last valid input, even if the keyboard is never validated.
 * Text matching no item is rejected, unless it starts a longer label. Once no longer label starts with it, the input is
 * complete and the [ImeAction] of [keyboardOptions] is performed: [ImeAction.Next] and [ImeAction.Previous] move the
 * focus, e.g. to the next picker (typing "9" in an hour picker labelled "00" to "23" goes straight to the minutes),
 * [ImeAction.None] does nothing and any other action ends the input. The keyboard's action key does the same.
 *
 * Input can also be started from code with a [androidx.compose.ui.focus.FocusRequester] given in [modifier], while
 * [androidx.compose.ui.focus.focusProperties] in [modifier] changes where [ImeAction.Next] and [ImeAction.Previous] go.
 * As moving the focus wraps around, the last of chained pickers should end the input rather than use [ImeAction.Next].
 * Chained pickers sharing a [ScrollPickerGroupState] in their [ScrollPickerState] also react as one: while any of them is
 * edited, all of them only show their selected value, and dragging or tapping a hidden row of any of them ends the input.
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
    itemHeight: Dp = ScrollPickerDefaults.ItemHeight,
    textStyle: TextStyle = ScrollPickerDefaults.textStyle,
    selectedTextStyle: TextStyle = ScrollPickerDefaults.selectedTextStyle,
    colors: ScrollPickerColors = ScrollPickerDefaults.colors(),
) {
    val labels = remember(items, itemLabel) { items.map(itemLabel) }
    val itemCount = labels.size
    val looping = loop && itemCount > 0
    val listState = state.lazyListState

    // Centers the selection, in the middle of the virtual items when looping, before the wheel is first measured.
    val listItemCount = remember(state, itemCount, looping) {
        val index = state.selectedIndex.coerceIn(0, (itemCount - 1).coerceAtLeast(0))
        state.loopSize = if (looping) itemCount else 0
        state.selectedListIndex = if (looping) LOOP_START + index else index
        state.selectedIndex = index
        listState.requestScrollToItem(state.selectedListIndex)
        if (looping) LOOP_ITEM_COUNT else itemCount
    }

    fun itemIndexOf(listIndex: Int) = if (looping) (listIndex - LOOP_START).mod(itemCount) else listIndex

    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.roundToPx() }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(state, itemCount, looping, itemHeightPx) {
        if (itemCount == 0) return@LaunchedEffect
        snapshotFlow {
            listState.firstVisibleItemIndex + if (listState.firstVisibleItemScrollOffset > itemHeightPx / 2) 1 else 0
        }.collect { listIndex ->
            // Ticks for every item passing the center while scrolled, not for typed input nor the initial position.
            if (listState.isScrollInProgress) haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
            state.selectedListIndex = listIndex
            state.selectedIndex = itemIndexOf(listIndex).coerceIn(0, itemCount - 1)
        }
    }

    var editing by remember { mutableStateOf(false) }
    val group = state.group
    // Only the selected value shows while this picker, or another one of its group, is edited by hand.
    fun showsOnlySelection() = editing || group?.isEditing == true
    DisposableEffect(state) { onDispose { group?.setEditing(state, editing = false) } }

    val focusManager = LocalFocusManager.current
    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect {
            if (it is DragInteraction.Start && showsOnlySelection()) focusManager.clearFocus()
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val width = remember(labels, textStyle, selectedTextStyle, textMeasurer, density) {
        val widest = labels.maxOfOrNull { label ->
            maxOf(
                textMeasurer.measure(label, textStyle, skipCache = true).size.width,
                textMeasurer.measure(label, selectedTextStyle, skipCache = true).size.width,
            )
        }
        with(density) { (widest ?: 0).toDp() }
    }

    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val inputFocusRequester = remember { FocusRequester() }
    val onItemClick: (Int) -> Unit = { listIndex ->
        if (listIndex == state.selectedListIndex && onInput != null) {
            if (editing) keyboardController?.show() else inputFocusRequester.requestFocus()
        } else if (showsOnlySelection()) {
            focusManager.clearFocus()
        } else {
            scope.launch { listState.animateScrollToItem(listIndex) }
        }
    }

    Box(
        modifier = modifier.clearAndSetSemantics {
            val selectedIndex = state.selectedIndex
            labels.getOrNull(selectedIndex)?.let { stateDescription = it }
            progressBarRangeInfo = ProgressBarRangeInfo(
                current = selectedIndex.toFloat(),
                range = 0f..(itemCount - 1).coerceAtLeast(0).toFloat(),
                steps = (itemCount - 2).coerceAtLeast(0),
            )
            if (enabled && itemCount > 0) setProgress { target ->
                scope.launch { state.scrollToIndex(target.roundToInt().coerceIn(0, itemCount - 1)) }
                true
            }
        },
        contentAlignment = Alignment.Center,
    ) {
        // Lies under the wheel, which handles all touches, and only shows while focused.
        if (onInput != null) ScrollPickerInputField(
            items = items,
            labels = labels,
            state = state,
            onInput = onInput,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            textStyle = selectedTextStyle.copy(color = colors.textColor(enabled, active = true), textAlign = TextAlign.Center),
            cursorColor = colors.cursorColor,
            modifier = Modifier
                .matchParentSize()
                .focusRequester(inputFocusRequester)
                .onFocusChanged {
                    editing = it.isFocused
                    group?.setEditing(state, it.isFocused)
                }
                .graphicsLayer { alpha = if (editing) 1f else 0f },
        )

        LazyColumn(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = if (editing) 0f else 1f },
            state = listState,
            contentPadding = PaddingValues(vertical = itemHeight),
            flingBehavior = rememberSnapFlingBehavior(listState, SnapPosition.Start),
            userScrollEnabled = enabled,
        ) {
            items(listItemCount) { listIndex ->
                ScrollPickerItem(
                    label = labels[itemIndexOf(listIndex)],
                    listIndex = listIndex,
                    listState = listState,
                    group = group,
                    enabled = enabled,
                    textMeasurer = textMeasurer,
                    textStyle = textStyle,
                    selectedTextStyle = selectedTextStyle,
                    colors = colors,
                    onClick = { onItemClick(listIndex) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                )
            }
        }

        // Sizes the picker by default, in pixels so the selection lines up with whole items.
        Spacer(Modifier.size(width, with(density) { (itemHeightPx * VISIBLE_ITEM_COUNT).toDp() }))
    }
}

/**
 * Draws the label twice: with [textStyle] outside the selection area, the center row of the wheel, and with
 * [selectedTextStyle] inside it, so an item crossing its border is split between both styles. Only the latter is drawn
 * while another picker of the [group] is edited.
 */
@Composable
private fun ScrollPickerItem(
    label: String,
    listIndex: Int,
    listState: LazyListState,
    group: ScrollPickerGroupState?,
    enabled: Boolean,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle,
    selectedTextStyle: TextStyle,
    colors: ScrollPickerColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier
            .focusProperties { canFocus = false }
            .clickable(interactionSource = null, indication = null, enabled = enabled, onClick = onClick)
            .drawWithCache {
                val text = textMeasurer.measure(label, textStyle)
                val selectedText = textMeasurer.measure(label, selectedTextStyle)
                onDrawBehind {
                    val color = colors.textColor(enabled, active = listState.isScrollInProgress)
                    // Read while drawing so that scrolling only redraws. The row at the wheel's content start is selected.
                    val selectionTop =
                        (listState.firstVisibleItemIndex - listIndex) * size.height + listState.firstVisibleItemScrollOffset
                    val selectionBottom = selectionTop + size.height
                    if (group?.isEditing != true) {
                        clipRect(top = selectionTop, bottom = selectionBottom, clipOp = ClipOp.Difference) {
                            drawCentered(text, color)
                        }
                    }
                    clipRect(top = selectionTop, bottom = selectionBottom) { drawCentered(selectedText, color) }
                }
            },
    )
}

private fun DrawScope.drawCentered(text: TextLayoutResult, color: Color) {
    drawText(text, color, Offset((size.width - text.size.width) / 2f, (size.height - text.size.height) / 2f))
}

@Composable
private fun <T> ScrollPickerInputField(
    items: List<T>,
    labels: List<String>,
    state: ScrollPickerState,
    onInput: (String) -> T?,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    textStyle: TextStyle,
    cursorColor: Color,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var value by remember { mutableStateOf(TextFieldValue()) }

    fun performImeAction() {
        when (keyboardOptions.imeAction) {
            ImeAction.Next -> focusManager.moveFocus(FocusDirection.Next)
            ImeAction.Previous -> focusManager.moveFocus(FocusDirection.Previous)
            ImeAction.None -> Unit
            else -> focusManager.clearFocus()
        }
    }

    // Moving the focus before the field recomposes with its new value would make it report its stale value on blur.
    var isInputComplete by remember { mutableStateOf(false) }
    LaunchedEffect(isInputComplete) {
        if (isInputComplete) {
            isInputComplete = false
            performImeAction()
        }
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
            val isPrefix = labels.any { it.length > text.length && it.startsWith(text, ignoreCase = true) }
            if (text.isNotEmpty() && index == null && !isPrefix) return@BasicTextField

            value = newValue
            if (index != null) {
                state.select(index)
                isInputComplete = !isPrefix
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
        keyboardActions = KeyboardActions(onAny = { performImeAction() }),
        singleLine = true,
        cursorBrush = SolidColor(cursorColor),
        decorationBox = { innerTextField -> Box(Modifier.fillMaxSize(), Alignment.Center) { innerTextField() } },
    )
}

private fun twoDigits(number: Int) = number.toString().padStart(2, '0')

@Preview
@Composable
private fun NumbersPreview() {
    MaterialTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                ScrollPicker(
                    items = (0..59).toList(),
                    state = rememberScrollPickerState(),
                    loop = true,
                    itemLabel = ::twoDigits,
                    onInput = String::toIntOrNull,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        }
    }
}

@Preview
@Composable
private fun TextPreview() {
    MaterialTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                ScrollPicker(items = listOf("Minutes", "Hours", "Days", "Weeks"), state = rememberScrollPickerState())
            }
        }
    }
}

@Preview
@Composable
private fun TimePreview() {
    MaterialTheme {
        Scaffold {
            Column(
                Modifier
                    .padding(16.dp)
                    .padding(it),
            ) {
                val group = rememberScrollPickerGroupState()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ScrollPicker(
                        items = (0..23).toList(),
                        state = rememberScrollPickerState(initialIndex = 10, group = group),
                        loop = true,
                        itemLabel = ::twoDigits,
                        onInput = String::toIntOrNull,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    )
                    Text(text = ":", style = ScrollPickerDefaults.selectedTextStyle)
                    ScrollPicker(
                        items = (0..59).toList(),
                        state = rememberScrollPickerState(group = group),
                        loop = true,
                        itemLabel = ::twoDigits,
                        onInput = String::toIntOrNull,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    )
                }
            }
        }
    }
}

private const val VISIBLE_ITEM_COUNT = 3
private const val LOOP_ITEM_COUNT = 100_000
private const val LOOP_START = LOOP_ITEM_COUNT / 2
