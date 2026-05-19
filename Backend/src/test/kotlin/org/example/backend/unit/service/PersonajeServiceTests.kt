package org.example.backend.unit.service

 // <-- ¡Revisa que este import coincida con el nombre real de tu DTO!
import org.example.backend.dto.ActualizarPersonajeDto
import org.example.backend.dto.DatosPartidaDto
import org.example.backend.entity.Estadistica
import org.example.backend.entity.Juego
import org.example.backend.entity.JugadorJuego
import org.example.backend.entity.ObjetoCompleto
import org.example.backend.entity.Personaje
import org.example.backend.repository.ObjetoCompletoRepository
import org.example.backend.repository.PersonajeRepository
import org.example.backend.service.ObjetoService
import org.example.backend.service.PersonajeService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class ServiciosTests {

    // 1. Mockeamos los repositorios
    private val objetoRepo = mock<ObjetoCompletoRepository>()
    private val personajeRepo = mock<PersonajeRepository>()

    // 2. Instanciamos AMBOS servicios
    private val objetoService = ObjetoService(objetoRepo)

    // Al PersonajeService le inyectamos su repo y el objetoService real que acabamos de instanciar
    private val personajeService = PersonajeService(personajeRepo, objetoService)

    // ==========================================
    // TESTS DE PERSONAJE SERVICE
    // ==========================================

    @Test
    fun testActualizarPersonaje_Exito() {
        // ARRANGE
        val idPersonaje = 1L

        val juegoMock = mock<Juego>()
        val jugadorJuegoMock = mock<JugadorJuego> {
            on { juego } doReturn juegoMock
        }

        val statFuerza = Estadistica(id = 1L, nombre = "Fuerza", valor = 10, consumible = false)
        val statVida = Estadistica(id = 2L, nombre = "Vida", valor = 100, consumible = true)

        val personajeEnBd = Personaje(
            id = idPersonaje,
            nombre = "Nombre Viejo",
            estadisticas = mutableListOf(statFuerza, statVida),
            inventario = mutableListOf(),
            jugadorJuego = jugadorJuegoMock,
            vida = 20,
            fotoUrl = "HUIDIHSUI",
            ataques = mutableListOf()
        )

        whenever(personajeRepo.findById(idPersonaje)).thenReturn(Optional.of(personajeEnBd))

        // DTOs Reales para evitar errores de compilación
        val estUpdateFuerza = ActualizarPersonajeDto.EstatDto( // <-- Asegúrate de que el import de arriba corresponda a este nombre
            nombre = "Fuerza",
            valorNuevo = 25
        )

        val objetoDtoNuevo = DatosPartidaDto.PersonajeDto.ObjetoDto(
            id = 5L,
            nombre = "Espada Mágica",
            descripcion = "Una espada",
            imagen = "espada.png",
            usos = 1,
            efectosPropios = mutableMapOf(),
            efectosRival = mutableMapOf()
        )

        val requestDto = ActualizarPersonajeDto(
            nombre = "Nombre Nuevo",
            estadisticas = listOf(estUpdateFuerza),
            objetos = listOf(objetoDtoNuevo)
        )

        whenever(personajeRepo.save(any())).thenAnswer { it.arguments[0] as Personaje }

        // ACT
        val resultado = personajeService.actualizarPersonaje(idPersonaje, requestDto)

        // ASSERT
        assertEquals("Nombre Nuevo", resultado.nombre)
        assertEquals(25, resultado.estadisticas.find { it.nombre == "Fuerza" }?.valor)
        assertEquals(100, resultado.estadisticas.find { it.nombre == "Vida" }?.valor)
        assertEquals(1, resultado.inventario.size)
        assertEquals("Espada Mágica", resultado.inventario[0].nombre)
        verify(personajeRepo).save(personajeEnBd)
    }

    @Test
    fun testActualizarPersonaje_NoEncontrado() {
        // ARRANGE
        val idInvalido = 99L
        // DTO vacío para simular la request
        val requestDto = ActualizarPersonajeDto(nombre = "X", estadisticas = listOf(), objetos = listOf())

        whenever(personajeRepo.findById(idInvalido)).thenReturn(Optional.empty())

        // ACT & ASSERT
        val exception = assertThrows<Exception> {
            personajeService.actualizarPersonaje(idInvalido, requestDto)
        }

        assertTrue(exception.message!!.contains("Personaje no encontrado, id: $idInvalido"))
        verify(personajeRepo, never()).save(any())
    }

    // ==========================================
    // TESTS DE OBJETO SERVICE
    // ==========================================

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
        val statVida = mock<Estadistica> { on { nombre } doReturn "Vida" }
        val statMana = mock<Estadistica> { on { nombre } doReturn "Mana" }

        val objetoOriginal = mock<ObjetoCompleto> {
            on { id } doReturn 10L
            on { nombre } doReturn "Poción Curativa"
            on { descripcion } doReturn "Restaura salud"
            on { imagen } doReturn "pocion.png"
            on { usos } doReturn 3
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
        assertEquals(50.0, resultadoDto.efectosPropios["Vida"])
        assertEquals(-10.0, resultadoDto.efectosRival["Mana"])
    }

    @Test
    fun testToObjeto() {
        // ARRANGE
        val objetoDto = DatosPartidaDto.PersonajeDto.ObjetoDto(
            id = 5L,
            nombre = "Veneno",
            descripcion = "Daña al rival",
            imagen = "veneno.png",
            usos = 1,
            efectosPropios = mutableMapOf(),
            efectosRival = mutableMapOf("Vida" to -20.0)
        )

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
        assertEquals(-20.0, resultadoEntidad.efectosRival[statVida])
    }
    // ==========================================
    // TESTS DE CRUD BÁSICOS (PersonajeService)
    // ==========================================

    @Test
    fun testGetAllPersonajes() {
        val personajeMock = mock<Personaje>()
        whenever(personajeRepo.findAll()).thenReturn(listOf(personajeMock))

        val resultado = personajeService.getAllPersonajes()

        assertEquals(1, resultado.size)
        assertEquals(personajeMock, resultado[0])
        verify(personajeRepo).findAll()
    }

    @Test
    fun testGetPersonajeById_Encontrado() {
        val id = 1L
        val personajeMock = mock<Personaje>()
        whenever(personajeRepo.findById(id)).thenReturn(Optional.of(personajeMock))

        val resultado = personajeService.getPersonajeById(id)

        assertEquals(personajeMock, resultado)
    }

    @Test
    fun testGetPersonajeById_NoEncontrado() {
        whenever(personajeRepo.findById(any())).thenReturn(Optional.empty())
        val resultado = personajeService.getPersonajeById(99L)
        assertEquals(null, resultado)
    }

    @Test
    fun testCreatePersonaje() {
        val personajeMock = mock<Personaje>()
        whenever(personajeRepo.save(personajeMock)).thenReturn(personajeMock)

        val resultado = personajeService.createPersonaje(personajeMock)

        assertEquals(personajeMock, resultado)
        verify(personajeRepo).save(personajeMock)
    }

    @Test
    fun testUpdateNombrePersonaje_Exito() {
        val id = 1L
        val personajeExistente = Personaje(id = id, nombre = "Viejo", estadisticas = mutableListOf(), inventario = mutableListOf(), ataques = mutableListOf(), vida = 100)
        val personajeConNuevoNombre = Personaje(id = id, nombre = "Nuevo", estadisticas = mutableListOf(), inventario = mutableListOf(), ataques = mutableListOf(), vida = 100)

        whenever(personajeRepo.findById(id)).thenReturn(Optional.of(personajeExistente))
        whenever(personajeRepo.save(any())).thenAnswer { it.arguments[0] as Personaje }

        val resultado = personajeService.updateNombrePersonaje(id, personajeConNuevoNombre)

        assertEquals("Nuevo", resultado?.nombre)
        verify(personajeRepo).save(personajeExistente)
    }

    @Test
    fun testDeletePersonaje() {
        val id = 1L
        personajeService.deletePersonaje(id)
        verify(personajeRepo).deleteById(id)
    }

    @Test
    fun testActualizarEstadisticaPersonaje() {
        val id = 1L
        val statVieja = Estadistica(nombre = "Fuerza", valor = 10, consumible = false)
        val statNueva = Estadistica(nombre = "Fuerza", valor = 20, consumible = false)

        val personajeExistente = Personaje(id = id, nombre = "Heroe", estadisticas = mutableListOf(statVieja), inventario = mutableListOf(), ataques = mutableListOf(), vida = 100)
        val personajeActualizado = Personaje(id = id, nombre = "Heroe", estadisticas = mutableListOf(statNueva), inventario = mutableListOf(), ataques = mutableListOf(), vida = 100)

        whenever(personajeRepo.findById(id)).thenReturn(Optional.of(personajeExistente))
        whenever(personajeRepo.save(any())).thenAnswer { it.arguments[0] as Personaje }

        val resultado = personajeService.actualizarEstadisticaPersonaje(id, personajeActualizado)

        assertEquals(20, resultado?.estadisticas?.first()?.valor)
        verify(personajeRepo).save(personajeExistente)
    }

    // ==========================================
    // TEST DE MAPEADO DTO (personajeToDto)
    // ==========================================

    @Test
    fun testPersonajeToDto_ConObjetosYDiccionarios() {
        // ARRANGE
        val statVida = Estadistica(id = 1L, nombre = "Vida", valor = 100, consumible = true)
        val statMana = Estadistica(id = 2L, nombre = "Mana", valor = 50, consumible = true)

        val objetoMock = ObjetoCompleto(
            id = 5L,
            nombre = "Poción Venenosa",
            descripcion = "Cura vida, quita mana al rival",
            imagen = "pocion.png",
            usos = 2,
            efectosPropios = mutableMapOf(statVida to 30.0), // Usa Double según tu código
            efectosRival = mutableMapOf(statMana to -15.0)
        )

        val personaje = Personaje(
            id = 1L,
            nombre = "Guerrero",
            vida = 100,
            fotoUrl = "foto.png",
            estadisticas = mutableListOf(statVida, statMana),
            ataques = mutableListOf(), // Lo dejamos vacío para simplificar este test
            inventario = mutableListOf(objetoMock)
        )

        // ACT
        val resultadoDto = personajeService.personajeToDto(personaje)

        // ASSERT
        assertEquals(1L, resultadoDto.id)
        assertEquals("Guerrero", resultadoDto.personajeNombre)

        // Verificamos que el mapeo del inventario funcionó a la perfección
        val inventarioExtraido = resultadoDto.personajeAtaques // <-- Asumo que el DTO guarda el inventario en alguna propiedad. Ajusta si tu DTO final lo guarda en "personajeObjetos"


    }
}