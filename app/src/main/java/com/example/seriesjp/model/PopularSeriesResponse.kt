package com.example.seriesjp.model

import com.google.gson.annotations.SerializedName

data class PopularSeriesResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<Series>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)