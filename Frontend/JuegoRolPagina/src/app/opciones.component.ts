import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Estadistica } from './models/estadistica';
import { Personaje, EstadisticaPersonaje } from './models/personaje';
import { Ataque } from './models/ataque';
import { Usuario } from './models/usuario';
import { JugadorJuego, Rol } from './models/jugador-juego';
import { Partida, Plantilla } from './models/partida';
import { CrearPartidaDto, DatosPartidaDto, ObjetoDto, PersonajeDto, ServicioAPI, toObjeto, toObjetoDto, toPersonaje, toPersonajeDto } from './servicio-api';
import { Dado } from './models/dado';
import { UsuarioService } from './servicios/usuario-service';
import { Objeto } from './models/objeto';


@Component({
  selector: 'app-opciones',
  standalone: true,
  imports: [RouterLink, CommonModule, FormsModule],
  templateUrl: './opciones.component.html',
  styleUrl: './opciones.component.css',
})
export class OpcionesComponent implements OnInit{
  constructor(
    private servicioAPI: ServicioAPI,
    public usuarioService: UsuarioService,
  ) {}
  ngOnInit(): void {
      this.cargarPlantillas()
  }

  nombre = '';
  descripcion = '';
  idioma = '';
  maxJugadores = 0;
  listaPlantillas : Plantilla[] = []
  plantillaSeleccionada: Plantilla|null = null;
  nombreNuevaPlantilla : string = '';

  paso = 1;
  estadisticas: Estadistica[] = [
    {
      nombre: '',
      valor: 0,
      consumible: false,
      id: null,
    },
  ];
  ataques: Ataque[] = [
    {
      nombre: '',
      id: null,
      dadoBase: 0,
      ratioDado: [],
      statReducePropio: [],
      statReduceRival: [],
      danoAtaque: 0,
    },
  ];

  dados: Dado[] = [
    {
      nombre: '',
      caras: 6,
      cantidad: 1,
    },
  ];
  personajes: Personaje[] = [
    {
      nombre: '',
      urlSprite: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg',
      vida: 100,
      ataquesDelPersonaje: [
        {
          nombre: '',
          dadoBase: 0,
          ratioDado: [],
          statReducePropio: [{ estadistica: '', valor: 0 }],
          statReduceRival: [{ estadistica: '', valor: 0 }],
          id: null,
          danoAtaque: 0,
        },
      ],
      estadisticasDelPersonaje: [
        {
          nombreEstadistica: '',
          valorPropio: 0,
          consumible: false,
          id: 0
        },
      ],
      id: null,
    },
  ];

  // ── OBJETOS ──────────────────────────────────────────────────────────────────
  // Lista final de objetos ya creados y guardados
  objetos: Objeto[] = [];

  // ─ Estado del creador de objeto activo ─
  nombreObjetoActual: string = '';
  descripcionObjetoActual: string = '';
  imagenObjetoActual: string = '';
  usosObjetoActual: number = 1;

  // Efectos del objeto (arrastrables)
  efectosPropiosObjeto: { nombre: string; valor: number; signo: 1 | -1 }[] = [];
  efectosRivalObjeto:   { nombre: string; valor: number; signo: 1 | -1 }[] = [];

  // Item que se está arrastrando (compartido con los ataques)
  itemArrastradoObjeto: any = null;

  iniciarArrastreObjeto(stat: any) {
    this.itemArrastradoObjeto = stat;
  }

  permitirDropObjeto(event: any) {
    event.preventDefault();
  }

  soltarEnEfectoPropio() {
    if (this.itemArrastradoObjeto) {
      this.efectosPropiosObjeto.push({
        nombre: this.itemArrastradoObjeto.nombre,
        valor: 1,
        signo: 1,
      });
      this.itemArrastradoObjeto = null;
    }
  }

  soltarEnEfectoRival() {
    if (this.itemArrastradoObjeto) {
      this.efectosRivalObjeto.push({
        nombre: this.itemArrastradoObjeto.nombre,
        valor: 1,
        signo: -1,
      });
      this.itemArrastradoObjeto = null;
    }
  }

  incrementarEfectoObjeto(item: any) {
    item.valor = Number((item.valor + 1).toFixed(0));
  }

  decrementarEfectoObjeto(item: any) {
    if (item.valor > 1) {
      item.valor = Number((item.valor - 1).toFixed(0));
    }
  }

