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
package com.infomaniak.calendar.components.foundation.utils.timeFormatter

import android.icu.text.DateTimePatternGenerator
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

// Plumbing shared by the formatter files. Use the dedicated formatter files instead to format anything you'd need.

private val formatters = ConcurrentHashMap<Pair<String, Locale>, DateTimeFormatter>()
private val localizedPatterns = ConcurrentHashMap<Pair<String, Locale>, String>()
private val dateTimeGlues = ConcurrentHashMap<Locale, String>()

private val quotedLiteral = Regex("'(.*?)'")

@Composable
internal fun currentLocale(): Locale = LocalLocale.current.platformLocale

@Composable
internal fun isUsing24HourFormat(): Boolean = DateFormat.is24HourFormat(LocalContext.current)

/** Formatter for a fixed [pattern], whose fields keep the specified order in every locale. */
internal fun fixedFormatter(pattern: String, locale: Locale): DateTimeFormatter {
    return formatters.getOrPut(pattern to locale) { DateTimeFormatter.ofPattern(pattern, locale) }
}

/**
 * Formatter for the fields of [skeleton], laid out and punctuated the way [locale] writes them.
 *
 * A skeleton only declares which fields to show, so unlike a fixed pattern it carries no field order of its own.
 */
internal fun localizedFormatter(skeleton: String, locale: Locale): DateTimeFormatter {
    val pattern = localizedPatterns.getOrPut(skeleton to locale) {
        DateTimePatternGenerator.getInstance(locale).getBestPattern(skeleton)
    }

    return fixedFormatter(pattern, locale)
}

/** Joins an already formatted [date] and [time] the way [locale] words it, e.g. `May 20, 08:00` or `20. toukokuuta klo 08:00`. */
internal fun joinDateAndTime(date: String, time: String, locale: Locale): String {
    val glue = dateTimeGlues.getOrPut(locale) {
        // the dateTimeFormat can return something of the form `{1} 'at' {0}`
        DateTimePatternGenerator.getInstance(locale).dateTimeFormat.unquoteLiterals()
    }

    return glue.replace("{1}", date).replace("{0}", time)
}

internal fun String.titlecaseFirstChar(locale: Locale): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

// If we find two consecutive quotes, it represents an escaped single quote and must not disappear. This will lead to an empty
// group which is why it defaults to the single quote character if empty.
private fun String.unquoteLiterals(): String = replace(quotedLiteral) { it.groupValues[1].ifEmpty { "'" } }
