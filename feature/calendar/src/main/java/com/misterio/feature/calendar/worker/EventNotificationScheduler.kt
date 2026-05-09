package com.misterio.feature.calendar.worker

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.misterio.core.common.SettingsManager
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.util.EventNotificationScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventNotificationSchedulerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager
) : EventNotificationScheduler {
    override fun schedule(event: Event) {
        val isEnabled = runBlocking { settingsManager.isNotificationsEnabled.first() }
        if (isEnabled == false) return

        val eventDateTime = event.date
        val delay = Duration.between(LocalDateTime.now(), eventDateTime)

        if (delay.isNegative) return

        val workRequest = OneTimeWorkRequestBuilder<EventNotificationWorker>()
            .setInitialDelay(delay)
            .setInputData(
                Data.Builder()
                    .putString(EventNotificationWorker.KEY_EVENT_TITLE, event.title)
                    .putString(EventNotificationWorker.KEY_EVENT_DESCRIPTION, event.description ?: "")
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
