import { CrearPartidaDto } from "../servicio-api";

export interface Partida {
    id: number | null;
    nombre: string;
    descripcion: string;
    idioma: string;
    maxJugadores: number;
}
export interface Plantilla{
    id: number|null;
    nombre: string;
    //Deberia de poder almacenar todo el json
    jsonConfiguration: CrearPartidaDto
}

