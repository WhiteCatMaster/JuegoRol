import { CrearPartidaDto } from "../servicio-api";

export interface JugadorUnido {
    usuarioId: number;
    nombre: string;
    fotoUrl: string;
}
export interface Partida {
    id: number | null;
    nombre: string;
    descripcion: string;
    idioma: string;
    maxJugadores: number;
    jugadoresActuales?: number;
    jugadoresUnidos?: JugadorUnido[];
}
export interface Plantilla{
    id: number|null;
    nombre: string;
    //Deberia de poder almacenar todo el json
    jsonConfiguration: CrearPartidaDto
}

