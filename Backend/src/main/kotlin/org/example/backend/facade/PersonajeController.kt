package org.example.backend.facade

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.backend.dto.ActualizarPersonajeDto
import org.example.backend.dto.DatosPartidaDto
import org.example.backend.service.PersonajeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/personaje")
@Tag(name = "Personajes", description = "Operaciones para consultar y modificar la información de los personajes")
class PersonajeController(
    private val personajeService: PersonajeService
) {

    @Operation(
        summary = "Modifica los datos de un personaje existente",
        description = "Actualiza o sobrescribe la información de un personaje específico a partir de su ID proporcionado en la URL."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Personaje modificado y devuelto con éxito"),
        ApiResponse(responseCode = "404", description = "No se ha encontrado el personaje a modificar")
    ])
    @PutMapping("/{id}")
    fun modificarPersonaje(
        @RequestBody personajeDto : ActualizarPersonajeDto,
        @Parameter(description = "ID del personaje que se va a modificar", example = "1") @PathVariable id: Long
    ): DatosPartidaDto.PersonajeDto{
        //Supuestamente es mejor aplastar debido al hibernate
        val personaje = personajeService.actualizarPersonaje(id, personajeDto)
        return personajeService.personajeToDto(personaje)

    }
    @Operation(
        summary = "Obtener los datos completos de un personaje por su ID",
        description = "Busca en la base de datos un personaje específico usando su ID único y devuelve todas sus estadísticas y ataques formateados a DTO."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Personaje encontrado y devuelto con éxito"),
        ApiResponse(responseCode = "404", description = "No se ha encontrado ningún personaje con ese ID")
    ])
    @GetMapping("/{id}")
    fun obtenerPersonajeById(
        @Parameter(description = "El ID numérico único del personaje que se quiere buscar", example = "1") @PathVariable id: Long
    ): ResponseEntity<DatosPartidaDto.PersonajeDto> {
        val personaje = personajeService.getPersonajeById(id) ?: return ResponseEntity.notFound().build()
        val personajeDto =  personajeService.personajeToDto(personaje)
        return ResponseEntity.ok(personajeDto)
    }
}
