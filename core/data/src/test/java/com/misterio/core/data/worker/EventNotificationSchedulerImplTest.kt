package com.misterio.core.data.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.misterio.core.data.receiver.EventAlarmReceiver
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.curakid.core.common.SettingsManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.util.UUID

class EventNotificationSchedulerImplTest {

    private val context: Context = mockk(relaxed = true)
    private val alarmManager: AlarmManager = mockk(relaxed = true)
    private val settingsManager: SettingsManager = mockk()

    private lateinit var scheduler: EventNotificationSchedulerImpl

    @Before
    fun setUp() {
        mockkStatic(PendingIntent::class)
        every { context.getSystemService(Context.ALARM_SERVICE) } returns alarmManager
        every { context.packageName } returns "com.misterio.curakid"
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns mockk(relaxed = true)

        scheduler = EventNotificationSchedulerImpl(context, settingsManager)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `schedule does nothing when notifications are disabled in settings`() = runTest {
        every { settingsManager.isNotificationsEnabled } returns flowOf(false)

        val event = Event.General(
            id = UUID.randomUUID(),
            title = "Огляд педіатра",
            type = EventType.DOCTOR,
            description = "Плановий",
            date = LocalDateTime.now().plusHours(2),
            status = EventStatus.PLANNED
        )

        scheduler.schedule(listOf(event))

        verify(exactly = 0) { alarmManager.setExactAndAllowWhileIdle(any(), any(), any()) }
    }

    @Test
    fun `schedule skips events that are in the past`() = runTest {
        every { settingsManager.isNotificationsEnabled } returns flowOf(true)

        val pastEvent = Event.General(
            id = UUID.randomUUID(),
            title = "Минула подія",
            type = EventType.USER,
            description = null,
            date = LocalDateTime.now().minusHours(1),
            status = EventStatus.PLANNED
        )

        scheduler.schedule(listOf(pastEvent))

        verify(exactly = 0) { alarmManager.setExactAndAllowWhileIdle(any(), any(), any()) }
    }

    @Test
    fun `schedule sets exact alarm when enabled and event is in the future`() = runTest {
        every { settingsManager.isNotificationsEnabled } returns flowOf(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            every { alarmManager.canScheduleExactAlarms() } returns true
        }

        val futureEvent = Event.General(
            id = UUID.randomUUID(),
            title = "Щеплення",
            type = EventType.VACCINE,
            description = "В поліклініці",
            date = LocalDateTime.now().plusMinutes(30),
            status = EventStatus.PLANNED
        )

        scheduler.schedule(listOf(futureEvent))

        verify(atLeast = 1) {
            alarmManager.setExactAndAllowWhileIdle(
                eq(AlarmManager.RTC_WAKEUP),
                any(),
                any()
            )
        }
    }

    @Test
    fun `cancel cancels existing PendingIntent in AlarmManager`() = runTest {
        val eventId = UUID.randomUUID()
        val mockPendingIntent: PendingIntent = mockk(relaxed = true)

        every {
            PendingIntent.getBroadcast(
                eq(context),
                eq(eventId.hashCode()),
                any(),
                eq(PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            )
        } returns mockPendingIntent

        scheduler.cancel(eventId)

        verify { alarmManager.cancel(mockPendingIntent) }
        verify { mockPendingIntent.cancel() }
    }
}
