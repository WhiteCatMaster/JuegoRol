package org.example.backend.config

import org.example.backend.dto.CrearPartidaDto
import org.example.backend.entity.Plantilla
import org.example.backend.repository.PlantillaRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.ObjectMapper

@Configuration
class DataSeedConfig {
    @Bean
    fun iniciarBaseDeDatos(
        plantillaRepo: PlantillaRepository,
        objectMapper: ObjectMapper
    ): CommandLineRunner {
        return CommandLineRunner {

            // 1. COMPROBACIÓN DE SEGURIDAD
            // Solo metemos los datos si la tabla está completamente vacía.
            // Así evitamos que se dupliquen cada vez que reinicias el servidor.
            if (plantillaRepo.count() == 0L) {
                println("🌱 Iniciando el sembrado de plantillas en la Base de Datos...")
                // 2. CREAMOS EL DTO DE LA PLANTILLA 1
                val partidaBasica = CrearPartidaDto(
                    nombre = "Aventura Básica",
                    descripcion = "Una partida ideal para empezar a jugar. Incluye un héroe y lo básico.",
                    idioma = "Español",
                    maximoJugadores = 4,
                    adminId = null, // El admin se asignará cuando un usuario la cargue
                    jugadores = mutableListOf(), // Aquí puedes meter personajes predefinidos si quieres
                    objetos = mutableListOf()
                )
                val personaje1 = CrearPartidaDto.PersonajeDto(
                    personajeNombre = "Personaje 1",
                    personajeVida = 1500,
                    personajeFotoUrl = "https://i.pinimg.com/736x/79/43/be/7943be7e31966d5de7f0f1971741d894.jpg",
                    personajeEstadisticas = mutableListOf(),
                    personajeAtaques = mutableListOf()
                )
                val estadistica1 = CrearPartidaDto.PersonajeDto.EstadisticaDto(
                    nombre = "Fuerza",
                    valor = 1500,
                    consumible = false
                )
                val estadistica2 = CrearPartidaDto.PersonajeDto.EstadisticaDto(
                    nombre = "Mana",
                    valor = 2000,
                    consumible = true
                )
                val ataque1 = CrearPartidaDto.PersonajeDto.AtaqueDto(
                    nombre = "Golpe",
                    manaAtacante = mutableMapOf("Mana" to 100),
                    estadisticasDefensor = mutableMapOf("Mana" to 2.5, "Fuerza" to 0.5),
                    dadoBase = 10,
                    ratioDado = mutableListOf(1,2)
                )
                personaje1.personajeAtaques.add(ataque1)
                personaje1.personajeEstadisticas.add(estadistica2)
                personaje1.personajeEstadisticas.add(estadistica1)
                partidaBasica.jugadores.add(personaje1)
                val personaje2 = CrearPartidaDto.PersonajeDto(
                    personajeNombre = "Personaje 2",
                    personajeVida = 2000,
                    personajeFotoUrl = "https://i.pinimg.com/736x/8a/f8/8b/8af88b36a9b36fb7bc292d313fb543ca.jpg",
                    personajeEstadisticas = mutableListOf(),
                    personajeAtaques = mutableListOf()
                )
                val estadistica3 = CrearPartidaDto.PersonajeDto.EstadisticaDto(
                    nombre = "Fuerza",
                    valor = 1000,
                    consumible = false
                )
                val estadistica4 = CrearPartidaDto.PersonajeDto.EstadisticaDto(
                    nombre = "Mana",
                    valor = 2500,
                    consumible = true
                )
                val ataque2 = CrearPartidaDto.PersonajeDto.AtaqueDto(
                    nombre = "Arañazo",
                    manaAtacante = mutableMapOf("Mana" to 150),
                    estadisticasDefensor = mutableMapOf("Mana" to 1.5, "Fuerza" to 2.5),
                    dadoBase = 11,
                    ratioDado = mutableListOf(3,4)
                )
                personaje2.personajeAtaques.add(ataque2)
                personaje2.personajeEstadisticas.add(estadistica3)
                personaje2.personajeEstadisticas.add(estadistica4)
                partidaBasica.jugadores.add(personaje2)
                // 3. Objetos del dto
                val objeto = CrearPartidaDto.PersonajeDto.ObjetoDto(
                    nombre = "objeto 1",
                    descripcion = "Descripcion objeto 1",
                    imagen = "https://detallesorballo.com/wp-content/uploads/2020/09/imagen-de-prueba-320x240-1.jpg",
                    efectosPropios = mutableMapOf("Mana" to 10.0),
                    efectosRival = mutableMapOf("Fuerza" to -6.0),
                    usos = 10
                )
                partidaBasica.objetos.add(objeto)
                // 4. GUARDAMOS EN BASE DE DATOS
                // Reutilizamos el objectMapper para generar el JSON perfecto, igual que en el Controller
                val plantilla1 = Plantilla(
                    nombre = "Plantilla: Aventura Básica",
                    jsonConfiguration = objectMapper.writeValueAsString(partidaBasica)
                )

                plantillaRepo.save(plantilla1)

                println("✅ Sembrado completado: 1 plantillas creadas.")
            } else {
                println("🌲 La base de datos ya tiene plantillas. Saltando sembrado.")
            }
        }
    }
}
