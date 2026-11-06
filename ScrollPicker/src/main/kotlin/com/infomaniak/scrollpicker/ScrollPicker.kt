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
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.focus.FocusManager
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
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
 * @param horizontalAlignment where the labels sit within the picker's width, e.g. to line up hours on their end and
 * minutes on their start.
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
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    itemHeight: Dp = ScrollPickerDefaults.ItemHeight,
    textStyle: TextStyle = ScrollPickerDefaults.inactiveTextStyle,
    selectedTextStyle: TextStyle = ScrollPickerDefaults.textStyle,
    colors: ScrollPickerColors = ScrollPickerDefaults.colors(),
) {
    val labels = remember(items, itemLabel) { items.map(itemLabel) }
    val itemCount = labels.size
    val listState = state.lazyListState
    val listItemCount = remember(state, itemCount, loop) { state.setItems(itemCount, loop) }

    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.roundToPx() }
    LaunchedEffect(state, itemCount, loop, itemHeightPx) { state.followCenteredRow(itemHeightPx) }

    val snapper = remember(listState, itemHeightPx) { ScrollPickerSnapper(listState, itemHeightPx) }
    ItemTickHaptics(listState, snapper, itemHeightPx)

    val group = state.group
    DisposableEffect(state) { onDispose { group.setEditing(state, editing = false) } }

    val focusManager = LocalFocusManager.current
    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect {
            if (it is DragInteraction.Start && group.isEditing) focusManager.clearFocus()
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val width = remember(labels, textStyle, selectedTextStyle, textMeasurer, density) {
        with(density) { textMeasurer.widestWidth(labels, textStyle, selectedTextStyle).toDp() }
    }

    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val inputFocusRequester = remember { FocusRequester() }
    val onItemClick: (Int) -> Unit = { listIndex ->
        if (listIndex == state.selectedListIndex && onInput != null) {
            if (state.isEditing) keyboardController?.show() else inputFocusRequester.requestFocus()
        } else if (group.isEditing) {
            focusManager.clearFocus()
        } else {
            scope.launch { listState.animateScrollToItem(listIndex) }
        }
    }

    Box(
        modifier = modifier.pickerSemantics(labels, state, enabled, scope),
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
            textStyle = selectedTextStyle.copy(color = colors.textColor(enabled, active = true, selected = true)),
            horizontalAlignment = horizontalAlignment,
            cursorColor = colors.cursorColor,
            modifier = Modifier
                .matchParentSize()
                .focusRequester(inputFocusRequester)
                .onFocusChanged { group.setEditing(state, it.isFocused) }
                .graphicsLayer { alpha = if (state.isEditing) 1f else 0f },
        )

        LazyColumn(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = if (state.isEditing) 0f else 1f },
            state = listState,
            contentPadding = PaddingValues(vertical = itemHeight),
            flingBehavior = rememberSnapFlingBehavior(snapper),
            userScrollEnabled = enabled,
        ) {
            items(listItemCount) { listIndex ->
                ScrollPickerItem(
                    label = labels[state.itemIndexOf(listIndex)],
                    listIndex = listIndex,
                    listState = listState,
                    group = group,
                    enabled = enabled,
                    textMeasurer = textMeasurer,
                    textStyle = textStyle,
                    selectedTextStyle = selectedTextStyle,
                    horizontalAlignment = horizontalAlignment,
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
 * Ticks when another item reaches the center, but a fling keeps its last item's tick for when it settles, even if it
 * passes it before.
 */
@Composable
private fun ItemTickHaptics(listState: LazyListState, snapper: ScrollPickerSnapper, itemHeightPx: Int) {
    val haptics = LocalHapticFeedback.current
    val dragged by listState.interactionSource.collectIsDraggedAsState()
    LaunchedEffect(snapper) {
        var previous = -1L
        var ticked = -1L
        var scrolled = false
        snapshotFlow { listState.isScrollInProgress to snapper.position }.collect { (scrolling, position) ->
            if (scrolling) {
                val forward = position > previous
                val item = if (forward) position / itemHeightPx else (position + itemHeightPx - 1) / itemHeightPx
                val reached = if (forward) item * itemHeightPx > previous else item * itemHeightPx < previous
                if (reached && item != ticked && (dragged || item != snapper.target)) {
                    haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                    ticked = item
                }
            } else {
                // Typed input and the initial position jump without scrolling, so they don't tick.
                val item = (position + itemHeightPx / 2) / itemHeightPx
                if (scrolled && item != ticked) haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                ticked = item
                snapper.target = -1
            }
            scrolled = scrolling
            previous = position
        }
    }
}

/** Width of the widest of [labels], in whichever style is wider. Measured once, so skips the measurer's cache. */
private fun TextMeasurer.widestWidth(labels: List<String>, textStyle: TextStyle, selectedTextStyle: TextStyle): Int {
    return labels.maxOfOrNull { label ->
        maxOf(
            measure(label, textStyle, skipCache = true).size.width,
            measure(label, selectedTextStyle, skipCache = true).size.width,
        )
    } ?: 0
}

/** Exposes the picker as a single adjustable value, like a slider, rather than as its many rows. */
private fun Modifier.pickerSemantics(
    labels: List<String>,
    state: ScrollPickerState,
    enabled: Boolean,
    scope: CoroutineScope,
): Modifier = clearAndSetSemantics {
    val itemCount = labels.size
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
}

/**
 * Draws the label twice: with [textStyle] outside the selection area, the center row of the wheel, and with
 * [selectedTextStyle] inside it, so an item crossing its border is split between both styles. Only the latter is drawn
 * while a picker of the [group] is edited.
 */
@Composable
private fun ScrollPickerItem(
    label: String,
    listIndex: Int,
    listState: LazyListState,
    group: ScrollPickerGroupState,
    enabled: Boolean,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle,
    selectedTextStyle: TextStyle,
    horizontalAlignment: Alignment.Horizontal,
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
                    val active = listState.isScrollInProgress
                    // Read while drawing so that scrolling only redraws. The row at the wheel's content start is selected.
                    val selectionTop =
                        (listState.firstVisibleItemIndex - listIndex) * size.height + listState.firstVisibleItemScrollOffset
                    val selectionBottom = selectionTop + size.height
                    if (!group.isEditing) {
                        clipRect(top = selectionTop, bottom = selectionBottom, clipOp = ClipOp.Difference) {
                            drawAligned(text, horizontalAlignment, colors.textColor(enabled, active, selected = false))
                        }
                    }
                    clipRect(top = selectionTop, bottom = selectionBottom) {
                        drawAligned(selectedText, horizontalAlignment, colors.textColor(enabled, active, selected = true))
                    }
                }
            },
    )
}

