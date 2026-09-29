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
package com.infomaniak.calendar.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.infomaniak.calendar.extensions.appGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.Clock

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!shouldRefreshAlarms(intent)) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val appGraph = context.appGraph
                if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                    appGraph.calendarDataValues.scheduledAlarmIds.setValue(emptySet())
                }
                appGraph.alarmScheduler.refreshUpcomingAlarms(from = Clock.System.now())
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun shouldRefreshAlarms(intent: Intent): Boolean {
        return when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> true
            else -> false
        }
    }
}
