import { Ataque } from "./ataque";

export interface EstadisticaPersonaje {
    nombreEstadistica: string;
    valorPropio: number;
    consumible: boolean;
  }
  
  export interface Personaje {
    id: number | null;
    nombre: string;
    urlSprite: string;
    vida: number;
    fotoUrl: string;
    ataquesDelPersonaje: Ataque[]; 
    estadisticasDelPersonaje: EstadisticaPersonaje[]; 
  }