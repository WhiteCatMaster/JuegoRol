import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ObjetoDto, PersonajeDto, ServicioAPI, toObjeto, toObjetoDto, toPersonaje } from '../servicio-api';
import { Location } from '@angular/common';
import { Personaje } from '../models/personaje';
import { Objeto } from '../models/objeto';

@Component({
  selector: 'app-editar-personaje',
  imports: [FormsModule],
  templateUrl: './editar-personaje.html',
  styleUrl: './editar-personaje.css',
})
export class EditarPersonaje implements OnInit {

  idPartida: string = '';
  objetosDisponibles: Objeto[] = [
    {
      id: 1,
      nombre: "Poción de Vida Menor",
      imagen: "https://static.wikia.nocookie.net/minecraft_gamepedia/images/3/3e/Potion_of_Healing_JE2_BE2.png/revision/latest/scale-to-width/360?cb=20191027040649",
      efectosPropios: [{
        estadistica: '',
        valor: 0
      }],
      usos: 20,
      descripcion: '',
      efectosRival: [{
        estadistica: '',
        valor: 0
      }]
    },
    {
      id: 2,
      nombre: "Elixir de Sabiduría",
      imagen: "https://static.wikia.nocookie.net/minecraft_gamepedia/images/3/3e/Potion_of_Healing_JE2_BE2.png/revision/latest/scale-to-width/360?cb=20191027040649",
      efectosPropios: [],
      usos: 15,
      descripcion: '',
      efectosRival: []
    },
    {
      id: 3,
      nombre: "Espada de Hierro",
      imagen: "https://static.wikia.nocookie.net/minecraft_gamepedia/images/3/3e/Potion_of_Healing_JE2_BE2.png/revision/latest/scale-to-width/360?cb=20191027040649",
      efectosPropios: [],
      usos: 10,
      descripcion: '',
      efectosRival: []
    }
  ];

  objetoSeleccionadoId: number | null = null;

  personajeEditar = signal<Personaje & { inventario?: Objeto[] }>({
    id: null,
    nombre: '',
    urlSprite: '',
    vida: 0,
    ataquesDelPersonaje: [],
    estadisticasDelPersonaje: [],
    inventario: []
  });

  nombreOriginal = '';
  idPersonaje = '';

  personaje: Personaje = {
    id: null,
    nombre: '',
    urlSprite: '',
    vida: 0,
    ataquesDelPersonaje: [],
    estadisticasDelPersonaje: [],
    inventario: []
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private servicioAPI: ServicioAPI,
    private location: Location
  ) { }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      if (params.get('idPartida') && params.get('id')) {
        this.idPartida = params.get('idPartida') ?? ''
        this.idPersonaje = params.get('id') ?? ''
        console.log(this.idPartida, ', ', this.idPersonaje)
      }
    })

    this.servicioAPI.obtenerObjetos(this.idPartida).subscribe({
      next: (objetosDto) => {
        this.objetosDisponibles = []
        console.log(objetosDto)
        for(let i of objetosDto){
          this.objetosDisponibles.push(toObjeto(i))
        }
      },
      error: (e) => {
        console.log('Error al obtener objetos de una partida: ', e)
      }
    })
    this.servicioAPI.obtenerPersonajexId(this.idPersonaje).subscribe({
      next: (personajeBD) => {
        this.obtenerPersonajeBD(personajeBD);
        this.nombreOriginal = this.personajeEditar().nombre
        console.log(personajeBD)
      }
    })

  }

  subirStat(index: number) {
    this.personajeEditar.update((pj) => {
      pj.estadisticasDelPersonaje[index].valorPropio++;
      return { ...pj };
    });
  }

  bajarStat(index: number) {
    this.personajeEditar.update((pj) => {
      pj.estadisticasDelPersonaje[index].valorPropio--;
      return { ...pj };
    });
  }

  // FUNCIONES DEL INVENTARIO 
  asignarObjeto() {
    if (this.objetoSeleccionadoId) {
      const objetoAAsignar = this.objetosDisponibles.find(obj => obj.id === this.objetoSeleccionadoId);
      if (objetoAAsignar) {
        this.personajeEditar.update((pj) => {
          // Metemos una copia del objeto en la mochila del personaje
          pj.inventario.push({ ...objetoAAsignar });
          return { ...pj };
        });
        // Reseteamos el selector
        this.objetoSeleccionadoId = null;
      }
    }
  }

  quitarObjeto(index: number) {
    this.personajeEditar.update((pj) => {
      if (pj.inventario) {
        pj.inventario.splice(index, 1);
      }
      return { ...pj };
    });
  }

  volver() {
    this.location.back();
  }

  guardar() {
    console.log('Enviando datos al backend para:', this.nombreOriginal);
    let estats: EstatDto[] = [];
    for (let i of this.personajeEditar().estadisticasDelPersonaje) {
      console.log(`metiendo valores: `, i.nombreEstadistica, ', ', i.valorPropio)
      let estat: EstatDto = {
        nombre: i.nombreEstadistica,
        valorNuevo: i.valorPropio
      }
      estats.push(estat)
    }
    let objetosDto: ObjetoDto[] = []
    for(let i of this.personajeEditar().inventario){
      objetosDto.push(toObjetoDto(i))
    }
    let payload: ActualizarPersonajeDto = {
      nombre: this.personajeEditar().nombre,
      estadisticas: estats,
      objetos: objetosDto
    };
    console.log('Payload de personaje enviado: ', payload);

    this.servicioAPI.actualizarPersonaje(this.idPersonaje, payload).subscribe({
      next: (respuesta) => {
        console.log('Se actualizó el personaje con éxito')
        console.log(respuesta)
      },
      error: (error) => {
        console.log('Ha ocurrido un error: ', error)
      }
    })
  }

  obtenerPersonajeBD(personajeDto: PersonajeDto) {
    // Al recoger los datos del backend, nos aseguramos de que tenga un array de inventario
    const personajeConvertido = toPersonaje(personajeDto);
    this.personajeEditar.set({ ...personajeConvertido, inventario: [] });
  }
}

export interface EstatDto {
  nombre: string,
  valorNuevo: number
}

export interface ActualizarPersonajeDto {
  nombre: string;
  estadisticas: EstatDto[];
  objetos: ObjetoDto[]
}

