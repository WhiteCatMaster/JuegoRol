package org.example.backend.repository

import org.example.backend.entity.JugadorJuego
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface JugadorJuegoRepository : JpaRepository<JugadorJuego, Long> {
}