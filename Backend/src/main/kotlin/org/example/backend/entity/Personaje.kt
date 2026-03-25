package org.example.backend.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table


@Entity
@Table(name = "personajes")
class Personaje(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var nombre: String,
    var vida : Int,
    var fotoUrl: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_juego_id")
    var jugadorJuego: JugadorJuego? = null,

    // Estadisticas
    @OneToMany(mappedBy = "personaje", cascade = [CascadeType.ALL], orphanRemoval = true)
    var estadisticas: MutableList<Estadistica> = mutableListOf(),

    // Ataques
    @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL], orphanRemoval = true)
    var ataques: MutableList<Ataque> = mutableListOf()
)


