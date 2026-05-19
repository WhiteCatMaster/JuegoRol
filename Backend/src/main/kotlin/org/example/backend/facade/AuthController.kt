package org.example.backend.facade

import org.example.backend.entity.Usuario
import org.example.backend.repository.UsuarioRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import com.google.firebase.auth.FirebaseAuth
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag

@Schema(description = "Objeto que contiene el token de acceso proporcionado por Firebase/Google tras hacer login en el frontend")
data class TokenRequest(
    @Schema(description = "Token JWT generado por Firebase", example = "eyJhbGciOiJSUzI1NiIs...")
    val token: String
)

@RestController
@RequestMapping("/auth")
@CrossOrigin(originPatterns = ["*"], allowCredentials = "true")
@Tag(name = "Autenticación", description = "Operaciones de inicio de sesión y validación de tokens de Google/Firebase")
class AuthController(
    private val usuarioRepo: UsuarioRepository,
) {
    @Operation(
        summary = "Iniciar sesión o registrarse con Google",
        description = "Recibe un token de Firebase, verifica su autenticidad y extrae los datos del usuario. Si es la primera vez que el usuario inicia sesión, lo guarda en la base de datos automáticamente."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Login exitoso. Devuelve los datos completos del usuario"),
        ApiResponse(responseCode = "401", description = "El token enviado es falso, inválido o ha caducado")
    ])
    @PostMapping("/login")
    fun loginConGoogle(@RequestBody request: TokenRequest): ResponseEntity<Any> {
        return try {
            // 1. El "Portero" de Firebase verifica si el pase VIP es real
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(request.token)

            // 2. Extraemos los datos seguros que nos da Google
            val uid = decodedToken.uid
            val email = decodedToken.email ?: ""
            val name = decodedToken.claims["name"] as? String ?: "Jugador"
            val rawPicture = decodedToken.claims["picture"] as? String ?: "fotoUrl"
// Cortamos de raíz cualquier URL larga
            val picture = if (rawPicture.length > 250) "fotoUrl" else rawPicture

            // 3. Buscamos en BD o creamos el usuario si es su primera vez jugando
            val usuario = usuarioRepo.findByGoogleId(uid).orElseGet {
                val nuevoUsuario = Usuario(
                    googleId = uid,
                    email = email,
                    nombre = name,
                    fotoUrl = picture,
                    partidasParticipa = mutableListOf()
                )
                usuarioRepo.save(nuevoUsuario)
            }

            // 4. Devolvemos el usuario a Angular
            ResponseEntity.ok(usuario)

        } catch (e: Exception) {
            // Si el token es falso o ha caducado
            ResponseEntity.status(401).body(mapOf("error" to "Token inválido o expirado"))
        }
    }
}
