package org.example.backend.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "usuarios")
class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // Este es el ID único que te da Google al iniciar sesión (el campo "sub" del token)
    @Column(unique = true, nullable = false)
    val googleId: String,

    @Column(unique = true, nullable = false)
    var email: String,
    var nombre: String,

    // Google suele devolver la foto de perfil, ¡queda genial para el frontend!
    var fotoUrl: String? = null,

    // Relación con la tabla intermedia que dice en qué partidas está y qué rol tiene
    @OneToMany(mappedBy = "usuario", cascade = [CascadeType.ALL], orphanRemoval = true)
    val partidasParticipa: MutableList<JugadorJuego> = mutableListOf()
)