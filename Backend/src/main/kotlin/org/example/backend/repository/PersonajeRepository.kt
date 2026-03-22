package org.example.backend.repository

import org.example.backend.entity.Personaje
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonajeRepository : JpaRepository<Personaje, Long> {
    fun findByNombre(nombre: String): Personaje?
    fun findByJugadorJuegoId(jugadorId: Long): List<Personaje>
}