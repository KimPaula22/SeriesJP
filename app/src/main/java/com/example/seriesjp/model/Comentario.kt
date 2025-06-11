package com.example.seriesjp.model

data class Comentario(
    var usuario: String = "",
    var puntuacion: Int = 0,
    var fecha: String = "",
    var texto: String = ""
)