  toggleSignoEfecto(item: any) {
    item.signo = item.signo === 1 ? -1 : 1;
  }

  eliminarEfectoPropio(index: number) {
    this.efectosPropiosObjeto.splice(index, 1);
  }

  eliminarEfectoRival(index: number) {
    this.efectosRivalObjeto.splice(index, 1);
  }

  guardarObjetoActual() {
    if (this.nombreObjetoActual.trim() === '') {
      alert('¡El objeto necesita un nombre!');
      return;
    }

    const objetoFinal: Objeto = {
      id: null,
      nombre: this.nombreObjetoActual,
      descripcion: this.descripcionObjetoActual,
      imagen: this.imagenObjetoActual || 'assets/img/objetos/default.png',
      efectosPropios: this.efectosPropiosObjeto.map(e => ({
        estadistica: e.nombre,
        valor: e.signo * e.valor,
      })),
      efectosRival: this.efectosRivalObjeto.map(e => ({
        estadistica: e.nombre,
        valor: e.signo * e.valor,
      })),
      usos: this.usosObjetoActual,
    };

    this.objetos.push(objetoFinal);
    console.log('Objeto creado:', objetoFinal);
    alert(`¡Objeto "${this.nombreObjetoActual}" creado con éxito!`);

    // Resetear el formulario del creador
    this.nombreObjetoActual = '';
    this.descripcionObjetoActual = '';
    this.imagenObjetoActual = '';
    this.usosObjetoActual = 1;
    this.efectosPropiosObjeto = [];
    this.efectosRivalObjeto = [];
  }

  eliminarObjeto(index: number) {
    this.objetos.splice(index, 1);
  }

  // ── FIN OBJETOS ───────────────────────────────────────────────────────────────

  faltanEstadisticas = false;
  faltasAtaques = false;
  ataquesConNumeros = false;

  juegoCreado: Partida = {
    id: null,
    nombre: this.nombre,
    descripcion: this.descripcion,
    idioma: this.idioma,
    maxJugadores: this.maxJugadores,
  };
  usuarioPrueba: Usuario = {
    id: null,
    googleId: '123456789',
    email: 'usuario@prueba.com',
    nombre: 'usuario 1',
    fotoUrl: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg',
    partidasParticipa: [],
  };
  jugadorJuego: JugadorJuego = {
    id: null,
    usuario: this.usuarioPrueba,
    juego: this.juegoCreado,
    rol: Rol.Admin,
    personaje: {
      id: null,
      nombre: '',
      urlSprite: '',
      vida: 0,
      ataquesDelPersonaje: [],
      estadisticasDelPersonaje: []
    }
  };

  irSiguiente() {
    if (this.paso == 1) {
      if (this.nombre == '') {
        alert('Introduce un nombre');
        return;
      }
    }

    if (this.paso == 2) {
      for (let estadistica of this.estadisticas) {
        if (estadistica.nombre == '' || estadistica.valor == null) {
          this.faltanEstadisticas = true;
        }
      }
      if (this.faltanEstadisticas == true) {
        alert('Faltan campos');
        this.faltanEstadisticas = false;
        return;
      }
    }

    if (this.paso == 3) {
      for (let personaje of this.personajes) {
        if (personaje.estadisticasDelPersonaje[0].nombreEstadistica == '') {
          personaje.estadisticasDelPersonaje = [];
          for (let estGlobal of this.estadisticas) {
            personaje.estadisticasDelPersonaje.push({
              nombreEstadistica: estGlobal.nombre,
              valorPropio: estGlobal.valor,
              consumible: estGlobal.consumible,
              id: estGlobal.id ?? -1
            });
          }
        }
      }
    }

    if (this.paso === 4) {
      for (let dado of this.dados) {
        if (dado.nombre === '' || dado.cantidad == null) {
          alert('Revisa los campos de los dados');
          return;
        }
      }
    }

    if (this.paso === 5) {
      this.paso = this.paso + 1;
      console.log(this.personajes);
      return;
    }

    if (this.paso < 6) {
      this.paso++;
    }
  }

  irAtras() {
    if (this.paso > 1) {
      this.paso = this.paso - 1;
    }
  }

  agregarEstadistica() {
    this.estadisticas.push({ nombre: '', valor: 0, consumible: false, id: null });
  }

  eliminarEstadistica(posicion: number) {
    this.estadisticas.splice(posicion, 1);
  }

