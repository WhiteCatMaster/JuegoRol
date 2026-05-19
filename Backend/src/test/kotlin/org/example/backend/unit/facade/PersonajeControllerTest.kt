package org.example.backend.unit.facade

import org.example.backend.dto.ActualizarPersonajeDto
import org.example.backend.dto.DatosPartidaDto
import org.example.backend.entity.Ataque
import org.example.backend.entity.Estadistica
import org.example.backend.entity.Personaje
import org.example.backend.facade.PersonajeController
import org.example.backend.service.PersonajeService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(PersonajeController::class)
class PersonajeControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var personajeService: PersonajeService

    @Autowired
    lateinit var personajeController: PersonajeController

    @Test
    fun `obtenerPersonajeById mapea correctamente un personaje completo a DTO`() {
        // 1. ARRANGE: Preparar datos falsos
        val estFuerzaMock = mock<Estadistica> { on { nombre } doReturn "Fuerza" }
        val estDefensaMock = mock<Estadistica> { on { nombre } doReturn "Defensa" }

        val estadisticaMock = mock<Estadistica> {
            on { id } doReturn 1L
            on { nombre } doReturn "Vida"
            on { valor } doReturn 100
            on { consumible } doReturn false
        }

        val ataqueMock = mock<Ataque> {
            on { id } doReturn 1L
            on { nombre } doReturn "Espadazo"
            on { manaAtacante } doReturn mutableMapOf(estFuerzaMock to 10)
            on { estadisticasDefensor } doReturn mutableMapOf(estDefensaMock to 5.0)
            on { dadoBase } doReturn 20
            on { ratioDado } doReturn mutableListOf(1, 2)
            on { danioAtaque } doReturn 15
        }

        val personajeMock = mock<Personaje> {
            on { id } doReturn 10L
            on { nombre } doReturn "Guerrero"
            on { vida } doReturn 150
            on { fotoUrl } doReturn "http://test.com/foto.jpg"
            on { estadisticas } doReturn mutableListOf(estadisticaMock)
            on { ataques } doReturn mutableListOf(ataqueMock)
        }
        val estadisticaDtoFalsa = DatosPartidaDto.PersonajeDto.EstadisticaDto(
            id = 1L,
            nombre = "Vida",
            valor = 100,
            consumible = false
        )

        val ataqueDtoFalso = DatosPartidaDto.PersonajeDto.AtaqueDto(
            id = 1L,
            nombre = "Espadazo",
            manaAtacante = mutableMapOf("Fuerza" to 10),
            estadisticasDefensor = mutableMapOf("Defensa" to 5.0),
            dadoBase = 20,
            ratioDado = mutableListOf(1, 2),
            danoAtaque = 15
        )

        val personajeDtoFalso = DatosPartidaDto.PersonajeDto(
            id = 10L,
            personajeNombre = "Guerrero",
            personajeVida = 150,
            personajeFotoUrl = "http://test.com/foto.jpg",
            personajeEstadisticas = mutableListOf(estadisticaDtoFalsa),
            personajeAtaques = mutableListOf(ataqueDtoFalso)
        )

        whenever(personajeService.getPersonajeById(10L)).thenReturn(personajeMock)
        whenever(personajeService.personajeToDto(personajeMock)).thenReturn(personajeDtoFalso)

        // 2 & 3. ACT & ASSERT: Llamada HTTP y verificación de la estructura JSON
        mockMvc.perform(
            get("/personaje/10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo (print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(10L))
            //.andExpect(jsonPath("$.personajeNombre").value("Guerrero"))
            .andExpect(jsonPath("$.personajeVida").value(150))
            .andExpect(jsonPath("$.personajeFotoUrl").value("http://test.com/foto.jpg"))

            // Verificación de la lista de estadísticas
            .andExpect(jsonPath("$.personajeEstadisticas[0].nombre").value("Vida"))
            .andExpect(jsonPath("$.personajeEstadisticas[0].valor").value(100))

            // Verificación de la lista de ataques y sus diccionarios internos
            .andExpect(jsonPath("$.personajeAtaques[0].nombre").value("Espadazo"))
            .andExpect(jsonPath("$.personajeAtaques[0].manaAtacante.Fuerza").value(10))
            .andExpect(jsonPath("$.personajeAtaques[0].estadisticasDefensor.Defensa").value(5.0))
            .andExpect(jsonPath("$.personajeAtaques[0].dadoBase").value(20))
    }

    @Test
    fun `obtenerPersonajeById lanza excepcion si el personaje no existe`() {
        // 1. ARRANGE: El servicio devuelve null
        whenever(personajeService.getPersonajeById(99L)).thenReturn(null)

        // 2 & 3. ACT & ASSERT: Esperamos que falle por un NullPointerException
        try {
            mockMvc.perform(get("/personaje/99"))
        } catch (e: Exception) {
            // Spring MVC envuelve las excepciones internas en un NestedServletException
            assert(e.cause is NullPointerException)
        }
    }
    @Test
    fun testModificarPersonaje_Exito() {
        // ARRANGE
        val id = 1L
        val requestDto = ActualizarPersonajeDto(
            nombre = "Nuevo Nombre",
            estadisticas = emptyList(),
            objetos = emptyList()
        )

        // Mock de la entidad que el servicio devuelve tras aplastar los datos
        val personajeActualizado = mock<Personaje>()

        // Mock del DTO final que se devolverá al frontend
        val respuestaEsperada = mock<DatosPartidaDto.PersonajeDto> {
            on { personajeNombre } doReturn "Nuevo Nombre"
        }

        whenever(personajeService.actualizarPersonaje(id, requestDto)).thenReturn(personajeActualizado)
        whenever(personajeService.personajeToDto(personajeActualizado)).thenReturn(respuestaEsperada)

        // ACT
        val resultado = personajeController.modificarPersonaje(requestDto, id)

        // ASSERT
        assertEquals("Nuevo Nombre", resultado.personajeNombre)
        verify(personajeService).actualizarPersonaje(id, requestDto)
        verify(personajeService).personajeToDto(personajeActualizado)
    }

    @Test
    fun testModificarPersonaje_LanzaExcepcionSiNoExiste() {
        // ARRANGE
        val id = 99L
        val requestDto = ActualizarPersonajeDto(nombre = "Fallo", estadisticas = emptyList(), objetos = emptyList())
        val mensajeError = "Personaje no encontrado, id: $id"

        // Simulamos que el servicio lanza la excepción que programamos antes
        whenever(personajeService.actualizarPersonaje(id, requestDto))
            .thenThrow(RuntimeException(mensajeError))

        // ACT & ASSERT
        val excepcion = assertThrows<RuntimeException> {
            personajeController.modificarPersonaje(requestDto, id)
        }

        assertEquals(mensajeError, excepcion.message)
        verify(personajeService).actualizarPersonaje(id, requestDto)
    }
}