package org.example.backend.facade
import org.example.backend.dto.JuegoDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// 1. Clase wrapper para coincidir exactamente con el JSON de entrada
data class CrearPartidaRequest(
    val juego: JuegoDto
)

@RestController
@RequestMapping("/partida")
class PartidaController(
    private val partidaService: PartidaService
) {

    @PostMapping
    fun crearPartida(@RequestBody request: CrearPartidaRequest): ResponseEntity<Any> {
        return try {

            val juegoGuardado = partidaService.crearNuevaPartida(request.juego)

            // Devolvemos un 201 Created con el resultado
            ResponseEntity.status(HttpStatus.CREATED).body(juegoGuardado)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        }
    }
}