  agregarAtaque() {
    this.ataques.push({
      nombre: '', id: null, dadoBase: 0, ratioDado: [],
      statReducePropio: [], statReduceRival: [], danoAtaque: 0,
    });
  }

  eliminarAtaque(posicion: number) {
    this.ataques.splice(posicion, 1);
  }

  agregarDado() {
    this.dados.push({ nombre: '', caras: 6, cantidad: 1 });
  }

  eliminarDado(posicion: number) {
    this.dados.splice(posicion, 1);
  }

  agregarPersonaje() {
    let estadisticasNuevas: EstadisticaPersonaje[] = [];
    for (let est of this.estadisticas) {
      estadisticasNuevas.push({
        nombreEstadistica: est.nombre,
        valorPropio: est.valor,
        consumible: est.consumible,
        id: est.id ?? -1
      });
    }
    this.personajes.push({
      nombre: '',
      urlSprite: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg',
      vida: 0,
      ataquesDelPersonaje: [{
        nombre: '', dadoBase: 0, ratioDado: [],
        statReducePropio: [{ estadistica: '', valor: 0 }],
        statReduceRival: [{ estadistica: '', valor: 0 }],
        id: null, danoAtaque: 0,
      }],
      estadisticasDelPersonaje: estadisticasNuevas,
      id: null,
    });
  }

  eliminarPersonaje(posicion: number) {
    if (this.personajes.length > 1) {
      this.personajes.splice(posicion, 1);
    } else {
      alert('Debe haber al menos un personaje');
    }
  }

  agregarAtaqueAPersonaje(posicionPersonaje: number) {
    this.personajes[posicionPersonaje].ataquesDelPersonaje.push({
      nombre: '', dadoBase: 0, ratioDado: [],
      statReducePropio: [{ estadistica: '', valor: 0 }],
      statReduceRival: [{ estadistica: '', valor: 0 }],
      id: null, danoAtaque: 0,
    });
  }

  eliminarAtaqueDePersonaje(posicionPersonaje: number, posicionAtaque: number) {
    this.personajes[posicionPersonaje].ataquesDelPersonaje.splice(posicionAtaque, 1);
  }

  agregarEstadisticaAPersonaje(posicionPersonaje: number) {
    this.personajes[posicionPersonaje].estadisticasDelPersonaje.push({
      nombreEstadistica: '', valorPropio: 0, consumible: false,
      id: 0
    });
  }

  eliminarEstadisticaDePersonaje(posicionPersonaje: number, posicionEstadistica: number) {
    this.personajes[posicionPersonaje].estadisticasDelPersonaje.splice(posicionEstadistica, 1);
  }

  alCambiarAtaque(nombreElegido: string, posicionPersonaje: number, posicionAtaque: number) {
    const ataqueOriginal = this.ataques.find((a) => a.nombre === nombreElegido);
    if (ataqueOriginal) {
      this.personajes[posicionPersonaje].ataquesDelPersonaje[posicionAtaque] = ataqueOriginal;
    }
  }

  alCambiarEstadistica(nombreElegido: string, posicionPersonaje: number, posicionEstadistica: number) {
    const estOriginal = this.estadisticas.find((e) => e.nombre === nombreElegido);
    if (estOriginal) {
      this.personajes[posicionPersonaje].estadisticasDelPersonaje[posicionEstadistica].valorPropio = estOriginal.valor;
    }
  }

  trackByFn(index: any, item: any) {
    return index;
  }

  // ── DRAG & DROP – ATAQUES ────────────────────────────────────────────────────
  itemArrastrado: Estadistica |null= null;
  costesEnMesa: {nombre: string, valor: number}[] = [];
  efectosEnMesa: {nombre: string, valor: number, ratioMin: number|null, ratioMax: number|null}[] = [];

  ratioDadoMin: number | null = null;
  ratioDadoMax: number | null = null;
  danoAtaque: number = 0;
  nombreAtaqueActual = '';


  finalizarArrastre(){
    this.itemArrastrado = null;
  }

  iniciarArrastre(item: Estadistica) {
    this.itemArrastrado = item;
    console.log(this.itemArrastrado)
  }

  permitirDrop(event: DragEvent) {
    event.preventDefault();
  }

