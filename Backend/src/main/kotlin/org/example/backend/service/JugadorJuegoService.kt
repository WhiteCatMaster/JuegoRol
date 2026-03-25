package org.example.backend.service

import jakarta.transaction.Transactional
import org.example.backend.entity.JugadorJuego
import org.example.backend.repository.JugadorJuegoRepository
import org.springframework.stereotype.Service

@Service
class JugadorJuegoService(private val jugadorJuegoRepo: JugadorJuegoRepository) {

    fun getAllJugadoresJuego() = jugadorJuegoRepo.findAll()

    fun getJugadorJuegoById(id: Long) = jugadorJuegoRepo.findById(id).orElse(null)

    @Transactional
    fun createJugadorJuego(jugadorJuego: JugadorJuego) = jugadorJuegoRepo.save(jugadorJuego)

    @Transactional
    fun updateJugadorJuego(id: Long, updatedJugadorJuego: JugadorJuego): JugadorJuego? {
        val existingJugadorJuego = jugadorJuegoRepo.findById(id).orElse(null) ?: return null
        existingJugadorJuego.rol = updatedJugadorJuego.rol
        existingJugadorJuego.personaje = updatedJugadorJuego.personaje
        return jugadorJuegoRepo.save(existingJugadorJuego)
    }

    @Transactional
    fun deleteJugadorJuego(id: Long) {
        jugadorJuegoRepo.deleteById(id)
    }

    @Transactional
    fun asignarPersonaje(jugadorJuegoId: Long, personajeId: Long): JugadorJuego? {
        val jugadorJuego = jugadorJuegoRepo.findById(jugadorJuegoId).orElse(null) ?: return null
        // Aquí iría la lógica de asignación del personaje
        return jugadorJuegoRepo.save(jugadorJuego)
    }
}