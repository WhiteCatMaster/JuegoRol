package org.example.backend.dto

import java.io.Serializable

data class ActualizarUsuarioDto(
    val nombre: String,
    val fotoUrl: String
): Serializable
