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

@Entity
class Ataque(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personaje_id")
    var owner: Personaje? = null,


    @OneToMany(mappedBy = "action", cascade = [CascadeType.ALL], orphanRemoval = true)
    var rules: MutableList<ReglaAtaque> = mutableListOf(),

    var targetStatToReduce: String = "Vida",
    var dadoBase: Int = 10
)