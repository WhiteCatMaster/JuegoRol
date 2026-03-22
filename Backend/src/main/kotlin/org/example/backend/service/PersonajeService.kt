package org.example.backend.service

import jakarta.transaction.Transactional
import org.example.backend.entity.Personaje
import org.example.backend.repository.PersonajeRepository
import org.springframework.stereotype.Service

@Service
class PersonajeService(private val personajeRepo: PersonajeRepository) {

    fun obtenerPersonaje(id: Long): Personaje =
        personajeRepo.findById(id).orElseThrow { Exception("Personaje no encontrado") }

    fun obtenerValorStat(personaje: Personaje, nombreStat : String): Int {
        val stat = personaje.estadisticas.find { it.nombre == nombreStat }
        if (stat != null) {
            return stat.valor.toInt()
        } else {
            throw Exception("Estadística no encontrada")
        }
    }

    @Transactional
    fun actualizarValorStat(personaje: Personaje, nombreStat : String, nuevoValor: Int) {
        val stat = personaje.estadisticas.find { it.nombre == nombreStat }
        if (stat != null) {
            stat.valor = nuevoValor.toString()
            personajeRepo.save(personaje)
        } else {
            throw Exception("Estadística no encontrada")
        }
    }

}