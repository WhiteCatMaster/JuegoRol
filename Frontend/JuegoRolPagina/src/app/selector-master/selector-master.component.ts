import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Partida } from '../models/partida';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatosPartidaDto, ServicioAPI, toPartida, toPersonaje } from '../servicio-api';
import { Personaje } from '../models/personaje';

@Component({
  selector: 'app-editor-selector',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './selector-master.component.html',
  styleUrl: './selector-master.component.css'
})
export class SelectorMasterComponent implements OnInit {
  personajesActual = signal<Personaje[]> ([])
  partidaActual = signal<Partida>({
    id: null,
    nombre: '',
    descripcion: '',
    idioma: '',
    maxJugadores: 0
  });
  combateID= signal<number>(-1);

  constructor(
    private router: Router, 
    private servicioAPI: ServicioAPI,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    //this.cargarDatosPrueba();
    let partidaID = this.route.snapshot.paramMap.get('id');
    if (partidaID) {
      this.servicioAPI.obtenerDatosPartida(partidaID).subscribe({
        next: (partidaBackend) => {
          this.cargasPartidasBD(partidaBackend);
          console.log(this.partidaActual());
        },
      });
    }
  }

  seleccionarParaEditar(personaje: any) {
    console.log('Editando a:', personaje.nombre);
    this.router.navigate(['/selector-master/',this.route.snapshot.paramMap.get('id'),'editar-personaje', personaje.id]);
  }
/*
cargarDatosPrueba() {
  this.partidaActual.set({
    personajes: [
      { nombre: 'Guerrero Valiente', fotoUrl: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg' },
      { nombre: 'Mago Oscuro', fotoUrl: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg' },
      { nombre: 'Mago Oscuro', fotoUrl: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg' },
      { nombre: 'Mago Oscuro', fotoUrl: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg' },
      { nombre: 'Mago Oscuro', fotoUrl: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg' },
      { nombre: 'Mago Oscuro', fotoUrl: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg' },
      { nombre: 'Pícaro Sombrío', fotoUrl: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg' }
    ]
  });
}
*/
  cargasPartidasBD(partidaDto: DatosPartidaDto) {
    let partida = toPartida(partidaDto)
    this.partidaActual.set(partida)
    let personajes: Personaje[] = []
    for(let i of partidaDto.jugadores){
      personajes.push(toPersonaje(i))
    }
    this.personajesActual.set(personajes)
  }
}