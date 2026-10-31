package com.misterio.core.network.dto

import com.google.gson.annotations.SerializedName

data class PagedResponse<T>(
    @SerializedName("content") val content: List<T>,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("last") val last: Boolean
)
