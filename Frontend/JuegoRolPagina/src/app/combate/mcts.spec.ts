import { describe, it, expect } from 'vitest';
import { GameState, MctsEngine } from './mcts';
import { Ataque } from '../models/ataque';
import { EstadisticaPersonaje } from '../models/personaje';

const ataque = (over: Partial<Ataque>): Ataque => ({
  id: 0,
  nombre: 'X',
  dadoBase: 4,
  ratioDado: [null, null],
  danoAtaque: 10,
  statReducePropio: [],
  statReduceRival: [],
  ...over,
});

const mana = (valor: number): EstadisticaPersonaje => ({
  nombreEstadistica: 'Mana',
  valorPropio: valor,
  consumible: true,
});

// Cuenta cuántas veces el motor elige cada ataque en N tiradas.
const tally = (engine: MctsEngine, state: GameState, n: number) => {
  const counts = new Map<string, number>();
  for (let i = 0; i < n; i++) {
    const chosen = engine.search(state);
    const k = chosen?.nombre ?? 'none';
    counts.set(k, (counts.get(k) ?? 0) + 1);
  }
  return counts;
};

describe('GameState — mecánica del estado', () => {
  it('getLegalActions filtra ataques que no se pueden pagar', () => {
    const barato = ataque({ nombre: 'Cheap', statReducePropio: [{ estadistica: 'Mana', valor: 5 }] });
    const caro = ataque({ nombre: 'Expensive', statReducePropio: [{ estadistica: 'Mana', valor: 50 }] });
    const gratis = ataque({ nombre: 'Free' });
    const state = new GameState([mana(10)], 100, 1.0, [barato, caro, gratis]);
    const legal = state.getLegalActions().map((a) => a.nombre).sort();
    expect(legal).toEqual(['Cheap', 'Free']);
  });

  it('applyAction reduce coste, baja HP y avanza turno', () => {
    const atk = ataque({
      danoAtaque: 30,
      statReducePropio: [{ estadistica: 'Mana', valor: 10 }],
    });
    const state = new GameState([mana(20)], 100, 1.0, [atk]);
    const next = state.applyAction(atk);
    expect(next.stats.find((s) => s.nombreEstadistica === 'Mana')!.valorPropio).toBe(10);
    expect(next.hpEnemigo).toBe(70);
    expect(next.turno).toBe(1);
    expect(next.turnosAtacados).toBe(1);
  });

  it('marca victoria cuando el HP llega a 0 o menos', () => {
    const atk = ataque({ danoAtaque: 200 });
    const next = new GameState([], 50, 1.0, [atk]).applyAction(atk);
    expect(next.victoria).toBe(true);
    expect(next.isTerminal()).toBe(true);
  });

  it('marca quedadoSeco cuando tras atacar ya no hay ataques pagables', () => {
    const caro = ataque({
      danoAtaque: 5,
      statReducePropio: [{ estadistica: 'Mana', valor: 10 }],
    });
    const next = new GameState([mana(15)], 1000, 1.0, [caro]).applyAction(caro);
    expect(next.quedadoSeco).toBe(true);
    expect(next.isTerminal()).toBe(true);
  });

  it('NO marca quedadoSeco si la victoria ocurre antes', () => {
    const caro = ataque({
      danoAtaque: 5000,
      statReducePropio: [{ estadistica: 'Mana', valor: 10 }],
    });
    const next = new GameState([mana(15)], 100, 1.0, [caro]).applyAction(caro);
    expect(next.victoria).toBe(true);
    expect(next.quedadoSeco).toBe(false);
  });

  it('la dificultad modula el daño en applyAction (núcleo del escalado)', () => {
    const atk = ataque({ danoAtaque: 100 });
    const baja = new GameState([], 1000, 0.1, [atk]).applyAction(atk);
    const alta = new GameState([], 1000, 1.0, [atk]).applyAction(atk);
    expect(1000 - baja.hpEnemigo).toBeCloseTo(10, 5);
    expect(1000 - alta.hpEnemigo).toBeCloseTo(100, 5);
  });

  it('aplica el suelo de 0.05 a la dificultad', () => {
    const atk = ataque({ danoAtaque: 100 });
    const cero = new GameState([], 1000, 0, [atk]).applyAction(atk);
    expect(1000 - cero.hpEnemigo).toBeCloseTo(5, 5);
  });

  it('getReward premia ganar rápido frente a un combate largo perdido', () => {
    const stats = [mana(10)];
    // Victoria al turno 1: 50 + (30 - 1) + 0.1*10 = 80
    const winFast = new GameState(stats, 0, 1.0, [], 1, 30, 1, true, false);
    // Sin victoria pero 25 turnos atacando, sin seco: 25 + 0.1*10 = 26
    const longNoWin = new GameState(stats, 100, 1.0, [], 25, 30, 25, false, false);
    expect(winFast.getReward()).toBeGreaterThan(longNoWin.getReward());
  });

  it('getReward penaliza quedadoSeco', () => {
    const stats = [mana(10)];
    const seco = new GameState(stats, 100, 1.0, [], 5, 30, 5, false, true);
    const noSeco = new GameState(stats, 100, 1.0, [], 5, 30, 5, false, false);
    expect(noSeco.getReward() - seco.getReward()).toBeCloseTo(30, 5);
  });
});

