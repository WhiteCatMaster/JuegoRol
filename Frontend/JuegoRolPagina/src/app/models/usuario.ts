import { JugadorJuego } from './jugador-juego';

export interface Usuario {
    googleId: string;
    email: string;
    nombre: string;
    fotoUrl: string;
    partidasParticipa: JugadorJuego[];
  }
