package org.example.backend.entity

import jakarta.persistence.*

@Entity
class ObjetoCompleto (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var nombre: String,

    @Column(nullable = false)
    var descripcion: String,

    @Column(nullable = false)
    var imagen: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personaje_id")
    var personaje: Personaje? = null,

    @ElementCollection
    @CollectionTable(name = "objeto_efectos_propios", joinColumns = [JoinColumn(name = "objeto_id")])
    @MapKeyJoinColumn(name = "estadistica_id")
    @Column(name = "valor")
    var efectosPropios: MutableMap<Estadistica, Double> = mutableMapOf(),

    @ElementCollection
    @CollectionTable(name = "objeto_efectos_rival", joinColumns = [JoinColumn(name = "objeto_id")])
    @MapKeyJoinColumn(name = "estadistica_id")
    @Column(name = "valor")
    var efectosRival: MutableMap<Estadistica, Double> = mutableMapOf(),

    @Column(nullable = false)
    var usos: Int,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juego_id")
    var juego: Juego? = null,
)