import { Ataque } from '../models/ataque';
import { Objeto } from '../models/objeto';
import { EstadisticaPersonaje } from '../models/personaje';

// 1. Unificamos las acciones posibles
export type AccionIA = Ataque | Objeto;

export interface MctsConfig {
  iterations?: number;
  explorationConstant?: number;
  rolloutDepth?: number;
}

export class GameState {
  constructor(
    public readonly hpPropio: number,       // NUEVO: Necesita saber su vida para curarse
    public readonly stats: EstadisticaPersonaje[],
    public readonly hpEnemigo: number,
    public readonly dificultad: number,
    public readonly ataques: Ataque[],
    public readonly objetos: Objeto[],      // NUEVO: Inventario disponible en la simulación
    public readonly turno: number = 0,
    public readonly maxTurnos: number = 30,
    public readonly turnosAtacados: number = 0,
    public readonly victoria: boolean = false,
    public readonly quedadoSeco: boolean = false
  ) {}

  getLegalActions(): AccionIA[] {
    const ataquesLegales = this.ataques.filter(atc => {
      if (!atc.statReducePropio || atc.statReducePropio.length === 0) return true;
      for (const coste of atc.statReducePropio) {
        const stat = this.stats.find(s => s.nombreEstadistica === coste.estadistica);
        if (!stat || stat.valorPropio < coste.valor) return false;
      }
      return true;
    });

    // Filtramos los objetos que se han gastado en esta simulación
    const objetosLegales = this.objetos.filter(obj => obj.usos > 0);

    // MCTS ahora explorará tanto ataques como objetos
    return [...ataquesLegales, ...objetosLegales];
  }

  applyAction(accion: AccionIA): GameState {
    let newHpPropio = this.hpPropio;
    let newHpEnemigo = this.hpEnemigo;
    const newStats = this.stats.map(s => ({ ...s }));
    const newObjetos = this.objetos.map(o => ({ ...o, efectosPropios: [...o.efectosPropios], efectosRival: [...o.efectosRival] }));

    // Type Guard de TypeScript: si tiene "usos", es un objeto
    if ('usos' in accion) {
             // 1. Restamos un uso al objeto en la simulación
             const objIndex = newObjetos.findIndex(o => o.nombre === accion.nombre);
             if (objIndex !== -1) newObjetos[objIndex].usos -= 1;

             // 2. Aplicamos TODOS los efectos propios a las estadísticas del array
             for (const ef of accion.efectosPropios) {
                 const stat = newStats.find(s => s.nombreEstadistica === ef.estadistica);
                 // Si la estadística existe (mana, fuerza, etc.), le sumamos el valor
                 if (stat) stat.valorPropio += ef.valor;
             }

             // 3. Efectos al rival
             // (Nota importante abajo sobre esto)

         } else {
        // Es un Ataque (Lógica original)
        for (const coste of (accion.statReducePropio ?? [])) {
          const stat = newStats.find(s => s.nombreEstadistica === coste.estadistica);
          if (stat) stat.valorPropio -= coste.valor;
        }

        const total = accion.dadoBase > 0 ? accion.dadoBase : 6;
        const dado = Math.floor(Math.random() * total) + 1;
        const caraCrit   = accion.ratioDado?.[0] ?? null;
        const caraMedium = accion.ratioDado?.[1] ?? null;

        let dano = accion.danoAtaque;
        if (dado === caraCrit)        dano *= 2;
        else if (dado === caraMedium) dano *= 1.5;

        dano *= Math.max(0.05, this.dificultad);
        newHpEnemigo -= dano;
    }

    const victoria  = newHpEnemigo <= 0;
    const newTurno  = this.turno + 1;

    const dummyState = new GameState(newHpPropio, newStats, newHpEnemigo, this.dificultad, this.ataques, newObjetos, newTurno, this.maxTurnos, this.turnosAtacados + 1, victoria, false);
    const quedadoSeco = !victoria && newTurno < this.maxTurnos && dummyState.getLegalActions().length === 0;

    return new GameState(newHpPropio, newStats, newHpEnemigo, this.dificultad, this.ataques, newObjetos, newTurno, this.maxTurnos, this.turnosAtacados + 1, victoria, quedadoSeco);
  }

