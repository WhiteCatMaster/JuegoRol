package org.example.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "juegos")
class Juego(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var nombre: String,

    var descripcion: String? = null,

    var idioma: String? = null,

    var maximoJugadores: Int? = null,

    @OneToMany(mappedBy = "juego", fetch = FetchType.LAZY)
    var jugadores: MutableList<JugadorJuego> = mutableListOf()
)

