package org.example.backend.unit.facade

import org.example.backend.dto.CrearPartidaDto
import org.example.backend.dto.PlantillaRequestDto
import org.example.backend.entity.Plantilla
import org.example.backend.facade.PlantillaController
import org.example.backend.repository.PlantillaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import tools.jackson.databind.ObjectMapper // Asegúrate de que este import coincida con el de tu controlador

class PlantillaControllerTests {

    // 1. Mockeamos las dependencias
    private val plantillaRepo = mock<PlantillaRepository>()
    private val objectMapper = mock<ObjectMapper>()

    // 2. Inyectamos los mocks en el controlador
    private val plantillaController = PlantillaController(plantillaRepo, objectMapper)

    @Test
    fun testGuardarPlantilla_Exito() {
        // ARRANGE
        // Mockeamos el objeto interno que viene en la request
        val crearPartidaDtoMock = mock<CrearPartidaDto>()

        val requestDto = PlantillaRequestDto(
            nombre = "Plantilla de Prueba",
            jsonConfiguration = crearPartidaDtoMock // Ajusta el tipo si tu request espera otra clase
        )
        val jsonTransformado = "{\"config\":\"test\"}"

        // Le decimos al ObjectMapper falso qué debe devolver al intentar convertir el objeto
        whenever(objectMapper.writeValueAsString(any())).thenReturn(jsonTransformado)

        // ACT
        val resultado = plantillaController.guardarPlantilla(requestDto)

        // ASSERT
        assertEquals(HttpStatus.OK, resultado.statusCode)

        // Verificamos que se llamó al repositorio con los datos EXACTOS esperados
        verify(plantillaRepo).save(argThat {
            nombre == "Plantilla de Prueba" && jsonConfiguration == jsonTransformado
        })
    }

    @Test
    fun testObtenerPlantillas_Exito() {
        // ARRANGE
        val jsonGuardadoEnBd = "{\"config\":\"test\"}"
        val plantillaBd = Plantilla(
            id = 15L,
            nombre = "Plantilla Guardada",
            jsonConfiguration = jsonGuardadoEnBd
        )

        // El repositorio devuelve una lista con nuestra plantilla falsa
        whenever(plantillaRepo.findAll()).thenReturn(listOf(plantillaBd))

        // Preparamos el objeto en el que se transformará el JSON
        val crearPartidaDtoTransformado = mock<CrearPartidaDto>()
        whenever(objectMapper.readValue(eq(jsonGuardadoEnBd), eq(CrearPartidaDto::class.java)))
            .thenReturn(crearPartidaDtoTransformado)

        // ACT
        val resultado = plantillaController.obtenerPlantillas()

        // ASSERT
        assertEquals(HttpStatus.OK, resultado.statusCode)

        val body = resultado.body!!
        assertEquals(1, body.size)

        // Verificamos que el DTO devuelto por la API tiene mapeado todo correctamente
        val plantillaDto = body[0]
        assertEquals(15L, plantillaDto.id)
        assertEquals("Plantilla Guardada", plantillaDto.nombre)
        assertEquals(crearPartidaDtoTransformado, plantillaDto.jsonConfiguration)
    }

    @Test
    fun testObtenerPlantillas_PlantillaSinId() {
        // Cubre la rama del Elvis (p.id ?: -1) cuando el id es null
        val jsonGuardadoEnBd = "{\"config\":\"sin-id\"}"
        val plantillaSinId = Plantilla(
            id = null,
            nombre = "Plantilla Sin Id",
            jsonConfiguration = jsonGuardadoEnBd
        )

        whenever(plantillaRepo.findAll()).thenReturn(listOf(plantillaSinId))

        val crearPartidaDtoTransformado = mock<CrearPartidaDto>()
        whenever(objectMapper.readValue(eq(jsonGuardadoEnBd), eq(CrearPartidaDto::class.java)))
            .thenReturn(crearPartidaDtoTransformado)

        val resultado = plantillaController.obtenerPlantillas()

        assertEquals(HttpStatus.OK, resultado.statusCode)
        val body = resultado.body!!
        assertEquals(1, body.size)
        assertEquals(-1L, body[0].id)
    }
}