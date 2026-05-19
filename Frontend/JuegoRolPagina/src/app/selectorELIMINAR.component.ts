import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Partida } from './models/partida';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatosPartidaDto, ServicioAPI, toPartida } from './servicio-api';

@Component({
  selector: 'app-combate',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './selectorELIMINAR.component.html',
  styleUrl: './selectorELIMINAR.component.css',
})
export class SelectorComponent implements OnInit {
  partidaActual = signal<Partida>({
    id: null,
    nombre: '',
    descripcion: '',
    idioma: '',
    maxJugadores: 0
  });
  partidaID: string | null = null;
  constructor(
    private route: ActivatedRoute,
    private servicioAPI: ServicioAPI,
  ) {}

  // Dado
  estaRodando = true;
  caraDelDado = 20;

  tirarDado() {
    const resultado = Math.floor(Math.random() * 20) + 1;
    this.estaRodando = false;
    this.caraDelDado = resultado;
  }

  // Lo otro, lo de la base de datos temporal
  ngOnInit(): void {
    this.partidaID = this.route.snapshot.paramMap.get('id');
    //this.cargarPartidaDePrueba();
    if (this.partidaID) {
      this.servicioAPI.obtenerDatosPartida(this.partidaID).subscribe({
        next: (partidaBackend) => {
            this.cargasPartidasBD(partidaBackend)
            console.log(this.partidaActual)
        },
      });
    }

  }

  cargarPartidaDePrueba() {
    this.partidaActual.set({
      id: null,
      nombre: 'La gran batalla del bosque',
      descripcion: 'El combate final está a punto de empezar...',
      idioma: 'ES',
      maxJugadores: 4,
      personajes: [
        {
          id: null,
          nombre: 'Guerrero Valiente',
          urlSprite: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg',
          vida: 100,
          estadisticasDelPersonaje: [
            { id: null, nombreEstadistica: 'Fuerza', valorPropio: 15, consumible: false },
            { id: null, nombreEstadistica: 'Maná', valorPropio: 50, consumible: true },
          ],
          ataquesDelPersonaje: [
            {
              id: null,
              nombre: 'Golpe Rompecráneos',
              dadoBase: 20,
              ratioDado: [1, 20],
              danoAtatque: 10,
              statReducePropio: [{ estadistica: 'Maná', valor: 10 }],
              statReduceRival: [{ estadistica: 'Vida', valor: 25 }],
            },
          ],
        },
        {
          id: null,
          nombre: 'Mago Oscuro',
          urlSprite: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg',
          vida: 70,
          estadisticasDelPersonaje: [
            { id: null, nombreEstadistica: 'Inteligencia', valorPropio: 20, consumible: false },
            { id: null, nombreEstadistica: 'Maná', valorPropio: 120, consumible: true },
          ],
          ataquesDelPersonaje: [
            {
              id: null,
              nombre: 'Bola de Fuego',
              dadoBase: 20,
              ratioDado: [1, 20],
              danoAtaque: 10,
              statReducePropio: [{ estadistica: 'Maná', valor: 30 }],
              statReduceRival: [{ estadistica: 'Vida', valor: 45 }],
            },
          ],
        },
      ],
    } as any);
  }

  cargasPartidasBD(partidaDto: DatosPartidaDto){
    this.partidaActual.set(toPartida(partidaDto))
  }


}



