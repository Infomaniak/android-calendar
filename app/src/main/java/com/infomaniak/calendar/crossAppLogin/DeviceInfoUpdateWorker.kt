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
package com.infomaniak.calendar.crossAppLogin

import android.content.Context
import androidx.work.WorkerParameters
import com.infomaniak.calendar.di.metroAndroidExtensions.worker.MetroWorker
import com.infomaniak.calendar.di.metroAndroidExtensions.worker.WorkerInstanceFactory
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.core.crossapplogin.back.internal.deviceinfo.AbstractDeviceInfoUpdateWorker
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import okhttp3.OkHttpClient

@AssistedInject
class DeviceInfoUpdateWorker(
    appContext: Context,
    @Assisted params: WorkerParameters,
    private val accountUtils: AccountUtils,
) : AbstractDeviceInfoUpdateWorker(appContext, params) {
    override suspend fun getConnectedHttpClient(userId: Int): OkHttpClient = accountUtils.getHttpClient(userId)

    @MetroWorker(DeviceInfoUpdateWorker::class)
    @ContributesIntoMap(scope = AppScope::class, binding = binding<WorkerInstanceFactory<*>>())
    @AssistedFactory
    abstract class Factory : WorkerInstanceFactory<DeviceInfoUpdateWorker>
}
