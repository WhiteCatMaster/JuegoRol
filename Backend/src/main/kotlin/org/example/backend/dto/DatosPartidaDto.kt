package org.example.backend.dto

import org.example.backend.entity.Estadistica
import java.io.Serializable

class DatosPartidaDto (
    val id: Long? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val idioma: String? = null,
    val maximoJugadores: Int? = null,
    val jugadores: MutableList<PersonajeDto> = mutableListOf(),
    objetos: MutableList<PersonajeDto.ObjetoDto> = mutableListOf(),
): Serializable {

    data class PersonajeDto(
        val id: Long? = null,
        val personajeNombre: String? = null,
        val personajeVida: Int? = null,
        val personajeFotoUrl: String? = null,
        val personajeEstadisticas: MutableList<EstadisticaDto> = mutableListOf(),
        val personajeAtaques: MutableList<AtaqueDto> = mutableListOf(),
    ) : Serializable {

        data class EstadisticaDto(
            val id: Long? = null,
            val nombre: String? = null,
            val valor: Int? = null,
            val consumible: Boolean = false
        ) :
            Serializable

        data class AtaqueDto(
            val id: Long? = null,
            val nombre: String? = null,
            val manaAtacante: MutableMap<String, Int> = mutableMapOf(),
            val estadisticasDefensor: MutableMap<String, Double> = mutableMapOf(),
            val dadoBase: Int = 10,
            val ratioDado: MutableList<Int>,
            val danoAtaque: Int = 0
        ) : Serializable

        data class ObjetoDto(
            val id: Long? = null,
            val nombre: String? = null,
            val descripcion: String? = null,
            var imagen: String,
            var efectosPropios: MutableMap<String, Double> = mutableMapOf(),
            var efectosRival: MutableMap<String, Double> = mutableMapOf(),
            var usos: Int


        ) : Serializable
    }
}