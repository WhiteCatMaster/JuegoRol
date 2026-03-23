import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { empty } from 'rxjs';
import { FormsModule } from '@angular/forms';

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
  ataques = [{nombre: '', sujeto: '', tipo: '', factor: '', umbral: ''}]
  personajes = [{nombre: '', urlSprite: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg', vida: 100, ataquesDelPersonaje: [''], estadisticasDelPersonaje: ['']}];

  tiposDeAtaque = ['Ninguno', 'Físico', 'Magia', 'Fuego', 'Veneno'];
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
            if(ataque.nombre == '' || ataque.sujeto== '' || ataque.tipo == ''  || ataque.factor == '' || ataque.umbral == '') {
               this.faltasAtaques = true; 
            }
        }
        if(this.faltasAtaques == true) {
            alert('Faltan campos')
            this.faltasAtaques = false;
            return;
        }
    }

    if (this.paso < 4) {
      this.paso = this.paso + 1;
    }
  }

  irAtras() {
    if (this.paso > 1) {
      this.paso = this.paso - 1;
    }
  }

  agregarEstadistica() {
    this.estadisticas.push({ nombre: '', valor: 0, consumible: false });
  }

  eliminarEstadistica(posicion: number) {
    this.estadisticas.splice(posicion, 1);
  }

  agregarAtaque() {
    this.ataques.push({ nombre: '', sujeto: '', tipo: '', factor: '', umbral: '' });
  }

  eliminarAtaque(posicion: number) {
    this.ataques.splice(posicion, 1);
  }

  agregarPersonaje() {
    this.personajes.push({nombre: '', urlSprite: 'https://i.pinimg.com/474x/9c/0f/06/9c0f06b14aba220811331c49718d6b93.jpg', vida: 0, ataquesDelPersonaje: [''], estadisticasDelPersonaje: ['']});
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
    this.personajes[posicionPersonaje].estadisticasDelPersonaje.push('');
  }

  eliminarEstadisticaDePersonaje(posicionPersonaje: number, posicionEstadistica: number) {
    this.personajes[posicionPersonaje].estadisticasDelPersonaje.splice(posicionEstadistica, 1);
  }

  trackByFn(index: any, item: any) {
    return index;
  }

}
