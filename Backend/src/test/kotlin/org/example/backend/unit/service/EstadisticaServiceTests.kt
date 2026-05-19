package org.example.backend.unit.service

import org.example.backend.entity.Estadistica
import org.example.backend.repository.EstadisticaRepository
import org.example.backend.service.EstadisticaService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class EstadisticaServiceTests {
    private val estadisticaRepository: EstadisticaRepository = mock<EstadisticaRepository>()
    private val estadisticaService = EstadisticaService(estadisticaRepository)

    @Test
    fun testBuscarEstadistica() {
        //Debería devolver la estadistica falsificada con mockito
        val estadisticaFalsa = Estadistica(1L, "Falsa", 1, true)
        whenever(estadisticaRepository.findById(1L)).thenReturn(Optional.of(estadisticaFalsa))


        val resultado = estadisticaService.getEstadisticaById(1L)

        assertEquals("Falsa", resultado?.nombre)
        verify(estadisticaRepository).findById(1L)
    }
    @Test
    fun testBuscarEstadisticaInexistente(){
        //No debería ser capaz de encontrar nada (devuelve null)
        val resultado = estadisticaService.getEstadisticaById(2L)

        assertEquals(null, resultado)
        verify(estadisticaRepository).findById(2L)
    }

    @Test
    fun testObtenerTodasLasEstadisticas() {
        //Debería devolver una lista con las dos estadisticas falsificadas
        val listaFalsa = listOf(
            Estadistica(1L, "Fuerza", 10, false),
            Estadistica(2L, "Mana", 50, true)
        )
        whenever(estadisticaRepository.findAll()).thenReturn(listaFalsa)

        val resultado = estadisticaService.getAllEstadisticas()

        assertEquals(2, resultado.size)
        assertEquals("Fuerza", resultado[0].nombre)
        verify(estadisticaRepository).findAll()
    }


    @Test
    fun testCrearEstadistica() {
        //Debería devolver el id de la estadistica falsa creada
        val nuevaEstadistica = Estadistica(null, "Vida", 100, false)
        val estadisticaGuardada = Estadistica(1L, "Vida", 100, false)


        whenever(estadisticaRepository.save(nuevaEstadistica)).thenReturn(estadisticaGuardada)

        val resultado = estadisticaService.createEstadistica(nuevaEstadistica)

        assertEquals(1L, resultado.id)
        verify(estadisticaRepository).save(nuevaEstadistica)
    }

    @Test
    fun testActualizarEstadistica() {
        //Debería devolver la estadistica actualizada
        val estadisticaExistente = Estadistica(1L, "Vieja", 5, false)
        val datosActualizados = Estadistica(null, "Nueva", 20, false)

        whenever(estadisticaRepository.findById(1L)).thenReturn(Optional.of(estadisticaExistente))
        whenever(estadisticaRepository.save(estadisticaExistente)).thenReturn(estadisticaExistente)

        val resultado = estadisticaService.updateEstadistica(1L, datosActualizados)

        assertEquals("Nueva", resultado?.nombre)
        assertEquals(20, resultado?.valor)
        verify(estadisticaRepository).findById(1L)
        verify(estadisticaRepository).save(estadisticaExistente)
    }

    @Test
    fun testActualizarEstadisticaInexistente() {
        //Se debería devolver un null
        val datosActualizados = Estadistica(null, "Nueva", 20, false)
        whenever(estadisticaRepository.findById(2L)).thenReturn(Optional.empty())

        val resultado = estadisticaService.updateEstadistica(2L, datosActualizados)

        assertEquals(null, resultado)
        verify(estadisticaRepository).findById(2L)
        verify(estadisticaRepository, never()).save(any())
    }

    @Test
    fun testActualizarValorEstadistica() {
        //Debería devolver la estadistica modificada
        val estadisticaExistente = Estadistica(1L, "Defensa", 10, false)

        whenever(estadisticaRepository.findById(1L)).thenReturn(Optional.of(estadisticaExistente))
        whenever(estadisticaRepository.save(estadisticaExistente)).thenReturn(estadisticaExistente)

        val resultado = estadisticaService.updateEstadisticaValor(1L, 50)

        assertEquals(50, resultado?.valor)
        verify(estadisticaRepository).findById(1L)
        verify(estadisticaRepository).save(estadisticaExistente)
    }

    @Test
    fun testEliminarEstadistica() {
        //Solo hace falta comprobar que el repositorio mock ha eliminado el id
        estadisticaService.deleteEstadistica(1L)

        verify(estadisticaRepository).deleteById(1L)
    }

    @Test
    fun testActualizarValorEstadisticaInexistente() {
        //Si el id no existe debe devolver null y no llamar al save
        whenever(estadisticaRepository.findById(99L)).thenReturn(Optional.empty())

        val resultado = estadisticaService.updateEstadisticaValor(99L, 100)

        assertEquals(null, resultado)
        verify(estadisticaRepository, never()).save(any())
    }

}