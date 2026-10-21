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
package com.infomaniak.calendar.components.timezonesearch.models

import java.text.Normalizer

/**
 * [text] made searchable regardless of case and accents.
 *
 * Normalizing char by char keeps track of where each searchable char comes from, so a match maps back onto [text]
 * even when stripping accents or lowercasing changed its length.
 */
internal class SearchableText(val text: String) {
    private val searchable = StringBuilder()
    private val sourceIndices = mutableListOf<Int>()

    init {
        text.forEachIndexed { index, char ->
            val searchableChars = char.toSearchable()
            searchable.append(searchableChars)
            repeat(searchableChars.length) { sourceIndices += index }
        }
    }

    /** Range of [text] matching [query], or null if there is none. */
    fun find(query: String): IntRange? {
        val searchableQuery = buildString { query.forEach { append(it.toSearchable()) } }
        val start = searchable.indexOf(searchableQuery).takeIf { it >= 0 } ?: return null
        return sourceIndexAt(start) until sourceIndexAt(start + searchableQuery.length)
    }

    // Past the last searchable char, the range extends to the end of the text to include any trailing accent.
    private fun sourceIndexAt(searchableIndex: Int) = sourceIndices.getOrElse(searchableIndex) { text.length }
}

private val COMBINING_MARKS = Regex("\\p{M}")

/** `Ü` -> `u` */
private fun Char.toSearchable(): String = Normalizer.normalize(toString(), Normalizer.Form.NFD).replace(COMBINING_MARKS, "").lowercase()
