package org.example.backend.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class JugadorJuego (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne @JoinColumn(name = "usuario_id")
    var usuario: Usuario,

    @ManyToOne
    @JoinColumn(name = "juego_id")
    var juego: Juego,

    @Enumerated(EnumType.STRING)
    val rol: RolJugador

)