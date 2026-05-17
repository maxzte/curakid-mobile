package com.misterio.curakid.feature.calendar.di

import com.misterio.core.domain.util.EventNotificationScheduler
import com.misterio.curakid.feature.calendar.worker.EventNotificationSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CalendarModule {

    @Binds
    @Singleton
    abstract fun bindEventNotificationScheduler(
        impl: EventNotificationSchedulerImpl
    ): EventNotificationScheduler
}
