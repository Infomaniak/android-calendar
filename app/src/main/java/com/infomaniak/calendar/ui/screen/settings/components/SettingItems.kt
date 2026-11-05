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
import androidx.compose.material3.Icon
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
import com.infomaniak.designsystem.icons.symbols.ChevronRight
import com.infomaniak.designsystem.icons.symbols.Cog
import com.infomaniak.designsystem.icons.symbols.SquareArrowOutUpRight
import com.infomaniak.designsystem.icons.symbols.Symbol

@Composable
fun SettingItemClickable(
    shapes: SettingShape,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.Companion,
    supportingText: String? = null,
    enabled: () -> Boolean = { true },
    leadingContent: @Composable (() -> Unit)? = null,
) {
    SettingItem(
        shapes = shapes,
        text = text,
        onClick = onClick,
        modifier = modifier,
        supportingText = supportingText,
        enabled = enabled,
        leadingContent = leadingContent,
        trailingContent = { SettingIcon(EsdsIcons.ChevronRight) },
    )
}

@Composable
fun SettingItemOpen(
    shapes: SettingShape,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.Companion,
    supportingText: String? = null,
    enabled: () -> Boolean = { true },
    leadingContent: @Composable (() -> Unit)? = null,
) {
    SettingItem(
        shapes = shapes,
        text = text,
        onClick = onClick,
        modifier = modifier,
        supportingText = supportingText,
        enabled = enabled,
        leadingContent = leadingContent,
        trailingContent = { SettingIcon(EsdsIcons.SquareArrowOutUpRight) },
    )
}

@Composable
fun SettingItemSwitch(
    shapes: SettingShape,
    text: String,
    checked: () -> Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier.Companion,
    trailingSwitch: SwitchType = SwitchType.None,
    supportingText: String? = null,
    enabled: () -> Boolean = { true },
    colors: ListItemColors = SettingItemsDefault.colors(),
) {
    val isChecked = checked()

    SegmentedListItem(
        checked = isChecked,
        onCheckedChange = onCheckedChange,
        shapes = shapes.segmentedShape(),
        modifier = modifier,
        enabled = enabled(),
        trailingContent = { trailingSwitch.content?.invoke(isChecked) },
        supportingContent = supportingText?.let { { Text(it) } },
        colors = colors,
        content = { Text(text) },
    )
}

@Composable
private fun SettingItem(
    shapes: SettingShape,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.Companion,
    supportingText: String? = null,
    enabled: () -> Boolean = { true },
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = SettingItemsDefault.colors(),
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes.segmentedShape(),
        modifier = modifier,
        enabled = enabled(),
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        supportingContent = supportingText?.let { { Text(it) } },
        colors = colors,
        content = { Text(text) },
    )
}

@Composable
private fun SettingIcon(symbol: Symbol, modifier: Modifier = Modifier.Companion) {
    Icon(symbol.Small, null, modifier)
}

object SettingItemsDefault {
    @Composable
    fun colors(): ListItemColors {
        return ListItemDefaults.segmentedColors(containerColor = CustomColors.SettingItemContainerColor)
    }

}

enum class SwitchType(val content: @Composable ((isChecked: Boolean) -> Unit)? = null) {
    Switch(content = { checked -> Switch(checked, null) }),
    None,
}

enum class SettingShape(val segmentedShape: @Composable () -> ListItemShapes) {
    Single({ ListItemDefaults.segmentedShapes(index = 0, count = 1) }),
    First({ ListItemDefaults.segmentedShapes(index = 0, count = 3) }),
    Middle({ ListItemDefaults.segmentedShapes(index = 1, count = 3) }),
    Last({ ListItemDefaults.segmentedShapes(index = 2, count = 3) }),
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

@Composable
private fun PreviewLeadingIcon() {
    Icon(EsdsIcons.Cog.Small, contentDescription = null)
}

@PreviewLightDark
@Composable
private fun SettingSectionPreview() {
    SettingsPreviewGroup(title = "Section") {
        SettingItemClickable(shapes = SettingShape.First, text = "Clickable", onClick = {})
        SettingItemSwitch(shapes = SettingShape.Middle, text = "Switch", checked = { true }, onCheckedChange = {})
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
            leadingContent = { PreviewLeadingIcon() },
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
            leadingContent = { PreviewLeadingIcon() },
        )
        SettingItemOpen(shapes = SettingShape.Last, text = "Disabled", onClick = {}, enabled = { false })
    }
}
