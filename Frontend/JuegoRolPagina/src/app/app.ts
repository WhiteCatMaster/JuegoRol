import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],   // 👈 ESTO ES CLAVE
  templateUrl: './app.html'
})
export class App {}