  soltarEnCoste() {
    if (this.itemArrastrado) {
      console.log(this.itemArrastrado)
      const nombreItem = this.itemArrastrado.nombre
      const yaExiste = this.costesEnMesa.some(c => c.nombre === nombreItem)
      if(yaExiste){
        alert('¡Esa estadística ya está en los costes!');
      } else{
        this.costesEnMesa.push(this.itemArrastrado);
      }
      this.itemArrastrado = null;
    }
  }

  soltarEnEfecto() {
    if (this.itemArrastrado) {
      console.log(this.itemArrastrado)
      const nombreItem = this.itemArrastrado.nombre;
      const yaExiste = this.efectosEnMesa.some(e => e.nombre === nombreItem);
      if(yaExiste){
        alert('¡Esa estadística ya está en los efectos!');
      }else{
        this.efectosEnMesa.push({
          nombre: this.itemArrastrado.nombre,
          valor: 1.0,
          ratioMin: null,
          ratioMax: null,
        });
      }

      this.itemArrastrado = null;
    }
  }

  incrementar(item: {nombre: string, valor: number}) { item.valor += 1; console.log(item)}
  decrementar(item: {nombre: string, valor: number}) { if (item.valor > 0) item.valor -= 1; }
  incrementarMultiplicador(item: {nombre: string, valor: number, ratioMin: number|null, ratioMax: number|null}) { item.valor = Number((item.valor + 0.1).toFixed(1)); console.log(item)}
  decrementarMultiplicador(item: {nombre: string, valor: number, ratioMin: number|null, ratioMax: number|null}) { if (item.valor > 0) item.valor = Number((item.valor - 0.1).toFixed(1)); console.log(item) }
  eliminarCoste(index: number) { this.costesEnMesa.splice(index, 1); }
  eliminarEfecto(index: number) { this.efectosEnMesa.splice(index, 1); }


  guardarAtaqueActual() {
    if (this.nombreAtaqueActual.trim() === '') {
      alert('¡Tu hechizo necesita un nombre para poder ser creado!');
      return;
    }
    if (this.ratioDadoMin === null || this.ratioDadoMax === null) {
      alert('¡Debes especificar el ratio mínimo y máximo del dado!');
      return;
    }
    if (this.ratioDadoMin > this.ratioDadoMax) {
      alert('¡El ratio mínimo no puede ser mayor que el máximo!');
      return;
    }

    let mapaManaAtacante: { estadistica: string, valor: number }[] = [];
    for (let coste of this.costesEnMesa) {
      mapaManaAtacante.push({estadistica: coste.nombre, valor: coste.valor});
    }

    let mapaEstadisticasDefensor: { estadistica: string, valor: number }[] = [];
    for (let efecto of this.efectosEnMesa) {
      mapaEstadisticasDefensor.push({estadistica: efecto.nombre, valor: efecto.valor})
    }

    let ataqueParaEnviar: Ataque = {
      id: null,
      nombre: this.nombreAtaqueActual,
      dadoBase: 20,
      ratioDado: [this.ratioDadoMin, this.ratioDadoMax],
      statReducePropio: mapaManaAtacante,
      statReduceRival: mapaEstadisticasDefensor,
      danoAtaque: this.danoAtaque,
    };
    this.ataques.push(ataqueParaEnviar);
    console.log(ataqueParaEnviar)

    alert('¡Hechizo "' + this.nombreAtaqueActual + '" creado con éxito!');
    this.nombreAtaqueActual = '';
    this.costesEnMesa = [];
    this.efectosEnMesa = [];
    this.ratioDadoMin = null;
    this.ratioDadoMax = null;
    this.danoAtaque = 0;
  }

  mandarPartida() {

    let jugadores: PersonajeDto[] = [];
    const adminId = this.usuarioService.usuarioActual()?.id;

    for (let personaje of this.personajes) {
      jugadores.push(toPersonajeDto(personaje))
      for(let i of personaje.ataquesDelPersonaje){
        console.log(i)
      }
    }

    let objetosDto : ObjetoDto[] = []
    for(let i of this.objetos){
      console.log(i)
      objetosDto.push(toObjetoDto(i))
    }

    let payload : CrearPartidaDto  = {
      adminId: adminId ?? -1,
      id: -1,
      nombre: this.nombre,
      descripcion: this.descripcion,
      idioma: this.idioma,
      maximoJugadores: this.maxJugadores,
      jugadores: jugadores,
      objetos: objetosDto
    };

    console.log('El payload final queda como ', payload);

    this.servicioAPI.mandarPartida(payload).subscribe({
      next: (response) => {
        console.log('Partida enviada con éxito:', response);
        alert('¡Partida enviada con éxito!');
      },
      error: (error) => {
        console.error('Error al enviar la partida:', error);
        alert('Error al enviar la partida. Revisa la consola.');
      },
    });
  }

