package org.example.backend.service

import jakarta.transaction.Transactional
import org.example.backend.dto.ActualizarPersonajeDto
import org.example.backend.dto.CrearPartidaDto
import org.example.backend.dto.DatosPartidaDto
import org.example.backend.entity.Ataque
import org.example.backend.entity.Estadistica
import org.example.backend.entity.ObjetoCompleto
import org.example.backend.entity.Personaje
import org.example.backend.repository.PersonajeRepository
import org.springframework.stereotype.Service
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

@Service
class PersonajeService(private val personajeRepo: PersonajeRepository) {

    fun getAllPersonajes(): List<Personaje> {
        return personajeRepo.findAll()
    }

    fun getPersonajeById(id: Long): Personaje? {
        return personajeRepo.findById(id).orElse(null)
    }

    @Transactional
    fun createPersonaje(personaje: Personaje): Personaje {
        return personajeRepo.save(personaje)
    }

    @Transactional
    fun updateNombrePersonaje(id: Long, updatedPersonaje: Personaje): Personaje? {
        val existingPersonaje = personajeRepo.findById(id).orElse(null) ?: return null
        existingPersonaje.nombre = updatedPersonaje.nombre
        return personajeRepo.save(existingPersonaje)
    }

    @Transactional
    fun deletePersonaje(id: Long) {
        personajeRepo.deleteById(id)
    }

    @Transactional
    fun actualizarEstadisticaPersonaje(id: Long, updatedPersonaje: Personaje): Personaje? {
        val personaje = personajeRepo.findById(id).orElse(null) ?: return null
        personaje.estadisticas = updatedPersonaje.estadisticas
        return personajeRepo.save(personaje)
    }

    @Transactional
    //Esta funcion actualiza nombre y estadsiticas del personje
    fun actualizarPersonaje(id:Long, dto: ActualizarPersonajeDto): Personaje{
        val personajeDB = personajeRepo.findById(id).orElseThrow{
            Exception("Personaje no encontrado, id: $id")
        }
        personajeDB.nombre = dto.nombre
        for ( i in dto.estadisticas){
            for (j in personajeDB.estadisticas){
                if (i.nombre == j.nombre){
                    j.valor = i.valorNuevo
                }
            }
        }
        return personajeRepo.save(personajeDB)

    }

