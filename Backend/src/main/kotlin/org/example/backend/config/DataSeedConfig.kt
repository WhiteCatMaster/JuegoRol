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
                    personajeVida = 150,
                    personajeFotoUrl = "https://upload.wikimedia.org/wikipedia/commons/f/fb/Serinus_canaria_gelb.JPG",
                    personajeEstadisticas = mutableListOf(),
                    personajeAtaques = mutableListOf()
                )
                val estadistica1 = CrearPartidaDto.PersonajeDto.EstadisticaDto(
                    nombre = "Fuerza",
                    valor = 150,
                    consumible = false
                )
                val estadistica2 = CrearPartidaDto.PersonajeDto.EstadisticaDto(
                    nombre = "Mana",
                    valor = 200,
                    consumible = true
                )
                val ataque1 = CrearPartidaDto.PersonajeDto.AtaqueDto(
                    nombre = "Golpe",
                    manaAtacante = mutableMapOf("Mana" to 10),
                    estadisticasDefensor = mutableMapOf("Mana" to 2.5, "Fuerza" to 0.5),
                    dadoBase = 10,
                    ratioDado = mutableListOf(1,2),
                    danoAtaque = 30
                )
                val ataqueBasico1 = CrearPartidaDto.PersonajeDto.AtaqueDto(
                    nombre = "Básico",
                    manaAtacante = mutableMapOf(),
                    estadisticasDefensor = mutableMapOf(),
                    dadoBase = 6,
                    ratioDado = mutableListOf(1,6),
                    danoAtaque = 20
                )
                personaje1.personajeAtaques.add(ataque1)
                personaje1.personajeAtaques.add(ataqueBasico1)
                personaje1.personajeEstadisticas.add(estadistica2)
                personaje1.personajeEstadisticas.add(estadistica1)
                partidaBasica.jugadores.add(personaje1)
                val personaje2 = CrearPartidaDto.PersonajeDto(
                    personajeNombre = "Personaje 2",
                    personajeVida = 200,
                    personajeFotoUrl = "https://upload.wikimedia.org/wikipedia/commons/0/05/Wild_rock_dove_at_Raikot%2C_Diamer%2C_Gilgit-Baltistan%2C_Pakistan.png?utm_source=es.wikipedia.org&utm_campaign=index&utm_content=original",
                    personajeEstadisticas = mutableListOf(),
                    personajeAtaques = mutableListOf()
                )
                val estadistica3 = CrearPartidaDto.PersonajeDto.EstadisticaDto(
                    nombre = "Fuerza",
                    valor = 100,
                    consumible = false
                )
                val estadistica4 = CrearPartidaDto.PersonajeDto.EstadisticaDto(
                    nombre = "Mana",
                    valor = 250,
                    consumible = true
                )
                val ataque2 = CrearPartidaDto.PersonajeDto.AtaqueDto(
                    nombre = "Arañazo",
                    manaAtacante = mutableMapOf("Mana" to 15),
                    estadisticasDefensor = mutableMapOf("Mana" to 1.5, "Fuerza" to 2.5),
                    dadoBase = 11,
                    ratioDado = mutableListOf(3,4),
                    danoAtaque = 20
                )
                val ataqueBasico2 = CrearPartidaDto.PersonajeDto.AtaqueDto(
                    nombre = "Básico",
                    manaAtacante = mutableMapOf(),
                    estadisticasDefensor = mutableMapOf(),
                    dadoBase = 6,
                    ratioDado = mutableListOf(1,6),
                    danoAtaque = 20
                )
                personaje2.personajeAtaques.add(ataque2)
                personaje2.personajeAtaques.add(ataqueBasico2)
                personaje2.personajeEstadisticas.add(estadistica3)
                personaje2.personajeEstadisticas.add(estadistica4)
                partidaBasica.jugadores.add(personaje2)
                // 3. Objetos del dto
                val objeto = CrearPartidaDto.PersonajeDto.ObjetoDto(
                    nombre = "objeto 1",
                    descripcion = "Descripcion objeto 1",
                    imagen = "https://static.wikia.nocookie.net/minecraft_gamepedia/images/f/f0/Potion_of_Strength_JE3.png/revision/latest/scale-to-width/360?cb=20230303163006",
                    efectosPropios = mutableMapOf("Mana" to 100.0),
                    efectosRival = mutableMapOf("Fuerza" to -6.0),
                    usos = 2
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
