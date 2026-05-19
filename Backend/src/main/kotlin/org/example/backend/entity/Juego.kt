package org.example.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "juegos")
class Juego(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var nombre: String,

    var descripcion: String? = null,

    var idioma: String? = null,

    var maximoJugadores: Int? = null,

    @OneToMany
    @JoinColumn(name = "juego_id")
    var personajes : MutableList<Personaje> = mutableListOf()   ,

    @OneToMany(mappedBy = "juego", fetch = FetchType.LAZY)
    var jugadores: MutableList<JugadorJuego> = mutableListOf(),

    @OneToMany(mappedBy = "juego", fetch = FetchType.LAZY)
    var combates: MutableList<Combate> = mutableListOf(),

    @OneToMany(mappedBy = "juego", fetch = FetchType.LAZY)
    var objetos: MutableList<ObjetoCompleto> = mutableListOf(),
)
