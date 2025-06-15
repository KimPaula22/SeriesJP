package com.example.seriesjp.model

data class Favoritos(
    val id: String = "",      // ID único de la serie o película
    val titulo: String = "",  // Título de la serie o película
    val posterUrl: String = "", // URL de la imagen del poster
    val tipo: String = ""
)