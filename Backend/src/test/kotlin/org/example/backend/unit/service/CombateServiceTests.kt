package org.example.backend.unit.service

import org.example.backend.dto.CrearCombateDto
import org.example.backend.dto.DatosPartidaDto
import org.example.backend.entity.Ataque
import org.example.backend.entity.Usuario
import org.example.backend.entity.JugadorJuego
import org.example.backend.entity.Personaje
import org.example.backend.entity.Combate
import org.example.backend.entity.Estadistica
import org.example.backend.entity.Juego
import org.example.backend.entity.RolJugador
import org.example.backend.repository.CombateRepository
import org.example.backend.repository.JuegoRepository
import org.example.backend.repository.JugadorJuegoRepository
import org.example.backend.repository.PersonajeRepository
import org.example.backend.repository.UsuarioRepository
import org.example.backend.service.CombateService
import org.example.backend.service.PersonajeService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.util.Optional
import kotlin.collections.mutableListOf

class CombateServiceTests {
    private val personajeService: PersonajeService = mock<PersonajeService>()
    private val combateRepo: CombateRepository = mock<CombateRepository>()
    private val juegoRepo: JuegoRepository = mock<JuegoRepository>()
    private val personajeRepo: PersonajeRepository = mock<PersonajeRepository>()
    private val jugadorJuegoRepo: JugadorJuegoRepository = mock<JugadorJuegoRepository>()
    private val usuarioRepo: UsuarioRepository = mock<UsuarioRepository>()
    private val combateService = CombateService(
        combateRepo,
        juegoRepo,
        personajeRepo,
        jugadorJuegoRepo,
        usuarioRepo,
        personajeService
    )



