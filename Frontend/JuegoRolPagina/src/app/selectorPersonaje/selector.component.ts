import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CrearCombateDto, DatosPartidaDto, ServicioAPI, toPartida, toPersonaje } from '../servicio-api';
import { UsuarioService } from '../servicios/usuario-service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { MusicaService } from '../servicio/musica.service';
import { CpuComponent } from '../combate/cpu.component';
import { Partida } from '../models/partida';
import { Personaje } from '../models/personaje';

@Component({
  selector: 'app-combate',
  standalone: true,
  imports: [CommonModule, FormsModule,RouterLink],
  templateUrl: './selector.component.html',
  styleUrl: './selector.component.css',
})
export class SelectorPersonajeComponent implements OnInit {
  partidaActual = signal<Partida>({
    id: null,
    nombre: '',
    descripcion: '',
    idioma: '',
    maxJugadores: 0
  });
  personajesActual = signal<Personaje[]>([])
  combateID= signal<number>(-1);
  partidaID: string |null= ''

  enlace = '';

  // Dado
  estaRodando = true;
  caraDelDado = 20;
  heroeSeleccionado: Personaje = {
    id: null,
    nombre: '',
    urlSprite: '',
    vida: 0,
    ataquesDelPersonaje: [],
    estadisticasDelPersonaje: [],
    inventario: []
  };
  enemigoSeleccionado: Personaje = {
    id: null,
    nombre: '',
    urlSprite: '',
    vida: 0,
    ataquesDelPersonaje: [],
    estadisticasDelPersonaje: [],
    inventario: []
  };

  constructor(
    private router: Router,
    private servicioAPI: ServicioAPI,
    private route: ActivatedRoute,
    public usuarioService : UsuarioService,
    public cpu: CpuComponent,
    private sanitizer: DomSanitizer,
    private musicaService: MusicaService
  ) {}

  // Lo otro, lo de la base de datos temporal
  ngOnInit(): void {
    //this.cargarPartidaDePrueba();
    this.partidaID = this.route.snapshot.paramMap.get('id');
    if (this.partidaID !== '') {
      this.servicioAPI.obtenerDatosPartida(this.partidaID?? '').subscribe({
        next: (partidaBackend) => {
          this.cargasPartidasBD(partidaBackend);
          console.log(this.partidaActual());
        },
      });
    }


  }

  pasarMusica() {
    if (this.enlace) {
      let codigoVideo = ''; 

      // enlaces largos
      if (this.enlace.includes("v=")) {
        codigoVideo = this.enlace.split("v=")[1]; 
      } 
      // enlaces cortos
      else if (this.enlace.includes("youtu.be/")) {
        codigoVideo = this.enlace.split("youtu.be/")[1];
      }

      if (codigoVideo && codigoVideo !== "") {
        let urlCompleta = "https://www.youtube.com/embed/" + codigoVideo + "?autoplay=1";
      
        this.musicaService.urlYoutube.set(urlCompleta);
      } else {
        this.musicaService.urlYoutube.set(null);
      }

    } else {
       this.musicaService.urlYoutube.set(null);
    }
  }

  seleccionarHeroe(personaje: Personaje) {
    this.heroeSeleccionado = personaje;
    console.log('Héroe elegido:', personaje);
  }

  seleccionarEnemigo(personaje: Personaje) {
    if (!this.heroeSeleccionado) {
      alert('¡Por favor, selecciona primero a tu Héroe!');
      return;
    }

    // Evitamos que el enemigo sea el mismo que el héroe
    if (personaje === this.heroeSeleccionado) {
      alert('No puedes luchar contra ti mismo, elige a otro rival.');
      return;
    }

    this.enemigoSeleccionado = personaje;
    console.log('Enemigo elegido:', personaje.nombre);

    this.pasarMusica();

    this.enviarCombateBackend().subscribe({
      next: (combateBackend) => {
        // 1. Guardamos el ID real que nos dio Spring Boot
        this.combateID.set(combateBackend.id);
        
        console.log('ID del combate real:', this.combateID());
        
        // 2. AHORA SÍ, navegamos a la pantalla usando el ID real
        this.router.navigate(['selector-personaje/',this.partidaID,'jugar-combate', this.combateID()]);
      },
      error: (err) => {
        alert('Hubo un error al crear el combate en el servidor');
        console.error(err);
        this.router.navigate(['selector-personaje/',this.partidaID,'/jugar-combate', this.combateID()]);
      }
    });
  }