  isTerminal(): boolean {
    if (this.victoria || this.hpPropio <= 0 || this.turno >= this.maxTurnos) return true;
    return this.getLegalActions().length === 0;
  }

  getReward(): number {
    if (this.hpPropio <= 0) return -100; // Fuerte castigo si la IA simula su propia muerte

    const statsRestantes = this.stats.reduce(
      (sum, s) => (s.consumible ? sum + Math.max(0, s.valorPropio) : sum), 0
    );
    const base = this.victoria ? 50 + (this.maxTurnos - this.turno) : this.turnosAtacados;
    return base - 30 * (this.quedadoSeco ? 1 : 0) + 0.1 * statsRestantes;
  }
}
// ─────────────────────────────────────────────
//  MctsNode  — nodo del árbol de búsqueda
// ─────────────────────────────────────────────
class MctsNode {
  children: Map<AccionIA, MctsNode> = new Map();
  visits      = 0;
  totalReward = 0;
  untriedActions: AccionIA[];

  constructor(
    public readonly state: GameState,
    public readonly parent: MctsNode | null = null,
    public readonly actionFromParent: AccionIA | null = null
  ) {
    this.untriedActions = [...state.getLegalActions()];
  }

  isFullyExpanded(): boolean {
    return this.untriedActions.length === 0;
  }

  ucb1(c: number): number {
    if (this.visits === 0) return Infinity;
    return (
      this.totalReward / this.visits
      + c * Math.sqrt(Math.log(this.parent!.visits) / this.visits)
    );
  }

  bestChild(c: number): MctsNode {
    let best: MctsNode | null = null;
    let bestScore = -Infinity;
    for (const child of this.children.values()) {
      const score = child.ucb1(c);
      if (score > bestScore) { bestScore = score; best = child; }
    }
    return best!;
  }
}

// ─────────────────────────────────────────────
//  MctsEngine  — motor MCTS genérico
// ─────────────────────────────────────────────
export class MctsEngine {
  private readonly iterations: number;
  private readonly explorationConstant: number;
  private readonly rolloutDepth: number;

  constructor(config: MctsConfig = {}) {
    this.iterations          = config.iterations          ?? 3000;
    this.explorationConstant = config.explorationConstant ?? Math.SQRT2;
    this.rolloutDepth        = config.rolloutDepth        ?? 30;
  }

  search(rootState: GameState): AccionIA | null {
    const legal = rootState.getLegalActions();
    if (legal.length === 0) return null;
    if (legal.length === 1) return legal[0];

    const root = new MctsNode(rootState);

    for (let i = 0; i < this.iterations; i++) {
      const leaf   = this.selectAndExpand(root);
      const reward = this.rollout(leaf.state);
      this.backpropagate(leaf, reward);
    }

    // Acción del hijo más visitado
    let bestAction: AccionIA | null = null;
    let bestVisits = -1;
    for (const [action, child] of root.children) {
      if (child.visits > bestVisits) {
        bestVisits = child.visits;
        bestAction = action;
      }
    }

    return bestAction ?? legal[0];
  }

  private selectAndExpand(node: MctsNode): MctsNode {
    while (!node.state.isTerminal()) {
      if (!node.isFullyExpanded()) return this.expand(node);
      node = node.bestChild(this.explorationConstant);
    }
    return node;
  }

  private expand(node: MctsNode): MctsNode {
    const idx    = Math.floor(Math.random() * node.untriedActions.length);
    const action = node.untriedActions.splice(idx, 1)[0];
    const child  = new MctsNode(node.state.applyAction(action), node, action);
    node.children.set(action, child);
    return child;
  }

  private rollout(state: GameState): number {
    let current = state;
    let depth   = 0;
    while (!current.isTerminal() && depth < this.rolloutDepth) {
      const actions = current.getLegalActions();
      if (actions.length === 0) break;
      current = current.applyAction(actions[Math.floor(Math.random() * actions.length)]);
      depth++;
    }
    return current.getReward();
  }

  private backpropagate(node: MctsNode, reward: number): void {
    let cur: MctsNode | null = node;
    while (cur !== null) {
      cur.visits++;
      cur.totalReward += reward;
      cur = cur.parent;
    }
  }
}
