package org.example.backend.unit.service

import org.example.backend.entity.Usuario
import org.example.backend.repository.UsuarioRepository
import org.example.backend.service.UsuarioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class UsuarioServiceTests {

    private val usuarioRepo: UsuarioRepository = mock<UsuarioRepository>()
    private val usuarioService = UsuarioService(usuarioRepo)

    @Test
    fun testGetAllUsuarios() {
        val listaFalsa = listOf(
            Usuario(1L, "google-1", "user1@test.com", "User Uno", "url1", mutableListOf()),
            Usuario(2L, "google-2", "user2@test.com", "User Dos", null, mutableListOf())
        )
        whenever(usuarioRepo.findAll()).thenReturn(listaFalsa)

        val resultado = usuarioService.getAllUsuarios()

        assertEquals(2, resultado.size)
        verify(usuarioRepo).findAll()
    }

    @Test
    fun testGetUsuarioById() {
        val usuarioFalso = Usuario(1L, "google-1", "user1@test.com", "User Uno", "url1", mutableListOf())
        whenever(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuarioFalso))

        val resultado = usuarioService.getUsuarioById(1L)

        assertEquals(1L, resultado?.id)
        assertEquals("User Uno", resultado?.nombre)
        verify(usuarioRepo).findById(1L)
    }

    @Test
    fun testGetUsuarioByIdInexistente() {
        whenever(usuarioRepo.findById(99L)).thenReturn(Optional.empty())

        val resultado = usuarioService.getUsuarioById(99L)

        assertEquals(null, resultado)
        verify(usuarioRepo).findById(99L)
    }

    @Test
    fun testCreateUsuario() {
        val nuevoUsuario = Usuario(null, "google-new", "new@test.com", "New User", null, mutableListOf())
        val usuarioGuardado = Usuario(1L, "google-new", "new@test.com", "New User", null, mutableListOf())

        whenever(usuarioRepo.save(nuevoUsuario)).thenReturn(usuarioGuardado)

        val resultado = usuarioService.createUsuario(nuevoUsuario)

        assertEquals(1L, resultado.id)
        assertEquals("google-new", resultado.googleId)
        verify(usuarioRepo).save(nuevoUsuario)
    }

    @Test
    fun testUpdateUsuario() {
        val usuarioExistente = Usuario(1L, "google-1", "viejo@test.com", "Viejo Nombre", null, mutableListOf())
        val datosActualizados = Usuario(null, "google-1", "nuevo@test.com", "Nuevo Nombre", null, mutableListOf())

        whenever(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuarioExistente))
        whenever(usuarioRepo.save(usuarioExistente)).thenReturn(usuarioExistente)

        val resultado = usuarioService.updateUsuario(1L, datosActualizados)

        assertEquals("Nuevo Nombre", resultado?.nombre)
        assertEquals("nuevo@test.com", resultado?.email)
        verify(usuarioRepo).findById(1L)
        verify(usuarioRepo).save(usuarioExistente)
    }

    @Test
    fun testUpdateUsuarioInexistente() {
        val datosActualizados = Usuario(null, "google-1", "nuevo@test.com", "Nuevo Nombre", null, mutableListOf())
        whenever(usuarioRepo.findById(99L)).thenReturn(Optional.empty())

        val resultado = usuarioService.updateUsuario(99L, datosActualizados)

        assertEquals(null, resultado)
        verify(usuarioRepo).findById(99L)
        verify(usuarioRepo, never()).save(any())
    }

    @Test
    fun testDeleteUsuario() {
        usuarioService.deleteUsuario(1L)
        verify(usuarioRepo).deleteById(1L)
    }

    @Test
    fun testFindByGoogleId() {
        val usuarioFalso = Usuario(1L, "google-123", "user@test.com", "User", null, mutableListOf())
        whenever(usuarioRepo.findByGoogleId("google-123")).thenReturn(Optional.of(usuarioFalso))

        val resultado = usuarioService.findByGoogleId("google-123")

        assertTrue(resultado.isPresent)
        assertEquals("google-123", resultado.get().googleId)
        verify(usuarioRepo).findByGoogleId("google-123")
    }

    @Test
    fun testAgregarFotoUsuario() {
        val usuarioExistente = Usuario(1L, "google-1", "user@test.com", "User", null, mutableListOf())

        whenever(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuarioExistente))
        whenever(usuarioRepo.save(usuarioExistente)).thenReturn(usuarioExistente)

        val resultado = usuarioService.agregarFotoUsuario(1L, "http://nueva-foto.com/foto.jpg")

        assertEquals("http://nueva-foto.com/foto.jpg", resultado?.fotoUrl)
        verify(usuarioRepo).findById(1L)
        verify(usuarioRepo).save(usuarioExistente)
    }

    @Test
    fun testAgregarFotoUsuarioInexistente() {
        whenever(usuarioRepo.findById(99L)).thenReturn(Optional.empty())

        val resultado = usuarioService.agregarFotoUsuario(99L, "url")

        assertEquals(null, resultado)
        verify(usuarioRepo, never()).save(any())
    }

    @Test
    fun testCambiarNombreUsuario() {
        val usuarioExistente = Usuario(1L, "google-1", "user@test.com", "Viejo Nombre", null, mutableListOf())

        whenever(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuarioExistente))
        whenever(usuarioRepo.save(usuarioExistente)).thenReturn(usuarioExistente)

        val resultado = usuarioService.cambiarNombreUsuario(1L, "Nombre Nuevo")

        assertEquals("Nombre Nuevo", resultado?.nombre)
        verify(usuarioRepo).findById(1L)
        verify(usuarioRepo).save(usuarioExistente)
    }

    @Test
    fun testCambiarEmailUsuario() {
        val usuarioExistente = Usuario(1L, "google-1", "viejo@test.com", "User", null, mutableListOf())

        whenever(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuarioExistente))
        whenever(usuarioRepo.save(usuarioExistente)).thenReturn(usuarioExistente)

        val resultado = usuarioService.cambiarEmailUsuario(1L, "nuevo@test.com")

        assertEquals("nuevo@test.com", resultado?.email)
        verify(usuarioRepo).findById(1L)
        verify(usuarioRepo).save(usuarioExistente)
    }

    @Test
    fun testCambiarNombreUsuarioInexistente() {
        whenever(usuarioRepo.findById(99L)).thenReturn(Optional.empty())

        val resultado = usuarioService.cambiarNombreUsuario(99L, "Otro")

        assertEquals(null, resultado)
        verify(usuarioRepo, never()).save(any())
    }

    @Test
    fun testCambiarEmailUsuarioInexistente() {
        // El id no aparece en el repo, asi que ni siquiera deberia intentar guardar
        whenever(usuarioRepo.findById(99L)).thenReturn(Optional.empty())

        val resultado = usuarioService.cambiarEmailUsuario(99L, "noexiste@test.com")

        assertEquals(null, resultado)
        verify(usuarioRepo, never()).save(any())
    }

}