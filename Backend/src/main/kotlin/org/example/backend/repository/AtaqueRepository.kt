package org.example.backend.repository

import org.example.backend.entity.Ataque
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AtaqueRepository : JpaRepository<Ataque, Long> {
    fun findByOwnerId(personajeId: Long): List<Ataque>
    fun findByTipo(tipo: String): List<Ataque>
}