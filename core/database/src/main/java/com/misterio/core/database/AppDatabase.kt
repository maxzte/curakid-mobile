package com.misterio.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.misterio.core.database.converter.DateConverters
import com.misterio.core.database.converter.ListConverter
import com.misterio.core.database.dao.AssessmentDao
import com.misterio.core.database.dao.ChildDao
import com.misterio.core.database.dao.EventDao
import com.misterio.core.database.dao.MeasurementDao
import com.misterio.core.database.dao.VaccineDao
import com.misterio.core.database.entity.AssessmentEntity
import com.misterio.core.database.entity.ChildEntity
import com.misterio.core.database.entity.EventEntity
import com.misterio.core.database.entity.MeasurementEntity
import com.misterio.core.database.entity.VaccineEntity

@Database(
    entities = [
        VaccineEntity::class,
        ChildEntity::class,
        MeasurementEntity::class,
        AssessmentEntity::class,
        EventEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class, ListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun childDao(): ChildDao

    abstract fun measurementDao(): MeasurementDao

    abstract fun assessmentDao(): AssessmentDao

    abstract fun vaccineDao(): VaccineDao

    abstract fun eventDao(): EventDao

}
