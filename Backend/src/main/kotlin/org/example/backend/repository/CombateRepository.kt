package org.example.backend.repository

import org.example.backend.entity.Combate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CombateRepository : JpaRepository<Combate, Long>