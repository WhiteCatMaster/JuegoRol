package org.example.backend.repository

import org.example.backend.entity.Estadistica
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EstadisticaRepository : JpaRepository<Estadistica, Long> {
    fun findByNombre(nombre: String): List<Estadistica>
}