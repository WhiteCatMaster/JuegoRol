package org.example.backend.service

import jakarta.transaction.Transactional
import org.example.backend.dto.DatosPartidaDto
import org.example.backend.entity.Estadistica
import org.example.backend.entity.Juego
import org.example.backend.entity.ObjetoCompleto
import org.example.backend.entity.Personaje
import org.example.backend.repository.ObjetoCompletoRepository
import org.springframework.stereotype.Service

@Service
class ObjetoService (
    private val objetoRepo: ObjetoCompletoRepository,
){
    @Transactional
    fun obtenerObjetosByJuegoId(juegoId : Long): List<ObjetoCompleto>{
        println("buscando: $juegoId")
        return objetoRepo.findByJuegoId(juegoId)
    }
    fun toObjetoDto(objeto: ObjetoCompleto): DatosPartidaDto.PersonajeDto.ObjetoDto {
        val resultado = DatosPartidaDto.PersonajeDto.ObjetoDto(
            id = objeto.id,
            nombre = objeto.nombre,
            descripcion = objeto.descripcion,
            imagen = objeto.imagen,
            efectosPropios = mutableMapOf(),
            efectosRival = mutableMapOf(),
            usos = objeto.usos
        )
        for ((i,j) in objeto.efectosPropios){
            resultado.efectosPropios[i.nombre] = j
        }
        for ((i,j) in objeto.efectosRival){
            resultado.efectosRival[i.nombre] = j
        }
        return resultado
    }
    fun toObjeto(objetoDto: DatosPartidaDto.PersonajeDto.ObjetoDto, personaje: Personaje, juego: Juego): ObjetoCompleto{
        val estadisticas = mutableMapOf<String, Estadistica>()
        for (i in personaje.estadisticas){
            estadisticas[i.nombre] = i
        }
        val resultado = ObjetoCompleto(
            id = objetoDto.id,
            nombre = objetoDto.nombre?: "",
            descripcion = objetoDto.descripcion ?: "",
            imagen = objetoDto.imagen,
            personaje = personaje,
            efectosPropios = mutableMapOf(),
            efectosRival = mutableMapOf(),
            usos = objetoDto.usos,
            juego = juego
        )
        for ((i,j) in objetoDto.efectosPropios){
            resultado.efectosPropios[estadisticas[i]!!] = j
        }
        for ((i,j) in objetoDto.efectosRival){
            resultado.efectosRival[estadisticas[i]!!] = j
        }
        return resultado
    }
}
