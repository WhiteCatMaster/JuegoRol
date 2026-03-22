package org.example.backend.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.FetchType

@Entity
class Estadistica(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var nombre: String, // No cambia (Nombre de la stat)

    var valor: String, // Sí cambia, por eso es 'var' (Hibernate genera el set)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    var personaje: Personaje? = null
) {

}