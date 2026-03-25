import { Component, OnInit } from '@angular/core';
import {RouterLink, RouterModule} from '@angular/router';

import { Partida } from '../models/partida';

@Component({
  selector: 'app-landing',
  standalone: true,
  templateUrl: './landing.html',
  styleUrl: './landing.css',
  imports: [RouterLink]
})
// export class Landing {}

export class Landing implements OnInit {

  partidas: Partida[] = [];

  ngOnInit(): void {
    this.cargarPartidasMock();
  }

  cargarPartidasMock() {
    this.partidas = [
      {
        nombre: 'Aventura en el bosque',
        descripcion: 'Exploración y misterio',
        idioma: 'ES',
        maxJugadores: 4
      },
      {
        nombre: 'Batalla final',
        descripcion: 'PvP intenso',
        idioma: 'EN',
        maxJugadores: 8
      }
    ];
  }

}