  cargarPartidaDePrueba() {
    this.partidaActual.set({
      id: null,
      nombre: 'La gran batalla del bosque',
      descripcion: 'El combate final está a punto de empezar...',
      idioma: 'ES',
      maxJugadores: 6,
    });
    let personajes: Personaje[] = [
        {
          id: 1,
          nombre: 'Guerrero Valiente',
          urlSprite: 'https://upload.wikimedia.org/wikipedia/commons/f/fb/Serinus_canaria_gelb.JPG',
          vida: 120,
          estadisticasDelPersonaje: [
            {
              nombreEstadistica: 'Fuerza', valorPropio: 18, consumible: false,
              id: 0
            },
            {
              nombreEstadistica: 'Fuerza', valorPropio: 18, consumible: false,
              id: 0
            },
            {
              nombreEstadistica: 'Fuerza', valorPropio: 18, consumible: false,
              id: 0
            },
            {
              nombreEstadistica: 'Defensa', valorPropio: 15, consumible: false,
              id: 0
            },
            {
              nombreEstadistica: 'Energía', valorPropio: 50, consumible: true,
              id: 0
            },
          ],
          ataquesDelPersonaje: [
            {
              id: null,
              nombre: 'Golpe Rompecráneos',
              dadoBase: 20,
              ratioDado: [19, 20],
              statReducePropio: [{ estadistica: 'Energía', valor: 10 }],
              statReduceRival: [{ estadistica: 'Vida', valor: 25 }],
              danoAtaque: 0
            },
          ],
          inventario: []
        },
        {
          id: 2,
          nombre: 'Mago Oscuro',
          urlSprite: 'https://upload.wikimedia.org/wikipedia/commons/f/fb/Serinus_canaria_gelb.JPG',
          vida: 70,
          estadisticasDelPersonaje: [
            {
              nombreEstadistica: 'Inteligencia', valorPropio: 20, consumible: false,
              id: 0
            },
            {
              nombreEstadistica: 'Maná', valorPropio: 120, consumible: true,
              id: 0
            },
          ],
          ataquesDelPersonaje: [
            {
              id: null,
              nombre: 'Bola de Fuego',
              dadoBase: 20,
              ratioDado: [18, 20],
              statReducePropio: [{ estadistica: 'Maná', valor: 30 }],
              statReduceRival: [{ estadistica: 'Vida', valor: 45 }],
              danoAtaque: 0
            },
          ],
          inventario: []
        },
        {
          id: 3,
          nombre: 'Pícaro Sombrío',
          urlSprite: 'https://upload.wikimedia.org/wikipedia/commons/f/fb/Serinus_canaria_gelb.JPG',
          vida: 85,
          estadisticasDelPersonaje: [
            {
              nombreEstadistica: 'Agilidad', valorPropio: 19, consumible: false,
              id: 0
            },
            {
              nombreEstadistica: 'Sigilo', valorPropio: 16, consumible: false,
              id: 0
            },
            {
              nombreEstadistica: 'Energía', valorPropio: 80, consumible: true,
              id: 0
            },
          ],
          ataquesDelPersonaje: [
            {
              id: null,
              nombre: 'Puñalada Trapera',
              dadoBase: 20,
              ratioDado: [15, 20],
              statReducePropio: [{ estadistica: 'Energía', valor: 15 }],
              statReduceRival: [{ estadistica: 'Vida', valor: 35 }],
              danoAtaque: 0
            },
          ],
          inventario: []
        },
        {
          id: 4,
          nombre: 'Clérigo de la Luz',
          urlSprite: 'https://upload.wikimedia.org/wikipedia/commons/f/fb/Serinus_canaria_gelb.JPG',
          vida: 100,
          estadisticasDelPersonaje: [
            {
              nombreEstadistica: 'Sabiduría', valorPropio: 17, consumible: false,
              id: 0
            },
            {
              nombreEstadistica: 'Fe', valorPropio: 100, consumible: true,
              id: 0
            },
          ],
          ataquesDelPersonaje: [
            {
              id: null,
              nombre: 'Castigo Divino',
              dadoBase: 20,
              ratioDado: [19, 20],
              statReducePropio: [{ estadistica: 'Fe', valor: 20 }],
              statReduceRival: [{ estadistica: 'Vida', valor: 30 }],
              danoAtaque: 0
            },
          ],
          inventario: []
        },
        {
          id: 5,
          nombre: 'Bárbaro Furioso',
          urlSprite: 'https://upload.wikimedia.org/wikipedia/commons/f/fb/Serinus_canaria_gelb.JPG',
          vida: 150,
          estadisticasDelPersonaje: [
            {
              nombreEstadistica: 'Fuerza Bruta', valorPropio: 22, consumible: false,
              id: 0
            },
            {
              nombreEstadistica: 'Furia', valorPropio: 50, consumible: true,
              id: 0
            },
          ],
          ataquesDelPersonaje: [
            {
              id: null,
              nombre: 'Hachazo Salvaje',
              dadoBase: 20,
              ratioDado: [17, 20],
              statReducePropio: [{ estadistica: 'Furia', valor: 15 }],
              statReduceRival: [{ estadistica: 'Vida', valor: 40 }],
              danoAtaque: 0
            },
          ],
          inventario: []
        },
        {
          id: 6,
          nombre: 'Arquero Élfico',
          urlSprite: 'https://upload.wikimedia.org/wikipedia/commons/f/fb/Serinus_canaria_gelb.JPG',
          vida: 90,
          estadisticasDelPersonaje: [
            {
              nombreEstadistica: 'Destreza', valorPropio: 20, consumible: false,
              id: 0
            },
            {
              nombreEstadistica: 'Percepción', valorPropio: 18, consumible: false,
              id: 0
            },
            {
              nombreEstadistica: 'Carcaj', valorPropio: 30, consumible: true,
              id: 0
            },
          ],
          ataquesDelPersonaje: [
            {
              id: null,
              nombre: 'Flecha Perforante',
              dadoBase: 20,
              ratioDado: [16, 20],
              statReducePropio: [{ estadistica: 'Carcaj', valor: 1 }],
              statReduceRival: [{ estadistica: 'Vida', valor: 28 }],
              danoAtaque: 0
            },
          ],
          inventario: []
        },
      ]
    this.personajesActual.set(personajes)
  }
  cargasPartidasBD(partidaDto: DatosPartidaDto) {
    this.partidaActual.set(toPartida(partidaDto))  
    let personajes: Personaje[] = [];
    for(let i of partidaDto.jugadores){
      personajes.push(toPersonaje(i))
      
    }
    this.personajesActual.set(personajes)
  }

  //Funcion para enviar los personajes que van a aprticipar en el combate
  enviarCombateBackend() {
    const usuarioId = this.usuarioService.usuarioActual()?.id;
    let payload: CrearCombateDto = {
      id: -1,
      nombre: 'nombreCombate',
      jugador1: {
        id: -1,
        usuarioId: usuarioId ?? -1,
        rol: 'JUGADOR',
        personajeId: this.heroeSeleccionado.id ?? -1
      },
      jugador2: {
        id: -1,
        usuarioId: usuarioId ?? -1,
        rol: 'JUGADOR',
        personajeId: this.enemigoSeleccionado.id ?? -1
      },
      juegoId: this.partidaActual().id ?? -1
    }
    return this.servicioAPI.enviarCombate(payload);
  }
}

