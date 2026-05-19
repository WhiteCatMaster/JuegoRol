package org.example.backend.facade

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.backend.dto.CrearPartidaDto
import org.example.backend.dto.DatosPartidaDto
import org.example.backend.dto.PartidaDto
import org.example.backend.service.JuegoService
import org.example.backend.service.ObjetoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.ObjectNode
import tools.jackson.module.kotlin.jacksonObjectMapper

// DTO para la solicitud de crear partida (es un alias de JuegoDto)
//typealias CrearPartidaDto = JuegoDto

@RestController
@RequestMapping("/api/partida", "/partida")
@Tag(name = "Partidas", description = "Operaciones relacionadas con la creación, consulta y gestión de las partidas (juegos)")
class PartidaController(
    private val partidaService: JuegoService,
    private val objetoService: ObjetoService
) {
    @GetMapping("/{id}/objeto")
    fun obtenerObjetos(@PathVariable id: Long): ResponseEntity<List<DatosPartidaDto.PersonajeDto.ObjetoDto>> {
        val resultado = mutableListOf<DatosPartidaDto.PersonajeDto.ObjetoDto>()
        val objetos = objetoService.obtenerObjetosByJuegoId(id)
        for (i in objetos){
            resultado.add(objetoService.toObjetoDto(i))
        }
        return ResponseEntity.ok(resultado)
    }

    @Operation(
        summary = "Crear una nueva partida",
        description = "Recibe un objeto JSON con los datos de la partida, lo normaliza (asegurando el ID del administrador) y crea la partida en la base de datos."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Partida creada y guardada con éxito"),
        ApiResponse(responseCode = "400", description = "Error en el formato del JSON o datos inválidos")
    ])
    @PostMapping
    fun crearPartida(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "JSON dinámico con los datos de la partida y el adminId")
        @RequestBody payload: JsonNode
    ): ResponseEntity<Any> {
        return try {
            val juegoNode = payload.get("juego") ?: payload
            val juegoDto = jsonMapper.treeToValue(normalizarNodoPartida(juegoNode), CrearPartidaDto::class.java)
            val juegoGuardado = partidaService.crearJuegoxDTO(juegoDto)

            // Devolvemos un 201 Created con el resultado
            ResponseEntity.status(HttpStatus.CREATED).body(juegoGuardado)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        }
    }

    private fun normalizarNodoPartida(juegoNode: JsonNode): JsonNode {
        val objectNode = (juegoNode as? ObjectNode)?.deepCopy() ?: return juegoNode

        val adminIdNode = objectNode.get("adminId")
        if (adminIdNode == null || adminIdNode.isNull) {
            objectNode.remove("adminId")
            return objectNode
        }

        if (adminIdNode.isString) {
            val adminIdTexto = adminIdNode.asString().trim()
            val adminId = adminIdTexto.toLongOrNull()
            if (adminId == null) {
                objectNode.remove("adminId")
            } else {
                objectNode.put("adminId", adminId)
            }
        }

        return objectNode
    }
    @Operation(
        summary = "Obtener una lista con todas las partidas",
        description = "Devuelve un array con todas las partidas disponibles en el sistema y quién es el administrador de cada una."
    )
    @ApiResponse(responseCode = "200", description = "Lista de partidas obtenida correctamente")

    @GetMapping
    fun obtenerPartidas(): ResponseEntity<List<PartidaDto>>{
        val listaPartidas: List<PartidaDto> = partidaService.getAllPartidas()
        val partidasDto = mutableListOf<PartidaDto>()
        for (partida in listaPartidas) {
            val partidaDto = PartidaDto(
                id = partida.id,
                nombre = partida.nombre,
                descripcion = partida.descripcion,
                idioma = partida.idioma,
                maximoJugadores = partida.maximoJugadores,
                adminId = partidaService.obtenerIdAdminxPartida(partida.id?: -1L)
            )
            partidasDto.add(partidaDto)
        }
        return ResponseEntity.status(HttpStatus.OK).body(partidasDto)
    }
    @Operation(
        summary = "Obtener los datos detallados de una partida concreta",
        description = "Busca una partida por su ID y devuelve toda su información."
    )
    @ApiResponse(responseCode = "200", description = "Datos de la partida devueltos con éxito")
    @GetMapping("/{id}")
    fun obtenerDatosPartida(
        @Parameter(description = "El ID único de la partida", example = "10") @PathVariable id: Long
    ): ResponseEntity<DatosPartidaDto>? {
        val partida = partidaService.obtenerDatosPartida(id)
        return partida
    }

}

private val jsonMapper = jacksonObjectMapper()

