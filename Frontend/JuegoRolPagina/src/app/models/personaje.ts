import { EstadisticaDto } from "../servicio-api";
import { Ataque } from "./ataque";
import { Estadistica } from "./estadistica";
import { Objeto } from "./objeto";

export interface EstadisticaPersonaje {
  id: number;  
  nombreEstadistica: string;
    valorPropio: number;
    consumible: boolean;
  }
export function toPersonajeEstadistica(dto: EstadisticaDto): EstadisticaPersonaje{
  let resultado : EstadisticaPersonaje = {
    nombreEstadistica: dto.nombre,
    valorPropio: dto.valor,
    consumible: dto.consumible,
    id: dto.id
  }
  return resultado
}
export function toPersonajeEstadisticaDto(estadistica: EstadisticaPersonaje): EstadisticaDto{
  let estadisticaDto: EstadisticaDto = {
        id: estadistica.id,
        nombre: estadistica.nombreEstadistica,
        valor: estadistica.valorPropio,
        consumible: estadistica.consumible
      }
      return estadisticaDto 
}
  
  export interface Personaje {
    id: number | null;
    nombre: string;
    urlSprite: string;
    vida: number;
    ataquesDelPersonaje: Ataque[]; 
    estadisticasDelPersonaje: EstadisticaPersonaje[]; 
    inventario: Objeto[]
  }