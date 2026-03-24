package org.example.backend.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.CollectionTable
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapKeyJoinColumn

@Entity
class Ataque(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var nombre: String,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "ataque_mana_atacante", joinColumns = [JoinColumn(name = "ataque_id")])
    @MapKeyJoinColumn(name = "estadistica_id")
    @Column(name = "valor")
    val manaAtacante: MutableMap<Estadistica, Int> = mutableMapOf(),

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "ataque_estadisticas_defensor", joinColumns = [JoinColumn(name = "ataque_id")])
    @MapKeyJoinColumn(name = "estadistica_id")
    @Column(name = "valor")
    val estadisticasDefensor: MutableMap<Estadistica, Double> = mutableMapOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personaje_id")
    var owner: Personaje? = null,

    @Column(nullable = false)
    var dadoBase: Int = 10,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "ataque_ratio_dado", joinColumns = [JoinColumn(name = "ataque_id")])
    @Column(name = "valor")
    var ratioDado: MutableList<Int> = mutableListOf()
)