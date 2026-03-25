package org.example.backend.facade

import org.example.backend.dto.JuegoDto
import org.example.backend.dto.PartidaDto
import org.example.backend.service.JuegoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// DTO para la solicitud de crear partida (es un alias de JuegoDto)
typealias CrearPartidaDto = JuegoDto

// Clase wrapper para coincidir exactamente con el JSON de entrada
data class CrearPartidaRequest(
    val juego: CrearPartidaDto
)

@RestController
@RequestMapping("/partida")
class PartidaController(
    private val partidaService: JuegoService
) {

    @PostMapping
    fun crearPartida(@RequestBody request: CrearPartidaRequest): ResponseEntity<Any> {
        return try {

            val juegoGuardado = partidaService.crearJuegoxDTO(request.juego)

            // Devolvemos un 201 Created con el resultado
            ResponseEntity.status(HttpStatus.CREATED).body(juegoGuardado)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        }
    }
    @GetMapping
    fun obtenerPartidas(): ResponseEntity<List<PartidaDto>>{

            val listaPartidas: List<PartidaDto> = partidaService.getAllPartidas()

            return ResponseEntity.status(HttpStatus.OK).body(listaPartidas)


    }
    @GetMapping("/{nombrePartida}")
    fun obtenerDatosCompletosPartida(@PathVariable nombrePartida: String): ResponseEntity<JuegoDto> {
        val datosCompletosPartida: JuegoDto? = partidaService.getDatosPartida(nombrePartida)
        return if (datosCompletosPartida == null) {
            ResponseEntity.notFound().build()
        } else {
            ResponseEntity.status(HttpStatus.OK).body(datosCompletosPartida)
        }
    }
}