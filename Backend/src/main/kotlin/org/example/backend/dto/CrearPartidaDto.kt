package org.example.backend.dto

import org.example.backend.entity.Estadistica
import org.example.backend.entity.ObjetoCompleto
import java.io.Serializable


data class CrearPartidaDto(
    val nombre: String? = null,
    val descripcion: String? = null,
    val idioma: String? = null,
    val maximoJugadores: Int? = null,
    val jugadores: MutableList<PersonajeDto> = mutableListOf(),
    val adminId: Long? = null,
    val objetos: MutableList<PersonajeDto.ObjetoDto> = mutableListOf(),
) : Serializable {

    data class PersonajeDto(
        val personajeNombre: String? = null,
        val personajeVida: Int? = null,
        val personajeFotoUrl: String? = null,
        val personajeEstadisticas: MutableList<EstadisticaDto> = mutableListOf(),
        val personajeAtaques: MutableList<AtaqueDto> = mutableListOf(),
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
            val manaAtacante: MutableMap<String, Int> = mutableMapOf(),
            val estadisticasDefensor: MutableMap<String, Double> = mutableMapOf(),
            val dadoBase: Int = 10,
            val ratioDado: MutableList<Int> = mutableListOf(),
            val danoAtaque: Int = 0
        ) : Serializable

        data class ObjetoDto(
            val nombre: String? = null,
            val descripcion: String? = null,
            var imagen: String,
            var efectosPropios: MutableMap<String, Double> = mutableMapOf(),
            var efectosRival: MutableMap<String, Double> = mutableMapOf(),
            var usos: Int,

        ) : Serializable{
        }
    }
}