    fun dtoToPersonaje(personajeDTO: CrearPartidaDto.PersonajeDto): Personaje {
        val estats = mutableListOf<Estadistica>()
        val buscadorEstadisticas = mutableMapOf<String, Estadistica>()

        // 1. Cargamos las estadísticas y las indexamos por nombre
        for (estatDTO in personajeDTO.personajeEstadisticas){
            val estat = Estadistica(
                nombre = estatDTO.nombre ?: "",
                valor = estatDTO.valor ?: 0,
                consumible = estatDTO.consumible,
            )
            println("Guardando estadistica: nombre=${estat.nombre}, valor=${estat.valor}, consumible=${estat.consumible}")
            estats.add(estat)
            buscadorEstadisticas[estat.nombre] = estat
        }

        val ataques = mutableListOf<Ataque>()

        // 2. Cargamos los ataques buscando las referencias directas en el diccionario
        for (ataqueDTO in personajeDTO.personajeAtaques){

            val estatsDefensor = mutableMapOf<Estadistica, Double>()
            for ((nombreEst, valorDef) in ataqueDTO.estadisticasDefensor) {
                val estadisticaEncontrada = buscadorEstadisticas[nombreEst]
                if (estadisticaEncontrada != null) {
                    estatsDefensor[estadisticaEncontrada] = valorDef
                }
            }

            val manaAtacante = mutableMapOf<Estadistica, Int>()
            for ((nombreEst, valorMana) in ataqueDTO.manaAtacante) {
                val estadisticaEncontrada = buscadorEstadisticas[nombreEst]
                if (estadisticaEncontrada != null) {
                    manaAtacante[estadisticaEncontrada] = valorMana
                }
            }

            val ataque = Ataque(
                nombre = ataqueDTO.nombre ?: "",
                dadoBase = ataqueDTO.dadoBase,
                ratioDado = ataqueDTO.ratioDado,
                estadisticasDefensor = estatsDefensor,
                manaAtacante = manaAtacante,
                danioAtaque = ataqueDTO.danoAtaque
            )
            ataques.add(ataque)
        }
        val inventarioEntidad = mutableListOf<ObjetoCompleto>()

        println("Guardando ataques desde DTO...")
        println("Guardando personajes desde DTO...")
        println("Guardando inventario desde DTO...")

        // 3. Montamos el personaje con sus listas ya enlazadas
        val personaje = Personaje(
            nombre = personajeDTO.personajeNombre ?: "",
            vida = personajeDTO.personajeVida ?: 0,
            fotoUrl = personajeDTO.personajeFotoUrl ?: "",
            estadisticas = estats,
            ataques = ataques,
        )

        // Relaciones bidireccionales
        for (i in personaje.estadisticas){
            i.personaje = personaje
        }
        for (i in personaje.ataques){
            i.owner = personaje
        }

        return personaje
    }
    fun personajeToDto(personaje: Personaje): DatosPartidaDto.PersonajeDto{
        val estadisticasDto = mutableListOf<DatosPartidaDto.PersonajeDto.EstadisticaDto>()
        val ataquesDto = mutableListOf<DatosPartidaDto.PersonajeDto.AtaqueDto>()
        for (estadistica in personaje.estadisticas){
            val estadisticaDto = DatosPartidaDto.PersonajeDto.EstadisticaDto(
                id = estadistica.id,
                nombre = estadistica.nombre,
                valor = estadistica.valor,
                consumible = estadistica.consumible,
            )
            estadisticasDto.add(estadisticaDto)
        }
        for (i in personaje.ataques){
            val manaAtacanteDto = mutableMapOf<String, Int>()
            val estadisticasDefensorDto = mutableMapOf<String, Double>()
            for (j in i.manaAtacante.keys){
                val clave = j.nombre
                manaAtacanteDto[clave] = i.manaAtacante[j] ?: 0
            }
            for (j in i.estadisticasDefensor.keys){
                val clave = j.nombre
                estadisticasDefensorDto[clave] = i.estadisticasDefensor[j] ?: 0.0
            }
            val ataqueDto = DatosPartidaDto.PersonajeDto.AtaqueDto(
                id = i.id,
                nombre = i.nombre,
                manaAtacante = manaAtacanteDto,
                estadisticasDefensor = estadisticasDefensorDto,
                dadoBase = i.dadoBase,
                ratioDado = i.ratioDado,
                danoAtaque = i.danioAtaque
            )
            ataquesDto.add(ataqueDto)

            val inventarioDto = mutableListOf<DatosPartidaDto.PersonajeDto.ObjetoDto>()

            for (objeto in personaje.inventario) {
                // Transformamos Map<Estadistica, Double> a Map<String, Double>
                val efectosPropiosDto = mutableMapOf<String, Double>()
                for ((estadistica, valor) in objeto.efectosPropios) {
                    efectosPropiosDto[estadistica.nombre] = valor
                }

                val efectosRivalDto = mutableMapOf<String, Double>()
                for ((estadistica, valor) in objeto.efectosRival) {
                    efectosRivalDto[estadistica.nombre] = valor
                }

                // Creamos el DTO (asegúrate de que esta clase exista en tu DatosPartidaDto)
                val objetoDto = DatosPartidaDto.PersonajeDto.ObjetoDto(
                    id = objeto.id,
                    nombre = objeto.nombre,
                    descripcion = objeto.descripcion,
                    imagen = objeto.imagen,
                    usos = objeto.usos,
                    efectosPropios = efectosPropiosDto,
                    efectosRival = efectosRivalDto
                )
                inventarioDto.add(objetoDto)
            }

        }
        val personajeDto = DatosPartidaDto.PersonajeDto(
            id = personaje.id,
            personajeNombre = personaje.nombre,
            personajeVida = personaje.vida,
            personajeFotoUrl = personaje.fotoUrl,
            personajeEstadisticas = estadisticasDto,
            personajeAtaques = ataquesDto
        )
        return personajeDto
    }

}