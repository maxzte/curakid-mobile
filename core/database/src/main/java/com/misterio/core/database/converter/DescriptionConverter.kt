package com.misterio.core.database.converter

import androidx.room.TypeConverter
import com.misterio.core.domain.model.LifePeriodDescription
import kotlinx.serialization.json.Json

class DescriptionConverter {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    @TypeConverter
    fun descriptionToJson(value: LifePeriodDescription?): String? = value?.let { json.encodeToString(it) }

    @TypeConverter
    fun jsonToDescription(value: String?): LifePeriodDescription? = if (value.isNullOrBlank()) null else runCatching {
        json.decodeFromString<LifePeriodDescription>(value)
    }.getOrNull()

}