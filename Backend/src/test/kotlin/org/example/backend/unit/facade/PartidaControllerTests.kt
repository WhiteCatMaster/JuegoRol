package org.example.backend.unit.facade

import org.example.backend.dto.CrearPartidaDto
import org.example.backend.dto.DatosPartidaDto
import org.example.backend.dto.PartidaDto
import org.example.backend.entity.ObjetoCompleto
import org.example.backend.facade.PartidaController
import org.example.backend.service.JuegoService
import org.example.backend.service.ObjetoService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
    fun testObtenerObjetos() {
        val juegoId = 7L
        val objeto1 = ObjetoCompleto(
            id = 100L,
            nombre = "Espada",
            descripcion = "Filosa",
            imagen = "espada.png",
            usos = 3
        )
        val dtoFalso = DatosPartidaDto.PersonajeDto.ObjetoDto(
            id = 100L,
            nombre = "Espada",
            descripcion = "Filosa",
            imagen = "espada.png",
            efectosPropios = mutableMapOf(),
            efectosRival = mutableMapOf(),
            usos = 3
        )

        whenever(objetoService.obtenerObjetosByJuegoId(juegoId)).thenReturn(listOf(objeto1))
        whenever(objetoService.toObjetoDto(objeto1)).thenReturn(dtoFalso)

        val result = partidaController.obtenerObjetos(juegoId)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(1, result.body?.size)
        assertEquals(dtoFalso, result.body!![0])
        verify(objetoService).obtenerObjetosByJuegoId(juegoId)
        verify(objetoService).toObjetoDto(objeto1)
    }

    @Test
    fun testObtenerObjetos_ListaVacia() {
        val juegoId = 8L
        whenever(objetoService.obtenerObjetosByJuegoId(juegoId)).thenReturn(emptyList())

        val result = partidaController.obtenerObjetos(juegoId)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(0, result.body?.size)
    }

    @Test
    fun testCrearPartida_SinNodoJuego() {
        // Cubre la rama Elvis: payload.get("juego") ?: payload
        // Cuando el payload no tiene un nodo "juego", se usa el payload entero
        val payload = jsonMapper.createObjectNode().apply {
            put("nombre", "Partida directa")
            put("maximoJugadores", 3)
        }

        val partidaCreada = PartidaDto(id = 99L, nombre = "Partida directa")
        whenever(partidaService.crearJuegoxDTO(any())).thenReturn(partidaCreada)

        val result = partidaController.crearPartida(payload)

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(partidaCreada, result.body)
    }

    @Test
    fun testCrearPartida_NodoNoEsObjectNode() {
        // Cubre la rama del `as? ObjectNode` cuando el nodo no es un ObjectNode
        // Se pasa un payload donde "juego" es un nodo de texto, no un objeto
        val payload = jsonMapper.createObjectNode()
        payload.put("juego", "esto-no-es-un-objeto")

        // El controlador intentará deserializar un texto como CrearPartidaDto y fallará
        val result = partidaController.crearPartida(payload)

        // Caemos en el catch -> 400 Bad Request
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        val body = result.body as Map<*, *>
        assertNotNull(body["error"])
    }

    @Test
    fun testCrearPartida_AdminIdNulo() {
        // Cubre las ramas: adminIdNode == null y la eliminación del campo
        val payload = jsonMapper.createObjectNode()
        val juegoNode = jsonMapper.createObjectNode().apply {
            put("nombre", "Partida sin admin")
            put("maximoJugadores", 4)
            putNull("adminId") // <-- isNull == true
        }
        payload.set("juego", juegoNode)

        val partidaCreada = PartidaDto(id = 1L, nombre = "Partida sin admin")
        whenever(partidaService.crearJuegoxDTO(any())).thenReturn(partidaCreada)

        val result = partidaController.crearPartida(payload)

        assertEquals(HttpStatus.CREATED, result.statusCode)
        verify(partidaService).crearJuegoxDTO(check { dto ->
            assertEquals(null, dto.adminId)
        })
    }

    @Test
    fun testCrearPartida_SinCampoAdminId() {
        // Cubre la rama: adminIdNode == null (cuando no existe el campo)
        val payload = jsonMapper.createObjectNode()
        val juegoNode = jsonMapper.createObjectNode().apply {
            put("nombre", "Partida sin campo admin")
            put("maximoJugadores", 4)
            // No incluimos adminId
        }
        payload.set("juego", juegoNode)

        val partidaCreada = PartidaDto(id = 2L, nombre = "Partida sin campo admin")
        whenever(partidaService.crearJuegoxDTO(any())).thenReturn(partidaCreada)

        val result = partidaController.crearPartida(payload)

        assertEquals(HttpStatus.CREATED, result.statusCode)
    }

    @Test
    fun testCrearPartida_ServicioLanzaExcepcion() {
        // Cubre el catch -> 400 Bad Request
        val payload = jsonMapper.createObjectNode()
        val juegoNode = jsonMapper.createObjectNode().apply {
            put("nombre", "Partida con error")
            put("maximoJugadores", 4)
        }
        payload.set("juego", juegoNode)

        whenever(partidaService.crearJuegoxDTO(any())).thenThrow(RuntimeException("Fallo en BD"))

        val result = partidaController.crearPartida(payload)

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        val body = result.body as Map<*, *>
        assertEquals("Fallo en BD", body["error"])
    }

    @Test
    fun testObtenerPartidas_ConPartidaIdNulo() {
        // Cubre la rama Elvis: partida.id ?: -1L
        val partidaSinId = PartidaDto(id = null, nombre = "Sin Id", descripcion = "x", idioma = "ES", maximoJugadores = 4, adminId = 0)
        whenever(partidaService.getAllPartidas()).thenReturn(listOf(partidaSinId))
        whenever(partidaService.obtenerIdAdminxPartida(-1L)).thenReturn(0)

        val result = partidaController.obtenerPartidas()

        assertEquals(HttpStatus.OK, result.statusCode)
        verify(partidaService).obtenerIdAdminxPartida(-1L)
    }

    @Test
    fun testObtenerDatosPartida() {
        val id = 5L
        val datos = mock<org.springframework.http.ResponseEntity<DatosPartidaDto>>()
        whenever(partidaService.obtenerDatosPartida(id)).thenReturn(datos)

        val result = partidaController.obtenerDatosPartida(id)

        assertEquals(datos, result)
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
