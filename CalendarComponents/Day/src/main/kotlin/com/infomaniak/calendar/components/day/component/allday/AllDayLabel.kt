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
package com.infomaniak.calendar.components.day.component.allday

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.day.TimelineDefaults
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme

/** Names the all-day band from the hour gutter, so the chips still begin where the timed events do. */
@Composable
internal fun AllDayLabel(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.allDayLabel),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        // Held against the chips it names, at the gutter's end rather than at the screen's edge.
        textAlign = TextAlign.End,
        modifier = modifier
            .width(TimelineDefaults.HourGutterWidth)
            .padding(end = EsdsTheme.spacing.lg),
    )
}

@Preview
@Composable
private fun AllDayLabelPreview() {
    Surface {
        AllDayLabel()
    }
}

/** The label wraps within the gutter rather than widening it. */
@Preview(fontScale = 2f)
@Composable
private fun AllDayLabelLargeTextPreview() {
    Surface {
        AllDayLabel()
    }
}
