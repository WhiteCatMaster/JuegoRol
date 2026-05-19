import { Usuario } from '../models/usuario';
import { Partida } from '../models/partida';
import { Rol, JugadorJuego } from '../models/jugador-juego'

const partidaElementos: Partida = {
  id: 101,
  nombre: 'Fuego contra agua',
  descripcion: 'Final del campeonato local',
  idioma: 'es-ES',
  maxJugadores: 2,
};

const partidaDungeon: Partida = {
  id: 102,
  nombre: 'Campaña D&D',
  descripcion: 'Buscando tesoros en la cueva',
  idioma: 'es-ES',
  maxJugadores: 5,
};

export const MOCK_USUARIO: Usuario = {
  id: 1,
  googleId: 'google-123',
  email: 'jugador.pro@gmail.com',
  nombre: 'Carlos Gamer',
  fotoUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Carlos',
  partidasParticipa: [],
};


const relacionAjedrez: JugadorJuego = {
  id: 500,
  usuario: MOCK_USUARIO,
  juego: partidaElementos,
  rol: Rol.Admin,
  personaje: {
    id: null,
    nombre: '',
    urlSprite: '',
    vida: 0,
    ataquesDelPersonaje: [],
    estadisticasDelPersonaje: [],
    inventario: []
  }
};

const relacionDungeon: JugadorJuego = {
  id: 501,
  usuario: MOCK_USUARIO,
  juego: partidaDungeon,
  rol: Rol.Jugador,
  personaje: {
    id: null,
    nombre: '',
    urlSprite: '',
    vida: 0,
    ataquesDelPersonaje: [],
    estadisticasDelPersonaje: [],
    inventario: []
  }
};

MOCK_USUARIO.partidasParticipa = [relacionAjedrez, relacionDungeon];
