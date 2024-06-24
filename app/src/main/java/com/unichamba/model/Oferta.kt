package com.unichamba.model

data class Oferta(
    val description: String = "",
    val imagen: String = "",
    val quienPublica: String = "",
    val imagenSmall: String = "",
    val carrera: List<String> = listOf() // Lista vacía por defecto
)

