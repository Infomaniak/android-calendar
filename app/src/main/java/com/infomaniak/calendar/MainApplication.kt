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
package com.infomaniak.calendar

import android.app.Application
import android.os.StrictMode
import com.infomaniak.calendar.crossAppLogin.DeviceInfoUpdateWorker
import com.infomaniak.calendar.di.AppGraph
import com.infomaniak.calendar.di.metroAndroidExtensions.MetroApplication
import com.infomaniak.calendar.utils.ConfigUtils
import com.infomaniak.core.common.AssociatedUserDataCleanable
import com.infomaniak.core.crossapplogin.back.internal.deviceinfo.DeviceInfoUpdateManager
import com.infomaniak.core.network.NetworkConfiguration
import com.infomaniak.core.sentry.SentryConfig.configureSentry
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainApplication : Application(), MetroApplication {
    override val appGraph by lazy { createGraphFactory<AppGraph.Factory>().create(applicationContext) }
    private val applicationScope = CoroutineScope(Dispatchers.Default + CoroutineName(this::class.java.simpleName))

    override fun onCreate() {
        super.onCreate()

        setupStrictModePolicies()

        NetworkConfiguration.init(
            appId = ConfigUtils.safePackage,
            appVersionCode = BuildConfig.VERSION_CODE,
            appVersionName = BuildConfig.VERSION_NAME,
        )

        configureSentry()

        MatomoCalendar.addTrackingCallbackForDebugLog()
        initCrossAppLogin()

        loadCalDavCredential()
    }

    private fun loadCalDavCredential() {
        applicationScope.launch {
            appGraph.davCredentialsManager.initStoredCredentials()
        }
    }

    /**
     * Reasons to discard Sentry events :
     * - Application is in Debug mode
     * - The exception was a NetworkException or a CancellationException, and we don't want to send them to Sentry
     */
    private fun configureSentry() {
        configureSentry(
            isDebug = BuildConfig.DEBUG,
            isSentryTrackingEnabled = { true }, // TODO[matomo]: Enable or disable based on user settings
        )
    }

    private fun setupStrictModePolicies() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyFlashScreen()
                    .build(),
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build(),
            )
        }
    }

    private fun initCrossAppLogin() {
        applicationScope.launch {
            DeviceInfoUpdateManager.scheduleWorkerOnDeviceInfoUpdate<DeviceInfoUpdateWorker>()
        }
    }

    companion object {
        @JvmStatic
        val userDataCleanableList: List<AssociatedUserDataCleanable> = listOf(DeviceInfoUpdateManager)
    }
}
