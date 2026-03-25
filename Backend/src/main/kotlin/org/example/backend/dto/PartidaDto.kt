package org.example.backend.dto

import java.io.Serializable

data class PartidaDto(
    val nombre: String? = null,
    val descripcion: String? = null,
    val idioma: String? = null,
    val maximoJugadores: Int? = null
) : Serializable