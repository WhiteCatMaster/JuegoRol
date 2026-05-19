package org.example.backend.unit.facade

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import org.example.backend.entity.Usuario
import org.example.backend.facade.AuthController
import org.example.backend.facade.TokenRequest
import org.example.backend.repository.UsuarioRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.util.Optional
import kotlin.collections.get

class AuthControllerTests {

    private val usuarioRepo: UsuarioRepository = mock()
    private val authController = AuthController(usuarioRepo)

    @Test
    fun testLogin_UsuarioExistente() {
        val tokenRequest = TokenRequest("token_valido")
        val uidGoogle = "google-123"

        // 1. Preparamos el usuario que devolverá la base de datos
        val usuarioExistente = Usuario(1L, uidGoogle, "test@test.com", "Test", "url", mutableListOf())
        whenever(usuarioRepo.findByGoogleId(uidGoogle)).thenReturn(Optional.of(usuarioExistente))

        // 2. Preparamos el token falso de Firebase
        val decodedToken = mock<FirebaseToken> {
            on { uid } doReturn uidGoogle
            on { email } doReturn "test@test.com"
            on { claims } doReturn mapOf("name" to "Test", "picture" to "url")
        }

        // 3. Interceptamos la llamada estática a FirebaseAuth.getInstance()
        mockStatic(FirebaseAuth::class.java).use { mockedAuth ->
            val firebaseAuthMock = mock<FirebaseAuth>()
            mockedAuth.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(firebaseAuthMock)
            whenever(firebaseAuthMock.verifyIdToken("token_valido")).thenReturn(decodedToken)

            // 4. Ejecutamos el controlador dentro del bloque donde Firebase está interceptado
            val resultado = authController.loginConGoogle(tokenRequest)

            // 5. Verificamos
            assertEquals(HttpStatus.OK, resultado.statusCode)
            assertEquals(usuarioExistente, resultado.body)
            verify(usuarioRepo).findByGoogleId(uidGoogle)
        }
    }

    @Test
    fun testLogin_UsuarioNuevo() {
        val tokenRequest = TokenRequest("token_valido")
        val uidGoogle = "google-nuevo"

        // 1. Simulamos que el usuario NO existe en la base de datos
        whenever(usuarioRepo.findByGoogleId(uidGoogle)).thenReturn(Optional.empty())

        // 2. Simulamos que al guardar, devuelve un usuario con ID asignado
        val usuarioGuardado = Usuario(2L, uidGoogle, "nuevo@test.com", "Nuevo", "url2", mutableListOf())
        whenever(usuarioRepo.save(any())).thenReturn(usuarioGuardado)

        // 3. Preparamos el token
        val decodedToken = mock<FirebaseToken> {
            on { uid } doReturn uidGoogle
            on { email } doReturn "nuevo@test.com"
            on { claims } doReturn mapOf("name" to "Nuevo", "picture" to "url2")
        }

        // 4. Interceptamos y ejecutamos
        mockStatic(FirebaseAuth::class.java).use { mockedAuth ->
            val firebaseAuthMock = mock<FirebaseAuth>()
            mockedAuth.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(firebaseAuthMock)
            whenever(firebaseAuthMock.verifyIdToken("token_valido")).thenReturn(decodedToken)

            val resultado = authController.loginConGoogle(tokenRequest)

            assertEquals(HttpStatus.OK, resultado.statusCode)
            assertEquals(usuarioGuardado, resultado.body)

            // Verificamos que se buscó y luego se guardó
            verify(usuarioRepo).findByGoogleId(uidGoogle)
            verify(usuarioRepo).save(any<Usuario>())
        }
    }

    @Test
    fun testLogin_TokenInvalido() {
        val tokenRequest = TokenRequest("token_falso")

        // Interceptamos Firebase para forzar que lance una excepción al verificar el token
        mockStatic(FirebaseAuth::class.java).use { mockedAuth ->
            val firebaseAuthMock = mock<FirebaseAuth>()
            mockedAuth.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(firebaseAuthMock)

            // Simulamos el fallo
            whenever(firebaseAuthMock.verifyIdToken("token_falso")).thenThrow(IllegalArgumentException("Token corrupto"))

            // Ejecutamos
            val resultado = authController.loginConGoogle(tokenRequest)

            // Verificamos que devuelve el 401 Unauthorized y el mensaje de error
            assertEquals(HttpStatus.UNAUTHORIZED, resultado.statusCode)
            val body = resultado.body as Map<*, *>
            assertEquals("Token inválido o expirado", body["error"])
        }
    }

