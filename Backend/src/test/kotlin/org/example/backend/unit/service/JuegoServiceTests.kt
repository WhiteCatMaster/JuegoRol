@file:Suppress("UNCHECKED_CAST")

package org.example.backend.unit.service

import org.example.backend.dto.CrearPartidaDto
import org.example.backend.dto.DatosPartidaDto
import org.example.backend.entity.Juego
import org.example.backend.entity.JugadorJuego
import org.example.backend.repository.AtaqueRepository
import org.example.backend.repository.EstadisticaRepository
import org.example.backend.repository.JuegoRepository
import org.example.backend.repository.JugadorJuegoRepository
import org.example.backend.repository.PersonajeRepository
import org.example.backend.repository.UsuarioRepository
import org.example.backend.entity.Ataque
import org.example.backend.entity.Estadistica
import org.example.backend.entity.ObjetoCompleto
import org.example.backend.entity.Personaje
import org.example.backend.entity.RolJugador
import org.example.backend.entity.Usuario
import org.example.backend.repository.ObjetoCompletoRepository
import org.example.backend.service.EstadisticaService
import org.example.backend.service.JuegoService
import org.example.backend.service.PersonajeService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class JuegoServiceTests {

    private val personajeService: PersonajeService = mock<PersonajeService>()
    private val juegoRepository: JuegoRepository = mock<JuegoRepository>()
    private val usuarioRepository: UsuarioRepository = mock<UsuarioRepository>()
    private val jugadorJuegoRepository: JugadorJuegoRepository = mock<JugadorJuegoRepository>()
    private val personajeRepository: PersonajeRepository = mock<PersonajeRepository>()
    private val estadisticaRepository: EstadisticaRepository = mock<EstadisticaRepository>()
    private val ataqueRepository: AtaqueRepository = mock<AtaqueRepository>()
    private val objetoCompletoRepository: ObjetoCompletoRepository = mock<ObjetoCompletoRepository>()
    private val estadisticaService = EstadisticaService(estadisticaRepository)
    private val juegoService = JuegoService(
        juegoRepository,
        usuarioRepository,
        jugadorJuegoRepository,
        personajeRepository,
        estadisticaRepository,
        ataqueRepository,
        objetoCompletoRepository,
        estadisticaService,
        personajeService
    )

    @Test
    fun testGetAllPartidas() {
        //Debería devolver una lista con las dos partidas falsificadas
        val listaFalsa = listOf(
            Juego(1L,"Partida primera","Una partida muy chula", "ES",4,mutableListOf()),
                    Juego(1L,"Partida segunda","Una partida muy aburrida", "EN",6,mutableListOf())
        )
        whenever(juegoRepository.findAll()).thenReturn(listaFalsa)

        val resultado = juegoService.getAllPartidas()

        assertEquals(2, resultado.size)
        assertEquals("Partida primera", resultado[0].nombre)
        verify(juegoRepository).findAll()
    }
    @Test
    fun testObtenerDatosPartida_Exito() {
        // 1. ARRANGE
        // Mocks para las claves de los mapas
        val estFuerzaMock = mock<Estadistica> { on { nombre } doReturn "Fuerza" }
        val estDefensaMock = mock<Estadistica> { on { nombre } doReturn "Defensa" }

        // Mock de la estadística
        val estadisticaMock = mock<Estadistica> {
            on { id } doReturn 10L
            on { nombre } doReturn "Vida"
            on { valor } doReturn 100
            on { consumible } doReturn false
        }

        // Mock del ataque
        val ataqueMock = mock<Ataque> {
            on { id } doReturn 20L
            on { nombre } doReturn "Tajo"
            on { manaAtacante } doReturn mutableMapOf(estFuerzaMock to 10)
            on { estadisticasDefensor } doReturn mutableMapOf(estDefensaMock to 5.0)
            on { dadoBase } doReturn 20
            on { ratioDado } doReturn mutableListOf(1, 2)
            on { danioAtaque } doReturn 15
        }

        // Mock del personaje
        val personajeMock = mock<Personaje> {
            on { id } doReturn 100L
            on { nombre } doReturn "Guerrero"
            on { vida } doReturn 150
            on { fotoUrl } doReturn "url_guerrero"
            on { estadisticas } doReturn mutableListOf(estadisticaMock)
            on { ataques } doReturn mutableListOf(ataqueMock)
        }

        // Mock de la partida
        val partidaMock = mock<Juego> {
            on { id } doReturn 1L
            on { nombre } doReturn "Partida de Prueba"
            on { descripcion } doReturn "Descripción"
            on { idioma } doReturn "ES"
            on { maximoJugadores } doReturn 4
            on { personajes } doReturn mutableListOf(personajeMock)
        }

        whenever(juegoRepository.findById(1L)).thenReturn(Optional.of(partidaMock))

        val estDtoFalsa = DatosPartidaDto.PersonajeDto.EstadisticaDto(
            id = 10L,
            nombre = "Vida",
            valor = 100,
            consumible = false
        )

        val atkDtoFalso = DatosPartidaDto.PersonajeDto.AtaqueDto(
            id = 20L,
            nombre = "Tajo",
            manaAtacante = mutableMapOf("Fuerza" to 10),
            estadisticasDefensor = mutableMapOf("Defensa" to 5.0),
            dadoBase = 20,
            ratioDado = mutableListOf(1, 2),
            danoAtaque = 15
        )

        // 2. Creamos el DTO principal con los datos que espera el ASSERT
        val personajeDtoFalso = DatosPartidaDto.PersonajeDto(
            id = 100L,
            personajeNombre = "Guerrero", // El assert buscará esta palabra exacta
            personajeVida = 150,
            personajeFotoUrl = "url_guerrero",
            personajeEstadisticas = mutableListOf(estDtoFalsa),
            personajeAtaques = mutableListOf(atkDtoFalso)
        )

        // 3. ¡Le entregamos el guion al actor!
        whenever(personajeService.personajeToDto(personajeMock)).thenReturn(personajeDtoFalso)
        // 2. ACT
        val resultado = juegoService.obtenerDatosPartida(1L)

        // 3. ASSERT
        assertEquals(200, resultado.statusCode.value())
        val body = resultado.body!!

        assertEquals(1L, body.id)
        assertEquals("Partida de Prueba", body.nombre)
        assertEquals(1, body.jugadores.size)

        // Verificamos el mapeo del personaje
        val jugadorDto = body.jugadores[0]
        assertEquals("Guerrero", jugadorDto.personajeNombre)

        // Verificamos las estadísticas
        assertEquals(1, jugadorDto.personajeEstadisticas.size)
        assertEquals("Vida", jugadorDto.personajeEstadisticas[0].nombre)

        // Verificamos los ataques y sus mapas
        assertEquals(1, jugadorDto.personajeAtaques.size)
        val ataqueDto = jugadorDto.personajeAtaques[0]
        assertEquals("Tajo", ataqueDto.nombre)
        assertEquals(10, ataqueDto.manaAtacante["Fuerza"])
        assertEquals(5.0, ataqueDto.estadisticasDefensor["Defensa"])
    }

    @Test
    fun testObtenerDatosPartida_NoEncontrado() {
        // ARRANGE
        whenever(juegoRepository.findById(99L)).thenReturn(Optional.empty())

        val resultado = juegoService.obtenerDatosPartida(99L)
        // ACT & ASSERT
        // Como el código usa .orElse(null) e inmediatamente después llama a `partida.personajes`,
        // al no encontrar la partida se producirá un NullPointerException.
        assertEquals(404, resultado.statusCode.value())
    }

    @Test
    fun testGetAllJuegos() {
        val listaFalsa = listOf(
            Juego(1L, "Juego 1", "Desc 1", "ES", 4, mutableListOf()),
            Juego(2L, "Juego 2", "Desc 2", "EN", 2, mutableListOf())
        )
        whenever(juegoRepository.findAll()).thenReturn(listaFalsa)

        val resultado = juegoService.getAllJuegos()

        assertEquals(2, resultado.size)
        verify(juegoRepository).findAll()
    }
    @Test
    fun testCreateJuego() {
        val nuevoJuego = Juego(null, "Nuevo", "Desc", "ES", 4, mutableListOf())
        val juegoGuardado = Juego(1L, "Nuevo", "Desc", "ES", 4, mutableListOf())

        whenever(juegoRepository.save(nuevoJuego)).thenReturn(juegoGuardado)

        val resultado = juegoService.createJuego(nuevoJuego)

        assertEquals(1L, resultado.id)
        verify(juegoRepository).save(nuevoJuego)
    }

    @Test
    fun testEliminarJuego() {
        juegoService.eliminarJuego(1L)
        verify(juegoRepository).deleteById(1L)
    }

    @Test
    fun testAsignarJugadorJuego() {
        val juegoExistente = Juego(1L, "Partida", "Desc", "ES", 4, mutableListOf())
        val nuevoJugador = JugadorJuego() // Asumiendo constructor vacío o con defaults para el test

        whenever(juegoRepository.findById(1L)).thenReturn(Optional.of(juegoExistente))
        whenever(juegoRepository.save(juegoExistente)).thenReturn(juegoExistente)

        val resultado = juegoService.asignarJugadorJuego(1L, nuevoJugador)

        assertEquals(1, resultado?.jugadores?.size)
        verify(juegoRepository).findById(1L)
        verify(juegoRepository).save(juegoExistente)
    }

    @Test
    fun testModificarJuegoInexistente() {
        val juegoActualizado = Juego(null, "Cambio", "Desc", "ES", 4, mutableListOf())

        // Simulamos que no se encuentra en la BD
        whenever(juegoRepository.findById(99L)).thenReturn(Optional.empty())

        val resultado = juegoService.modificarJuego(99L, juegoActualizado)

        assertEquals(null, resultado)
        verify(juegoRepository).findById(99L)
        // Comprobación de seguridad: Nunca debe intentar guardar si no existe
        verify(juegoRepository, never()).save(any())
    }

    @Test
    fun testCambiarIdiomaJuego() {
        val juegoExistente = Juego(1L, "Partida", "Desc", "ES", 4, mutableListOf())

        whenever(juegoRepository.findById(1L)).thenReturn(Optional.of(juegoExistente))
        whenever(juegoRepository.save(juegoExistente)).thenReturn(juegoExistente)

        val resultado = juegoService.cambiarIdiomaJuego(1L, "English")

        assertEquals("English", resultado?.idioma)
        verify(juegoRepository).findById(1L)
        verify(juegoRepository).save(juegoExistente)
    }

    @Test
    fun testCrearJuegoxDTO() {

        val estatDto = CrearPartidaDto.PersonajeDto.EstadisticaDto("Fuerza", 8, false)
        val ataqueDto = CrearPartidaDto.PersonajeDto.AtaqueDto(
            nombre = "Golpe",
            dadoBase = 10,
            ratioDado = listOf(1, 2) as MutableList<Int>,
            estadisticasDefensor = mapOf("Fuerza" to 2.0) as MutableMap<String, Double>,
            manaAtacante = mapOf("Fuerza" to 1) as MutableMap<String, Int>
        )
        val personajeDto = CrearPartidaDto.PersonajeDto(
            personajeNombre = "Paco",
            personajeVida = 100,
            personajeFotoUrl = "url",
            personajeEstadisticas = listOf(estatDto) as MutableList<CrearPartidaDto.PersonajeDto.EstadisticaDto>,
            personajeAtaques = listOf(ataqueDto) as MutableList<CrearPartidaDto.PersonajeDto.AtaqueDto>
        )

        val partidaDto = CrearPartidaDto(
            nombre = "Partida Test",
            idioma = "ES",
            descripcion = "Desc",
            maximoJugadores = 4,
            jugadores = listOf(personajeDto) as MutableList<CrearPartidaDto.PersonajeDto>,
            adminId = 1L
        )

        val personajeFalso = Personaje(
            id = 100L,
            nombre = "Paco",
            vida = 100,
            fotoUrl = "url",
            estadisticas = mutableListOf(), // Lista vacía para que el bucle de estats no explote
            ataques = mutableListOf()       // Lista vacía para que el bucle de ataques no explote
        )

        // 2. Le enseñamos al mock qué devolver cuando llamen a dtoToPersonaje
        whenever(personajeService.dtoToPersonaje(any())).thenReturn(personajeFalso)
        //Mocks para las llamadas a la BD
        val juegoSimulado = Juego(1L, "Partida Test", "Desc", "ES", 4, mutableListOf())

        whenever(juegoRepository.save(any<Juego>())).thenReturn(juegoSimulado)
        whenever(personajeRepository.saveAll(any<List<Personaje>>())).thenAnswer { it.arguments[0] as List<Personaje> }
        whenever(estadisticaRepository.saveAll(any<List<Estadistica>>())).thenReturn(emptyList())
        whenever(ataqueRepository.saveAll(any<List<Ataque>>())).thenReturn(emptyList())
        whenever(usuarioRepository.findById(1L)).thenReturn(Optional.of(mock<Usuario>()))
        whenever(jugadorJuegoRepository.save(any())).thenAnswer { invocation -> invocation.arguments[0] }


        val resultado = juegoService.crearJuegoxDTO(partidaDto)

        assertEquals(1L, resultado.id)
        assertEquals("Partida Test", resultado.nombre)

        verify(juegoRepository).save(any<Juego>())
        verify(personajeRepository).saveAll(any<List<Personaje>>())
        verify(estadisticaRepository, atLeastOnce()).saveAll(any<List<Estadistica>>())
        verify(ataqueRepository, atLeastOnce()).saveAll(any<List<Ataque>>())
    }
    @Test
    fun testObtenerIdAdminxPartida_AdminEncontrado() {
        // 1. ARRANGE
        val idPartida = 1L
        val idAdminEsperado = 42L

        // Creamos el mock del usuario que será el admin
        val usuarioAdminMock = mock<Usuario> {
            on { id } doReturn idAdminEsperado
        }
        // Creamos el mock de un usuario normal (para despistar)
        val usuarioNormalMock = mock<Usuario> {
            on { id } doReturn 99L
        }

        // Mockeamos la partida asegurando que devuelva el ID correcto
        val juegoMock = mock<Juego> {
            on { id } doReturn idPartida
        }

        // Creamos un jugador normal
        val jugadorNormal = mock<JugadorJuego> {
            on { juego } doReturn juegoMock
            on { rol } doReturn RolJugador.JUGADOR // O el rol por defecto que uses
            on { usuario } doReturn usuarioNormalMock
        }

        // Creamos el jugador que sí es ADMIN
        val jugadorAdmin = mock<JugadorJuego> {
            on { juego } doReturn juegoMock
            on { rol } doReturn RolJugador.ADMIN
            on { usuario } doReturn usuarioAdminMock
        }

        // Hacemos que la partida devuelva nuestra lista mezclada de jugadores
        whenever(juegoMock.jugadores).thenReturn(mutableListOf(jugadorNormal, jugadorAdmin))

        // El repositorio devuelve la partida mockeada
        whenever(juegoRepository.findById(idPartida)).thenReturn(Optional.of(juegoMock))

        // 2. ACT
        val resultado = juegoService.obtenerIdAdminxPartida(idPartida)

        // 3. ASSERT
        // Debe ignorar al jugador normal y devolver el ID del admin (42)
        assertEquals(idAdminEsperado, resultado)
        verify(juegoRepository).findById(idPartida)
    }

    @Test
    fun testObtenerIdAdminxPartida_SinAdminDevuelveMenosUno() {
        // 1. ARRANGE
        val idPartida = 1L

        val juegoMock = mock<Juego> {
            on { id } doReturn idPartida
        }

        val usuarioNormalMock = mock<Usuario> {
            on { id } doReturn 99L
        }

        // Creamos solo un jugador normal, sin ningún admin en la lista
        val jugadorNormal = mock<JugadorJuego> {
            on { juego } doReturn juegoMock
            on { rol } doReturn RolJugador.JUGADOR
            on { usuario } doReturn usuarioNormalMock
        }

        whenever(juegoMock.jugadores).thenReturn(mutableListOf(jugadorNormal))
        whenever(juegoRepository.findById(idPartida)).thenReturn(Optional.of(juegoMock))

        // 2. ACT
        val resultado = juegoService.obtenerIdAdminxPartida(idPartida)

        // 3. ASSERT
        // Al no encontrar coincidencia con RolJugador.ADMIN, la variable 'resultado' se queda en -1L
        assertEquals(-1L, resultado)
    }

    @Test
    fun testObtenerIdAdminxPartida_AdminSinUsuarioDevuelveMenosUno() {
        // Si el admin existe pero no tiene usuario asociado, el ?: -1 entra en juego
        val idPartida = 1L
        val juegoMock = mock<Juego> { on { id } doReturn idPartida }
        val jugadorAdminSinUsuario = mock<JugadorJuego> {
            on { juego } doReturn juegoMock
            on { rol } doReturn RolJugador.ADMIN
            on { usuario } doReturn null
        }
        whenever(juegoMock.jugadores).thenReturn(mutableListOf(jugadorAdminSinUsuario))
        whenever(juegoRepository.findById(idPartida)).thenReturn(Optional.of(juegoMock))

        val resultado = juegoService.obtenerIdAdminxPartida(idPartida)

        assertEquals(-1L, resultado)
    }

    @Test
    fun testObtenerIdAdminxPartida_AdminConJuegoSinIdSeIgnora() {
        // El juego del jugador existe pero no tiene id (id=null) -> null == partidaId = false
        val idPartida = 1L
        val juegoBuscadoMock = mock<Juego> { on { id } doReturn idPartida }
        val juegoSinIdMock = mock<Juego> { on { id } doReturn null }
        val jugador = mock<JugadorJuego> {
            on { juego } doReturn juegoSinIdMock
            on { rol } doReturn RolJugador.ADMIN
        }
        whenever(juegoBuscadoMock.jugadores).thenReturn(mutableListOf(jugador))
        whenever(juegoRepository.findById(idPartida)).thenReturn(Optional.of(juegoBuscadoMock))

        val resultado = juegoService.obtenerIdAdminxPartida(idPartida)

        assertEquals(-1L, resultado)
    }

    @Test
    fun testObtenerIdAdminxPartida_JugadorSinJuegoSeIgnora() {
        // El jugador no tiene juego asociado: el safe call ?.id devuelve null y no entra al if
        val idPartida = 1L
        val juegoBuscadoMock = mock<Juego> { on { id } doReturn idPartida }
        val jugadorSinJuego = mock<JugadorJuego> {
            on { juego } doReturn null
            on { rol } doReturn RolJugador.ADMIN
        }
        whenever(juegoBuscadoMock.jugadores).thenReturn(mutableListOf(jugadorSinJuego))
        whenever(juegoRepository.findById(idPartida)).thenReturn(Optional.of(juegoBuscadoMock))

        val resultado = juegoService.obtenerIdAdminxPartida(idPartida)

        assertEquals(-1L, resultado)
    }

    @Test
    fun testObtenerIdAdminxPartida_JugadorDeOtraPartidaSeIgnora() {
        // Cubre la rama del if cuando jugadorJuego.juego.id != partidaId
        val idPartida = 1L
        val juegoBuscadoMock = mock<Juego> { on { id } doReturn idPartida }
        val juegoOtroMock = mock<Juego> { on { id } doReturn 42L } // distinto

        val jugadorOtraPartida = mock<JugadorJuego> {
            on { juego } doReturn juegoOtroMock
            on { rol } doReturn RolJugador.ADMIN
        }
        whenever(juegoBuscadoMock.jugadores).thenReturn(mutableListOf(jugadorOtraPartida))
        whenever(juegoRepository.findById(idPartida)).thenReturn(Optional.of(juegoBuscadoMock))

        val resultado = juegoService.obtenerIdAdminxPartida(idPartida)

        // El admin pertenece a otra partida, asi que no cuenta -> -1
        assertEquals(-1L, resultado)
    }

    @Test
    fun testModificarJuego_Exito() {
        // El test que ya existia solo cubria el caso de id no encontrado,
        // este cubre la rama feliz (rebautiza el nombre y guarda)
        val juegoExistente = Juego(1L, "Antiguo", "Desc", "ES", 4, mutableListOf())
        val datos = Juego(null, "Nombre Nuevo", "Desc", "ES", 4, mutableListOf())

        whenever(juegoRepository.findById(1L)).thenReturn(Optional.of(juegoExistente))
        whenever(juegoRepository.save(juegoExistente)).thenReturn(juegoExistente)

        val resultado = juegoService.modificarJuego(1L, datos)

        assertEquals("Nombre Nuevo", resultado?.nombre)
        verify(juegoRepository).save(juegoExistente)
    }

    @Test
    fun testCambiarIdiomaJuegoInexistente() {
        whenever(juegoRepository.findById(99L)).thenReturn(Optional.empty())

        val resultado = juegoService.cambiarIdiomaJuego(99L, "FR")

        assertEquals(null, resultado)
        verify(juegoRepository, never()).save(any())
    }

    @Test
    fun testAsignarJugadorJuegoInexistente() {
        whenever(juegoRepository.findById(99L)).thenReturn(Optional.empty())

        val resultado = juegoService.asignarJugadorJuego(99L, JugadorJuego())

        assertEquals(null, resultado)
        verify(juegoRepository, never()).save(any())
    }

    @Test
    fun testCrearJuegoxDTO_ConObjetosEnElDto() {
        // El bucle de objetos del service no se ejecuta si pasamos la lista vacia.
        // Aqui montamos un DTO con un objeto y comprobamos que se guardan vinculando estadisticas.
        val estatDto = CrearPartidaDto.PersonajeDto.EstadisticaDto("Fuerza", 5, false)
        val personajeDto = CrearPartidaDto.PersonajeDto(
            personajeNombre = "Paco",
            personajeVida = 100,
            personajeFotoUrl = "url",
            personajeEstadisticas = mutableListOf(estatDto),
            personajeAtaques = mutableListOf()
        )
        val objetoDto = CrearPartidaDto.PersonajeDto.ObjetoDto(
            nombre = "Espada",
            descripcion = "Corta",
            imagen = "espada.png",
            usos = 1,
            efectosPropios = mutableMapOf("Fuerza" to 3.0),
            efectosRival = mutableMapOf("Fuerza" to -1.0)
        )
        val partidaDto = CrearPartidaDto(
            nombre = "Partida con objetos",
            idioma = "ES",
            descripcion = "Desc",
            maximoJugadores = 4,
            jugadores = mutableListOf(personajeDto),
            adminId = 1L,
            objetos = mutableListOf(objetoDto)
        )

        // El personaje falso lleva la stat con el mismo nombre que la usada en los efectos
        // para que el buscadorEstadisticas la encuentre y entre en el if
        val estatFalsa = Estadistica(id = 1L, nombre = "Fuerza", valor = 5, consumible = false)
        val personajeFalso = Personaje(
            id = 100L,
            nombre = "Paco",
            vida = 100,
            fotoUrl = "url",
            estadisticas = mutableListOf(estatFalsa),
            ataques = mutableListOf()
        )
        val juegoSimulado = Juego(1L, "Partida con objetos", "Desc", "ES", 4, mutableListOf())

        whenever(personajeService.dtoToPersonaje(any())).thenReturn(personajeFalso)
        whenever(juegoRepository.save(any<Juego>())).thenReturn(juegoSimulado)
        whenever(personajeRepository.saveAll(any<List<Personaje>>())).thenAnswer { it.arguments[0] as List<Personaje> }
        whenever(estadisticaRepository.saveAll(any<List<Estadistica>>())).thenAnswer { it.arguments[0] as List<Estadistica> }
        whenever(ataqueRepository.saveAll(any<List<Ataque>>())).thenReturn(emptyList())
        whenever(objetoCompletoRepository.saveAll(any<List<ObjetoCompleto>>())).thenAnswer { it.arguments[0] as List<ObjetoCompleto> }
        whenever(usuarioRepository.findById(1L)).thenReturn(Optional.of(mock<Usuario>()))
        whenever(jugadorJuegoRepository.save(any())).thenAnswer { it.arguments[0] }

        val resultado = juegoService.crearJuegoxDTO(partidaDto)

        assertEquals(1L, resultado.id)
        assertEquals("Partida con objetos", resultado.nombre)
        verify(objetoCompletoRepository).saveAll(any<List<ObjetoCompleto>>())
    }

    @Test
    fun testCrearJuegoxDTO_SinNombreNiAdmin() {
        // Cubre los ?: del juego.nombre, adminId null y la rama negativa de los let
        val partidaDto = CrearPartidaDto(
            nombre = null,
            idioma = "ES",
            descripcion = "Sin nombre",
            maximoJugadores = 2,
            jugadores = mutableListOf(),
            adminId = null,
            objetos = mutableListOf()
        )
        val juegoSimulado = Juego(7L, "", "Sin nombre", "ES", 2, mutableListOf())
        whenever(juegoRepository.save(any<Juego>())).thenReturn(juegoSimulado)
        whenever(personajeRepository.saveAll(any<List<Personaje>>())).thenReturn(emptyList())
        whenever(estadisticaRepository.saveAll(any<List<Estadistica>>())).thenReturn(emptyList())
        whenever(objetoCompletoRepository.saveAll(any<List<ObjetoCompleto>>())).thenReturn(emptyList())

        val resultado = juegoService.crearJuegoxDTO(partidaDto)

        assertEquals(7L, resultado.id)
        // adminId queda null porque no se ha creado JugadorJuego admin
        assertEquals(null, resultado.adminId)
        // No deberia haber tocado jugadorJuegoRepo.save porque jugadorAdmin es null
        verify(jugadorJuegoRepository, never()).save(any())
    }

    @Test
    fun testCrearJuegoxDTO_ObjetoConEstatNoEncontrada() {
        // Si el efecto referencia una stat que no esta en buscadorEstadisticas, el if se saltea
        val estatDto = CrearPartidaDto.PersonajeDto.EstadisticaDto("Fuerza", 5, false)
        val personajeDto = CrearPartidaDto.PersonajeDto(
            personajeNombre = "Paco",
            personajeVida = 100,
            personajeFotoUrl = "url",
            personajeEstadisticas = mutableListOf(estatDto),
            personajeAtaques = mutableListOf()
        )
        // OJO: efectos apuntan a "Inexistente" que no esta en las estadisticas del personaje
        // y de paso pasamos nombre/descripcion null para tocar los ?: "" del service
        val objetoDto = CrearPartidaDto.PersonajeDto.ObjetoDto(
            nombre = null,
            descripcion = null,
            imagen = "raro.png",
            usos = 1,
            efectosPropios = mutableMapOf("Inexistente" to 5.0),
            efectosRival = mutableMapOf("Tampoco" to -1.0)
        )
        val partidaDto = CrearPartidaDto(
            nombre = "Test",
            idioma = "ES",
            descripcion = "Desc",
            maximoJugadores = 4,
            jugadores = mutableListOf(personajeDto),
            adminId = 1L,
            objetos = mutableListOf(objetoDto)
        )

        val statFuerza = Estadistica(id = 1L, nombre = "Fuerza", valor = 5, consumible = false)
        val personajeFalso = Personaje(
            id = 100L, nombre = "Paco", vida = 100, fotoUrl = "url",
            estadisticas = mutableListOf(statFuerza), ataques = mutableListOf()
        )
        val juegoSimulado = Juego(1L, "Test", "Desc", "ES", 4, mutableListOf())

        whenever(personajeService.dtoToPersonaje(any())).thenReturn(personajeFalso)
        whenever(juegoRepository.save(any<Juego>())).thenReturn(juegoSimulado)
        whenever(personajeRepository.saveAll(any<List<Personaje>>())).thenAnswer { it.arguments[0] as List<Personaje> }
        whenever(estadisticaRepository.saveAll(any<List<Estadistica>>())).thenAnswer { it.arguments[0] as List<Estadistica> }
        whenever(ataqueRepository.saveAll(any<List<Ataque>>())).thenReturn(emptyList())
        whenever(objetoCompletoRepository.saveAll(any<List<ObjetoCompleto>>())).thenAnswer { it.arguments[0] as List<ObjetoCompleto> }
        whenever(usuarioRepository.findById(1L)).thenReturn(Optional.of(mock<Usuario>()))
        whenever(jugadorJuegoRepository.save(any())).thenAnswer { it.arguments[0] }

        val resultado = juegoService.crearJuegoxDTO(partidaDto)

        // Aunque las estadisticas no coincidan, el juego se crea igual
        assertEquals(1L, resultado.id)
    }

}