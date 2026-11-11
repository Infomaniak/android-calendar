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
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.network.NetworkClient
import coil3.network.NetworkFetcher
import coil3.network.NetworkRequest
import coil3.network.NetworkResponse
import coil3.network.okhttp.asNetworkClient
import com.infomaniak.calendar.components.foundation.manager.AvatarFinder
import com.infomaniak.calendar.components.foundation.models.AvatarData
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.calendar.utils.avatarData
import com.infomaniak.multiplatform_core.contacts.ContactsManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.concurrent.ConcurrentHashMap

@Inject
@SingleIn(AppScope::class)
class AppAvatarFinder(
    private val appContext: Context,
    private val contactsManager: ContactsManager,
    private val accountUtils: AccountUtils,
) : AvatarFinder {
    private val imageLoaders = ConcurrentHashMap<Long, ImageLoader>()

    override suspend fun avatarOf(name: String?, email: String): AvatarData? {
        return contactsManager.findContact(email, name)?.avatarData
    }

    @OptIn(ExperimentalCoilApi::class)
    override fun imageLoaderOf(accountId: Long?): ImageLoader {
        val default = SingletonImageLoader.get(appContext)
        if (accountId == null) return default

        return imageLoaders.getOrPut(accountId) {
            // newBuilder() keeps the singleton's memory and disk caches, so loaders only differ by their authenticated client
            default.newBuilder()
                .components { add(NetworkFetcher.Factory(networkClient = { AccountNetworkClient(accountId.toInt()) })) }
                .build()
        }
    }

    private inner class AccountNetworkClient(private val userId: Int) : NetworkClient {
        override suspend fun <T> executeRequest(request: NetworkRequest, block: suspend (NetworkResponse) -> T): T {
            return accountUtils.getHttpClient(userId).asNetworkClient().executeRequest(request, block)
        }
    }
}