  obtenerImagenDado(caras: number): string {
    switch (caras) {
      case 4:   return 'assets/img/dados/d4.png';
      case 6:   return 'assets/img/dados/d6.png';
      case 8:   return 'assets/img/dados/d8.png';
      case 10:  return 'assets/img/dados/d10.png';
      case 12:  return 'assets/img/dados/d12.png';
      case 20:  return 'assets/img/dados/d20.png';
      case 100: return 'assets/img/dados/d100.jpg';
      default:  return 'assets/img/dados/default.jpg';
    }
  }


  crearPLantillaNueva(nombrePlantilla: string){
    let jugadores: PersonajeDto[] = [];
    const adminId = this.usuarioService.usuarioActual()?.id;

    for (let personaje of this.personajes) {
      jugadores.push(toPersonajeDto(personaje))
    }

    let objetosDto : ObjetoDto[] = []
    for(let i of this.objetos){
      console.log(i)
      objetosDto.push(toObjetoDto(i))
    }

    let payload : CrearPartidaDto  = {
      adminId: adminId ?? -1,
      id: -1,
      nombre: this.nombre,
      descripcion: this.descripcion,
      idioma: this.idioma,
      maximoJugadores: this.maxJugadores,
      jugadores: jugadores,
      objetos: objetosDto
    };
    this.servicioAPI.guardarPlantilla(nombrePlantilla,payload).subscribe({
      next: (respuesta) => {
        console.log('Plantilla guardada en el catalogo')
        nombrePlantilla = '';
        this.cargarPlantillas()
        console.log(respuesta)
      },
      error: (error) => {
        console.log('Ha ocurrido un error: ', error)
      }
    })

  }
  //Plantillas
  cargarPlantilla(plantillaSeleccionada: Plantilla){
    let payload = plantillaSeleccionada.jsonConfiguration;
    payload.adminId = this.usuarioService.usuarioActual()?.id ?? -1
    //Supongo que ahora seria cargar todo con el dto desde backend
    this.nombre = payload.nombre
    this.descripcion =payload.descripcion
    this.idioma = payload.idioma
    this.maxJugadores = payload.maximoJugadores
    this.personajes = []
    this.estadisticas = []
    //Ahora los personajes
    for(let i of payload.jugadores){
      let personaje = toPersonaje(i)
      if(!this.personajes.includes(personaje)){
        this.personajes.push(personaje)
      }
      for(let ataque of personaje.ataquesDelPersonaje){
        console.log(ataque)
        if(this.ataques.find((value) => value.nombre === ataque.nombre) === undefined){
          ataque.id = null
          this.ataques.push(ataque)
        }
      }
      for(let estadisticaPersonaje of personaje.estadisticasDelPersonaje){
        console.log(estadisticaPersonaje)
        let estadistica: Estadistica ={
          id: null,
          nombre: estadisticaPersonaje.nombreEstadistica,
          valor: estadisticaPersonaje.valorPropio,
          consumible: estadisticaPersonaje.consumible
        }
        if(this.estadisticas.find((value) => value.nombre === estadistica.nombre) === undefined){
          this.estadisticas.push(estadistica)
        }
      }
    }
    for(let i of plantillaSeleccionada.jsonConfiguration.objetos){
      this.objetos.push(toObjeto(i))
    }
  }
  ejecutarCarga(){
    if(this.plantillaSeleccionada){
      this.cargarPlantilla(this.plantillaSeleccionada)
      alert('Plantilla cargada')

    }
  }
  guardarPlantilla(){
    if (this.nombreNuevaPlantilla.trim() === ''){
      alert('Necesita un nombre valido la plantilla')
      return;
    }
    this.crearPLantillaNueva(this.nombreNuevaPlantilla)

  }
  cargarPlantillas(){
    this.servicioAPI.obtenerPlantillas().subscribe({
      next: (respuesta) => {
        console.log('plantillas obtenidas:', respuesta)
        this.listaPlantillas = respuesta
      },
      error: (error) => {
        console.log('Error al obtener plantillas: ', error)
      }
    })
  }
}
