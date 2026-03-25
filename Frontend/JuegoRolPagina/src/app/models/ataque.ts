import { Personaje } from "./personaje";

export interface Ataque {
    id: number | null;
    nombre: string;
    dadoBase: number;
    ratioDado: (number | null)[];
    statReducePropio: {estadistica: string, valor: number}[];
    statReduceRival: {estadistica: string, valor: number}[];
}
