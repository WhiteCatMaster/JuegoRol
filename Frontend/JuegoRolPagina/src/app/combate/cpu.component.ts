import { Injectable } from '@angular/core';
import { Ataque } from '../models/ataque';
import { EstadisticaPersonaje } from '../models/personaje';
import { GameState, MctsEngine, MctsConfig } from './mcts';
import { Objeto } from '../models/objeto';
import { AccionIA } from './mcts';

@Injectable({ providedIn: 'root' })
export class CpuComponent {
  usarCpu    = false;
  dificultad = 0.5;

  // Parámetros MCTS expuestos para ajuste externo
  mctsConfig: MctsConfig = {
    iterations:          3000,
    explorationConstant: Math.SQRT2,
    rolloutDepth:        30,
  };

  /**
   * Elige el mejor ataque usando MCTS.
   * @param objetos
   * @param ataques   Todos los ataques del personaje CPU (sin pre-filtrar)
   * @param dificultad Escala 0–1: modula el daño en las simulaciones
   * @param stats      Estadísticas actuales del personaje CPU
   * @param hpEnemigo  HP actual del jugador humano
   */
  elegirAccion(
      ataques: Ataque[],
      objetos: Objeto[],
      dificultad: number = this.dificultad,
      stats: EstadisticaPersonaje[] = [],
      hpEnemigo: number = 100,
      hpPropio: number = 100
    ): AccionIA | null {
      if (!ataques || ataques.length === 0) return null;

      const state  = new GameState(hpPropio, stats, hpEnemigo, dificultad, ataques, objetos);
      const engine = new MctsEngine(this.mctsConfig);
      return engine.search(state);
    }
}
