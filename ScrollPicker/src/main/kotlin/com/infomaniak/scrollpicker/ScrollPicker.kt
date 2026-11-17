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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor
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
 * Closing the keyboard also ends the input, bringing the wheel back.
 *
 * Input can also be started from code with a [androidx.compose.ui.focus.FocusRequester] given in [modifier], while
 * [androidx.compose.ui.focus.focusProperties] in [modifier] changes where [ImeAction.Next] and [ImeAction.Previous] go.
 * As moving the focus wraps around, the last of chained pickers should end the input rather than use [ImeAction.Next].
 * Chained pickers sharing a [ScrollPickerGroupState] in their [ScrollPickerState] also react as one: while any of them is
 * edited, all of them only show their selected value, and dragging or tapping a hidden row of any of them ends the input.
 *
 * @param items the values to pick from, numbers, text or anything [itemLabel] can render.
 * @param state hoisted selection and scroll state, see [rememberScrollPickerState].
 * @param contentDescription text used by accessibility services to describe what this picker selects, e.g. "Hours".
 * @param loop whether the wheel wraps around infinitely instead of stopping at the first and last item.
 * @param itemLabel the text displayed for an item.
 * @param itemAccessibilityDescription the text accessibility services announce for an item, e.g. "9 hours" for "09".
 * It should match the text rendered by [itemLabel].
 * @param onInput parses the text typed after clicking the selected item, returning `null` to reject it.
 * When `null`, the selected item isn't clickable and values can only be picked by scrolling.
 * @param keyboardOptions keyboard used for manual input, e.g. [androidx.compose.ui.text.input.KeyboardType.Number].
 * @param horizontalAlignment where the labels sit within the picker's width, e.g. to line up hours on their end and
 * minutes on their start.
 * @param itemHeight height of each row, which must round to at least one pixel.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ScrollPicker(
    items: List<T>,
    state: ScrollPickerState,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    loop: Boolean = false,
    enabled: Boolean = true,
    itemLabel: (T) -> String = { it.toString() },
    itemAccessibilityDescription: (T) -> String = itemLabel,
    onInput: ((String) -> T?)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    itemHeight: Dp = ScrollPickerDefaults.ItemHeight,
    textStyle: TextStyle = ScrollPickerDefaults.textStyle,
    inactiveTextStyle: TextStyle = ScrollPickerDefaults.inactiveTextStyle,
    colors: ScrollPickerColors = ScrollPickerDefaults.colors(),
) {
    val labels = remember(items, itemLabel) { items.map(itemLabel) }
    val descriptions = remember(items, itemAccessibilityDescription) { items.map(itemAccessibilityDescription) }
    val itemCount = labels.size
    val listState = state.lazyListState
    val listItemCount = remember(state, itemCount, loop) { state.setItems(itemCount, loop) }

    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.roundToPx() }
    require(itemHeightPx > 0) { "itemHeight must be at least one pixel" }
    LaunchedEffect(state, itemCount, loop, itemHeightPx) { state.followCenteredRow(itemHeightPx) }
    LaunchedEffect(state, itemCount, loop) { state.animateToSelection() }

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

    // Closing the keyboard keeps the focus, so end the input ourselves to bring the wheels back.
    val isImeVisible by rememberUpdatedState(WindowInsets.isImeVisible)
    LaunchedEffect(state) {
        snapshotFlow { isImeVisible }.drop(1).filter { !it }.collect {
            if (state.isEditing) focusManager.clearFocus()
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val width = remember(labels, textStyle, inactiveTextStyle, textMeasurer, density) {
        with(density) { textMeasurer.widestWidth(labels, textStyle, inactiveTextStyle).toDp() }
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
        modifier = modifier.pickerSemantics(
            contentDescription = contentDescription,
            descriptions = descriptions,
            state = state,
            enabled = enabled,
            scope = scope,
            inputFocusRequester = onInput?.let { inputFocusRequester },
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (onInput != null) Box(
            Modifier
                .matchParentSize()
                .zIndex(if (state.isEditing) 1f else 0f),
            contentAlignment = Alignment.Center,
        ) {
            ScrollPickerInputField(
                items = items,
                labels = labels,
                state = state,
                onInput = onInput,
                enabled = enabled,
                keyboardOptions = keyboardOptions,
                textStyle = textStyle.copy(color = colors.textColor(enabled, pressed = true, inactive = false)),
                horizontalAlignment = horizontalAlignment,
                cursorColor = colors.cursorColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .focusRequester(inputFocusRequester)
                    .onFocusChanged { group.setEditing(state, it.isFocused) }
                    .graphicsLayer { alpha = if (state.isEditing) 1f else 0f },
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .matchParentSize()
                .clipToBounds(),
            contentAlignment = Alignment.Center,
        ) {
            val rowHeight = with(density) { itemHeightPx.toDp() }
            val wheelHeight = maxOf(maxHeight, rowHeight)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(wheelHeight)
                    .clearAndSetSemantics {}
                    .graphicsLayer { alpha = if (state.isEditing) 0f else 1f },
                state = listState,
                contentPadding = PaddingValues(vertical = (wheelHeight - rowHeight) / 2),
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
                        inactiveTextStyle = inactiveTextStyle,
                        horizontalAlignment = horizontalAlignment,
                        colors = colors,
                        onClick = { onItemClick(listIndex) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight),
                    )
                }
            }
        }

        // Sizes the picker by default, in pixels so the selection lines up with whole items.
        Spacer(Modifier.size(width, with(density) { (itemHeightPx * VISIBLE_ITEM_COUNT).toDp() }))
    }
}

