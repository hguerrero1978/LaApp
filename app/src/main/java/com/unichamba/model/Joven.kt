package com.unichamba.model

data class Trabajo(
    val icono: String,
    val nombre: String
)

data class Joven(
    val id: String,
    val nombre: String,
    val trabajos: List<Trabajo>,
    val imagen: String,
    val carrera: String,
    val municipio: String,
    val descripcion: String
)
