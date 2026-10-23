package com.misterio.core.data.mapper

import com.misterio.core.database.entity.ChildEntity
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Gender
import com.misterio.core.network.dto.ChildResponse
import com.misterio.core.network.dto.UpdateChildRequest
import java.time.LocalDate

fun ChildEntity.toDomain() = Child(
    id = id,
    name = name,
    birthDate = birthDate,
    gender = Gender.entries.find { it.name.equals(gender, ignoreCase = true) } ?: Gender.MALE,
    isPremature = isPremature,
    gestationalWeeks = gestationalWeeks,
    allergies = allergies,
    chronicDiseases = chronicDiseases,
    pastDiseases = pastDiseases,
    specialFeatures = specialFeatures,
)

fun ChildEntity.toUpdateRequest() = UpdateChildRequest(
    name = name,
    birthDate = birthDate.toString(),
    gender = gender,
    gestationalWeeks = gestationalWeeks,
    allergies = allergies,
    chronicDiseases = chronicDiseases,
    pastDiseases = pastDiseases,
    specialFeatures = specialFeatures
)

fun ChildResponse.toEntity() = ChildEntity(
    id = id,
    name = name,
    birthDate = LocalDate.parse(birthDate),
    gender = gender,
    isPremature = isPremature,
    gestationalWeeks = gestationalWeeks,
    allergies = allergies,
    chronicDiseases = chronicDiseases,
    pastDiseases = pastDiseases,
    specialFeatures = specialFeatures,
)

fun Child.toEntity() = ChildEntity(
    id = id,
    name = name,
    birthDate = birthDate,
    gender = gender.name,
    isPremature = isPremature,
    gestationalWeeks = gestationalWeeks,
    allergies = allergies,
    chronicDiseases = chronicDiseases,
    pastDiseases = pastDiseases,
    specialFeatures = specialFeatures,
)
