package org.example.backend.facade

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.backend.dto.ActualizarUsuarioDto
import org.example.backend.dto.UsuarioDto
import org.example.backend.entity.Usuario
import org.example.backend.repository.UsuarioRepository
import org.example.backend.service.UsuarioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Schema(description = "Datos necesarios para registrar un usuario por primera vez")
data class RegistrarUsuarioRequest(
    @Schema(description = "ID único proporcionado por Google", example = "10485930291")
    val googleId: String,
    @Schema(description = "Correo electrónico del usuario", example = "jugador@gmail.com")
    val email: String,
    @Schema(description = "Nombre a mostrar en el juego", example = "Thor")
    val nombre: String,
    @Schema(description = "URL de la foto de perfil de Google", example = "https://lh3.googleusercontent.com/a/...")
    val fotoUrl: String? = null
)

@RestController
@RequestMapping("/api/usuario", "/usuario")
@Tag(name = "Usuarios", description = "Operaciones relacionadas con los jugadores")
class UsuarioController(
    private val usuarioService: UsuarioService,
    private val usuarioRepo : UsuarioRepository
) {

    @Operation(summary = "Registra un nuevo usuario en la base de datos tras iniciar sesión con Google")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Usuario creado y guardado con éxito"),
            ApiResponse(responseCode = "400", description = "Error en los datos enviados o el usuario ya existe")
        ]
    )
    @PostMapping
    fun registrarUsuario(@RequestBody request: RegistrarUsuarioRequest): ResponseEntity<Any> {
        return try {
            val usuario = Usuario(
                googleId = request.googleId,
                email = request.email,
                nombre = request.nombre,
                fotoUrl = request.fotoUrl
            )
            val usuarioGuardado = usuarioService.createUsuario(usuario)
            ResponseEntity.status(HttpStatus.CREATED).body(usuarioGuardado)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        }
    }

    @Operation(summary = "Obtiene la lista completa de todos los usuarios registrados en el juego")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Lista devuelta correctamente")
    ])
    @GetMapping
    fun listarUsuarios(): ResponseEntity<List<Usuario>> {
        return ResponseEntity.ok(usuarioService.getAllUsuarios())
    }

    @Operation(
        summary = "Busca un usuario específico mediante su ID de Google",
        description = "Devuelve todos los datos del usuario transformados a DTO."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Usuario encontrado con éxito"),
        ApiResponse(responseCode = "404", description = "No se ha encontrado ningún usuario con ese Google ID")
    ])
    @GetMapping("/{googleId}")
    fun obtenerUsuarioxGoogleId(
        @Parameter(description = "El ID único proporcionado por Google al iniciar sesión", example = "10485930291")
        @PathVariable googleId: String
    ): ResponseEntity<UsuarioDto> {
        val usuario = usuarioService.findByGoogleId(googleId).orElse(null)
        if (usuario != null) {
            val resultado = usuario.usuarioToDto()
            return ResponseEntity.ok(resultado)
        } else {
            return ResponseEntity.notFound().build()
        }
    }

    @Operation(
        summary = "Actualiza el nombre o la foto de perfil de un usuario existente",
        description = "Busca al usuario por su ID de Google y actualiza los campos proporcionados."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Usuario actualizado con éxito"),
        ApiResponse(responseCode = "404", description = "No se ha encontrado al usuario para actualizar")
    ])
    @PutMapping("/{googleId}")
    fun cambiarFotoONombre(
        // <-- Añadido el "example" aquí también
        @Parameter(description = "El ID único proporcionado por Google", example = "10485930291")
        @PathVariable googleId: String,

        @RequestBody usuarioDto : ActualizarUsuarioDto
    ): ResponseEntity<UsuarioDto> {
        val usuario = usuarioService.findByGoogleId(googleId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        usuario.nombre = usuarioDto.nombre
        usuario.fotoUrl = usuarioDto.fotoUrl
        val usuarioGuardado = usuarioRepo.save(usuario)
        val resultado = usuarioGuardado.usuarioToDto()
        return ResponseEntity.ok(resultado)
    }
}