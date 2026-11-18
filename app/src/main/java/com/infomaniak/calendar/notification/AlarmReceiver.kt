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

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.infomaniak.calendar.extensions.appGraph
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AlarmScheduler.ACTION_EVENT_REMINDER -> runAsync { handleEventReminder(context, intent) }
            AlarmScheduler.ACTION_REFRESH_ALARMS -> runAsync { syncAlarms(context) }
        }
    }

    private suspend fun handleEventReminder(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID)
        intent.toNotification(context)?.let { (tag, notification) -> postNotification(context, tag, notification) }
        syncAlarms(context, firedAlarmId = alarmId)
    }

    private suspend fun Intent.toNotification(context: Context): Pair<String, Notification>? {
        val occurrenceIdJson = getStringExtra(AlarmScheduler.EXTRA_OCCURRENCE_ID_JSON) ?: return null
        val calendarId = getStringExtra(AlarmScheduler.EXTRA_CALENDAR_ID)?.let(::CalendarId) ?: return null
        val alarmId = getStringExtra(AlarmScheduler.EXTRA_ALARM_ID)

        // Without its channel, the notification would be silently dropped
        withTimeoutOrNull(CHANNEL_TIMEOUT) { context.appGraph.calendarNotificationChannels.ensureChannel(calendarId) }

        val notification = NotificationHelper.buildEventNotification(
            context = context,
            channelId = CalendarNotificationChannels.channelId(calendarId),
            occurrenceIdJson = occurrenceIdJson,
            title = getStringExtra(AlarmScheduler.EXTRA_EVENT_TITLE).orEmpty(),
            location = getStringExtra(AlarmScheduler.EXTRA_EVENT_LOCATION),
            startInstantMs = getLongExtra(AlarmScheduler.EXTRA_EVENT_START_MS, 0L),
            endInstantMs = getLongExtra(AlarmScheduler.EXTRA_EVENT_END_MS, 0L),
            isAllDay = getBooleanExtra(AlarmScheduler.EXTRA_IS_ALL_DAY, false),
        )
        // Tagged with the full ID, as its hash code could be the same as another reminder's
        return (alarmId ?: occurrenceIdJson) to notification
    }

    private fun postNotification(context: Context, tag: String, notification: Notification) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (notificationManager.areNotificationsEnabled()) {
            runCatching { notificationManager.notify(tag, NOTIFICATION_ID, notification) }
        }
    }

    private suspend fun syncAlarms(context: Context, firedAlarmId: String? = null) {
        val appGraph = context.appGraph
        if (firedAlarmId != null) {
            appGraph.calendarDataValues.scheduledAlarmIds.update { it - firedAlarmId }
        }
        appGraph.alarmScheduler.syncUpcomingAlarms()
    }

    /** Keeps the broadcast alive until [block] is over, as this process may be killed or frozen once it's done. */
    private fun runAsync(block: suspend () -> Unit) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                block()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 0
        // Leaves the alarms sync most of the 10 s a broadcast receiver is given to finish
        private val CHANNEL_TIMEOUT = 1.seconds
    }
}
