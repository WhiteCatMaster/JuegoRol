package org.example.backend.repository

import org.example.backend.entity.Personaje
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonajeRepository : JpaRepository<Personaje, Long> {
    fun findByNombre(nombre: String): List<Personaje>
    fun findByJugadorJuegoId(jugadorJuegoId: Long): List<Personaje>
}