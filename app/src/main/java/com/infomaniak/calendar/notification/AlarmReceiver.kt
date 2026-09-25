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
import androidx.core.app.NotificationManagerCompat
import com.infomaniak.calendar.extensions.appGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.Clock

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmScheduler.ACTION_EVENT_REMINDER) return

        val occurrenceIdJson = intent.getStringExtra(AlarmScheduler.EXTRA_OCCURRENCE_ID_JSON) ?: return
        val alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID)
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_EVENT_TITLE).orEmpty()
        val location = intent.getStringExtra(AlarmScheduler.EXTRA_EVENT_LOCATION)
        val startInstantMs = intent.getLongExtra(AlarmScheduler.EXTRA_EVENT_START_MS, 0L)
        val endInstantMs = intent.getLongExtra(AlarmScheduler.EXTRA_EVENT_END_MS, 0L)
        val isAllDay = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_ALL_DAY, false)

        val notification = NotificationHelper.buildEventNotification(
            context = context,
            occurrenceIdJson = occurrenceIdJson,
            title = title,
            location = location,
            startInstantMs = startInstantMs,
            endInstantMs = endInstantMs,
            isAllDay = isAllDay,
        )

        val notificationId = alarmId?.hashCode() ?: occurrenceIdJson.hashCode()
        val notificationManager = NotificationManagerCompat.from(context)
        if (notificationManager.areNotificationsEnabled()) {
            runCatching {
                notificationManager.notify(notificationId, notification)
            }
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val appGraph = context.appGraph
                if (alarmId != null) {
                    appGraph.calendarDataValues.scheduledAlarmIds.update { it - alarmId }
                }
                appGraph.alarmScheduler.refreshUpcomingAlarms(from = Clock.System.now())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
