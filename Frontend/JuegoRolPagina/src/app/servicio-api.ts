import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';
import { Usuario } from './models/usuario';
import { MOCK_USUARIO } from './usuario/usuario.mock';
import { ActualizarPersonajeDto } from './editar-personaje/editar-personaje';
import { ActualizarUsuarioDto } from './usuario/usuarioWeb';
import { Estadistica } from './models/estadistica';
import { Ataque } from './models/ataque';
import { EstadisticaPersonaje, Personaje, toPersonajeEstadistica, toPersonajeEstadisticaDto } from './models/personaje';
import { Partida, Plantilla } from './models/partida';
import { Objeto } from './models/objeto';


@Injectable({
  providedIn: 'root',
})
export class ServicioAPI {
  private apiUrl = 'http://localhost:8081';
  constructor(private http: HttpClient) {}

  mandarPartida(payload: CrearPartidaDto): Observable<PartidaDto> {
    return this.http.post<any>(`${this.apiUrl}/partida`, payload);
  }

  recogerPartidas(): Observable<PartidaDto[]> {
    return this.http.get<any[]>(`${this.apiUrl}/partida`);
  }

  obtenerDatosUsuario(googleId: string|undefined): Observable<Usuario> {
    //return of(MOCK_USUARIO).pipe(delay(0));
    return this.http.get<any>(`${this.apiUrl}/usuario/${googleId}`);
  }
  obtenerDatosPartida(id:number|string): Observable<DatosPartidaDto> {
    //Deberia de devolver lo que necesita selectorELIMINAR para poder cargar una partida y sus personajes
    return this.http.get<any>(`${this.apiUrl}/partida/${id}`)
  }

  enviarCombate(payload: CrearCombateDto): Observable<CrearCombateDto>{
    return this.http.post<any>(`${this.apiUrl}/partida/combate`, payload);
  }

  obtenerCombate(id: number|string): Observable<CombatePersonajesDto>{
    return this.http.get<any>(`${this.apiUrl}/partida/combate/${id}`)
  }
  loginConGoogle(token: string) {
    // Lo enviamos en un objeto JSON simple
    return this.http.post<any>(`${this.apiUrl}/auth/login`, { token: token });
  }
  obtenerPersonajexId(id:number|string):Observable<PersonajeDto>{
    return this.http.get<any>(`${this.apiUrl}/personaje/${id}`)
  }
  actualizarPersonaje(id:number|string, payload: ActualizarPersonajeDto):Observable<PersonajeDto>{
    return this.http.put<any>(`${this.apiUrl}/personaje/${id}`, payload)
  }
  actualizarUsuario(googleId: string|undefined, payload: ActualizarUsuarioDto): Observable<Usuario>{
    return this.http.put<any>(`${this.apiUrl}/usuario/${googleId}`, payload)
  }

  obtenerPlantillas(): Observable<Plantilla[]>{
    return this.http.get<any[]>(`${this.apiUrl}/plantilla`);
  }


  //El jsonConfig deberia de ser un json cuando llegue aqui
  guardarPlantilla(nombre: string, jsonConfig: CrearPartidaDto): Observable<Plantilla>{
    let payload: Plantilla = {
      id: null,
      nombre: nombre,
      jsonConfiguration: jsonConfig
    }
    return this.http.post<any>(`${this.apiUrl}/plantilla`, payload);
  }

  obtenerObjetos(idPartida: string): Observable<ObjetoDto[]>{
    return this.http.get<any[]>(`${this.apiUrl}/partida/${idPartida}/objeto`);
  }


}
export interface EstadisticaDto{
  id: number;
  nombre: string;
  valor : number;
  consumible: boolean;
    
}
export interface AtaqueDto{
  id:number;
  nombre : string;
  manaAtacante: {[key: string]: number}
  estadisticasDefensor: {[key: string]: number}
  dadoBase: number;
  ratioDado: number[];
  danoAtaque: number;
}