    @Test
    fun testCrearCombatexDTO() {
        // 1. ARRANGE: Instanciamos los DTOs de entrada reales (sin mocks)

        val jugadorUnoDto = CrearCombateDto.JugadorDto(10L, 20L, "ADMIN", 100L)
        val jugadorDosDto = CrearCombateDto.JugadorDto(11L, 21L, "JUGADOR", 101L)
        val crearCombateDto = CrearCombateDto(1L, "batalla epica", jugadorUnoDto, jugadorDosDto, 1000L)

        // 2. ARRANGE: Preparamos las entidades falsas que devolverá la base de datos
        // Usamos mocks para las entidades devueltas y así nos saltamos sus constructores
        val partidaMock = mock<Juego> { on { id } doReturn 1000L }
        val personaje1Mock = mock<Personaje> { on { id } doReturn 100L }
        val personaje2Mock = mock<Personaje> { on { id } doReturn 101L }

        // El usuario guardado debe tener la lista inicializada para que el service pueda hacer .add()
        val usuarioGuardadoMock = mock<Usuario> {
            on { id } doReturn 1L
            on { partidasParticipa } doReturn mutableListOf()
        }

        val jugadorJuego1Mock = mock<JugadorJuego> {
            on { id } doReturn 10L
            on { rol } doReturn RolJugador.ADMIN
            on { personaje } doReturn personaje1Mock
            on { usuario } doReturn usuarioGuardadoMock
        }
        val jugadorJuego2Mock = mock<JugadorJuego> {
            on { id } doReturn 11L
            on { rol } doReturn RolJugador.JUGADOR
            on { personaje } doReturn personaje2Mock
            on { usuario } doReturn usuarioGuardadoMock
        }

        val combateGuardadoMock = mock<Combate> {
            on { id } doReturn 1L
            on { nombre } doReturn "batalla epica"
            on { jugador1 } doReturn jugadorJuego1Mock
            on { jugador2 } doReturn jugadorJuego2Mock
            on { juego } doReturn partidaMock
        }

        val usuarioFalso1 = Usuario(20L,"google_id", "anEmail@mail.com","UsuarioUno","fotoUsuario.url",mutableListOf())
        val usuarioFalso2 = Usuario(21L,"google_id2", "anEmail@mail.com","UsuarioDos","fotoUsuario.url",mutableListOf())
        // 3. ARRANGE: Configuramos los comportamientos de los repositorios
        // Como el service usa .get() después del findById, debemos devolver un Optional
        whenever(juegoRepo.findById(1000L)).thenReturn(Optional.of(partidaMock))
        whenever(usuarioRepo.save(any())).thenReturn(usuarioGuardadoMock)
        whenever(jugadorJuegoRepo.save(any())).thenAnswer { invocation ->
            // Capturamos el objeto que intentan guardar y le indicamos su clase
            val entidad = invocation.arguments[0] as JugadorJuego

            entidad
        }

        whenever(personajeRepo.findById(100L)).thenReturn(Optional.of(personaje1Mock))
        whenever(personajeRepo.findById(101L)).thenReturn(Optional.of(personaje2Mock))
        whenever(usuarioRepo.findById(20L)).thenReturn(Optional.of(usuarioFalso1))
        whenever(usuarioRepo.findById(21L)).thenReturn(Optional.of(usuarioFalso2))

        whenever(jugadorJuegoRepo.saveAll(any<List<JugadorJuego>>())).thenReturn(listOf(jugadorJuego1Mock, jugadorJuego2Mock))
        whenever(combateRepo.save(any())).thenReturn(combateGuardadoMock)


        // 4. ACT: Llamamos al metodo
        val resultado = combateService.crearCombatexDTO(crearCombateDto)

        // 5. ASSERT: Verificamos los resultados
        assertEquals(1L, resultado.id)
        assertEquals("batalla epica", resultado.nombre)
        assertEquals(10L, resultado.jugador1.id)
        assertEquals(11L, resultado.jugador2.id)

        verify(jugadorJuegoRepo, times(2)).save(any())
        verify(combateRepo).save(any())
    }
    @Test
    fun testObtenerCombateById_Exito() {
        // 1. ARRANGE
        val estFuerza = mock<Estadistica> { on { nombre } doReturn "Fuerza" }

        // Datos para Jugador 1
        val est1 = mock<Estadistica> {
            on { id } doReturn 1L
            on { nombre } doReturn "Vida"
            on { valor } doReturn 100
            on { consumible } doReturn false
        }
        val atk1 = mock<Ataque> {
            on { id } doReturn 1L
            on { nombre } doReturn "Golpe"
            on { manaAtacante } doReturn mutableMapOf(estFuerza to 5)
            on { estadisticasDefensor } doReturn mutableMapOf()
            on { ratioDado } doReturn mutableListOf(1)
        }

        // --- CLAVE PARA EL COVERAGE: Datos para Jugador 2 ---
        val est2 = mock<Estadistica> {
            on { id } doReturn 2L
            on { nombre } doReturn "Defensa"
            on { valor } doReturn 50
            on { consumible } doReturn true
        }
        val atk2 = mock<Ataque> {
            on { id } doReturn 2L
            on { nombre } doReturn "Escudo"
            on { manaAtacante } doReturn mutableMapOf(estFuerza to 2)
            on { estadisticasDefensor } doReturn mutableMapOf(est2 to 10.0) // Entra en el bucle de defensor
            on { ratioDado } doReturn mutableListOf(1)
        }

        val personaje1Mock = mock<Personaje> {
            on { id } doReturn 100L
            on { estadisticas } doReturn mutableListOf(est1)
            on { ataques } doReturn mutableListOf(atk1)
        }

        val personaje2Mock = mock<Personaje> {
            on { id } doReturn 200L
            on { nombre } doReturn "Enemigo"
            on { estadisticas } doReturn mutableListOf(est2) // Ya no está vacía
            on { ataques } doReturn mutableListOf(atk2)      // Ya no está vacía
        }

        val jugador1Mock = mock<JugadorJuego> { on { personaje } doReturn personaje1Mock }
        val jugador2Mock = mock<JugadorJuego> { on { personaje } doReturn personaje2Mock }

        val combateMock = mock<Combate> {
            on { id } doReturn 1L
            on { jugador1 } doReturn jugador1Mock
            on { jugador2 } doReturn jugador2Mock
        }

        whenever(combateRepo.findById(1L)).thenReturn(Optional.of(combateMock))
        val dto1Falso = DatosPartidaDto.PersonajeDto(
            id = 100L,
            personajeNombre = "Heroe",
            personajeVida = 100,
            personajeFotoUrl = "url",
            personajeEstadisticas = mutableListOf(),
            personajeAtaques = mutableListOf()
        )

        // 2. DTO falso para el Jugador 2 (¡Este es vital porque tus ASSERTS lo comprueban!)
        val atk2DtoFalso = DatosPartidaDto.PersonajeDto.AtaqueDto(
            id = 2L,
            nombre = "Escudo",
            manaAtacante = mutableMapOf("Fuerza" to 2),
            estadisticasDefensor = mutableMapOf("Defensa" to 10.0), // El assert busca este 10.0
            dadoBase = 10,
            ratioDado = mutableListOf(1),
            danoAtaque = 5
        )

        val stat2DtoFalsa = DatosPartidaDto.PersonajeDto.EstadisticaDto(
            id = 2L,
            nombre = "Defensa",
            valor = 50,
            consumible = true
        )

        val dto2Falso = DatosPartidaDto.PersonajeDto(
            id = 200L,
            personajeNombre = "Enemigo", // El assert busca "Enemigo"
            personajeVida = 100,
            personajeFotoUrl = "url",
            personajeEstadisticas = mutableListOf(stat2DtoFalsa),
            personajeAtaques = mutableListOf(atk2DtoFalso)
        )

        // 3. ¡Le damos el guion al actor (mock)!
        whenever(personajeService.personajeToDto(personaje1Mock)).thenReturn(dto1Falso)
        whenever(personajeService.personajeToDto(personaje2Mock)).thenReturn(dto2Falso)

        // 2. ACT
        val resultado = combateService.obtenerCombateById(1L)

        // 3. ASSERT
        assertEquals(HttpStatus.OK, resultado.statusCode)
        val body = resultado.body!!

        // Verificamos que el mapeo del Jugador 2 se ejecutó
        assertEquals("Enemigo", body.personaje2.personajeNombre)
        assertEquals(1, body.personaje2.personajeEstadisticas.size)
        assertEquals(1, body.personaje2.personajeAtaques.size)
        assertEquals(10.0, body.personaje2.personajeAtaques[0].estadisticasDefensor["Defensa"])
    }

