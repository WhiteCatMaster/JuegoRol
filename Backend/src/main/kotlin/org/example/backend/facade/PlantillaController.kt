package org.example.backend.facade

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.backend.dto.CrearPartidaDto
import org.example.backend.dto.PlantillaDto
import org.example.backend.dto.PlantillaRequestDto
import org.example.backend.entity.Plantilla
import org.example.backend.repository.PlantillaRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import tools.jackson.databind.ObjectMapper

@RestController
@RequestMapping("/plantilla")
@Tag(name = "Plantillas", description = "Operaciones para gestionar las plantillas predefinidas de los juegos de rol")
class PlantillaController(
    private val plantillaRepo: PlantillaRepository,
    private val objectMapper: ObjectMapper,

    ) {

    @Operation(
        summary = "Guardar una nueva plantilla",
        description = "Recibe los datos de una plantilla (nombre y configuración JSON), transforma el JSON a texto y lo guarda en la base de datos."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Plantilla guardada con éxito en la base de datos"),
        ApiResponse(responseCode = "400", description = "Error en el formato de los datos enviados")
    ])
    @PostMapping
    fun guardarPlantilla(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la nueva plantilla a guardar")
        @RequestBody nuevaPlantilla: PlantillaRequestDto
    ): ResponseEntity<Any>{
        val jsonTextoValido = objectMapper.writeValueAsString(nuevaPlantilla.jsonConfiguration)

        val plantilla = Plantilla(
            nombre = nuevaPlantilla.nombre,
            jsonConfiguration = jsonTextoValido // Guardamos el JSON perfecto en la BD
        )

        plantillaRepo.save(plantilla)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "Obtener todas las plantillas",
        description = "Devuelve una lista completa con todas las plantillas disponibles, convirtiendo el texto de la base de datos de nuevo a formato JSON."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Lista de plantillas obtenida y parseada correctamente")
    ])
    @GetMapping
    fun obtenerPlantillas(): ResponseEntity<List<PlantillaDto>>{
        val plantillas = plantillaRepo.findAll()
        val respuesta = plantillas.map {
            p-> PlantillaDto(
                id = p.id ?: -1,
                nombre = p.nombre,
                jsonConfiguration = objectMapper.readValue(p.jsonConfiguration, CrearPartidaDto::class.java),
            )
        }
        return ResponseEntity.ok(respuesta)
    }
}
