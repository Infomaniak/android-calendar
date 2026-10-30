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
package com.infomaniak.calendar.utils

import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.multiplatform_core.contacts.domain.model.Contact

val Contact.contactKey: String
    get() = ContactKey(name = name, email = email).toString()

val AttendeeUi.contactKey: String
    get() = ContactKey(name = displayName, email = email).toString()

/** A unique key that matches between [Contact] and [AttendeeUi] so we can compare them. It's unique for both. */
private data class ContactKey(val name: String?, val email: String)
