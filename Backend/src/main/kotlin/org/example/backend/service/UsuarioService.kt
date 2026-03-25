package org.example.backend.service

import org.example.backend.entity.Usuario
import org.example.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class UsuarioService(private val usuarioRepo: UsuarioRepository){

    fun getAllUsuarios() = usuarioRepo.findAll()

    fun getUsuarioById(id: Long) = usuarioRepo.findById(id).orElse(null)

    fun createUsuario(usuario: Usuario) = usuarioRepo.save(usuario)

    fun updateUsuario(id: Long, updatedUsuario: Usuario): Usuario? {
        val existingUsuario = usuarioRepo.findById(id).orElse(null) ?: return null
        existingUsuario.nombre = updatedUsuario.nombre
        existingUsuario.email = updatedUsuario.email
        return usuarioRepo.save(existingUsuario)
    }

    fun deleteUsuario(id: Long) {
        usuarioRepo.deleteById(id)
    }

    fun findByGoogleId(googleId: String) = usuarioRepo.encontrarxGoogleId(googleId)

    fun agregarFotoUsuario(id: Long, fotoUrl: String): Usuario? {
        val existingUsuario = usuarioRepo.findById(id).orElse(null) ?: return null
        existingUsuario.fotoUrl = fotoUrl
        return usuarioRepo.save(existingUsuario)
    }

    fun cambiarNombreUsuario(id: Long, nuevoNombre: String): Usuario? {
        val existingUsuario = usuarioRepo.findById(id).orElse(null) ?: return null
        existingUsuario.nombre = nuevoNombre
        return usuarioRepo.save(existingUsuario)
    }

    fun cambiarEmailUsuario(id: Long, nuevoEmail: String): Usuario? {
        val existingUsuario = usuarioRepo.findById(id).orElse(null) ?: return null
        existingUsuario.email = nuevoEmail
        return usuarioRepo.save(existingUsuario)
    }
}