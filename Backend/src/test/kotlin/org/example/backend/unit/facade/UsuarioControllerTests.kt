package org.example.backend.unit.facade

import org.example.backend.dto.ActualizarUsuarioDto
import org.example.backend.entity.Juego
import org.example.backend.entity.JugadorJuego
import org.example.backend.entity.Personaje
import org.example.backend.entity.RolJugador
import org.example.backend.entity.Usuario
import org.example.backend.facade.RegistrarUsuarioRequest
import org.example.backend.facade.UsuarioController
import org.example.backend.repository.UsuarioRepository
import org.example.backend.service.UsuarioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional


class UsuarioControllerTests {
    private val usuarioService = mock<UsuarioService>()
    private val usuarioRepository = mock<UsuarioRepository>()
    private val usuarioController = UsuarioController(usuarioService, usuarioRepository)

    @Test
    fun testRegistrarUsuario_Exito() {
        // ARRANGE
        val request = RegistrarUsuarioRequest(
            googleId = "337d",
            email = "aMail@mail.com",
            nombre = "Pepe",
            fotoUrl = "foto.url"
        )
        val usuarioGuardado = Usuario(
            id = 1L,
            googleId = "337d",
            email = "aMail@mail.com",
            nombre = "Pepe",
            fotoUrl = "foto.url",
            partidasParticipa = mutableListOf()
        )

        // Simulamos que todo va bien al guardar
        whenever(usuarioService.createUsuario(any())).thenReturn(usuarioGuardado)

        // ACT
        val result = usuarioController.registrarUsuario(request)

        // ASSERT
        assertEquals(HttpStatus.CREATED, result.statusCode)
        val responseBody = result.body as Usuario
        assertEquals("337d", responseBody.googleId)
        verify(usuarioService).createUsuario(any())
    }

    @Test
    fun testRegistrarUsuario_Error() {
        // ARRANGE
        val request = RegistrarUsuarioRequest(
            googleId = "337d",
            email = "aMail@mail.com",
            nombre = "Pepe",
            fotoUrl = "foto.url"
        )
        val mensajeFallo = "Error de base de datos"

        // Forzamos a que el servicio lance una excepción para entrar al bloque catch
        whenever(usuarioService.createUsuario(any())).thenThrow(RuntimeException(mensajeFallo))

        // ACT
        val result = usuarioController.registrarUsuario(request)

        // ASSERT
        // Verificamos que el controlador atrapó el error y devolvió un 400 BAD REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)

        // Verificamos que el cuerpo de la respuesta contiene el mapa con el error
        val responseBody = result.body as Map<*, *>
        assertEquals(mensajeFallo, responseBody["error"])

        verify(usuarioService).createUsuario(any())
    }

    @Test
    fun testListarUsuario() {

        whenever(usuarioService.getAllUsuarios()).thenReturn(listOf())

        val result = usuarioController.listarUsuarios()
        assertEquals(HttpStatus.OK, result.statusCode)
        verify(usuarioService).getAllUsuarios()

    }
    @Test
    fun testObtenerUsuarioxGoogleId_Exito() {
        // 1. ARRANGE
        val googleIdBuscado = "google-123"

        // Usamos objetos REALES en lugar de Mocks para las entidades
        val personajeReal = Personaje(
            id = 50L,
            nombre = "Heroe Test",
            vida = 100,
            estadisticas = mutableListOf(),
            ataques = mutableListOf(),
            inventario = mutableListOf()
        )

        val juegoReal = Juego(
            id = 100L,
            nombre = "Juego Test",
            descripcion = "Descripción Test",
            idioma = "ES",
            maximoJugadores = 4,
            jugadores = mutableListOf(),
            personajes = mutableListOf()
        )

        val jugadorJuegoReal = JugadorJuego(
            id = 10L,
            juego = juegoReal,
            rol = RolJugador.ADMIN,
            personaje = personajeReal
        )

        val usuarioReal = Usuario(
            id = 1L,
            googleId = googleIdBuscado,
            email = "test@test.com",
            nombre = "Usuario Test",
            fotoUrl = "url_foto",
            partidasParticipa = mutableListOf(jugadorJuegoReal)
        )

        // Cerramos la referencia circular igual que hace Hibernate
        jugadorJuegoReal.usuario = usuarioReal

        // El servicio sí es un mock, y devuelve nuestro usuario real
        whenever(usuarioService.findByGoogleId(googleIdBuscado)).thenReturn(Optional.of(usuarioReal))

        // 2. ACT
        val resultado = usuarioController.obtenerUsuarioxGoogleId(googleIdBuscado)

        // 3. ASSERT
        assertEquals(HttpStatus.OK, resultado.statusCode)

        val body = resultado.body!!
        assertEquals(1L, body.id)
        assertEquals(googleIdBuscado, body.googleId)
        assertEquals("test@test.com", body.email)

        assertEquals(1, body.partidasParticipa.size)
        val partidaDto = body.partidasParticipa[0]
        assertEquals(10L, partidaDto.id)
        assertEquals("ADMIN", partidaDto.rol.toString())

        assertEquals(100L, partidaDto.juego?.id)
        assertEquals("Juego Test", partidaDto.juego?.nombre)
    }

