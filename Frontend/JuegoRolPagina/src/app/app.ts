import { Component, signal } from '@angular/core';
import { OpcionesComponent } from './opciones.component';

@Component({
  selector: 'app-root',
  imports: [OpcionesComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('JuegoRolPagina');
}
