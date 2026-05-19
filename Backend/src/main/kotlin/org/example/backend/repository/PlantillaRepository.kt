package org.example.backend.repository

import org.example.backend.entity.Plantilla
import org.springframework.data.jpa.repository.JpaRepository

interface PlantillaRepository : JpaRepository<Plantilla, Long> {

}
