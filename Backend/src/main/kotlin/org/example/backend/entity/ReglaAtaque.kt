package org.example.backend.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class ReglaAtaque(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id")
    val ataque: Ataque,

    val nombre_estadistica: String, // El nombre de la stat que ya existe (Ej: "Fuerza", "Defensa")

    val multiplicador: Double, // Por cuánto se multiplica (Ej: 1.5, 0.8)

    @Enumerated(EnumType.STRING)
    val objetivo: Objetivo // ¿De quién es esta stat? ¿Mía o del rival?
)
