package org.example.backend.entity

import org.example.backend.entity.Ataque
import jakarta.persistence.*

@Entity
@Table(name = "reglas_ataque")
class ReglaAtaque(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "ataque_id")
    var action: Ataque? = null,
    val nombreStat: String,
    val sujeto: String,
    val tipoOperacion: String,
    val factor: Double = 1.0,
    val umbralCondicion: Double? = null
)