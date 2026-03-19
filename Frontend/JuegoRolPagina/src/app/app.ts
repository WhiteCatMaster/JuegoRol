import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { OpcionesComponent } from './opciones.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, OpcionesComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('JuegoRolPagina');
}
