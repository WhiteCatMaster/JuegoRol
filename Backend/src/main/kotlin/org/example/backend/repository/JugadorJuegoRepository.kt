package org.example.backend.repository

import org.example.backend.entity.JugadorJuego
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional


@Repository
interface JugadorJuegoRepository : JpaRepository<JugadorJuego, Long> {
    fun findByUsuarioId(usuarioId: Long): List<JugadorJuego>
    fun findByJuegoId(juegoId: Long): List<JugadorJuego>
    fun findByUsuarioIdAndJuegoId(usuarioId: Long, juegoId: Long): Optional<JugadorJuego>
}