export interface PersonajeDto {
  id: number;
  personajeNombre : string;
  personajeVida: number;
  personajeFotoUrl: string;
  personajeEstadisticas: EstadisticaDto[];
  personajeAtaques: AtaqueDto[];
  inventario: ObjetoDto[]
}
export interface ObjetoDto{
  id: number
  nombre: string, 
  descripcion: string,
  imagen: string,
  efectosPropios: {[key: string]: number},
  efectosRival: {[key: string]: number},
  usos: number,
}
export interface DatosPartidaDto{
  id: number;
  nombre: string;
  descripcion: string;
  idioma: string;
  maximoJugadores: number;
  jugadores: PersonajeDto[];
  objetos: ObjetoDto[];
}
export interface JugadorDto{
  id: number;
  usuarioId: number,
  rol: string,
  personajeId: number
}
export interface CrearCombateDto{
  id: number,
  nombre: string,
  jugador1: JugadorDto;
  jugador2: JugadorDto;
  juegoId: number;
}

export interface CombatePersonajesDto{
  id: number;
  personaje1: PersonajeDto;
  personaje2 : PersonajeDto;
};

export interface CrearPartidaDto extends DatosPartidaDto{
  adminId: number
}
export interface PartidaDto{
  id: number;
  nombre: string;
  descripcion: string;
  idioma: string;
  maximoJugadores: number;
  adminId: number;
}

export function toEstadistica(dto: EstadisticaDto) : Estadistica{
  let resultado: Estadistica = {
    id: dto.id,
    nombre: dto.nombre,
    valor: dto.valor,
    consumible: dto.consumible
  } 
  return resultado
};
export function toEstadisticaDto(estadistica: Estadistica): EstadisticaDto{
  let resultado : EstadisticaDto = {
    id: estadistica.id ?? -1,
    nombre: estadistica.nombre,
    valor: estadistica.valor,
    consumible: estadistica.consumible
  }
  return resultado
}
export function toAtaque(dto: AtaqueDto): Ataque{
  let manaAtacante : {estadistica: string, valor: number}[] = [];
  let estadisticasDefensor : {estadistica: string, valor: number}[] = [];
  for(const [nombre, coste] of Object.entries(dto.manaAtacante)){
    let stat: {estadistica: string, valor: number} = {
      estadistica: nombre,
      valor: coste
    };
    manaAtacante.push(stat);
  }
  for(const [nombre, coste] of Object.entries(dto.manaAtacante)){
    let stat: {estadistica: string, valor: number} = {
      estadistica: nombre,
      valor: coste
    };
    estadisticasDefensor.push(stat);
  }
  let resultado:Ataque ={
    id: dto.id,
    nombre: dto.nombre,
    dadoBase: dto.dadoBase,
    ratioDado: dto.ratioDado,
    statReducePropio: manaAtacante,
    statReduceRival: estadisticasDefensor,
    danoAtaque: dto.danoAtaque
  } 
  return resultado
}
export function toAtaqueDto(ataque: Ataque): AtaqueDto{
  let manaAtacante: {[key: string]: number} = {}; 
  
  for (let i of ataque.statReducePropio) {
    manaAtacante[i.estadistica] = i.valor
  }
  let estadisticasDefensor: {[key: string]: number} = {}; 
  
  for (let i of ataque.statReduceRival) {
    estadisticasDefensor[i.estadistica] = i.valor
  }
  let resultado: AtaqueDto = {
    id: ataque.id?? -1,
    nombre: ataque.nombre,
    manaAtacante: manaAtacante,
    estadisticasDefensor: estadisticasDefensor,
    dadoBase: ataque.dadoBase,
    ratioDado: ataque.ratioDado.filter((valor): valor is number => valor !== null),
    danoAtaque: ataque.danoAtaque
  }
  return resultado
}
export function toPersonaje(dto: PersonajeDto): Personaje{
  let ataques: Ataque[] = [];
  let estats: EstadisticaPersonaje[] = []
  let objetos: Objeto[] = []
  for( let i of dto.personajeAtaques){
    let ataque: Ataque = toAtaque(i)
    ataques.push(ataque)
  }
  for (let i of dto.personajeEstadisticas){
    estats.push(toPersonajeEstadistica(i))
  }
  if(dto.inventario){
    for(let i of dto.inventario){
      objetos.push(toObjeto(i))
    }
  }
  let resultado: Personaje = {
    id: dto.id,
    nombre: dto.personajeNombre,
    urlSprite: dto.personajeFotoUrl,
    vida: dto.personajeVida,
    ataquesDelPersonaje: ataques,
    estadisticasDelPersonaje: estats,
    inventario: objetos
  }
  return resultado
}
export function toPersonajeDto(personaje: Personaje): PersonajeDto{
  let estadisticas: EstadisticaDto[] = []
  for(let i of personaje.estadisticasDelPersonaje){
      estadisticas.push(toPersonajeEstadisticaDto(i))
  }
  let ataques: AtaqueDto[] = []
  for(let i of personaje.ataquesDelPersonaje){
    ataques.push(toAtaqueDto(i))
  }
  let objetos: ObjetoDto[] = []
  for(let i of personaje.inventario){
    objetos.push(toObjetoDto(i))
  }
  let resultado : PersonajeDto = {
    id: personaje.id ?? -1,
    personajeNombre: personaje.nombre,
    personajeVida: personaje.vida,
    personajeFotoUrl: personaje.urlSprite, //Creo que en la mayoria utilizamos la urlSprite 
    personajeEstadisticas: estadisticas,
    personajeAtaques: ataques,
    inventario: objetos
  }
  return resultado
}
export function toPartida(dto: DatosPartidaDto): Partida{
  let resultado: Partida = {
    id: dto.id,
    nombre: dto.nombre,
    descripcion: dto.descripcion,
    idioma: dto.idioma,
    maxJugadores: dto.maximoJugadores
  }
  return resultado
}
export function toPartidaDto(partida: Partida, adminId: number): PartidaDto{
  let resultado: PartidaDto = {
    id: partida.id ?? -1,
    nombre: partida.nombre,
    descripcion: partida.descripcion,
    idioma: partida.idioma,
    maximoJugadores: partida.maxJugadores,
    adminId: adminId
  }
  return resultado
}

