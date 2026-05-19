package org.example.backend.dto

import org.example.backend.entity.Usuario
import java.io.Serializable


data class UsuarioDto(
    val id: Long? = null,
    val googleId: String? = null,
    val email: String? = null,
    val nombre: String? = null,
    val fotoUrl: String? = null,
    val partidasParticipa: MutableList<JugadorJuegoDto>
): Serializable{
    data class JugadorJuegoDto(
        val id: Long? = null,
        //Esto no hace falta porque quien lo va a llamar ya tiene un usuario
        //val usuarioId: Long? = null,
        val juego: JuegoDto? = null,
        val rol: String? = null,
    ): Serializable
    data class JuegoDto(
        val id : Long? = null,
        val nombre: String? = null,
        val descripcion: String? = null,
        val idioma: String? = null,
        val maxJugadores: Int? = null,
    )

}
