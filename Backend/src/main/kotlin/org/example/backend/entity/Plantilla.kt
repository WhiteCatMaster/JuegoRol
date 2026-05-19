package org.example.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.example.backend.dto.CrearPartidaDto
import java.io.Serializable


@Entity
@Table(name = "plantillas")
class Plantilla (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var nombre: String,
    @Column(columnDefinition = "TEXT")
    var jsonConfiguration: String,
    ): Serializable