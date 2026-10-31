package com.misterio.core.database

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.room.Room
import com.misterio.core.database.dao.AssessmentDao
import com.misterio.core.database.dao.ChartNormDao
import com.misterio.core.database.dao.ChatDao
import com.misterio.core.database.dao.ChildDao
import com.misterio.core.database.dao.ChildReportDao
import com.misterio.core.database.dao.EventDao
import com.misterio.core.database.dao.LifePeriodDao
import com.misterio.core.database.dao.MeasurementDao
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
        val builder = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "curakid_db"
        )
        // Allow destructive migration only if explicitly running in debug/testing mode or before first store release
        if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            builder.fallbackToDestructiveMigration(false)
        }
        return builder.build()
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
    fun provideEventDao(db: AppDatabase): EventDao = db.eventDao()

    @Provides
    fun provideChildReportDao(db: AppDatabase): ChildReportDao = db.childReportDao()

    @Provides
    fun provideChartNormsDao(db: AppDatabase): ChartNormDao = db.chartNormDao()

    @Provides
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()

}
