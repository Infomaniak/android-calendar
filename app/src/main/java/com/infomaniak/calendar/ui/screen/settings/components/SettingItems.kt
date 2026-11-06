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
package com.infomaniak.calendar.ui.screen.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.ui.theme.CustomColors
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Check
import com.infomaniak.designsystem.icons.symbols.ChevronRight
import com.infomaniak.designsystem.icons.symbols.Cog
import com.infomaniak.designsystem.icons.symbols.SquareArrowOutUpRight
import com.infomaniak.designsystem.icons.symbols.Symbol

@Composable
fun SettingItemClickable(
    shapes: ListItemShapes,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    symbol: Symbol? = null,
    supportingText: String? = null,
    enabled: () -> Boolean = { true },
) {
    SettingItem(
        shapes = shapes,
        text = text,
        onClick = onClick,
        modifier = modifier,
        symbol = symbol,
        supportingText = supportingText,
        enabled = enabled,
        trailingContent = { SettingIcon(EsdsIcons.ChevronRight) },
    )
}

@Composable
fun SettingItemOpen(
    shapes: ListItemShapes,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    symbol: Symbol? = null,
    supportingText: String? = null,
    enabled: () -> Boolean = { true },
) {
    SettingItem(
        shapes = shapes,
        text = text,
        onClick = onClick,
        modifier = modifier,
        symbol = symbol,
        supportingText = supportingText,
        enabled = enabled,
        trailingContent = { SettingIcon(EsdsIcons.SquareArrowOutUpRight) },
    )
}

@Composable
fun SettingItemSwitch(
    shapes: ListItemShapes,
    text: String,
    checked: () -> Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    symbol: Symbol? = null,
    trailingSwitch: SwitchType = SwitchType.Check,
    supportingText: String? = null,
    enabled: () -> Boolean = { true },
    colors: ListItemColors = SettingItemsDefault.colors(),
) {
    val isChecked = checked()

    SegmentedListItem(
        checked = isChecked,
        onCheckedChange = onCheckedChange,
        shapes = shapes,
        modifier = modifier,
        enabled = enabled(),
        leadingContent = symbol?.let { { SettingIcon(it) } },
        trailingContent = { trailingSwitch.content(isChecked)?.invoke() },
        supportingContent = supportingText?.let { { Text(it) } },
        colors = colors,
        content = { Text(text) },
    )
}

@Composable
private fun SettingItem(
    shapes: ListItemShapes,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    symbol: Symbol? = null,
    supportingText: String? = null,
    enabled: () -> Boolean = { true },
    trailingContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = SettingItemsDefault.colors(),
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        modifier = modifier,
        enabled = enabled(),
        leadingContent = symbol?.let { { SettingIcon(it) } },
        trailingContent = trailingContent,
        supportingContent = supportingText?.let { { Text(it) } },
        colors = colors,
        content = { Text(text) },
    )
}

object SettingItemsDefault {
    @Composable
    fun colors(): ListItemColors {
        return ListItemDefaults.segmentedColors(containerColor = CustomColors.SettingItemContainerColor)
    }
}

enum class SwitchType(val content: ((isChecked: Boolean) -> @Composable (() -> Unit)?)) {
    Switch(content = { checked -> { Switch(checked, null) } }),
    Check(content = { checked -> if (checked) ({ SettingIcon(EsdsIcons.Check) }) else null }),
}

object SettingShape {
    val Single @Composable get() = ListItemDefaults.segmentedShapes(index = 0, count = 1)
    val First @Composable get() = ListItemDefaults.segmentedShapes(index = 0, count = 3)
    val Middle @Composable get() = ListItemDefaults.segmentedShapes(index = 1, count = 3)
    val Last @Composable get() = ListItemDefaults.segmentedShapes(index = 2, count = 3)
}

@Composable
private fun SettingsPreviewGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    CalendarThemeForPreview {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(
                modifier = Modifier.padding(EsdsTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                SettingTitle(title)
                content()
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SettingSectionPreview() {
    SettingsPreviewGroup(title = "Section") {
        SettingItemClickable(shapes = SettingShape.First, text = "Clickable", onClick = {})
        SettingItemSwitch(
            shapes = SettingShape.Middle,
            text = "Switch",
            checked = { true },
            onCheckedChange = {},
            trailingSwitch = SwitchType.Switch,
        )
        SettingItemOpen(shapes = SettingShape.Last, text = "Open", onClick = {})
    }
}

@PreviewLightDark
@Composable
private fun SettingItemClickablePreview() {
    SettingsPreviewGroup(title = "Clickable") {
        SettingItemClickable(shapes = SettingShape.First, text = "Default", onClick = {})
        SettingItemClickable(
            shapes = SettingShape.Middle,
            text = "Leading and supporting",
            onClick = {},
            supportingText = "Supporting text",
            symbol = EsdsIcons.Cog,
        )
        SettingItemClickable(shapes = SettingShape.Last, text = "Disabled", onClick = {}, enabled = { false })
    }
}

@PreviewLightDark
@Composable
private fun SettingItemSwitchPreview() {
    SettingsPreviewGroup(title = "Switch") {
        SettingItemSwitch(shapes = SettingShape.First, text = "Checked", checked = { true }, onCheckedChange = {})
        SettingItemSwitch(
            shapes = SettingShape.Middle,
            text = "Unchecked",
            checked = { false },
            onCheckedChange = {},
            supportingText = "Supporting text",
            symbol = EsdsIcons.Cog,
        )
        SettingItemSwitch(
            shapes = SettingShape.Middle,
            text = "Unchecked",
            checked = { false },
            onCheckedChange = {},
            supportingText = "Supporting text",
            symbol = EsdsIcons.Cog,
            trailingSwitch = SwitchType.Switch,
        )
        SettingItemSwitch(
            shapes = SettingShape.Last,
            text = "Disabled",
            checked = { true },
            enabled = { false },
            onCheckedChange = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SettingItemOpenPreview() {
    SettingsPreviewGroup(title = "Open") {
        SettingItemOpen(shapes = SettingShape.First, text = "Default", onClick = {})
        SettingItemOpen(
            shapes = SettingShape.Middle,
            text = "Leading and supporting",
            onClick = {},
            supportingText = "Supporting text",
            symbol = EsdsIcons.Cog,
        )
        SettingItemOpen(shapes = SettingShape.Last, text = "Disabled", onClick = {}, enabled = { false })
    }
}
