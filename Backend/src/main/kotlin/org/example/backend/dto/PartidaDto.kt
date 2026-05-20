package org.example.backend.dto

import java.io.Serializable

data class PartidaDto(
    val id: Long? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val idioma: String? = null,
    val maximoJugadores: Int? = null,
    val adminId: Long? = null,
    val jugadoresActuales: Int = 0,
    val jugadoresUnidos: List<JugadorUnidoDto> = emptyList(),
) : Serializable {
    data class JugadorUnidoDto(
        val usuarioId: Long? = null,
        val nombre: String? = null,
        val fotoUrl: String? = null,
    ) : Serializable
}