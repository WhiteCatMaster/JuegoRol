package org.example.backend.service

import jakarta.transaction.Transactional
import org.example.backend.entity.Personaje
import org.example.backend.repository.PersonajeRepository
import org.springframework.stereotype.Service

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
    fun updatePersonaje(id: Long, updatedPersonaje: Personaje): Personaje? {
        val existingPersonaje = personajeRepo.findById(id).orElse(null) ?: return null
        existingPersonaje.nombre = updatedPersonaje.nombre
        existingPersonaje.jugadorJuego = updatedPersonaje.jugadorJuego
        // Actualiza otros campos según sea necesario
        return personajeRepo.save(existingPersonaje)
    }

    @Transactional
    fun deletePersonaje(id: Long) {
        personajeRepo.deleteById(id)
    }

}