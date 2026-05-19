import { Partida } from "./partida";
import { Personaje } from "./personaje";
import { Usuario } from "./usuario";

export enum Rol {
    Admin = "ADMIN",
    Jugador = "JUGADOR"
}

export interface JugadorJuego {
    id: number | null;
    usuario: Usuario;
    juego: Partida; 
    rol: Rol;
    personaje: Personaje;
}