package org.example.backend.service

import jakarta.transaction.Transactional
import org.example.backend.dto.CrearPartidaDto
import org.example.backend.dto.DatosPartidaDto
import org.example.backend.dto.PartidaDto
import org.example.backend.entity.Estadistica
import org.example.backend.entity.Juego
import org.example.backend.entity.JugadorJuego
import org.example.backend.entity.ObjetoCompleto
import org.example.backend.entity.Personaje
import org.example.backend.entity.RolJugador
import org.example.backend.repository.AtaqueRepository
import org.example.backend.repository.EstadisticaRepository
import org.example.backend.repository.JuegoRepository
import org.example.backend.repository.JugadorJuegoRepository
import org.example.backend.repository.ObjetoCompletoRepository
import org.example.backend.repository.PersonajeRepository
import org.example.backend.repository.UsuarioRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class JuegoService(
    private val juegoRepo: JuegoRepository,
    private val usuarioRepo: UsuarioRepository,
    private val jugadorJuegoRepo: JugadorJuegoRepository,
    private val personajeRepo: PersonajeRepository,
    private val estadisticaRepo: EstadisticaRepository,
    private val ataqueRepo: AtaqueRepository,
    private val objetoRepo: ObjetoCompletoRepository,
    private val estadisticaService: EstadisticaService,
    private val personajeService: PersonajeService
) {
    fun getAllJuegos() = juegoRepo.findAll()

    fun obtenerIdAdminxPartida(partidaId:Long): Long{
        //Supongo que deberia de buscar dentro de la partida cual es el admin
        val partida = juegoRepo.findById(partidaId).get()
        var resultado = -1L
        for (jugadorJuego in partida.jugadores){
            if (jugadorJuego.juego?.id == partidaId && jugadorJuego.rol == RolJugador.ADMIN){
                resultado = jugadorJuego.usuario?.id ?: -1
            }
        }
        return resultado
    }
    fun getAllPartidas(): List<PartidaDto> {
        return juegoRepo.findAll().map { juego ->
            val resumen = resumenJugadores(juego)
            PartidaDto(
                id = juego.id,
                nombre = juego.nombre,
                descripcion = juego.descripcion,
                idioma = juego.idioma,
                maximoJugadores = juego.maximoJugadores,
                adminId = resumen.adminId,
                jugadoresActuales = resumen.unidos.size,
                jugadoresUnidos = resumen.unidos,
            )
        }
    }

    private data class ResumenJugadores(
        val adminId: Long?,
        val unidos: List<PartidaDto.JugadorUnidoDto>,
    )

    private fun resumenJugadores(juego: Juego): ResumenJugadores {
        val adminId = juego.jugadores.firstOrNull { it.rol == RolJugador.ADMIN }?.usuario?.id
        val unidos = juego.jugadores
            .mapNotNull { it.usuario }
            .filter { it.id != null && it.id != adminId }
            .distinctBy { it.id }
            .map {
                PartidaDto.JugadorUnidoDto(
                    usuarioId = it.id,
                    nombre = it.nombre,
                    fotoUrl = it.fotoUrl,
                )
            }
        return ResumenJugadores(adminId, unidos)
    }

    class PartidaLlenaException(mensaje: String) : RuntimeException(mensaje)

    @Transactional
    fun unirseAPartida(juegoId: Long, usuarioId: Long): PartidaDto {
        val juego = juegoRepo.findById(juegoId).orElseThrow {
            NoSuchElementException("Partida no encontrada")
        }
        val usuario = usuarioRepo.findById(usuarioId).orElseThrow {
            NoSuchElementException("Usuario no encontrado")
        }

        val resumenPrevio = resumenJugadores(juego)
        val esAdmin = resumenPrevio.adminId == usuarioId
        val yaUnido = resumenPrevio.unidos.any { it.usuarioId == usuarioId }

        if (!esAdmin && !yaUnido) {
            val limite = juego.maximoJugadores ?: Int.MAX_VALUE
            if (resumenPrevio.unidos.size >= limite) {
                throw PartidaLlenaException("La partida está llena")
            }
            val nuevo = JugadorJuego(
                usuario = usuario,
                juego = juego,
                rol = RolJugador.JUGADOR,
                personaje = null,
            )
            val guardado = jugadorJuegoRepo.save(nuevo)
            juego.jugadores.add(guardado)
        }

        val resumen = resumenJugadores(juego)
        return PartidaDto(
            id = juego.id,
            nombre = juego.nombre,
            descripcion = juego.descripcion,
            idioma = juego.idioma,
            maximoJugadores = juego.maximoJugadores,
            adminId = resumen.adminId,
            jugadoresActuales = resumen.unidos.size,
            jugadoresUnidos = resumen.unidos,
        )
    }

    fun obtenerDatosPartida(id: Long): ResponseEntity<DatosPartidaDto> {
        val partida = juegoRepo.findById(id).orElse(null)
        val personajes = mutableListOf<DatosPartidaDto.PersonajeDto>()
        if (partida != null) {
            for (personaje in partida.personajes) {
                val personajeDto = personajeService.personajeToDto(personaje)
                personajes.add(personajeDto)
            }
            val resultado = DatosPartidaDto(
                id = partida.id,
                nombre = partida.nombre,
                descripcion = partida.descripcion,
                idioma = partida.idioma,
                maximoJugadores = partida.maximoJugadores,
                jugadores = personajes
            )
            return ResponseEntity.ok(resultado)
        }
        return ResponseEntity.notFound().build()

    }
    fun createJuego(juego: Juego) = juegoRepo.save(juego)

    fun eliminarJuego(id: Long) = juegoRepo.deleteById(id)

    fun asignarJugadorJuego(juegoId: Long, jugadorJuego: JugadorJuego): Juego? {
        val juego = juegoRepo.findById(juegoId).orElse(null) ?: return null
        juego.jugadores.add(jugadorJuego)
        return juegoRepo.save(juego)
    }

    fun modificarJuego(juegoId: Long, updatedJuego: Juego): Juego? {
        val existingJuego = juegoRepo.findById(juegoId).orElse(null) ?: return null
        existingJuego.nombre = updatedJuego.nombre
        existingJuego.jugadores = updatedJuego.jugadores
        return juegoRepo.save(existingJuego)
    }
    fun cambiarIdiomaJuego(juegoId: Long, nuevoIdioma: String): Juego? {
        val existingJuego = juegoRepo.findById(juegoId).orElse(null) ?: return null
        existingJuego.idioma = nuevoIdioma
        return juegoRepo.save(existingJuego)
    }
    @Transactional
    fun crearJuegoxDTO(juegoDTO: CrearPartidaDto): PartidaDto {
        val personajes = mutableListOf<Personaje>()
        val objetos = mutableListOf<ObjetoCompleto>()
        val juego = Juego(
            nombre = juegoDTO.nombre ?: "",
            idioma = juegoDTO.idioma,
            descripcion = juegoDTO.descripcion,
            maximoJugadores = juegoDTO.maximoJugadores,
            personajes = mutableListOf(),
            objetos = mutableListOf(),
        )

        val usuarioadmin = juegoDTO.adminId?.let { usuarioRepo.findById(it).orElse(null) }
        val jugadorAdmin = usuarioadmin?.let {
            JugadorJuego(usuario = it, juego = juego, rol = RolJugador.ADMIN, personaje = null)
        }

        println("1. Extrayendo personajes y estadísticas desde el servicio...")
        val todasLasEstadisticas = mutableListOf<Estadistica>()

        for (personajeDTO in juegoDTO.jugadores) {
            val personaje = personajeService.dtoToPersonaje(personajeDTO)
            personajes.add(personaje)

            // El personaje ya viene con sus entidades Estadistica creadas por el servicio.
            // Las recolectamos todas para guardarlas primero.
            todasLasEstadisticas.addAll(personaje.estadisticas)
        }

        // 🔥 EL CAMBIO CRÍTICO: Guardamos todas las estadísticas primero.
        // Al hacer saveAll, JPA modifica los objetos en memoria y les asigna su ID real de la BD.
        println("2. Guardando estadísticas por adelantado para obtener sus IDs...")
        val estadisticasGuardadas = estadisticaRepo.saveAll(todasLasEstadisticas)

        // Indexamos las estadísticas ya guardadas por su nombre para el buscador
        val buscadorEstadisticas = estadisticasGuardadas.associateBy { it.nombre }

        println("3. Construyendo objetos y vinculándolos a las estadísticas guardadas...")
        for (i in juegoDTO.objetos) {
            val propios = mutableMapOf<Estadistica, Double>()
            val rival = mutableMapOf<Estadistica, Double>()

            // Procesamos efectos propios
            for (j in i.efectosPropios.keys) {
                val estatGuardada = buscadorEstadisticas[j]
                if (estatGuardada != null) {
                    propios[estatGuardada] = i.efectosPropios[j]!!
                }
            }

            // Procesamos efectos rivales
            for (j in i.efectosRival.keys) {
                val estatGuardada = buscadorEstadisticas[j]
                if (estatGuardada != null) {
                    rival[estatGuardada] = i.efectosRival[j]!!
                }
            }

            val objeto = ObjetoCompleto(
                id = null,
                nombre = i.nombre ?: "",
                descripcion = i.descripcion ?: "",
                imagen = i.imagen,
                personaje = null,
                efectosPropios = propios,
                efectosRival = rival,
                usos = i.usos,
                juego = juego,
            )
            objetos.add(objeto)
        }

        // Asignamos las listas al juego
        juego.objetos = objetos
        juego.personajes = personajes

        // Ahora que las estadísticas tienen ID y los objetos apuntan a ellas, el save es 100% seguro
        println("4. Guardando el Juego, Jugadores y Personajes...")
        val juegoGuardado = juegoRepo.save(juego)
        val jugadorJuegoGuardado = jugadorAdmin?.let { jugadorJuegoRepo.save(it) }
        val personajesGuardados = personajeRepo.saveAll(personajes)

        // Guardamos los ataques de cada personaje (asumiendo que se guardan en cascada o individual)
        for (i in personajesGuardados) {
            ataqueRepo.saveAll(i.ataques)
            // i.inventario se guardará cuando asignes objetos a personajes en el futuro
        }

        // Guardamos los objetos generales del juego
        val objeGuardados = objetoRepo.saveAll(objetos)
        println("✅ Partida y componentes creados con éxito. Objetos guardados: ${objeGuardados.size}")

        return PartidaDto(
            id = juegoGuardado.id,
            nombre = juegoGuardado.nombre,
            idioma = juegoGuardado.idioma,
            descripcion = juegoGuardado.descripcion,
            maximoJugadores = juegoGuardado.maximoJugadores,
            adminId = jugadorJuegoGuardado?.usuario?.id,
        )
    }

}