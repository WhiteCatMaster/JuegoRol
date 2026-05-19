package org.example.backend.dto

import java.io.Serializable

data class ActualizarPersonajeDto(
    val nombre: String,
    //Supongo que esto es tipo
    val estadisticas: List<EstatDto>,
    val objetos: List<DatosPartidaDto.PersonajeDto.ObjetoDto>

): Serializable{
    data class EstatDto (
        val nombre: String,
        val valorNuevo: Int
    ): Serializable
}
