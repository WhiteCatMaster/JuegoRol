export interface EstadisticaPersonaje {
    nombreEstadistica: string;
    valorPropio: number;
  }
  
  export interface Personaje {
    nombre: string;
    urlSprite: string;
    vida: number;
    ataquesDelPersonaje: string[]; 
    estadisticasDelPersonaje: EstadisticaPersonaje[]; 
  }