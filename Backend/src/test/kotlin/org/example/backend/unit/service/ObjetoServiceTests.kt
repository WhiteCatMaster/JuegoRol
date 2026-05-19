package org.example.backend.unit.service

import org.example.backend.dto.DatosPartidaDto
import org.example.backend.entity.Estadistica
import org.example.backend.entity.Juego
import org.example.backend.entity.ObjetoCompleto
import org.example.backend.entity.Personaje
import org.example.backend.repository.ObjetoCompletoRepository
import org.example.backend.service.ObjetoService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ObjetoServiceTests {

    // 1. Mockeamos el repositorio
    private val objetoRepo = mock<ObjetoCompletoRepository>()

    // 2. Inyectamos el mock en el servicio
    private val objetoService = ObjetoService(objetoRepo)


    @Test
    fun testObtenerObjetosByJuegoId() {
        // ARRANGE
        val juegoId = 1L
        val objetoMock = mock<ObjetoCompleto>()
        whenever(objetoRepo.findByJuegoId(juegoId)).thenReturn(listOf(objetoMock))

        // ACT
        val resultado = objetoService.obtenerObjetosByJuegoId(juegoId)

        // ASSERT
        assertEquals(1, resultado.size)
        assertEquals(objetoMock, resultado[0])
        verify(objetoRepo).findByJuegoId(juegoId)
    }

    @Test
    fun testToObjetoDto() {
        // ARRANGE
        // Mockeamos estadísticas para que tengan nombre
        val statVida = mock<Estadistica> { on { nombre } doReturn "Vida" }
        val statMana = mock<Estadistica> { on { nombre } doReturn "Mana" }

        // Mockeamos la entidad ObjetoCompleto
        val objetoOriginal = mock<ObjetoCompleto> {
            on { id } doReturn 10L
            on { nombre } doReturn "Poción Curativa"
            on { descripcion } doReturn "Restaura salud"
            on { imagen } doReturn "pocion.png"
            on { usos } doReturn 3
            // Usamos 50.0 (Double), si tu DTO usa Float, cámbialo a 50.0f
            on { efectosPropios } doReturn mutableMapOf(statVida to 50.0)
            on { efectosRival } doReturn mutableMapOf(statMana to -10.0)
        }

        // ACT
        val resultadoDto = objetoService.toObjetoDto(objetoOriginal)

        // ASSERT
        assertEquals(10L, resultadoDto.id)
        assertEquals("Poción Curativa", resultadoDto.nombre)
        assertEquals("Restaura salud", resultadoDto.descripcion)
        assertEquals("pocion.png", resultadoDto.imagen)
        assertEquals(3, resultadoDto.usos)

        // Verificamos que las claves del mapa ahora son Strings
        assertEquals(50.0, resultadoDto.efectosPropios["Vida"])
        assertEquals(-10.0, resultadoDto.efectosRival["Mana"])
    }

    @Test
    fun testToObjeto() {
        // ARRANGE
        // Creamos un DTO real
        val objetoDto = DatosPartidaDto.PersonajeDto.ObjetoDto(
            id = 5L,
            nombre = "Veneno",
            descripcion = "Daña al rival",
            imagen = "veneno.png",
            usos = 1,
            efectosPropios = mutableMapOf(),
            efectosRival = mutableMapOf("Vida" to -20.0) // Si usa Float, pon -20.0f
        )

        // Preparamos la estadística "Vida" para que haga match con la clave del DTO
        val statVida = mock<Estadistica> { on { nombre } doReturn "Vida" }

        val personajeMock = mock<Personaje> {
            on { estadisticas } doReturn mutableListOf(statVida)
        }

        val juegoMock = mock<Juego>()

        // ACT
        val resultadoEntidad = objetoService.toObjeto(objetoDto, personajeMock, juegoMock)

        // ASSERT
        assertEquals(5L, resultadoEntidad.id)
        assertEquals("Veneno", resultadoEntidad.nombre)
        assertEquals("Daña al rival", resultadoEntidad.descripcion)
        assertEquals(1, resultadoEntidad.usos)
        assertEquals(juegoMock, resultadoEntidad.juego)
        assertEquals(personajeMock, resultadoEntidad.personaje)

        // Verificamos que recuperó el objeto Estadistica real a partir del String "Vida"
        assertEquals(-20.0, resultadoEntidad.efectosRival[statVida])
    }
}