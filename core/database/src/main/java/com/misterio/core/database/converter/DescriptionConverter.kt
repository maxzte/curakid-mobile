package com.misterio.core.database.converter

import androidx.room.TypeConverter
import com.misterio.core.domain.model.LifePeriodDescription
import kotlinx.serialization.json.Json

class DescriptionConverter {

    @TypeConverter
    fun descriptionToJson(value: LifePeriodDescription?) = Json.encodeToString(value)

    @TypeConverter
    fun jsonToDescription(value: String): LifePeriodDescription? = runCatching {
        Json.decodeFromString<LifePeriodDescription>(value)
    }.getOrNull()

}