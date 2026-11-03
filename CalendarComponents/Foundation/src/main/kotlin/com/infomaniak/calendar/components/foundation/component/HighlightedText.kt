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
package com.infomaniak.calendar.components.foundation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.models.SearchableText

/** [text] with its first match of [highlightedText] highlighted, ignoring case and accents. */
@Composable
fun HighlightedText(
    text: SearchableText,
    highlightedText: () -> String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        buildAnnotatedString {
            append(text.text)
            text.find(highlightedText())?.let {
                addStyle(
                    style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
                    start = it.first,
                    end = it.last + 1,
                )
            }
        },
        modifier = modifier,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun HighlightedText(
    text: String,
    highlightedText: () -> String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    HighlightedText(remember(text) { SearchableText(text) }, highlightedText, modifier, style, maxLines, overflow)
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            Column {
                HighlightedText(text = "Résumé review", highlightedText = { "RESUME" })
                HighlightedText(text = "Team meeting", highlightedText = { "meet" })
                HighlightedText(text = "No match here", highlightedText = { "xyz" })
            }
        }
    }
}
