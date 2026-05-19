package org.example.backend.facade

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.backend.dto.CombatePersonajesDto
import org.example.backend.dto.CrearCombateDto
import org.example.backend.service.CombateService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/partida/combate")
@Tag(name = "Combates", description = "Operaciones relacionadas con el sistema de combate de la partida")
class CombateController(
    private val combateService: CombateService
) {
    @Operation(
        summary = "Crear o iniciar un nuevo combate",
        description = "Inicia un combate dentro de una partida, recibiendo los datos iniciales necesarios mediante un DTO."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Combate creado y registrado con éxito"),
        ApiResponse(responseCode = "400", description = "Error en los datos enviados o configuración de combate inválida")
    ])
    @PostMapping
    fun crearCombate(@RequestBody dto: CrearCombateDto): ResponseEntity<CrearCombateDto> {
        val resultado = combateService.crearCombatexDTO(dto)
        return ResponseEntity.ok(resultado)
    }

    @Operation(
        summary = "Obtener los personajes de un combate",
        description = "Busca un combate específico por su ID y devuelve toda la información detallada de los personajes que están participando en él."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Datos del combate y personajes devueltos con éxito"),
        ApiResponse(responseCode = "404", description = "No se ha encontrado un combate activo con ese ID")
    ])
    @GetMapping("/{id}")
    fun obtenerPersonajesCombateById(
        @Parameter(description = "El ID único del combate en la base de datos", example = "5") @PathVariable id: Long
    ): ResponseEntity<CombatePersonajesDto> {
        val resultado = combateService.obtenerCombateById(id)
        return resultado
    }
}
