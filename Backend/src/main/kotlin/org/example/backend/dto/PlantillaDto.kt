package org.example.backend.dto

import java.io.Serializable

data class PlantillaDto(
    val id: Long,
    val nombre: String,
    val jsonConfiguration: CrearPartidaDto,
): Serializable
data class PlantillaRequestDto(
    val nombre: String,
    val jsonConfiguration: CrearPartidaDto
): Serializable