    @Test
    fun testObtenerUsuarioxGoogleId_NoEncontrado() {
        // 1. ARRANGE
        val googleIdBuscado = "google-no-existe"
        whenever(usuarioService.findByGoogleId(googleIdBuscado)).thenReturn(Optional.empty())

        // 2. ACT: Llamamos al controlador de forma normal
        val resultado = usuarioController.obtenerUsuarioxGoogleId(googleIdBuscado)

        // 3. ASSERT: Verificamos que devuelve el código de error HTTP correcto
        assertEquals(HttpStatus.NOT_FOUND, resultado.statusCode)
    }
    @Test
    fun testCambiarFotoONombre_Exito() {
        // 1. ARRANGE
        val googleIdBuscado = "google-123"
        val requestActualizacion = ActualizarUsuarioDto(
            nombre = "Nuevo Thor",
            fotoUrl = "http://nueva.url/foto"
        )

        // Usuario original antes de actualizar
        val usuarioExistente = Usuario(
            id = 1L,
            googleId = googleIdBuscado,
            email = "jugador@gmail.com",
            nombre = "Thor Viejo",
            fotoUrl = "http://vieja.url/foto",
            partidasParticipa = mutableListOf()
        )

        // Simulamos la búsqueda
        whenever(usuarioService.findByGoogleId(googleIdBuscado)).thenReturn(Optional.of(usuarioExistente))

        // Simulamos el guardado (Hibernate devuelve la entidad actualizada)
        whenever(usuarioRepository.save(any())).thenAnswer { invocacion ->
            invocacion.getArgument(0) as Usuario
        }

        // 2. ACT
        val resultado = usuarioController.cambiarFotoONombre(googleIdBuscado, requestActualizacion)

        // 3. ASSERT
        assertEquals(HttpStatus.OK, resultado.statusCode)

        val body = resultado.body!!
        assertEquals("Nuevo Thor", body.nombre)
        assertEquals("http://nueva.url/foto", body.fotoUrl)
        assertEquals(googleIdBuscado, body.googleId)

        // Verificamos que se llamó al repositorio para guardar los cambios
        verify(usuarioRepository).save(usuarioExistente)
    }

    @Test
    fun testCambiarFotoONombre_NoEncontrado() {
        // 1. ARRANGE
        val googleIdBuscado = "google-no-existe"
        val requestActualizacion = ActualizarUsuarioDto(
            nombre = "Nuevo Thor",
            fotoUrl = "http://nueva.url/foto"
        )

        whenever(usuarioService.findByGoogleId(googleIdBuscado)).thenReturn(Optional.empty())

        // 2. ACT & ASSERT
        val excepcion = assertThrows<ResponseStatusException> {
            usuarioController.cambiarFotoONombre(googleIdBuscado, requestActualizacion)
        }

        // Verificamos que lanza un 404 NOT FOUND
        assertEquals(HttpStatus.NOT_FOUND, excepcion.statusCode)

        // Verificamos que NUNCA se intentó guardar nada en la base de datos
        verify(usuarioRepository, org.mockito.Mockito.never()).save(any())
    }
}