private fun DrawScope.drawAligned(text: TextLayoutResult, alignment: Alignment.Horizontal, color: Color) {
    val x = alignment.align(text.size.width, size.width.roundToInt(), layoutDirection)
    drawText(text, color, Offset(x.toFloat(), (size.height - text.size.height) / 2f))
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
    horizontalAlignment: Alignment.Horizontal,
    cursorColor: Color,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var value by remember { mutableStateOf(TextFieldValue()) }

    // Moving the focus before the field recomposes with its new value would make it report its stale value on blur.
    var isInputComplete by remember { mutableStateOf(false) }
    LaunchedEffect(isInputComplete) {
        if (isInputComplete) {
            isInputComplete = false
            focusManager.performImeAction(keyboardOptions.imeAction)
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
            val isPrefix = labels.anyLongerStartingWith(text)
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

/** Moves the focus for [ImeAction.Next] and [ImeAction.Previous], and ends the input for any action but [ImeAction.None]. */
private fun FocusManager.performImeAction(imeAction: ImeAction) {
    when (imeAction) {
        ImeAction.Next -> moveFocus(FocusDirection.Next)
        ImeAction.Previous -> moveFocus(FocusDirection.Previous)
        ImeAction.None -> Unit
        else -> clearFocus()
    }
}

/** Whether [text] is the start of a longer label, so typing could still complete it. */
private fun List<String>.anyLongerStartingWith(text: String): Boolean {
    return any { it.length > text.length && it.startsWith(text, ignoreCase = true) }
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
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                val items = listOf("Minutes", "Hours", "Days", "Weeks")

                ScrollPicker(items = items, state = rememberScrollPickerState(), horizontalAlignment = Alignment.End)
                ScrollPicker(items = items, state = rememberScrollPickerState())
                ScrollPicker(items = items, state = rememberScrollPickerState(), horizontalAlignment = Alignment.Start)
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
                        modifier = Modifier.width(100.dp),
                    )
                    Text(text = ":", style = ScrollPickerDefaults.textStyle)
                    ScrollPicker(
                        items = (0..59).toList(),
                        state = rememberScrollPickerState(group = group),
                        loop = true,
                        itemLabel = ::twoDigits,
                        onInput = String::toIntOrNull,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        modifier = Modifier.width(100.dp),
                    )
                }
            }
        }
    }
}

private const val VISIBLE_ITEM_COUNT = 3
