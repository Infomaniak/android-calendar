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
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.core.app.NotificationCompat
import com.infomaniak.calendar.MainActivity
import com.infomaniak.calendar.R
import com.infomaniak.core.notifications.buildNotificationChannel
import com.infomaniak.core.notifications.createNotificationChannels
import java.util.Date

object NotificationHelper {

    const val CHANNEL_EVENT_REMINDERS = "event_reminders"
    const val EXTRA_OCCURRENCE_ID_JSON = "com.infomaniak.calendar.EXTRA_OCCURRENCE_ID_JSON"

    fun initNotificationChannels(context: Context) {
        val channel = buildNotificationChannel(
            channelId = CHANNEL_EVENT_REMINDERS,
            name = context.getString(R.string.notificationChannelEventRemindersTitle),
            importance = NotificationManager.IMPORTANCE_HIGH,
            description = context.getString(R.string.notificationChannelEventRemindersDescription),
        )
        context.createNotificationChannels(listOf(channel))
    }

    fun buildEventNotification(
        context: Context,
        occurrenceIdJson: String,
        title: String,
        location: String?,
        startInstantMs: Long,
        endInstantMs: Long,
        isAllDay: Boolean,
    ): Notification {
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OCCURRENCE_ID_JSON, occurrenceIdJson)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            occurrenceIdJson.hashCode(),
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val timeText = formatTimingText(context, startInstantMs, endInstantMs, isAllDay)
        val contentText = if (!location.isNullOrBlank()) "$timeText • $location" else timeText
        val notificationTitle = title.ifBlank { context.getString(R.string.notificationDefaultEventTitle) }

        return NotificationCompat.Builder(context, CHANNEL_EVENT_REMINDERS)
            .setSmallIcon(R.drawable.ic_calendar_number_one)
            .setContentTitle(notificationTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun formatTimingText(
        context: Context,
        startInstantMs: Long,
        endInstantMs: Long,
        isAllDay: Boolean,
    ): String {
        if (isAllDay) return context.getString(R.string.notificationAllDay)

        val timeFormat = DateFormat.getTimeFormat(context)
        val startTime = timeFormat.format(Date(startInstantMs))
        val endTime = timeFormat.format(Date(endInstantMs))
        return "$startTime – $endTime"
    }
}
