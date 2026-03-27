import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { empty } from 'rxjs';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Estadistica } from './models/estadistica';
import { Personaje, EstadisticaPersonaje } from './models/personaje';
import { Ataque } from './models/ataque';
import { Usuario } from './models/usuario';
import { JugadorJuego, Rol } from './models/jugador-juego';
import { Partida } from './models/partida';
import { ServicioAPI } from './servicio-api';

@Component({
  selector: 'app-opciones',
  standalone: true,
  imports: [RouterLink, CommonModule, FormsModule],
  templateUrl: './opciones.component.html',
  styleUrl: './opciones.component.css'
})

export class OpcionesComponent {

  constructor(private servicioAPI: ServicioAPI) {

  }

  nombre = '';
  descripcion = '';
  idioma = '';
  maxJugadores = 0;
  
  paso = 1;
  estadisticas:Estadistica[] = [{
    nombre: '', valor: 0, consumible: false,
    id: null
  }];
  ataques : Ataque[]= [{
    nombre: '',
    id: null,
    dadoBase: 0,
    ratioDado: [],
    statReducePropio: [],
    statReduceRival: []
  }]
  personajes: Personaje[] = [{
    nombre: '', urlSprite: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg', vida: 100, ataquesDelPersonaje: [{
      nombre: '', dadoBase: 0, ratioDado: [], statReducePropio: [{ estadistica: '', valor: 0 }], statReduceRival: [{ estadistica: '', valor: 0 }],
      id: null
    }], estadisticasDelPersonaje: [{
      nombreEstadistica: '', valorPropio: 0,
      consumible: false
    }],
    id: null,
    fotoUrl: ''
  }];

  faltanEstadisticas = false;
  faltasAtaques = false;
  ataquesConNumeros = false;
  //En pos de tener un usuario (ya que todavia no existe ni login ni logout) creo un usuario de prueba
  juegoCreado : Partida ={
    id: null,
    nombre: this.nombre,
    descripcion: this.descripcion,
    idioma: this.idioma,
    maxJugadores: this.maxJugadores
  } 
  usuarioPrueba: Usuario = {
    id: null,
    googleId: '123456789',
    email: 'usuario@prueba.com',
    nombre: 'usuario 1',
    fotoUrl: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg',
    partidasParticipa: []
  };
  jugadorJuego: JugadorJuego = {
    id: null,
    usuario: this.usuarioPrueba,
    juego: this.juegoCreado,
    rol: Rol.Admin
  }

  irSiguiente() {
    if(this.paso == 1) {
      if(this.nombre == '') {
        alert('Introduce un nombre')
        return;
      }
      console.log(this.nombre, this.descripcion, this.idioma, this.maxJugadores);
    }

    if(this.paso == 2) {
      for(let estadistica of this.estadisticas) {
        if(estadistica.nombre == '' || estadistica.valor == null) {
          this.faltanEstadisticas = true;
        }
      }
      if(this.faltanEstadisticas == true) {
          alert('Faltan campos')
          this.faltanEstadisticas = false;
          return;
      }
      console.log(this.estadisticas);
    }

    if(this.paso == 3) {
        for(let ataque of this.ataques) {
            if(this.ataques.length === 0) {
            if(ataque.nombre == '' || ataque.dadoBase == null || ataque.ratioDado == null) {
               this.faltasAtaques = true; 
            }
        }
        if(this.faltasAtaques == true) {
            this.faltasAtaques = false;
        }
        //console.log(this.ataques);
    }
  }

    if (this.paso < 4) {
      this.paso = this.paso + 1;
      console.log(this.personajes);
      console.log(this.paso);
    }
    
  }

  irAtras() {
    if (this.paso > 1) {
      this.paso = this.paso - 1;
      console.log(this.personajes);
    }
  }

  agregarEstadistica() {
    this.estadisticas.push({
      nombre: '', valor: 0, consumible: false,
      id: null
    });
  }

  eliminarEstadistica(posicion: number) {
    this.estadisticas.splice(posicion, 1);
  }

  agregarAtaque() {
    this.ataques.push({ nombre: '', id: null, dadoBase: 0, ratioDado: [], statReducePropio: [], statReduceRival: [] });
  }

  eliminarAtaque(posicion: number) {
    this.ataques.splice(posicion, 1);
  }

  agregarPersonaje() {
    this.personajes.push({
      nombre: '', urlSprite: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg', vida: 0, ataquesDelPersonaje: [{
        nombre: '', dadoBase: 0, ratioDado: [], statReducePropio: [{ estadistica: '', valor: 0 }], statReduceRival: [{ estadistica: '', valor: 0 }],
        id: null
      }], estadisticasDelPersonaje: [{
        nombreEstadistica: '', valorPropio: 0,
        consumible: false
      }],
      id: null,
      fotoUrl: ''
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
      nombre: '', dadoBase: 0, ratioDado: [], statReducePropio: [{ estadistica: '', valor: 0 }], statReduceRival: [{ estadistica: '', valor: 0 }],
      id: null
    });
  }

  eliminarAtaqueDePersonaje(posicionPersonaje: number, posicionAtaque: number) {
    this.personajes[posicionPersonaje].ataquesDelPersonaje.splice(posicionAtaque, 1);
  }

  agregarEstadisticaAPersonaje(posicionPersonaje: number) {
    this.personajes[posicionPersonaje].estadisticasDelPersonaje.push({
      nombreEstadistica: '', valorPropio: 0,
      consumible: false
    }); // Actualizado
  }

  eliminarEstadisticaDePersonaje(posicionPersonaje: number, posicionEstadistica: number) {
    this.personajes[posicionPersonaje].estadisticasDelPersonaje.splice(posicionEstadistica, 1);
  }

  alCambiarAtaque(nombreElegido: string, posicionPersonaje: number, posicionAtaque: number) {
    const ataqueOriginal = this.ataques.find(a => a.nombre === nombreElegido);
    if (ataqueOriginal) {
      this.personajes[posicionPersonaje].ataquesDelPersonaje[posicionAtaque] = ataqueOriginal;
    }
  }
  alCambiarEstadistica(nombreElegido: string, posicionPersonaje: number, posicionEstadistica: number) {
    const estOriginal = this.estadisticas.find(e => e.nombre === nombreElegido);
    
    if (estOriginal) {
      this.personajes[posicionPersonaje].estadisticasDelPersonaje[posicionEstadistica].valorPropio = estOriginal.valor;
    }
  }

  trackByFn(index: any, item: any) {
    return index;
  }

  //para el drag an drop
  itemArrastrado: any = null;

  costesEnMesa: any[] = [];
  efectosEnMesa: any[] = [];

  ratioDadoMin: number | null = null;
  ratioDadoMax: number | null = null;

  iniciarArrastre(item: any) {
    this.itemArrastrado = item;
  }

  permitirDrop(event: any) {
    event.preventDefault();
  }

  soltarEnCoste() {
    if (this.itemArrastrado) {
      this.costesEnMesa.push({
        nombre: this.itemArrastrado.nombre || this.itemArrastrado, 
        valor: 0
      });
      this.itemArrastrado = null;
    }
  }

  soltarEnEfecto() {
    if (this.itemArrastrado) {
      this.efectosEnMesa.push({
        nombre: this.itemArrastrado.nombre || this.itemArrastrado,
        valor: 1.0,     
        ratioMin: null, 
        ratioMax: null   
      });
      this.itemArrastrado = null;
    }
  }
  incrementar(item: any) {
  item.valor += 1;
  }

  decrementar(item: any) {
    if (item.valor > 0) {
      item.valor -= 1;
    }
  }

  incrementarMultiplicador(item: any) {
    item.valor = Number((item.valor + 0.1).toFixed(1));
  }

  decrementarMultiplicador(item: any) {
    if (item.valor > 0) {
      item.valor = Number((item.valor - 0.1).toFixed(1));
    }
  }

  eliminarCoste(index: number) {
    this.costesEnMesa.splice(index, 1);
  }

  eliminarEfecto(index: number) {
    this.efectosEnMesa.splice(index, 1);
  }

  //resultado logica
  nombreAtaqueActual: string = '';

  guardarAtaqueActual() {
    if (this.nombreAtaqueActual.trim() === '') {
      alert('¡Tu hechizo necesita un nombre para poder ser creado!');
      return;
    }

    const mapaManaAtacante: { [key: string]: number } = {};
    for (let coste of this.costesEnMesa) {
      mapaManaAtacante[coste.nombre] = coste.valor;
    }

    const mapaEstadisticasDefensor: { [key: string]: number } = {};
    for (let efecto of this.efectosEnMesa) {
      mapaEstadisticasDefensor[efecto.nombre] = efecto.valor;
    }

    const ataqueFinal = {
      nombre: this.nombreAtaqueActual,
      manaAtacante: mapaManaAtacante,               
      estadisticasDefensor: mapaEstadisticasDefensor,
      dadoBase: 20,                                
      ratioDado: [this.ratioDadoMin, this.ratioDadoMax] 
    };

    console.log("¡Hechizo creado y listo para enviar!", ataqueFinal);
    let ataqueParaEnviar: Ataque = {
      id: null,
      nombre: ataqueFinal.nombre,
      dadoBase: ataqueFinal.dadoBase,
      ratioDado: ataqueFinal.ratioDado,
      statReducePropio: ataqueFinal.manaAtacante ? Object.entries(ataqueFinal.manaAtacante).map(([nombre, valor]) => ({ estadistica: nombre, valor })) : [],
      statReduceRival: ataqueFinal.estadisticasDefensor ? Object.entries(ataqueFinal.estadisticasDefensor).map(([nombre, valor]) => ({ estadistica: nombre, valor })) : []
    }
    this.ataques.push(ataqueParaEnviar);
    console.log("Ataque para enviar a la API:", ataqueParaEnviar);
    
    alert('¡Hechizo "' + this.nombreAtaqueActual + '" creado con éxito!');

    this.nombreAtaqueActual = '';
    this.costesEnMesa = [];
    this.efectosEnMesa = [];
    this.ratioDadoMin = null;
    this.ratioDadoMax = null;
  }

 
  mandarPartida() {
    let jugadores = [];
    
    for (let personaje of this.personajes) {
      
      // 1. Procesamos las estadísticas del personaje
      let estadisticasPersonaje = [];
      for (let estadistica of personaje.estadisticasDelPersonaje) {
        let estadisticaPersonaje = {
          "nombre": estadistica.nombreEstadistica,
          // ¡Importante! Lo convertimos a String porque el DTO de Kotlin pide String
          "valor": estadistica.valorPropio.toString(), 
          "consumible": this.estadisticas.find(e => e.nombre === estadistica.nombreEstadistica)?.consumible || false
        };
        estadisticasPersonaje.push(estadisticaPersonaje);
      }

      // 2. Procesamos los ataques del personaje (El Mapper de Arrays a Mapas)
      let ataquesPersonaje = [];
      for (let ataque of personaje.ataquesDelPersonaje) {
        
        // Convertimos el array de Maná a Diccionario {}
        let diccionarioMana: { [key: string]: number } = {};
        if (ataque.statReducePropio) {
          for (let stat of ataque.statReducePropio) {
            if (stat.estadistica && stat.estadistica.trim() !== '') {
              diccionarioMana[stat.estadistica] = stat.valor;
            }
          }
        }

        // Convertimos el array de Defensa a Diccionario {}
        let diccionarioDefensa: { [key: string]: number } = {};
        if (ataque.statReduceRival) {
          for (let stat of ataque.statReduceRival) {
            if (stat.estadistica && stat.estadistica.trim() !== '') {
              diccionarioDefensa[stat.estadistica] = stat.valor;
            }
          }
        }

        let ataquepersonaje = {
          "nombre": ataque.nombre,
          "manaAtacante": diccionarioMana, 
          "estadisticasDefensor": diccionarioDefensa,
          "dadoBase": ataque.dadoBase,
          "ratioDado": ataque.ratioDado
        };
        ataquesPersonaje.push(ataquepersonaje);
      }

      // 3. Montamos el DTO del Personaje
      let personajePayload = {
        "personajeNombre": personaje.nombre,
        "personajeVida": personaje.vida,
        "personajeFotoUrl": personaje.fotoUrl,
        "personajeEstadisticas": estadisticasPersonaje,
        "personajeAtaques": ataquesPersonaje
      }
      jugadores.push(personajePayload);
    }

    // 4. Montamos el Payload Final (Fíjate que ya NO está envuelto en "juego: {}")
    const payload = {
      "juego": {
        "nombre": this.nombre,
        "descripcion": this.descripcion,
        "idioma": this.idioma,
        "maximoJugadores": this.maxJugadores,
        "jugadores": jugadores
      }
    };

    console.log("El payload final queda como ", payload);
    
    // 5. ¡Enviamos al servidor!
    this.servicioAPI.mandarPartida(payload).subscribe({
      next: (response) => {
        console.log('Partida enviada con éxito:', response);
        alert('¡Partida enviada con éxito!');
      },
      error: (error) => {
        console.error('Error al enviar la partida:', error);
        alert('Error al enviar la partida. Revisa la consola.');
      }
    });
  }

}
