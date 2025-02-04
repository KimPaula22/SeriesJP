package com.example.seriesjp.model

import com.google.gson.annotations.SerializedName

data class PopularSeriesResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<Series>, // Lista de series
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)