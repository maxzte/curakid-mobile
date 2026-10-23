package com.misterio.core.database.converter

import androidx.room.TypeConverter
import com.misterio.core.domain.model.AssessmentAnswer
import com.misterio.core.domain.model.LifePeriodDescription
import com.misterio.core.domain.model.Question
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class ListConverter {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    @TypeConverter
    fun listToJson(value: List<String>?): String = json.encodeToString(value ?: emptyList())

    @TypeConverter
    fun jsonToList(value: String?): List<String> = if (value.isNullOrBlank()) emptyList() else runCatching {
        json.decodeFromString<List<String>>(value)
    }.getOrDefault(emptyList())

    @TypeConverter
    fun questionsToJson(value: List<Question>?): String = json.encodeToString(value ?: emptyList())

    @TypeConverter
    fun jsonToQuestions(value: String?): List<Question> = if (value.isNullOrBlank()) emptyList() else runCatching {
        json.decodeFromString<List<Question>>(value)
    }.getOrDefault(emptyList())

    @TypeConverter
    fun answersToJson(value: List<AssessmentAnswer>?): String = json.encodeToString(value ?: emptyList())

    @TypeConverter
    fun jsonToAnswers(value: String?): List<AssessmentAnswer> = if (value.isNullOrBlank()) emptyList() else runCatching {
        json.decodeFromString<List<AssessmentAnswer>>(value)
    }.getOrDefault(emptyList())

}
