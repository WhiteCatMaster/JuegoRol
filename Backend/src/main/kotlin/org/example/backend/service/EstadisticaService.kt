package org.example.backend.service

import org.example.backend.entity.Estadistica
import org.example.backend.repository.EstadisticaRepository
import org.springframework.stereotype.Service

@Service
class EstadisticaService(private val estadisticaRepo: EstadisticaRepository){

    fun getAllEstadisticas(): List<Estadistica> {
        return estadisticaRepo.findAll()
    }

    fun getEstadisticaById(id: Long): Estadistica? {
        return estadisticaRepo.findById(id).orElse(null)
    }

    fun createEstadistica(estadistica: Estadistica): Estadistica {
        return estadisticaRepo.save(estadistica)
    }

    fun updateEstadistica(id: Long, updatedEstadistica: Estadistica): Estadistica? {
        val existingEstadistica = estadisticaRepo.findById(id).orElse(null) ?: return null
        existingEstadistica.nombre = updatedEstadistica.nombre
        existingEstadistica.valor = updatedEstadistica.valor
        return estadisticaRepo.save(existingEstadistica)
    }
    fun updateEstadisticaValor(id: Long, nuevoValor: Int): Estadistica? {
        val existingEstadistica = estadisticaRepo.findById(id).orElse(null) ?: return null
        existingEstadistica.valor = nuevoValor
        return estadisticaRepo.save(existingEstadistica)
    }

    fun deleteEstadistica(id: Long) {
        estadisticaRepo.deleteById(id)
    }
}