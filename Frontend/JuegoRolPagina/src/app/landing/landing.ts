import { ChangeDetectorRef, Component, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterModule } from '@angular/router';

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
    private router: Router,
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
        let admins: number[] = []
        for(let i of partidasBackend){
          let partida: Partida = {
            id: i.id,
            nombre: i.nombre,
            descripcion: i.descripcion,
            idioma: i.idioma,
            maxJugadores: i.maximoJugadores,
            jugadoresActuales: i.jugadoresActuales ?? 0,
            jugadoresUnidos: i.jugadoresUnidos ?? []
          }
          partidas.push(partida);
          admins.push(i.adminId)
        }
        this.idsAdmin.set(admins)
        this.partidas.set(partidas)
      },
      error: (error) => {
        console.log('Parece que ha ocurrido un error:', error);
      },
    });
  }

  partidaLlena(partida: Partida): boolean {
    const actuales = partida.jugadoresActuales ?? 0;
    return actuales >= partida.maxJugadores;
  }

  yaEstoyDentro(partida: Partida): boolean {
    const usuarioId = this.usuarioService.usuarioActual()?.id;
    if (!usuarioId) return false;
    return (partida.jugadoresUnidos ?? []).some(j => j.usuarioId === usuarioId);
  }

  unirseYJugar(partida: Partida) {
    const usuarioId = this.usuarioService.usuarioActual()?.id;
    if (!partida.id || !usuarioId) {
      this.router.navigate(['/selector-personaje', partida.id]);
      return;
    }
    this.servicioAPI.unirsePartida(partida.id, usuarioId).subscribe({
      next: () => {
        this.router.navigate(['/selector-personaje', partida.id]);
      },
      error: (err) => {
        if (err?.status === 409) {
          alert('La partida está llena.');
          this.cargarPartidasBD();
        } else {
          console.log('No se pudo unir a la partida:', err);
          alert('No se pudo unir a la partida.');
        }
      }
    });
  }
}
