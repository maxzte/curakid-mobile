package com.misterio.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.misterio.core.database.converter.DateConverters
import com.misterio.core.database.converter.ListConverter
import com.misterio.core.database.dao.AssessmentDao
import com.misterio.core.database.dao.ChartNormDao
import com.misterio.core.database.dao.ChildDao
import com.misterio.core.database.dao.ChildReportDao
import com.misterio.core.database.dao.EventDao
import com.misterio.core.database.dao.LifePeriodDao
import com.misterio.core.database.dao.MeasurementDao
import com.misterio.core.database.entity.AssessmentEntity
import com.misterio.core.database.entity.ChartNormEntity
import com.misterio.core.database.entity.ChildSkillAssessmentEntity
import com.misterio.core.database.entity.ChildEntity
import com.misterio.core.database.entity.ChildReportEntity
import com.misterio.core.database.entity.EventEntity
import com.misterio.core.database.entity.LifePeriodEntity
import com.misterio.core.database.entity.MeasurementEntity

@Database(
    entities = [
        ChildEntity::class,
        MeasurementEntity::class,
        AssessmentEntity::class,
        EventEntity::class,
        LifePeriodEntity::class,
        ChildSkillAssessmentEntity::class,
        ChildReportEntity::class,
        ChartNormEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class, ListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lifePeriodDao() : LifePeriodDao

    abstract fun childDao(): ChildDao

    abstract fun measurementDao(): MeasurementDao

    abstract fun assessmentDao(): AssessmentDao

    abstract fun eventDao(): EventDao

    abstract fun childReportDao(): ChildReportDao

    abstract fun chartNormDao(): ChartNormDao

}
