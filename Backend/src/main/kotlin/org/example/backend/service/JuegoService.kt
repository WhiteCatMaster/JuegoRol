package org.example.backend.service

import jakarta.transaction.Transactional
import org.example.backend.dto.JuegoDto
import org.example.backend.dto.PartidaDto
import org.example.backend.entity.Ataque
import org.example.backend.entity.Estadistica
import org.example.backend.entity.Juego
import org.example.backend.entity.JugadorJuego
import org.example.backend.entity.Personaje
import org.example.backend.entity.RolJugador
import org.example.backend.entity.Usuario
import org.example.backend.repository.AtaqueRepository
import org.example.backend.repository.EstadisticaRepository
import org.example.backend.repository.JuegoRepository
import org.example.backend.repository.JugadorJuegoRepository
import org.example.backend.repository.PersonajeRepository
import org.example.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class JuegoService(
    private val juegoRepo: JuegoRepository,
    private val usuarioRepo: UsuarioRepository,
    private val jugadorJuegoRepo: JugadorJuegoRepository,
    private val personajeRepo: PersonajeRepository,
    private val estadisticaRepo: EstadisticaRepository,
    private val ataqueRepo: AtaqueRepository
) {
    fun getAllJuegos() = juegoRepo.findAll()

    fun getAllPartidas(): List<PartidaDto> {
        return juegoRepo.findAll().map { juego ->
            PartidaDto(
                id = juego.id,
                nombre = juego.nombre,
                descripcion = juego.descripcion,
                idioma = juego.idioma,
                maximoJugadores = juego.maximoJugadores
            )
        }
    }

    fun getDatosPartida(nombrePartida: String): JuegoDto? {
        val juego = juegoRepo.findByNombre(nombrePartida) ?: return null

        val jugadoresDTO = juego.jugadores.map { jugadorJuego ->
            JuegoDto.JugadorJuegoDto(
                id = jugadorJuego.id,
                usuarioGoogleId = jugadorJuego.usuario?.googleId,
                usuarioEmail = jugadorJuego.usuario?.email,
                usuarioNombre = jugadorJuego.usuario?.nombre,
                usuarioFotoUrl = jugadorJuego.usuario?.fotoUrl,
                rol = jugadorJuego.rol,
                personajeNombre = jugadorJuego.personaje?.nombre,
                personajeVida = jugadorJuego.personaje?.vida,
                personajeFotoUrl = jugadorJuego.personaje?.fotoUrl,
                personajeEstadisticas = jugadorJuego.personaje?.estadisticas?.map { stats ->
                    JuegoDto.JugadorJuegoDto.EstadisticaDto(
                        nombre = stats.nombre,
                        valor = stats.valor.toString(),
                        consumible = stats.consumible
                    )
                }?.toMutableList() ?: mutableListOf(),
                personajeAtaques = jugadorJuego.personaje?.ataques?.map { ataque ->
                    JuegoDto.JugadorJuegoDto.AtaqueDto(
                        id = ataque.id,
                        nombre = ataque.nombre,
                        manaAtacante = ataque.manaAtacante.toMutableMap(),
                        estadisticasDefensor = ataque.estadisticasDefensor.toMutableMap(),
                        dadoBase = ataque.dadoBase,
                        ratioDado = ataque.ratioDado.toMutableList()
                    )
                }?.toMutableList() ?: mutableListOf()
            )
        }.toMutableList()

        return JuegoDto(
            nombre = juego.nombre,
            descripcion = juego.descripcion,
            idioma = juego.idioma,
            maximoJugadores = juego.maximoJugadores,
            jugadores = jugadoresDTO
        )
    }

    fun createJuego(juego: Juego) = juegoRepo.save(juego)

    fun eliminarJuego(id: Long) = juegoRepo.deleteById(id)

    fun asignarJugadorJuego(juegoId: Long, jugadorJuego: JugadorJuego): Juego? {
        val juego = juegoRepo.findById(juegoId).orElse(null) ?: return null
        juego.jugadores.add(jugadorJuego)
        return juegoRepo.save(juego)
    }

    fun modificarJuego(juegoId: Long, updatedJuego: Juego): Juego? {
        val existingJuego = juegoRepo.findById(juegoId).orElse(null) ?: return null
        existingJuego.nombre = updatedJuego.nombre
        existingJuego.jugadores = updatedJuego.jugadores
        return juegoRepo.save(existingJuego)
    }
    fun cambiarIdiomaJuego(juegoId: Long, nuevoIdioma: String): Juego? {
        val existingJuego = juegoRepo.findById(juegoId).orElse(null) ?: return null
        existingJuego.idioma = nuevoIdioma
        return juegoRepo.save(existingJuego)
    }
    @Transactional
    fun crearJuegoxDTO(juegoDTO: JuegoDto): Juego {
        // Crear el Juego
        val juego = Juego(
            nombre = juegoDTO.nombre ?: "",
            idioma = juegoDTO.idioma,
            descripcion = juegoDTO.descripcion,
            maximoJugadores = juegoDTO.maximoJugadores
        )
        val juegoGuardado = juegoRepo.save(juego)

        // Crear los jugadores del DTO
        for (jugadorDTO in juegoDTO.jugadores) {
            // Crear o buscar el Usuario
            val usuario = usuarioRepo.encontrarxGoogleId(jugadorDTO.usuarioGoogleId ?: "")
                .orElseGet {
                    Usuario(
                        googleId = jugadorDTO.usuarioGoogleId ?: "",
                        email = jugadorDTO.usuarioEmail ?: "",
                        nombre = jugadorDTO.usuarioNombre ?: "",
                        fotoUrl = jugadorDTO.usuarioFotoUrl
                    )
                }.let { usuarioRepo.save(it) }

            // Crear JugadorJuego
            val jugadorJuego = JugadorJuego(
                usuario = usuario,
                juego = juegoGuardado,
                rol = jugadorDTO.rol ?: RolJugador.JUGADOR,
                personaje = null // Se asignará después
            )
            val jugadorJuegoGuardado = jugadorJuegoRepo.save(jugadorJuego)

            // Crear Personaje si existe en el DTO
            if (jugadorDTO.personajeNombre != null) {
                val personaje = Personaje(
                    nombre = jugadorDTO.personajeNombre,
                    vida = jugadorDTO.personajeVida ?: 100,
                    fotoUrl = jugadorDTO.personajeFotoUrl,
                    jugadorJuego = jugadorJuegoGuardado
                )
                val personajeGuardado = personajeRepo.save(personaje)

                // Crear Estadísticas del Personaje
                for (estadisticaDTO in jugadorDTO.personajeEstadisticas) {
                    val estadistica = Estadistica(
                        nombre = estadisticaDTO.nombre ?: "",
                        valor = estadisticaDTO.valor?.toIntOrNull() ?: 0,
                        consumible = estadisticaDTO.consumible,
                        personaje = personajeGuardado
                    )
                    estadisticaRepo.save(estadistica)
                    personajeGuardado.estadisticas.add(estadistica)
                }

                // Crear Ataques del Personaje
                for (ataqueDTO in jugadorDTO.personajeAtaques) {
                    val ataque = Ataque(
                        nombre = ataqueDTO.nombre ?: "",
                        manaAtacante = ataqueDTO.manaAtacante, // Usa Map<Long, Int>
                        estadisticasDefensor = ataqueDTO.estadisticasDefensor, // Usa Map<Long, Double>
                        owner = personajeGuardado,
                        dadoBase = ataqueDTO.dadoBase,
                        ratioDado = ataqueDTO.ratioDado
                    )
                    ataqueRepo.save(ataque)
                    personajeGuardado.ataques.add(ataque)
                }

                // Guardar el Personaje actualizado
                personajeRepo.save(personajeGuardado)

                // Actualizar el JugadorJuego con el Personaje
                jugadorJuegoGuardado.personaje = personajeGuardado
                jugadorJuegoRepo.save(jugadorJuegoGuardado)
            }

            // Agregar el JugadorJuego al Juego
            juegoGuardado.jugadores.add(jugadorJuegoGuardado)
        }

        // Guardar el Juego final con todos los jugadores
        return juegoRepo.save(juegoGuardado)
    }

}