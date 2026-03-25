package org.example.backend.dto

import org.example.backend.entity.RolJugador
import java.io.Serializable


data class JuegoDto(
    val nombre: String? = null,
    val descripcion: String? = null,
    val idioma: String? = null,
    val maximoJugadores: Int? = null,
    val jugadores: MutableList<JugadorJuegoDto> = mutableListOf()
) : Serializable {

    data class JugadorJuegoDto(
        val id: Long? = null,
        val usuarioGoogleId: String? = null,
        val usuarioEmail: String? = null,
        val usuarioNombre: String? = null,
        val usuarioFotoUrl: String? = null,
        val rol: RolJugador? = null,
        val personajeNombre: String? = null,
        val personajeVida: Int? = null,
        val personajeFotoUrl: String? = null,
        val personajeEstadisticas: MutableList<EstadisticaDto> = mutableListOf(),
        val personajeAtaques: MutableList<AtaqueDto> = mutableListOf()
    ) : Serializable {

        data class EstadisticaDto(
            val id: Long? = null,
            val nombre: String? = null,
            val valor: String? = null,
            val consumible: Boolean = false
        ) : Serializable

        data class AtaqueDto(
            val id: Long? = null,
            val nombre: String? = null,
            val manaAtacante: MutableMap<EstadisticaDto, Int> = mutableMapOf(),
            val estadisticasDefensor: MutableMap<EstadisticaDto, Double> = mutableMapOf(),
            val dadoBase: Int = 10,
            val ratioDado: MutableList<Int> = mutableListOf()
        ) : Serializable
    }
}