    @Test
    fun testObtenerCombateById_NoEncontrado() {
        // 1. ARRANGE: El repositorio devuelve vacío
        whenever(combateRepo.findById(99L)).thenReturn(Optional.empty())

        // 2 & 3. ACT & ASSERT: Esperamos que lance NoSuchElementException por culpa del .get()
        assertThrows<NoSuchElementException> {
            combateService.obtenerCombateById(99L)
        }
    }

    @Test
    fun testCrearCombatexDTO_ConCamposNulos() {
        // Cubre las ramas ?: -1 y ?. cuando los ids / usuarios / personajes vienen null
        val jugadorUnoDto = CrearCombateDto.JugadorDto(id = null, usuarioId = null, rol = "ADMIN", personajeId = 100L)
        val jugadorDosDto = CrearCombateDto.JugadorDto(id = null, usuarioId = null, rol = "JUGADOR", personajeId = 101L)
        val crearCombateDto = CrearCombateDto(null, "combate nulo", jugadorUnoDto, jugadorDosDto, 1000L)

        // OJO: hay que stubear el id explicitamente a null porque Mockito devuelve 0L
        // por defecto para Long?, lo que se come la rama del ?: -1
        val partidaMock = mock<Juego> {
            on { id } doReturn null
        }

        // Estos representan los usuarios devueltos por findById(-1L) tras el ?: -1L
        val usuarioFantasma = Usuario(null, "ghost", "g@mail.com", "Ghost", null, mutableListOf())
        val personaje1Mock = mock<Personaje>()
        val personaje2Mock = mock<Personaje>()

        // El combate guardado tiene jugadores SIN usuario, SIN personaje y SIN ids
        val jugadorJuegoSinTodo = mock<JugadorJuego> {
            on { id } doReturn null
            on { rol } doReturn RolJugador.ADMIN
            on { usuario } doReturn null
            on { personaje } doReturn null
        }
        val combateGuardadoMock = mock<Combate> {
            on { id } doReturn null
            on { nombre } doReturn "combate nulo"
            on { jugador1 } doReturn jugadorJuegoSinTodo
            on { jugador2 } doReturn jugadorJuegoSinTodo
            on { juego } doReturn partidaMock
        }

        whenever(juegoRepo.findById(1000L)).thenReturn(Optional.of(partidaMock))
        whenever(usuarioRepo.findById(-1L)).thenReturn(Optional.of(usuarioFantasma))
        whenever(personajeRepo.findById(100L)).thenReturn(Optional.of(personaje1Mock))
        whenever(personajeRepo.findById(101L)).thenReturn(Optional.of(personaje2Mock))
        whenever(jugadorJuegoRepo.save(any())).thenAnswer { it.arguments[0] }
        whenever(combateRepo.save(any())).thenReturn(combateGuardadoMock)

        val resultado = combateService.crearCombatexDTO(crearCombateDto)

        // Todos los ids deberian haber caido en el default -1
        assertEquals(-1L, resultado.id)
        assertEquals(-1L, resultado.jugador1.id)
        assertEquals(-1L, resultado.jugador1.personajeId)
        assertEquals(-1L, resultado.jugador2.id)
        assertEquals(-1L, resultado.jugador2.personajeId)
        assertEquals(-1L, resultado.juegoId)
    }

    @Test
    fun testObtenerCombateById_ConCombateIdNulo() {
        // Cubre la rama ?: -1 del id del combate guardado dentro de obtenerCombateById
        val personajeMock = mock<Personaje>()
        val jugadorMock = mock<JugadorJuego> { on { personaje } doReturn personajeMock }
        val combateConIdNulo = mock<Combate> {
            on { id } doReturn null
            on { jugador1 } doReturn jugadorMock
            on { jugador2 } doReturn jugadorMock
        }
        val dtoFalso = DatosPartidaDto.PersonajeDto(
            id = 1L,
            personajeNombre = "X",
            personajeVida = 1,
            personajeFotoUrl = "x",
            personajeEstadisticas = mutableListOf(),
            personajeAtaques = mutableListOf()
        )

        whenever(combateRepo.findById(5L)).thenReturn(Optional.of(combateConIdNulo))
        whenever(personajeService.personajeToDto(personajeMock)).thenReturn(dtoFalso)

        val resultado = combateService.obtenerCombateById(5L)

        assertEquals(HttpStatus.OK, resultado.statusCode)
        assertEquals(-1L, resultado.body!!.id)
    }
}
