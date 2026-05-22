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

import com.infomaniak.calendar.BuildConfig

object ConfigUtils {

    /**
     * Application id with the `.debug` suffix stripped out, so that login / cross-app login can resolve
     * the production package even from a debug build.
     *
     * Mirrors `com.infomaniak.swisstransfer.ui.utils.ConfigUtils.safePackage`.
     */
    val safePackage: String by lazy { BuildConfig.APPLICATION_ID.replace(".debug", "") }
}
