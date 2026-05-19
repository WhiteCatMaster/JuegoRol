export interface Objeto {
  id: number|null;
  nombre: string;
  descripcion: string;
  imagen: string;
  efectosPropios: { estadistica: string; valor: number }[];
  efectosRival: { estadistica: string; valor: number }[];
  usos: number;
}
