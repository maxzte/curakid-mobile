package com.misterio.curakid.feature.calendar.worker

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.misterio.core.common.SettingsManager
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.util.EventNotificationScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventNotificationSchedulerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager
) : EventNotificationScheduler {
    override suspend fun schedule(event: Event) {
        val isEnabled = settingsManager.isNotificationsEnabled.first()
        if (isEnabled == false) return

        val eventDateTime = event.date
        val delay = Duration.between(LocalDateTime.now(), eventDateTime)

        if (delay.isNegative) return

        val workRequest = OneTimeWorkRequestBuilder<EventNotificationWorker>()
            .setInitialDelay(delay)
            .setInputData(
                Data.Builder()
                    .putString(EventNotificationWorker.KEY_EVENT_TITLE, event.title)
                    .putString(
                        EventNotificationWorker.KEY_EVENT_DESCRIPTION,
                        event.description ?: ""
                    )
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    override suspend fun schedule(events: List<Event>) {
        val isEnabled = settingsManager.isNotificationsEnabled.first()
        if (!isEnabled) return

        val now = LocalDateTime.now()
        val workManager = WorkManager.getInstance(context)

        val workRequests = events.mapNotNull { event ->
            val delay = Duration.between(now, event.date)

            // Пропускаємо події, що вже минули
            if (delay.isNegative) return@mapNotNull null

            OneTimeWorkRequestBuilder<EventNotificationWorker>()
                .setInitialDelay(delay)
                .setInputData(
                    workDataOf(
                        EventNotificationWorker.KEY_EVENT_TITLE to event.title,
                        EventNotificationWorker.KEY_EVENT_DESCRIPTION to (event.description ?: "")
                    )
                )
                .addTag("event_${event.id}")
                .build()
        }

        if (workRequests.isNotEmpty()) {
            workManager.enqueue(workRequests)
        }
    }

    override suspend fun cancel(eventId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("event_$eventId")
    }

    override suspend fun cancelAll(childId: String) {
        // У цьому випадку ми можемо або додавати тег дитини до всіх воркерів, 
        // або скасувати всі за тегами івентів. Але зазвичай достатньо скасувати конкретні.
        // Якщо потрібно скасувати все для дитини, варто було б додавати тег childId при плануванні.
    }
}