/** Width of the widest of [labels], in whichever style is wider. Measured once, so skips the measurer's cache. */
private fun TextMeasurer.widestWidth(labels: List<String>, textStyle: TextStyle, inactiveTextStyle: TextStyle): Int {
    return labels.maxOfOrNull { label ->
        maxOf(
            measure(label, textStyle, skipCache = true).size.width,
            measure(label, inactiveTextStyle, skipCache = true).size.width,
        )
    } ?: 0
}

/** Exposes one adjustable value until editing, when the input field exposes its own semantics. */
private fun Modifier.pickerSemantics(
    contentDescription: String?,
    descriptions: List<String>,
    state: ScrollPickerState,
    enabled: Boolean,
    scope: CoroutineScope,
    inputFocusRequester: FocusRequester?,
): Modifier = if (state.isEditing) this else clearAndSetSemantics {
    val itemCount = descriptions.size
    val selectedIndex = state.selectedIndex
    if (contentDescription != null) this.contentDescription = contentDescription
    descriptions.getOrNull(selectedIndex)?.let { stateDescription = it }
    progressBarRangeInfo = ProgressBarRangeInfo(
        current = selectedIndex.toFloat(),
        range = 0f..(itemCount - 1).coerceAtLeast(0).toFloat(),
        steps = (itemCount - 2).coerceAtLeast(0),
    )
    if (enabled && itemCount > 0) setProgress { target ->
        // With two items, steps is 0 so TalkBack treats the range as continuous and moves by a 20th of it (e.g. 0.05),
        // which rounding would bring back to the current index. Rounding towards the requested direction moves one item.
        val index = when {
            target > selectedIndex -> ceil(target)
            target < selectedIndex -> floor(target)
            else -> target
        }.roundToInt()
        scope.launch { state.scrollToIndex(index.coerceIn(0, itemCount - 1)) }
        true
    }
    if (enabled && itemCount > 0 && inputFocusRequester != null) onClick {
        inputFocusRequester.requestFocus()
        true
    }
}

/**
 * Draws the label twice: with [inactiveTextStyle] outside the selection area, the center row of the wheel, and with
 * [textStyle] inside it, so an item crossing its border is split between both styles. Only the latter is drawn
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
    inactiveTextStyle: TextStyle,
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
                val inactiveText = textMeasurer.measure(label, inactiveTextStyle)
                onDrawBehind {
                    val pressed = listState.isScrollInProgress
                    // Read while drawing so that scrolling only redraws. The row at the wheel's content start is selected.
                    val selectionTop =
                        (listState.firstVisibleItemIndex - listIndex) * size.height + listState.firstVisibleItemScrollOffset
                    val selectionBottom = selectionTop + size.height
                    if (!group.isEditing) {
                        clipRect(top = selectionTop, bottom = selectionBottom, clipOp = ClipOp.Difference) {
                            drawAligned(inactiveText, horizontalAlignment, colors.textColor(enabled, pressed, inactive = true))
                        }
                    }
                    clipRect(top = selectionTop, bottom = selectionBottom) {
                        drawAligned(text, horizontalAlignment, colors.textColor(enabled, pressed, inactive = false))
                    }
                }
            },
    )
}

private fun DrawScope.drawAligned(text: TextLayoutResult, alignment: Alignment.Horizontal, color: Color) {
    val x = alignment.align(text.size.width, size.width.roundToInt(), layoutDirection)
    drawText(text, color, Offset(x.toFloat(), (size.height - text.size.height) / 2f))
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
                    contentDescription = null,
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

                ScrollPicker(
                    items = items,
                    state = rememberScrollPickerState(),
                    contentDescription = null,
                    horizontalAlignment = Alignment.End,
                )
                ScrollPicker(items = items, state = rememberScrollPickerState(), contentDescription = null)
                ScrollPicker(
                    items = items,
                    state = rememberScrollPickerState(),
                    contentDescription = null,
                    horizontalAlignment = Alignment.Start,
                )
            }
        }
    }
}

@Preview
@Composable
private fun TimePreview() {
    MaterialTheme(typography = Typography(fontFamily = FontFamily.SansSerif)) { // Use correct typography with emphasized tokens defined for expressive
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
                        contentDescription = null,
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
                        contentDescription = null,
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
