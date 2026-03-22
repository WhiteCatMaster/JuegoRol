package org.example.backend.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.FetchType
import jakarta.persistence.GenerationType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "personajes")
class Personaje(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var nombre : String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_juego_id")
    var jugadorJuego: JugadorJuego? = null,


    // Estadíscicas
    @OneToMany(mappedBy = "personaje", cascade = [CascadeType.ALL], orphanRemoval = true)
    var estadisticas: MutableList<Estadistica> = mutableListOf(),

    // Ataques
    @OneToMany(mappedBy = "personaje", cascade = [CascadeType.ALL], orphanRemoval = true)
    var ataques: MutableList<Ataque> = mutableListOf()
)  {
    // Sumar estadística
    fun añadirEstadística(Estadistica: Estadistica) {
        estadisticas.add(Estadistica)
        Estadistica.personaje = this
    }
    // Añadir Ataque
    fun añadirAtaque(ataque: Ataque) {
        ataques.add(ataque)
        ataque.owner = this
    }
}

