import { ChangeDetectorRef, Component, OnInit, signal } from '@angular/core';
import { RouterLink, RouterModule } from '@angular/router';

import { Partida } from '../models/partida';
import { ServicioAPI, toPartida } from '../servicio-api';
import { Usuario } from '../models/usuario';
import { Rol } from '../models/jugador-juego';
import { UsuarioService } from '../servicios/usuario-service';


@Component({
  selector: 'app-landing',
  standalone: true,
  templateUrl: './landing.html',
  styleUrl: './landing.css',
  imports: [RouterLink],
})
// export class Landing {}
export class Landing implements OnInit {
  public Rol = Rol;
  constructor(
    private servicioAPI: ServicioAPI,
    private cdRef: ChangeDetectorRef,
    public usuarioService: UsuarioService
  ) {}

  usuario: Usuario | null = null;
  partidas = signal<Partida[]>([]);
  idsAdmin = signal<number[]>([]);

  ngOnInit(): void {
    
    this.cargarPartidasMock();
    this.cargarPartidasBD();
    const googleId = this.usuarioService.usuarioActual()?.googleId
    this.servicioAPI.obtenerDatosUsuario(googleId).subscribe({
      next: (datos) => {
        console.log('Datos cargados:', datos);
        this.usuario = datos;
        this.cdRef.detectChanges();
      },
    });
  }

  cargarPartidasMock() {
    this.partidas.set([
      {
        nombre: 'Aventura en el bosque',
        descripcion: 'Exploración y misterio',
        idioma: 'ES',
        maxJugadores: 4,
        id: null,
      },
      {
        nombre: 'Batalla final',
        descripcion: 'PvP intenso',
        idioma: 'EN',
        maxJugadores: 8,
        id: null,
      },
    ]);
  }

  cargarPartidasBD() {
    //Se deberian de cargar las partidas que hay en DB

    this.servicioAPI.recogerPartidas().subscribe({
      next: (partidasBackend) => {
        let partidas: Partida[] = []
        for(let i of partidasBackend){
          let partida: Partida = {
            id: i.id,
            nombre: i.nombre,
            descripcion: i.descripcion,
            idioma: i.idioma,
            maxJugadores: i.maximoJugadores
          }
          partidas.push(partida);
          this.idsAdmin.update((array) => {
            let array1 = array;
            array1.push(i.adminId)
            return array1
          })
        }
        this.partidas.set(partidas)
      },
      error: (error) => {
        console.log('Parece que ha ocurrido un error:', error);
      },
    });
  }
}
