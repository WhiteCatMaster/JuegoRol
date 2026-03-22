package org.example.backend.repository

import org.springframework.stereotype.Repository
import org.example.backend.entity.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

@Repository
interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun encontrarxGoogleId(googleId: String): Optional<Usuario>
    fun encontrarxEmail(email: String) : Optional<Usuario>
    fun encontrarxNombreEmail(nombre : String, email : String): Optional<Usuario>
    fun existexGoogleId(googleId: String): Boolean
    fun existexEmail(email: String): Boolean

}