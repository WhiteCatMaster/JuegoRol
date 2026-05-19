package org.example.backend.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.example.backend.dto.UsuarioDto

@Entity
@Table(name = "usuarios")
class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, nullable = false)
    var googleId: String,

    @Column(unique = true, nullable = false)
    var email: String,

    @Column(nullable = false)
    var nombre: String,

    var fotoUrl: String? = null,

    @OneToMany(mappedBy = "usuario", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var partidasParticipa: MutableList<JugadorJuego> = mutableListOf()
){
    fun usuarioToDto(): UsuarioDto{
        val partidasDto = mutableListOf<UsuarioDto.JugadorJuegoDto>()
        for(jugadorJuego in this.partidasParticipa){
            val juegoDto = UsuarioDto.JuegoDto(
                id = jugadorJuego.juego?.id,
                nombre = jugadorJuego.juego?.nombre,
                descripcion = jugadorJuego.juego?.descripcion,
                idioma = jugadorJuego.juego?.idioma,
                maxJugadores = jugadorJuego.juego?.maximoJugadores
            )
            val jugadorJuegoDto = UsuarioDto.JugadorJuegoDto(
                id = jugadorJuego.id,
                juego =juegoDto,
                rol = jugadorJuego.rol.toString()
            )
            partidasDto.add(jugadorJuegoDto)
        }
        val resultado = UsuarioDto(
            id = this.id,
            googleId = this.googleId,
            email = this.email,
            nombre = this.nombre,
            fotoUrl = this.fotoUrl,
            partidasParticipa = partidasDto,
        )
        return resultado
    }
}

