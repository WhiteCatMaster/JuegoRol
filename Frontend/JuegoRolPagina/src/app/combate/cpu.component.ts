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
   * Elige la mejor acción (ataque u objeto) usando MCTS.
   * La fuerza de la IA escala con `dificultad` (0..1):
   *  - Daño infligido en simulación (multiplicador en MCTS).
   *  - Número de iteraciones MCTS: 600 (fácil) → 3000 (difícil).
   *  - Probabilidad epsilon de elegir aleatoriamente: 25% en 0, 0% en 1.
   *
   * @param danoRivalEsperado Daño medio que se espera del jugador por turno.
   *                          Permite modelar el contraataque dentro de la simulación.
   */
  elegirAccion(
      ataques: Ataque[],
      objetos: Objeto[],
      dificultad: number = this.dificultad,
      stats: EstadisticaPersonaje[] = [],
      hpEnemigo: number = 100,
      hpPropio: number = 100,
      danoRivalEsperado: number = 0
    ): AccionIA | null {
      if (!ataques || ataques.length === 0) return null;

      // Blindajes contra entradas raras: garantizamos finitud y rangos sanos.
      const diff = Number.isFinite(dificultad) ? Math.max(0, Math.min(1, dificultad)) : 0.5;
      const danoRival = Number.isFinite(danoRivalEsperado) ? Math.max(0, danoRivalEsperado) : 0;
      const hpEn = Number.isFinite(hpEnemigo) ? hpEnemigo : 100;
      const hpPr = Number.isFinite(hpPropio) ? hpPropio : 100;

      const state = new GameState(
        hpPr, stats, hpEn, diff, ataques, objetos,
        0, 30, 0, false, false, false,
        danoRival, 0
      );

      // Atajo: si no hay nada legal, no fingimos pensar.
      const legal = state.getLegalActions();
      if (legal.length === 0) return null;
      if (legal.length === 1) return legal[0];

      // Epsilon-greedy: en dificultad baja a veces tira una acción al azar.
      const epsilon = 0.25 * (1 - diff);
      if (epsilon > 0 && Math.random() < epsilon) {
        return legal[Math.floor(Math.random() * legal.length)];
      }

      // Iteraciones acordes a la dificultad (con suelo, nunca 0).
      const iterations = Math.max(200, Math.round(600 + 2400 * diff));
      const engine = new MctsEngine({ ...this.mctsConfig, iterations });

      // Defensa final: si MCTS devolviera null (no debería con legal>0), caemos al primer legal.
      return engine.search(state) ?? legal[0];
    }
}
