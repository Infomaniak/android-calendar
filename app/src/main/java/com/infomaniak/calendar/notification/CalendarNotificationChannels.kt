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

import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.infomaniak.calendar.R
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.core.common.cancellable
import com.infomaniak.core.notifications.buildNotificationChannel
import com.infomaniak.core.notifications.createNotificationChannels
import com.infomaniak.core.sentry.SentryLog
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarSection
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import com.infomaniak.multiplatform_core.account.domain.model.AccountId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Keeps one notification channel per calendar, so users can tune the reminders of each calendar on its own, grouped by
 * account (named after its email), like Mail does per mailbox.
 */
@Inject
@SingleIn(AppScope::class)
class CalendarNotificationChannels(
    private val appContext: Context,
    private val calendarManager: CalendarManager,
    private val accountUtils: AccountUtils,
) {
    private val notificationManager by lazy { appContext.getSystemService<NotificationManager>() }
    private val syncMutex = Mutex()

    fun startObserving(scope: CoroutineScope) {
        scope.launch {
            wantedChannels().collectLatest { (sections, emails) ->
                // An uncaught failure would crash the app. The next calendars change retries it.
                runCatching { syncChannels(sections, emails) }
                    .cancellable()
                    .onFailure { exception ->
                        SentryLog.e(TAG, "Failed to sync the notification channels", exception)
                    }
            }
        }
    }

    fun hasChannel(calendarId: CalendarId): Boolean {
        return notificationManager?.getNotificationChannel(channelId(calendarId)) != null
    }

    /** Creates the channel of [calendarId] if it's missing, for an alarm firing before [startObserving] synced it. */
    suspend fun ensureChannel(calendarId: CalendarId) {
        if (hasChannel(calendarId)) return
        val (sections, emails) = wantedChannels().first()
        syncChannels(sections, emails)
    }

    private fun wantedChannels(): Flow<Pair<List<CalendarSection>, Map<AccountId, String>>> {
        return combine(calendarManager.observeCalendars(), accountUtils.emailsByUserId, ::Pair)
    }

    private suspend fun syncChannels(sections: List<CalendarSection>, emails: Map<AccountId, String>) = syncMutex.withLock {
        val notificationManager = notificationManager ?: return@withLock

        val groups = sections.map { it.accountId }.distinct().mapNotNull { accountId ->
            emails[accountId]?.let { email -> NotificationChannelGroup(groupId(accountId), email) }
        }
        val groupIds = groups.mapTo(mutableSetOf()) { it.id }

        val description = appContext.getString(R.string.notificationChannelEventRemindersDescription)
        val channels = sections.flatMap { section ->
            section.calendars.map { calendar ->
                buildNotificationChannel(
                    channelId = channelId(calendar.id),
                    name = calendar.displayName,
                    importance = NotificationManager.IMPORTANCE_HIGH,
                    description = description,
                    groupId = groupId(section.accountId).takeIf { it in groupIds },
                )
            }
        }
        val channelIds = channels.mapTo(mutableSetOf()) { it.id }

        // Existing channels only get their name, description and missing group updated, keeping the user's settings
        appContext.createNotificationChannels(channels, groups)

        // A channel created again with the same ID gets back the settings it had when deleted
        notificationManager.notificationChannels
            .filter { it.id == LEGACY_CHANNEL_EVENT_REMINDERS || (it.id.startsWith(CHANNEL_PREFIX) && it.id !in channelIds) }
            .forEach { notificationManager.deleteNotificationChannel(it.id) }
        notificationManager.notificationChannelGroups
            .filter { it.id.startsWith(GROUP_PREFIX) && it.id !in groupIds }
            .forEach { notificationManager.deleteNotificationChannelGroup(it.id) }
    }

    companion object {
        private const val TAG = "CalendarNotificationChannels"
        private const val CHANNEL_PREFIX = "calendar_reminders_"
        private const val GROUP_PREFIX = "account_"
        // The single channel every reminder used to go to
        private const val LEGACY_CHANNEL_EVENT_REMINDERS = "event_reminders"

        fun channelId(calendarId: CalendarId): String = "$CHANNEL_PREFIX${calendarId.url}"

        private fun groupId(accountId: AccountId): String = "$GROUP_PREFIX${accountId.value}"
    }
}
