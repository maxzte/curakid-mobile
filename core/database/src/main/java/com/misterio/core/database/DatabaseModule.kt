package com.misterio.core.database

import android.content.Context
import androidx.room.Room
import com.misterio.core.database.dao.AssessmentDao
import com.misterio.core.database.dao.ChildDao
import com.misterio.core.database.dao.ChildReportDao
import com.misterio.core.database.dao.EventDao
import com.misterio.core.database.dao.LifePeriodDao
import com.misterio.core.database.dao.MeasurementDao
import com.misterio.core.database.dao.VaccineDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "curakid_db"
        ).build()
    }

    @Provides
    fun provideChildDao(db: AppDatabase): ChildDao = db.childDao()

    @Provides
    fun provideMeasurementDao(db: AppDatabase): MeasurementDao = db.measurementDao()

    @Provides
    fun provideLifePeriodDao(db: AppDatabase): LifePeriodDao = db.lifePeriodDao()

    @Provides
    fun provideAssessmentDao(db: AppDatabase): AssessmentDao = db.assessmentDao()

    @Provides
    fun provideVaccineDao(db: AppDatabase): VaccineDao = db.vaccineDao()

    @Provides
    fun provideEventDao(db: AppDatabase): EventDao = db.eventDao()

    @Provides
    fun provideChildReportDao(db: AppDatabase): ChildReportDao = db.childReportDao()

}
