import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { empty } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { Estadistica } from './models/estadistica';
import { Personaje, EstadisticaPersonaje } from './models/personaje';

@Component({
  selector: 'app-opciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './opciones.component.html',
  styleUrl: './opciones.component.css'
})

export class OpcionesComponent {
  nombre = '';
  descripcion = '';
  idioma = '';
  maxJugadores = 0;

  paso = 1;
  estadisticas = [{ nombre: '', valor: 0, consumible: false }];
  ataques: any[] = [];
  personajes: Personaje[] = [{nombre: '', urlSprite: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg', vida: 100, ataquesDelPersonaje: [''], estadisticasDelPersonaje: [{ nombreEstadistica: '', valorPropio: 0 }]}];

  faltanEstadisticas = false;
  faltasAtaques = false;
  ataquesConNumeros = false;

  irSiguiente() {
    if(this.paso == 1) {
      if(this.nombre == '') {
        alert('Introduce un nombre')
        return;
      }
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
    }

    if(this.paso == 3) {
        for(let ataque of this.ataques) {
            if(this.ataques.length === 0) {
               this.faltasAtaques = true; 
            }
        }
        if(this.faltasAtaques == true) {
            this.faltasAtaques = false;
        }
    }

    if (this.paso < 4) {
      this.paso = this.paso + 1;
    }
  }

  irAtras() {
    if (this.paso > 1) {
      this.paso = this.paso - 1;
      console.log(this.personajes);
    }
  }

  agregarEstadistica() {
    this.estadisticas.push({ nombre: '', valor: 0, consumible: false });
  }

  eliminarEstadistica(posicion: number) {
    this.estadisticas.splice(posicion, 1);
  }

  eliminarAtaque(posicion: number) {
    this.ataques.splice(posicion, 1);
  }

  agregarPersonaje() {
    this.personajes.push({nombre: '', urlSprite: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg', vida: 0, ataquesDelPersonaje: [''], estadisticasDelPersonaje: [{ nombreEstadistica: '', valorPropio: 0 }]});
  }

  eliminarPersonaje(posicion: number) {
    if (this.personajes.length > 1) {
      this.personajes.splice(posicion, 1);
    } else {
      alert('Debe haber al menos un personaje');
    }
  }

  agregarAtaqueAPersonaje(posicionPersonaje: number) {
    this.personajes[posicionPersonaje].ataquesDelPersonaje.push('');
  }

  eliminarAtaqueDePersonaje(posicionPersonaje: number, posicionAtaque: number) {
    this.personajes[posicionPersonaje].ataquesDelPersonaje.splice(posicionAtaque, 1);
  }

  agregarEstadisticaAPersonaje(posicionPersonaje: number) {
    this.personajes[posicionPersonaje].estadisticasDelPersonaje.push({ nombreEstadistica: '', valorPropio: 0 }); // Actualizado
  }

  eliminarEstadisticaDePersonaje(posicionPersonaje: number, posicionEstadistica: number) {
    this.personajes[posicionPersonaje].estadisticasDelPersonaje.splice(posicionEstadistica, 1);
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
    
    this.ataques.push(ataqueFinal);
    
    alert('¡Hechizo "' + this.nombreAtaqueActual + '" creado con éxito!');

    this.nombreAtaqueActual = '';
    this.costesEnMesa = [];
    this.efectosEnMesa = [];
    this.ratioDadoMin = null;
    this.ratioDadoMax = null;
  }

 
}