export function toObjeto(dto: ObjetoDto): Objeto{
  let propios: {estadistica: string, valor: number}[] = []
  let rival: {estadistica: string, valor: number}[] = []
  for(let [nombre, valor] of Object.entries(dto.efectosPropios)){
    let stat: {estadistica: string, valor: number} = {
      estadistica: nombre,
      valor: valor
    }
    propios.push(stat)
  }
  for(let [nombre, valor] of Object.entries(dto.efectosRival)){
    let stat: {estadistica: string, valor: number} = {
      estadistica: nombre,
      valor: valor
    }
    rival.push(stat)
  }
  let resultado: Objeto = {
    id: dto.id,
    nombre: dto.nombre,
    descripcion: dto.descripcion,
    imagen: dto.imagen,
    efectosPropios: propios,
    efectosRival: rival,
    usos: dto.usos,
  }
  return resultado 
}

export function toObjetoDto(objeto: Objeto): ObjetoDto{
  let propios: {[key: string]: number} = {}
  let rival: {[key: string]: number} = {}
  for (let i of objeto.efectosPropios){
    propios[i.estadistica] = i.valor
  }
  for (let i of objeto.efectosRival){
    rival[i.estadistica] = i.valor
  }
  let resultado: ObjetoDto = {
    id: objeto.id ?? -1,
    nombre: objeto.nombre,
    descripcion: objeto.descripcion,
    imagen: objeto.imagen,
    efectosPropios: propios,
    efectosRival: rival,
    usos: objeto.usos,
  }
  return resultado
}