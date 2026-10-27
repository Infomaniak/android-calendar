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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchableTextTest {

    @Test
    fun find_ignoresCaseAndAccents() {
        assertEquals(0..5, SearchableText("Zürich, Suisse").find("ZURICH"))
        assertEquals(8..10, SearchableText("Heure d’été").find("ete"))
    }

    @Test
    fun find_mapsBackOntoDecomposedText() {
        // "été" written with combining accents is 5 chars long but only 3 once searchable
        assertEquals(0..4, SearchableText("e\u0301te\u0301").find("ete"))
    }

    @Test
    fun find_returnsNullWithoutMatch() {
        assertNull(SearchableText("Zurich").find("paris"))
    }
}
