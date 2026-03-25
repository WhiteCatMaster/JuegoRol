package org.example.backend.repository

import org.springframework.stereotype.Repository
import org.example.backend.entity.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

@Repository
interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByGoogleId(googleId: String): Optional<Usuario>

    fun findByEmail(email: String): Optional<Usuario>

    @Query("SELECT u FROM Usuario u WHERE u.nombre = :nombre AND u.email = :email")
    fun findByNombreAndEmail(@Param("nombre") nombre: String, @Param("email") email: String): Optional<Usuario>

    fun existsByGoogleId(googleId: String): Boolean

    fun existsByEmail(email: String): Boolean

    // Alias para compatibilidad con código anterior
    fun encontrarxGoogleId(googleId: String): Optional<Usuario> = findByGoogleId(googleId)
}