import { Partida } from "./partida";
import { Usuario } from "./usuario";

export enum Rol {
    Admin = "admin",
    Jugador = "jugador"
}

export interface JugadorJuego {
    id: number | null;
    usuario: Usuario;
    juego: Partida; 
    rol: Rol;
}