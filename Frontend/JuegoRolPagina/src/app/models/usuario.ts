import { JugadorJuego } from './jugador-juego';

export interface Usuario {
  id: number | null;
  googleId: string;
  email: string;
  nombre: string;
  fotoUrl: string;
  partidasParticipa: JugadorJuego[];
}
