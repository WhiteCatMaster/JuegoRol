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
  paso = 1;
  estadisticas = [{ nombre: '', palabra: '' }]
  faltanEstadisticas = false;

  irSiguiente() {
    if(this.paso == 1) {
      if(this.nombre == '') {
        alert('Introduce un nombre')
        return;
      }
    }

    if(this.paso == 2) {
      for(let estadistica of this.estadisticas) {
        if(estadistica.nombre == '' || estadistica.palabra == '') {
          this.faltanEstadisticas = true;
        }
      }
      if(this.faltanEstadisticas == true) {
          alert('Faltan campos')
          this.faltanEstadisticas = false;
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
    this.estadisticas.push({ nombre: '', palabra: '' });
  }

  eliminarEstadistica(posicion: number) {
    this.estadisticas.splice(posicion, 1);
  }

}
