package com.example.seriesjp.model

import com.google.gson.annotations.SerializedName

data class WatchProvidersResponse(
    val results: Map<String, CountryWatchProviders>
)

data class CountryWatchProviders(
    val link: String?,
    val flatrate: List<Provider>?,
    val rent: List<Provider>?,
    val buy: List<Provider>?
)

data class Provider(
    @SerializedName("provider_id") val providerId: Int,
    @SerializedName("provider_name") val providerName: String,
    @SerializedName("logo_path") val logoPath: String?
)
