package org.example.backend.unit.service

import org.example.backend.entity.Estadistica
import org.example.backend.entity.JugadorJuego
import org.example.backend.entity.Personaje
import org.example.backend.entity.RolJugador
import org.example.backend.repository.PersonajeRepository
import org.example.backend.service.ObjetoService
import org.example.backend.service.PersonajeService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class PersonajeServiceTests {

    private val personajeRepo: PersonajeRepository = mock<PersonajeRepository>()
    private val objetoService: ObjetoService = mock<ObjetoService>()
    private val personajeService = PersonajeService(personajeRepo, objetoService)

    @Test
    fun testGetAllPersonajes() {
        val estadisticaFalsa = Estadistica(1L, "Falsa", 1, true)
        val jugadorJuego1 = JugadorJuego(1L, null, null, RolJugador.JUGADOR, null)
        val jugadorJuego2 = JugadorJuego(2L, null, null, RolJugador.JUGADOR, null)
        val listaFalsa = listOf(
            Personaje(1L, "Guerrero", 100, "url_foto", jugadorJuego1, mutableListOf(estadisticaFalsa)),
            Personaje(2L, "Mago", 80, "url_foto_2", jugadorJuego2, mutableListOf(estadisticaFalsa))
        )
        whenever(personajeRepo.findAll()).thenReturn(listaFalsa)

        val resultado = personajeService.getAllPersonajes()

        assertEquals(2, resultado.size)
        verify(personajeRepo).findAll()
    }

    @Test
    fun testGetPersonajeById() {
        val estadisticaFalsa = Estadistica(1L, "Falsa", 1, true)
        val jugadorJuego1 = JugadorJuego(1L, null, null, RolJugador.JUGADOR, null)
        val personajeFalso = Personaje(1L, "Guerrero", 100, "url", jugadorJuego1, mutableListOf(estadisticaFalsa))
        whenever(personajeRepo.findById(1L)).thenReturn(Optional.of(personajeFalso))

        val resultado = personajeService.getPersonajeById(1L)

        assertEquals(1L, resultado?.id)
        assertEquals("Guerrero", resultado?.nombre)
        verify(personajeRepo).findById(1L)
    }

    @Test
    fun testGetPersonajeByIdInexistente() {
        whenever(personajeRepo.findById(99L)).thenReturn(Optional.empty())

        val resultado = personajeService.getPersonajeById(99L)

        assertEquals(null, resultado)
        verify(personajeRepo).findById(99L)
    }

    @Test
    fun testCreatePersonaje() {
        val estadisticaFalsa = Estadistica(1L, "Falsa", 1, true)
        val jugadorJuego1 = JugadorJuego(1L, null, null, RolJugador.JUGADOR, null)
        val jugadorJuego2 = JugadorJuego(2L, null, null, RolJugador.JUGADOR, null)
        val nuevoPersonaje = Personaje(null, "Arquero", 90, "url", jugadorJuego1, mutableListOf(estadisticaFalsa))
        val personajeGuardado = Personaje(1L, "Arquero", 90, "url2", jugadorJuego2, mutableListOf(estadisticaFalsa))

        whenever(personajeRepo.save(nuevoPersonaje)).thenReturn(personajeGuardado)

        val resultado = personajeService.createPersonaje(nuevoPersonaje)

        assertEquals(1L, resultado.id)
        verify(personajeRepo).save(nuevoPersonaje)
    }

    @Test
    fun testUpdateNombrePersonaje() {
        val jugadorJuego1 = JugadorJuego(1L, null, null, RolJugador.JUGADOR, null)
        val jugadorJuego2 = JugadorJuego(2L, null, null, RolJugador.JUGADOR, null)
        val estadisticaFalsa = Estadistica(1L, "Falsa", 1, true)
        val personajeExistente = Personaje(1L, "Viejo Nombre", 100, "url", jugadorJuego1, mutableListOf(estadisticaFalsa))
        val datosActualizados = Personaje(null, "Nuevo Nombre", 100, "url", jugadorJuego2, mutableListOf(estadisticaFalsa))

        whenever(personajeRepo.findById(1L)).thenReturn(Optional.of(personajeExistente))
        whenever(personajeRepo.save(personajeExistente)).thenReturn(personajeExistente)

        val resultado = personajeService.updateNombrePersonaje(1L, datosActualizados)

        assertEquals("Nuevo Nombre", resultado?.nombre)
        verify(personajeRepo).findById(1L)
        verify(personajeRepo).save(personajeExistente)
    }

    @Test
    fun testUpdateNombrePersonajeInexistente() {
        val estadisticaFalsa = Estadistica(1L, "Falsa", 1, true)
        val jugadorJuego1 = JugadorJuego(1L, null, null, RolJugador.JUGADOR, null)
        val datosActualizados = Personaje(null, "Nuevo Nombre", 100, "url", jugadorJuego1, mutableListOf(estadisticaFalsa))
        whenever(personajeRepo.findById(99L)).thenReturn(Optional.empty())

        val resultado = personajeService.updateNombrePersonaje(99L, datosActualizados)

        assertEquals(null, resultado)
        verify(personajeRepo).findById(99L)
        verify(personajeRepo, never()).save(any())
    }

    @Test
    fun testDeletePersonaje() {
        personajeService.deletePersonaje(1L)
        verify(personajeRepo).deleteById(1L)
    }

    @Test
    fun testActualizarEstadisticaPersonaje() {
        val jugadorJuego1 = JugadorJuego(1L, null, null, RolJugador.JUGADOR, null)
        val personajeExistente = Personaje(1L, "Guerrero", 100, "url", jugadorJuego1, mutableListOf())

        // Simulamos la nueva lista de estadísticas
        val nuevasEstadisticas = mutableListOf(
            Estadistica(1L, "Fuerza", 20, false, null)
        )
        val datosActualizados = Personaje(null, "Guerrero", 100, "url", jugadorJuego1, nuevasEstadisticas)

        whenever(personajeRepo.findById(1L)).thenReturn(Optional.of(personajeExistente))
        whenever(personajeRepo.save(personajeExistente)).thenReturn(personajeExistente)

        val resultado = personajeService.actualizarEstadisticaPersonaje(1L, datosActualizados)

        assertEquals(1, resultado?.estadisticas?.size)
        assertEquals("Fuerza", resultado?.estadisticas?.first()?.nombre)
        verify(personajeRepo).findById(1L)
        verify(personajeRepo).save(personajeExistente)
    }

    @Test
    fun testActualizarEstadisticaPersonajeInexistente() {
        val jugadorJuego1 = JugadorJuego(1L, null, null, RolJugador.JUGADOR, null)
        val estadisticaFalsa = Estadistica(1L, "Falsa", 1, true)
        val datosActualizados = Personaje(null, "Guerrero", 100, "url", jugadorJuego1, mutableListOf(estadisticaFalsa))
        whenever(personajeRepo.findById(99L)).thenReturn(Optional.empty())

        val resultado = personajeService.actualizarEstadisticaPersonaje(99L, datosActualizados)

        assertEquals(null, resultado)
        verify(personajeRepo).findById(99L)
        verify(personajeRepo, never()).save(any())
    }
}