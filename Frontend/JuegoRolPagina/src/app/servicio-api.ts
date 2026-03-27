import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Personaje } from './models/personaje';
import { Observable } from 'rxjs';
import { Ataque } from './models/ataque';
import { Estadistica } from './models/estadistica';

@Injectable({
  providedIn: 'root',
})
export class ServicioAPI {
  private apiUrl = 'http://localhost:8080'; 
  constructor(private http: HttpClient) {

  }

  mandarPartida(payload: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/partida`, payload);
  }


  mandarPersonaje(personaje: Personaje):Observable<Personaje> {
    return this.http.post<Personaje>(`${this.apiUrl}/personajes`, personaje);
  }
  mandarAtaque(ataque: Ataque):Observable<Ataque> {
    return this.http.post<Ataque>(`${this.apiUrl}/ataques`, ataque);
  }
  mandarEstadistica(estadistica: Estadistica):Observable<Estadistica> {
    return this.http.post<Estadistica>(`${this.apiUrl}/estadisticas`, estadistica);
  }
  mandarPersonajes(personajes: Personaje[]):Observable<Personaje[]> {
    return this.http.post<Personaje[]>(`${this.apiUrl}/personajes/batch`, personajes);
  }
  mandarAtaques(ataques: Ataque[]):Observable<Ataque[]> {
    return this.http.post<Ataque[]>(`${this.apiUrl}/ataques/batch`, ataques);
  }
  mandarEstadisticas(estadisticas: Estadistica[]):Observable<Estadistica[]> {
    return this.http.post<Estadistica[]>(`${this.apiUrl}/estadisticas/batch`, estadisticas);
  }
  mandarDatosPartida(datos: any):Observable<any>{
    return this.http.post<any>(`${this.apiUrl}/partidas`, datos);
  }
}
