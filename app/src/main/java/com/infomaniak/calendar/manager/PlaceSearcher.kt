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
package com.infomaniak.calendar.manager

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build.VERSION.SDK_INT
import com.infomaniak.calendar.components.foundation.models.LocationUi
import com.infomaniak.core.common.cancellable
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Searches places through the platform [Geocoder], which some devices don't have (without Google Play services, mostly). */
@Inject
class PlaceSearcher(appContext: Context) {
    private val geocoder = if (Geocoder.isPresent()) Geocoder(appContext) else null

    private val legacyGeocoderScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    /** Empty when the device has no geocoder or the search fails, offline for instance. */
    suspend fun search(query: String): List<LocationUi.Place> {
        val geocoder = geocoder ?: return emptyList()

        return runCatching { geocoder.addressesOf(query) }
            .cancellable()
            .getOrDefault(emptyList())
            .mapNotNull { it.toPlaceUi() }
            .distinct()
    }

    private suspend fun Geocoder.addressesOf(query: String): List<Address> = if (SDK_INT >= 33) {
        suspendCancellableCoroutine { continuation ->
            getFromLocationName(
                query, MAX_RESULTS,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) = continuation.resume(addresses)
                    override fun onError(errorMessage: String?) = continuation.resumeWithException(IOException(errorMessage))
                },
            )
        }
    } else {
        @Suppress("DEPRECATION")
        val lookup = legacyGeocoderScope.async { getFromLocationName(query, MAX_RESULTS).orEmpty() }
        try {
            lookup.await()
        } finally {
            lookup.cancel() // Drops it when it hasn't started yet; a running lookup can't be interrupted, its result is ignored
        }
    }

    /** The place's name above its address, when the address doesn't already hold it (a shop, a monument…). */
    private fun Address.toPlaceUi(): LocationUi.Place? {
        val address = getAddressLine(0) ?: return featureName?.let { LocationUi.Place(name = it, address = null) }
        val name = featureName?.takeUnless { it in address } ?: return LocationUi.Place(name = address, address = null)
        return LocationUi.Place(name = name, address = address)
    }

    companion object {
        private const val MAX_RESULTS = 3
    }
}
