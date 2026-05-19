package org.example.backend.unit.facade

import org.example.backend.dto.CrearPartidaDto
import org.example.backend.dto.PartidaDto
import org.example.backend.facade.PartidaController
import org.example.backend.service.JuegoService
import org.example.backend.service.ObjetoService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import tools.jackson.module.kotlin.jacksonObjectMapper
import org.mockito.kotlin.check

class PartidaControllerTests {

    private val partidaService = mock<JuegoService>()
    private val objetoService = mock<ObjetoService>()
    private val jsonMapper = jacksonObjectMapper()
    private val partidaController = PartidaController(partidaService, objetoService)

    @Test
    fun testCrearPartida() {
        val jugador = CrearPartidaDto.PersonajeDto()
        val dtoEntrada = CrearPartidaDto(
            nombre = "Partida",
            descripcion = "Description",
            idioma = "ES",
            maximoJugadores = 5,
            jugadores = mutableListOf(jugador),
            adminId = 1L
        )

        val payload = jsonMapper.createObjectNode()
        payload.set("juego", jsonMapper.valueToTree(dtoEntrada))


        val partidaCreada = PartidaDto(id = 1L, nombre = "Partida Creada")

        whenever(partidaService.crearJuegoxDTO(any())).thenReturn(partidaCreada)

        val result = partidaController.crearPartida(payload)

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(partidaCreada, result.body)
    }
    @Test
    fun testObtenerPartidas() {

        val partida1 = PartidaDto(id = 1L, nombre = "Partida 1", descripcion = "Desc 1", idioma = "ES", maximoJugadores = 4, adminId = 0)
        val partida2 = PartidaDto(id = 2L, nombre = "Partida 2", descripcion = "Desc 2", idioma = "EN", maximoJugadores = 6, adminId = 0)
        val listaFalsa = listOf(partida1, partida2)


        whenever(partidaService.getAllPartidas()).thenReturn(listaFalsa)


        val result = partidaController.obtenerPartidas()

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(listaFalsa, result.body)
        verify(partidaService).getAllPartidas()
    }
    @Test
    fun testObtenerPartidasListaVacia() {

        val listaVacia = emptyList<PartidaDto>()
        whenever(partidaService.getAllPartidas()).thenReturn(listaVacia)


        val result = partidaController.obtenerPartidas()

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(emptyList<PartidaDto>(), result.body)
        verify(partidaService).getAllPartidas()
    }
    @Test
    fun testCrearPartida_NormalizaAdminIdString_A_Numero() {
        // 1. Preparamos un JSON donde adminId viene como texto ("42") en vez de número
        val payload = jsonMapper.createObjectNode()
        val juegoNode = jsonMapper.createObjectNode().apply {
            put("nombre", "Partida con admin texto")
            put("maximoJugadores", 4)
            put("adminId", "42") // <-- Textual
        }
        payload.set("juego", juegoNode)

        val partidaCreada = PartidaDto(id = 1L, nombre = "Partida Creada")
        whenever(partidaService.crearJuegoxDTO(any())).thenReturn(partidaCreada)

        // 2. Ejecutamos el controlador
        val result = partidaController.crearPartida(payload)

        // 3. Verificamos que la función privada normalizó el "42" a 42L
        assertEquals(HttpStatus.CREATED, result.statusCode)

        verify(partidaService).crearJuegoxDTO(check { dtoCapturado ->
            // Si la normalización funcionó, el DTO tendrá un 42 numérico
            assertEquals(42L, dtoCapturado.adminId)
        })
    }

    @Test
    fun testCrearPartida_EliminaAdminIdSiElTextoEsInvalido() {
        // 1. Preparamos un JSON donde adminId es un texto que no se puede parsear
        val payload = jsonMapper.createObjectNode()
        val juegoNode = jsonMapper.createObjectNode().apply {
            put("nombre", "Partida con admin invalido")
            put("maximoJugadores", 4)
            put("adminId", "texto_invalido_que_no_es_numero") // <-- Inválido
        }
        payload.set("juego", juegoNode)

        val partidaCreada = PartidaDto(id = 1L, nombre = "Partida Creada")
        whenever(partidaService.crearJuegoxDTO(any())).thenReturn(partidaCreada)

        // 2. Ejecutamos el controlador
        val result = partidaController.crearPartida(payload)

        // 3. Verificamos que la función privada eliminó el campo
        assertEquals(HttpStatus.CREATED, result.statusCode)

        verify(partidaService).crearJuegoxDTO(check { dtoCapturado ->
            // Al eliminarse del JSON, Jackson lo mapeará al valor por defecto
            // (Si tu adminId en el DTO es Long?, aquí llegará null. Si es primitivo, podría ser 0L o nulo dependiendo de tu configuración).
            assertEquals(null, dtoCapturado.adminId)
        })
    }
}
