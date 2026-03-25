package org.example.backend.repository

import org.example.backend.entity.Juego
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JuegoRepository : JpaRepository<Juego, Long> {
    fun findByNombre(nombre: String): Juego?
}