    @Test
    fun testLogin_UsuarioNuevo_ConCamposNulos() {
        // Cubre ramas: email null, claims["name"] null, claims["picture"] null
        val tokenRequest = TokenRequest("token_valido")
        val uidGoogle = "google-sin-datos"

        whenever(usuarioRepo.findByGoogleId(uidGoogle)).thenReturn(Optional.empty())
        val usuarioGuardado = Usuario(3L, uidGoogle, "", "Jugador", "fotoUrl", mutableListOf())
        whenever(usuarioRepo.save(any())).thenReturn(usuarioGuardado)

        val decodedToken = mock<FirebaseToken> {
            on { uid } doReturn uidGoogle
            on { email } doReturn null
            on { claims } doReturn emptyMap()
        }

        mockStatic(FirebaseAuth::class.java).use { mockedAuth ->
            val firebaseAuthMock = mock<FirebaseAuth>()
            mockedAuth.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(firebaseAuthMock)
            whenever(firebaseAuthMock.verifyIdToken("token_valido")).thenReturn(decodedToken)

            val resultado = authController.loginConGoogle(tokenRequest)

            assertEquals(HttpStatus.OK, resultado.statusCode)
            assertEquals(usuarioGuardado, resultado.body)
        }
    }

    @Test
    fun testLogin_UsuarioNuevo_ConPictureTipoIncorrecto() {
        // Cubre ramas del `as? String` cuando los claims existen pero no son String
        val tokenRequest = TokenRequest("token_valido")
        val uidGoogle = "google-claims-no-string"

        whenever(usuarioRepo.findByGoogleId(uidGoogle)).thenReturn(Optional.empty())
        val usuarioGuardado = Usuario(4L, uidGoogle, "x@x.com", "Jugador", "fotoUrl", mutableListOf())
        whenever(usuarioRepo.save(any())).thenReturn(usuarioGuardado)

        val decodedToken = mock<FirebaseToken> {
            on { uid } doReturn uidGoogle
            on { email } doReturn "x@x.com"
            on { claims } doReturn mapOf<String, Any>("name" to 123, "picture" to true)
        }

        mockStatic(FirebaseAuth::class.java).use { mockedAuth ->
            val firebaseAuthMock = mock<FirebaseAuth>()
            mockedAuth.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(firebaseAuthMock)
            whenever(firebaseAuthMock.verifyIdToken("token_valido")).thenReturn(decodedToken)

            val resultado = authController.loginConGoogle(tokenRequest)

            assertEquals(HttpStatus.OK, resultado.statusCode)
            assertEquals(usuarioGuardado, resultado.body)
        }
    }

    @Test
    fun testLogin_PictureLargaSeReemplaza() {
        // Cubre la rama del if (rawPicture.length > 250)
        val tokenRequest = TokenRequest("token_valido")
        val uidGoogle = "google-foto-larga"
        val pictureLarga = "h".repeat(300)

        whenever(usuarioRepo.findByGoogleId(uidGoogle)).thenReturn(Optional.empty())
        val usuarioGuardado = Usuario(5L, uidGoogle, "y@y.com", "Yoyo", "fotoUrl", mutableListOf())
        whenever(usuarioRepo.save(any())).thenReturn(usuarioGuardado)

        val decodedToken = mock<FirebaseToken> {
            on { uid } doReturn uidGoogle
            on { email } doReturn "y@y.com"
            on { claims } doReturn mapOf("name" to "Yoyo", "picture" to pictureLarga)
        }

        mockStatic(FirebaseAuth::class.java).use { mockedAuth ->
            val firebaseAuthMock = mock<FirebaseAuth>()
            mockedAuth.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(firebaseAuthMock)
            whenever(firebaseAuthMock.verifyIdToken("token_valido")).thenReturn(decodedToken)

            val resultado = authController.loginConGoogle(tokenRequest)

            assertEquals(HttpStatus.OK, resultado.statusCode)
            verify(usuarioRepo).save(org.mockito.kotlin.check<Usuario> { user ->
                assertEquals("fotoUrl", user.fotoUrl)
            })
        }
    }
}