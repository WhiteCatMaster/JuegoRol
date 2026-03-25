package org.example.backend.dto

import org.example.backend.entity.Estadistica
import org.example.backend.entity.RolJugador
import java.io.Serializable


data class CrearPartidaDto(
    val nombre: String? = null,
    val descripcion: String? = null,
    val idioma: String? = null,
    val maximoJugadores: Int? = null,
    val jugadores: MutableList<PersonajeDto> = mutableListOf()
) : Serializable {

    data class PersonajeDto(
        val personajeNombre: String? = null,
        val personajeVida: Int? = null,
        val personajeFotoUrl: String? = null,
        val personajeEstadisticas: MutableList<EstadisticaDto> = mutableListOf(),
        val personajeAtaques: MutableList<AtaqueDto> = mutableListOf()
    ) : Serializable {

        data class EstadisticaDto(
            val nombre: String? = null,
            val valor: Int? = null,
            val consumible: Boolean = false
        ) :
            Serializable

        data class AtaqueDto(
            //val id: Long? = null,
            val nombre: String? = null,
            val manaAtacante: MutableMap<String, Double> = mutableMapOf(),
            val estadisticasDefensor: MutableMap<String, Int> = mutableMapOf(),
            val dadoBase: Int = 10,
            val ratioDado: MutableList<Int>
        ) : Serializable
    }
}