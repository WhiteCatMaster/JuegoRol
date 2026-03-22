package org.example.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
class Juego(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var nombre: String,
    var descripcion: String?,

    @Column(columnDefinition = "TEXT")
    var configuracion: String, // JSON con reglas específicas de la partida

    @OneToMany(mappedBy = "juego")
    val jugadores: MutableList<JugadorJuego> = mutableListOf()
)