describe('MctsEngine — comportamiento básico', () => {
  it('devuelve null si no hay ataques jugables', () => {
    const caro = ataque({ statReducePropio: [{ estadistica: 'Mana', valor: 5 }] });
    const state = new GameState([mana(0)], 100, 1.0, [caro]);
    expect(new MctsEngine({ iterations: 50 }).search(state)).toBeNull();
  });

  it('devuelve directamente el único ataque legal (atajo)', () => {
    const unico = ataque({ nombre: 'Solo' });
    const state = new GameState([], 100, 1.0, [unico]);
    expect(new MctsEngine({ iterations: 50 }).search(state)?.nombre).toBe('Solo');
  });
});

describe('MctsEngine — inteligencia (prueba estadística)', () => {
  it('evita el ataque que conduce a quedadoSeco', () => {
    // Bomba: cuesta 10, pega 5 → tras usarla, mana=5, no se puede pagar ni Bomba (10) ni Chispa (6).
    //   → quedadoSeco inmediato, reward ≈ -28.
    // Chispa: cuesta 6, pega 3 → permite 2 Chispas (12 mana) y luego se queda en 3 → no paga otra → quedadoSeco al turno 2.
    //   → mejor reward que Bomba porque sobreviven más turnos antes del seco.
    // El MCTS debe preferir Chispa la inmensa mayoría de las veces.
    const bomba = ataque({
      nombre: 'Bomba',
      danoAtaque: 5,
      statReducePropio: [{ estadistica: 'Mana', valor: 10 }],
    });
    const chispa = ataque({
      nombre: 'Chispa',
      danoAtaque: 3,
      statReducePropio: [{ estadistica: 'Mana', valor: 6 }],
    });
    const state = new GameState([mana(15)], 1000, 1.0, [bomba, chispa]);
    const counts = tally(new MctsEngine({ iterations: 1500 }), state, 15);
    const eligeChispa = counts.get('Chispa') ?? 0;
    expect(eligeChispa).toBeGreaterThanOrEqual(12);
  });

  it('prefiere el ataque que remata cuando la victoria es 1-shot', () => {
    const fuerte = ataque({ nombre: 'Fuerte', danoAtaque: 80 });
    const debil = ataque({ nombre: 'Debil', danoAtaque: 2 });
    const state = new GameState([], 50, 1.0, [fuerte, debil]);
    const counts = tally(new MctsEngine({ iterations: 1500 }), state, 15);
    expect((counts.get('Fuerte') ?? 0)).toBeGreaterThanOrEqual(13);
  });

  it('prefiere terminar rápido cuando puede ganar en pocos turnos', () => {
    // HP=160. Fuerte (80) gana en 2 turnos; Debil (10) en 16. Reward fuerte: 50+(30-2)=78.
    // Reward debil: 50+(30-16)=64. Fuerte debe ganar la mayoría.
    const fuerte = ataque({ nombre: 'Fuerte', danoAtaque: 80 });
    const debil = ataque({ nombre: 'Debil', danoAtaque: 10 });
    const state = new GameState([], 160, 1.0, [fuerte, debil]);
    const counts = tally(new MctsEngine({ iterations: 1500 }), state, 15);
    expect((counts.get('Fuerte') ?? 0)).toBeGreaterThanOrEqual(12);
  });

  it('en escenarios sin victoria al alcance, prefiere el ataque sostenible al kamikaze', () => {
    // HP enemigo enorme → ninguna estrategia logra victoria en 30 turnos.
    // Caro: cuesta 30, pega 50 → 3 usos → quedadoSeco.
    // Mantenible: cuesta 2, pega 6 → durabilidad muy alta, no quedadoSeco.
    // El MCTS debe favorecer Mantenible (sin penalización -30).
    const caro = ataque({
      nombre: 'Caro',
      danoAtaque: 50,
      statReducePropio: [{ estadistica: 'Mana', valor: 30 }],
    });
    const mantenible = ataque({
      nombre: 'Mantenible',
      danoAtaque: 6,
      statReducePropio: [{ estadistica: 'Mana', valor: 2 }],
    });
    const state = new GameState([mana(100)], 5000, 1.0, [caro, mantenible]);
    const counts = tally(new MctsEngine({ iterations: 1500 }), state, 15);
    expect((counts.get('Mantenible') ?? 0)).toBeGreaterThanOrEqual(12);
  });
});
