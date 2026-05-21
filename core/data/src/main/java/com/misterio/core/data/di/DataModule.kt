package com.misterio.core.data.di

import com.misterio.core.data.repository.AiRepositoryImpl
import com.misterio.core.data.repository.AssessmentRepositoryImpl
import com.misterio.core.data.repository.AuthRepositoryImpl
import com.misterio.core.data.repository.ChatRepositoryImpl
import com.misterio.core.data.repository.ChildRepositoryImpl
import com.misterio.core.data.repository.ConnectivityRepositoryImpl
import com.misterio.core.data.repository.DevelopmentRepositoryImpl
import com.misterio.core.data.repository.EventRepositoryImpl
import com.misterio.core.data.repository.MeasurementRepositoryImpl
import com.misterio.core.data.repository.VaccineRepositoryImpl
import com.misterio.core.domain.repository.AiRepository
import com.misterio.core.domain.repository.AssessmentRepository
import com.misterio.core.domain.repository.AuthRepository
import com.misterio.core.domain.repository.ChatRepository
import com.misterio.core.domain.repository.ChildRepository
import com.misterio.core.domain.repository.ConnectivityRepository
import com.misterio.core.domain.repository.DevelopmentRepository
import com.misterio.core.domain.repository.EventRepository
import com.misterio.core.domain.repository.MeasurementRepository
import com.misterio.core.domain.repository.VaccineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindChildRepository(
        childRepositoryImpl: ChildRepositoryImpl
    ): ChildRepository

    @Binds
    @Singleton
    abstract fun bindMeasurementRepository(
        measurementRepositoryImpl: MeasurementRepositoryImpl
    ): MeasurementRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(
        aiRepositoryImpl: AiRepositoryImpl
    ): AiRepository

    @Binds
    @Singleton
    abstract fun bindDevelopmentRepository(
        developmentRepositoryImpl: DevelopmentRepositoryImpl
    ): DevelopmentRepository

    @Binds
    @Singleton
    abstract fun bindAssessmentRepository(
        assessmentRepositoryImpl: AssessmentRepositoryImpl
    ): AssessmentRepository

    @Binds
    @Singleton
    abstract fun bindVaccineRepository(
        vaccineRepositoryImpl: VaccineRepositoryImpl
    ): VaccineRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        eventRepositoryImpl: EventRepositoryImpl
    ): EventRepository

    @Binds
    @Singleton
    abstract fun bindConnectivityRepository(
        connectivityRepositoryImpl: ConnectivityRepositoryImpl
    ): ConnectivityRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository
}
