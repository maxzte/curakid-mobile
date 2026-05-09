package com.misterio.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class ListConverter {

    @TypeConverter
    fun listToJson(value: List<String>?) = Json.encodeToString(value)

    @TypeConverter
    fun jsonToList(value: String) = Json.decodeFromString<List<String>>(value)

}