package org.example.backend.service

import jakarta.transaction.Transactional
import org.example.backend.dto.CombatePersonajesDto
import org.example.backend.dto.CrearCombateDto
import org.example.backend.entity.Combate
import org.example.backend.entity.JugadorJuego
import org.example.backend.repository.CombateRepository
import org.example.backend.repository.JuegoRepository
import org.example.backend.repository.JugadorJuegoRepository
import org.example.backend.repository.PersonajeRepository
import org.example.backend.repository.UsuarioRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class CombateService (
    private val combateRepo: CombateRepository,
    private val juegoRepo: JuegoRepository,
    private val personajeRepo: PersonajeRepository,
    private val jugadorJuegoRepo: JugadorJuegoRepository,
    private val usuarioRepo: UsuarioRepository,
    private val personajeService: PersonajeService
){
    @Transactional
    fun crearCombatexDTO(combateDTO: CrearCombateDto): CrearCombateDto {
        val partida = juegoRepo.findById(combateDTO.juegoId).get()
        //Deberia de crear 2 nuevos jugador juego para que el admin tambien pueda jugar con el mismo usuario pero en modo jugador
        val jugadorjuego1 = JugadorJuego(
            usuario = usuarioRepo.findById(combateDTO.jugador1.usuarioId?: -1L).get(),
            juego = partida,
            rol = enumValueOf(combateDTO.jugador1.rol),
            personaje = personajeRepo.findById(combateDTO.jugador1.personajeId).get()
        )
        val jugadorjuego2 = JugadorJuego(
            usuario = usuarioRepo.findById(combateDTO.jugador2.usuarioId?: -1L).get(),
            juego = partida,
            rol = enumValueOf(combateDTO.jugador2.rol),
            personaje = personajeRepo.findById(combateDTO.jugador2.personajeId).get()
        )
        val jugadorjuego1Guardado =jugadorJuegoRepo.save(jugadorjuego1)
        val jugadorjuego2Guardado = jugadorJuegoRepo.save(jugadorjuego2)

        val combateCreado = Combate(
            nombre = combateDTO.nombre,
            jugador1 = jugadorjuego1Guardado, // Usamos los que ya tienen ID
            jugador2 = jugadorjuego2Guardado, // Usamos los que ya tienen ID
            juego = partida
        )

        // Ahora sí, Hibernate guardará el combate sin quejarse
        val combateGuardado = combateRepo.save(combateCreado)

        // --- El resto de tu código de mapeo de DTO sigue igual ---
        val usuario1Id = combateGuardado.jugador1.usuario?.id
        val usuario2Id = combateGuardado.jugador2.usuario?.id
        val jugador1 = CrearCombateDto.JugadorDto(
            id = combateGuardado.jugador1.id ?: -1,
            usuarioId = usuario1Id,
            rol = combateGuardado.jugador1.rol.toString(),
            personajeId = combateGuardado.jugador1.personaje?.id ?: -1 ,
        )

        val jugador2 = CrearCombateDto.JugadorDto(
            id = combateGuardado.jugador2.id ?: -1,
            usuarioId = usuario2Id,
            rol = combateGuardado.jugador2.rol.toString(),
            personajeId = combateGuardado.jugador2.personaje?.id ?: -1,
        )

        return CrearCombateDto(
            id = combateGuardado.id ?: -1,
            nombre = combateGuardado.nombre,
            jugador1 = jugador1,
            jugador2 = jugador2,
            juegoId = combateGuardado.juego.id ?: -1
        )
    }

    fun obtenerCombateById(id: Long): ResponseEntity<CombatePersonajesDto> {
        val combateGuardado = combateRepo.findById(id).get()
        val personaje1Dto = personajeService.personajeToDto(combateGuardado.jugador1.personaje!!)
        val personaje2Dto = personajeService.personajeToDto(combateGuardado.jugador2.personaje!!)
        println(combateGuardado.jugador1)
        val resultado = CombatePersonajesDto(
            id = combateGuardado.id ?: -1,
            personaje1 = personaje1Dto,
            personaje2 = personaje2Dto
        )
        return ResponseEntity.ok(resultado)
    }
}
