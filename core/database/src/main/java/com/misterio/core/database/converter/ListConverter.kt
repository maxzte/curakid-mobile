package com.misterio.core.database.converter

import androidx.room.TypeConverter
import com.misterio.core.domain.model.AssessmentAnswer
import com.misterio.core.domain.model.Question
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ListConverter {

    @TypeConverter
    fun listToJson(value: List<String>?) = Json.encodeToString(value)

    @TypeConverter
    fun jsonToList(value: String) = Json.decodeFromString<List<String>>(value)

    @TypeConverter
    fun questionsToJson(value: List<Question>?) = Json.encodeToString(value)

    @TypeConverter
    fun jsonToQuestions(value: String) = Json.decodeFromString<List<Question>>(value)

    @TypeConverter
    fun answersToJson(value: List<AssessmentAnswer>?) = Json.encodeToString(value)

    @TypeConverter
    fun jsonToAnswers(value: String) = Json.decodeFromString<List<AssessmentAnswer>>(value)

}