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

import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventColors
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.AlarmAction
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.AlarmTrigger
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.EventAlarm
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.TriggerRelation
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.UpcomingAlarm
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.UpcomingAlarmId
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Instant

class AlarmSchedulerTest {

    @Test
    fun `max scheduled alarms constant leaves safety margin below Android system limit`() {
        // Android AlarmManagerService enforces MAX_ALARMS_PER_UID = 500.
        // We ensure our cap leaves a comfortable margin for system safety.
        assertEquals(400, AlarmScheduler.MAX_SCHEDULED_ALARMS)
        assertTrue(AlarmScheduler.MAX_SCHEDULED_ALARMS < 500)
    }

    @Test
    fun `alarms in the past are excluded from scheduling`() {
        val nowMs = 1_000_000L
        val pastAlarm = testAlarm(id = "past", firesAtMs = nowMs - 1000)
        val futureAlarm = testAlarm(id = "future", firesAtMs = nowMs + 1000)

        val plan = AlarmScheduler.computeAlarmSyncPlan(
            upcomingAlarms = listOf(pastAlarm, futureAlarm),
            previouslyScheduledIds = emptySet(),
            nowMs = nowMs,
        )

        assertEquals(1, plan.toSchedule.size)
        assertEquals("future", plan.toSchedule.single().id.value)
        assertEquals(setOf("future"), plan.newScheduledIds)
    }

    @Test
    fun `when upcoming alarms exceed 400, only the first 400 are scheduled`() {
        val nowMs = 1_000_000L
        val totalAlarms = 401
        val alarms = (1..totalAlarms).map { index ->
            testAlarm(id = "alarm-$index", firesAtMs = nowMs + index * 1000)
        }

        val plan = AlarmScheduler.computeAlarmSyncPlan(
            upcomingAlarms = alarms,
            previouslyScheduledIds = emptySet(),
            nowMs = nowMs,
        )

        assertEquals(400, plan.toSchedule.size)
        assertEquals(400, plan.newScheduledIds.size)
        assertTrue(plan.newScheduledIds.contains("alarm-1"))
        assertTrue(plan.newScheduledIds.contains("alarm-400"))
        assertFalse(plan.newScheduledIds.contains("alarm-401"))
    }

    @Test
    fun `when first alarm fires and is removed, 401st alarm enters the schedule`() {
        val nowMs = 1_000_000L
        val totalAlarms = 401
        val allAlarms = (1..totalAlarms).map { index ->
            testAlarm(id = "alarm-$index", firesAtMs = nowMs + index * 1000)
        }

        // Initial sync: alarms 1..400 scheduled
        val initialPlan = AlarmScheduler.computeAlarmSyncPlan(
            upcomingAlarms = allAlarms,
            previouslyScheduledIds = emptySet(),
            nowMs = nowMs,
        )
        assertEquals(400, initialPlan.newScheduledIds.size)
        assertFalse(initialPlan.newScheduledIds.contains("alarm-401"))

        // Time passes: alarm-1 fired, nowMs advanced past alarm-1
        val advancedNowMs = nowMs + 1500
        val remainingAlarms = allAlarms.subList(1, totalAlarms) // alarms 2..401

        val updatedPlan = AlarmScheduler.computeAlarmSyncPlan(
            upcomingAlarms = remainingAlarms,
            previouslyScheduledIds = initialPlan.newScheduledIds,
            nowMs = advancedNowMs,
        )

        // alarm-1 must be cancelled
        assertEquals(setOf("alarm-1"), updatedPlan.toCancel)
        // alarm-401 is now included
        assertTrue(updatedPlan.newScheduledIds.contains("alarm-401"))
        assertEquals(400, updatedPlan.newScheduledIds.size)
    }

    @Test
    fun `diffing properly cancels removed alarms and retains unchanged ones`() {
        val nowMs = 1_000_000L
        val alarm2 = testAlarm(id = "alarm-2", firesAtMs = nowMs + 2000)
        val alarm3 = testAlarm(id = "alarm-3", firesAtMs = nowMs + 3000)

        val plan = AlarmScheduler.computeAlarmSyncPlan(
            upcomingAlarms = listOf(alarm2, alarm3),
            previouslyScheduledIds = setOf("alarm-1", "alarm-2"),
            nowMs = nowMs,
        )

        assertEquals(setOf("alarm-1"), plan.toCancel)
        assertEquals(setOf("alarm-2", "alarm-3"), plan.newScheduledIds)
    }

    private fun testAlarm(id: String, firesAtMs: Long): UpcomingAlarm {
        val eventId = EventId("event://$id")
        val event = Event(
            masterEventId = eventId,
            occurrenceId = OccurrenceId.Master(eventId),
            calendarId = CalendarId("calendar://test"),
            accountId = AccountId(1L),
            title = "Test Event $id",
            timing = EventTiming(
                start = LocalDateTime(2026, 9, 18, 10, 0),
                end = LocalDateTime(2026, 9, 18, 11, 0),
                startTimeZone = TimeZone.UTC,
                endTimeZone = TimeZone.UTC,
                isAllDay = false,
            ),
            colors = EventColors.from(eventSourceColor = 0, calendarSourceColor = 0),
            canEdit = true,
        )
        return UpcomingAlarm(
            id = createUpcomingAlarmId(id),
            firesAt = Instant.fromEpochMilliseconds(firesAtMs),
            alarm = EventAlarm(AlarmAction.Display, AlarmTrigger.Relative(Duration.ZERO, TriggerRelation.Start)),
            event = event,
        )
    }

    private fun createUpcomingAlarmId(value: String): UpcomingAlarmId {
        val constructor = UpcomingAlarmId::class.java.getDeclaredConstructor(String::class.java)
        constructor.isAccessible = true
        return constructor.newInstance(value)
    }
}
