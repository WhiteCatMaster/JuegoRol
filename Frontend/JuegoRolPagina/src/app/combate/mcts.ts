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
    public readonly hpPropio: number,
    public readonly stats: EstadisticaPersonaje[],
    public readonly hpEnemigo: number,
    public readonly dificultad: number,
    public readonly ataques: Ataque[],
    public readonly objetos: Objeto[],
    public readonly turno: number = 0,
    public readonly maxTurnos: number = 30,
    public readonly turnosAtacados: number = 0,
    public readonly victoria: boolean = false,
    public readonly quedadoSeco: boolean = false,
    public readonly usoObjetoUtil: boolean = false,
    public readonly danoRivalEsperado: number = 0,
    public readonly danoTotalHecho: number = 0
  ) {}

  getLegalActions(): AccionIA[] {
    const ataquesLegales = this.ataques.filter(atc => {
      if (!atc.statReducePropio || atc.statReducePropio.length === 0) return true;
      for (const coste of atc.statReducePropio) {
        // 🔥 LOGICA MEJORADA: Comparamos en minúsculas por si hay problemas de 'Mana' vs 'mana'
        const stat = this.stats.find(s => s.nombreEstadistica.toLowerCase() === coste.estadistica.toLowerCase());
        if (!stat || stat.valorPropio < coste.valor) return false;
      }
      return true;
    });

    const objetosLegales = this.objetos.filter(obj => {
      if (obj.usos <= 0) return false;

      // Daño al rival → siempre aporta algo
      for (const ef of obj.efectosRival) {
        if (ef.estadistica.toLowerCase() === 'vida' && ef.valor < 0) return true;
      }

      // Curación útil solo si estamos heridos; buffs útiles solo si la stat está baja
      for (const ef of obj.efectosPropios) {
        const nombre = ef.estadistica.toLowerCase();
        if (nombre === 'vida') {
          if (ef.valor > 0 && this.hpPropio < 70) return true;
        } else if (ef.valor > 0) {
          const stat = this.stats.find(s => s.nombreEstadistica.toLowerCase() === nombre);
          if (stat && stat.valorPropio < 30) return true;
        }
      }

      return false;
    });

    return [...ataquesLegales, ...objetosLegales];
  }

  applyAction(accion: AccionIA): GameState {
    let newHpPropio = this.hpPropio;
    let newHpEnemigo = this.hpEnemigo;
    const newStats = this.stats.map(s => ({ ...s }));
    const newObjetos = this.objetos.map(o => ({ ...o, efectosPropios: [...o.efectosPropios], efectosRival: [...o.efectosRival] }));

    let objetoUtilUsado = false;
    let danoEsteHecho = 0;

    if ('usos' in accion) {
        const objIndex = newObjetos.findIndex(o => o.nombre === accion.nombre);
        if (objIndex !== -1 && newObjetos[objIndex].usos > 0) {
            newObjetos[objIndex].usos -= 1;
        }

        for (const ef of accion.efectosPropios) {
            // 🔥 LOGICA MEJORADA: Búsqueda case-insensitive
            const stat = newStats.find(s => s.nombreEstadistica.toLowerCase() === ef.estadistica.toLowerCase());
            if (stat) {
                if (stat.valorPropio < 20) objetoUtilUsado = true;
                stat.valorPropio += ef.valor;
            }

            if (ef.estadistica.toLowerCase() === 'vida') {
                if (newHpPropio < 70) objetoUtilUsado = true;
                newHpPropio += ef.valor;
            }
        }

        for (const ef of accion.efectosRival) {
            if (ef.estadistica.toLowerCase() === 'vida') {
                newHpEnemigo += ef.valor; // Suponiendo daño en negativo
                if (ef.valor < 0) danoEsteHecho += -ef.valor;
                objetoUtilUsado = true;
            }
        }
    } else {
        for (const coste of (accion.statReducePropio ?? [])) {
          const stat = newStats.find(s => s.nombreEstadistica.toLowerCase() === coste.estadistica.toLowerCase());
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
        danoEsteHecho = Math.max(0, dano);
    }

    const victoria  = newHpEnemigo <= 0;

    // Contraataque del rival: el combate real es por turnos alternos, así que
    // si la IA no ha rematado, el jugador devuelve un golpe de daño esperado.
    if (!victoria && this.danoRivalEsperado > 0) {
        newHpPropio -= this.danoRivalEsperado;
    }

    const newTurno  = this.turno + 1;
    const newDanoTotal = this.danoTotalHecho + danoEsteHecho;

    // Calculamos si se quedó seco para el SIGUIENTE turno
    const dummyState = new GameState(newHpPropio, newStats, newHpEnemigo, this.dificultad, this.ataques, newObjetos, newTurno, this.maxTurnos, this.turnosAtacados + 1, victoria, false);
    const quedadoSeco = !victoria && newTurno < this.maxTurnos && dummyState.getLegalActions().length === 0;

    return new GameState(
        newHpPropio, newStats, newHpEnemigo, this.dificultad, this.ataques, newObjetos,
        newTurno, this.maxTurnos,
        'usos' in accion ? this.turnosAtacados : this.turnosAtacados + 1,
        victoria, quedadoSeco, this.usoObjetoUtil || objetoUtilUsado,
        this.danoRivalEsperado, newDanoTotal
    );
  }

  isTerminal(): boolean {
    if (this.victoria || this.hpPropio <= 0 || this.turno >= this.maxTurnos) return true;
    return this.getLegalActions().length === 0;
  }

  getReward(): number {
    if (this.hpPropio <= 0) return -100;
    const statsRestantes = this.stats.reduce(
      (sum, s) => (s.consumible ? sum + Math.max(0, s.valorPropio) : sum), 0
    );
    // Señal continua: damos crédito por daño infligido, no solo por sobrevivir turnos.
    // Así MCTS distingue ataques fuertes de débiles aunque ninguno gane el combate.
    let base = this.victoria
      ? 100 + (this.maxTurnos - this.turno) * 2
      : this.danoTotalHecho * 0.5 + this.turnosAtacados * 0.5;
    if (this.usoObjetoUtil) base += 20;
    // Bonus por HP propio restante: incentiva curarse / sobrevivir cuando hay contraataque.
    base += 0.2 * Math.max(0, this.hpPropio);
    return base - 50 * (this.quedadoSeco ? 1 : 0) + 0.1 * statsRestantes;
  }
}

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
    return (this.totalReward / this.visits) + c * Math.sqrt(Math.log(this.parent!.visits) / this.visits);
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
    console.log("=== INICIANDO PENSAMIENTO DE IA ===");
    console.log("Estado de la IA:", JSON.parse(JSON.stringify(rootState.stats)));
    console.log("HP de la IA:", rootState.hpPropio);
    
    const legal = rootState.getLegalActions();
    console.log("Acciones legales encontradas por la IA:", legal.map(a => a.nombre));

    if (legal.length === 0) {
        console.warn("⚠️ LA IA NO TIENE ACCIONES LEGALES. ESTÁ SECA.");
        return null;
    }
    
    // Si solo hay un objeto/ataque disponible, no pensamos, lo tiramos directo
    if (legal.length === 1) {
        console.log("Solo hay 1 acción posible. Ejecutando:", legal[0].nombre);
        return legal[0];
    }

    const root = new MctsNode(rootState);

    for (let i = 0; i < this.iterations; i++) {
      const leaf   = this.selectAndExpand(root);
      const reward = this.rollout(leaf.state);
      this.backpropagate(leaf, reward);
    }

    let bestAction: AccionIA | null = null;
    let bestVisits = -1;
    let bestScore = -Infinity;
    
    console.log("=== RESULTADOS DEL ÁRBOL DE DECISIONES ===");
    for (const [action, child] of root.children) {
      const avgReward = (child.totalReward / child.visits).toFixed(2);
      console.log(`- Acción [${action.nombre}]: Visitado ${child.visits} veces | Recompensa media: ${avgReward}`);
      
      if (child.visits > bestVisits) {
        bestVisits = child.visits;
        bestAction = action;
        bestScore = child.totalReward / child.visits;
      }
    }

    console.log(`🏆 LA IA HA DECIDIDO USAR: ${bestAction?.nombre} (Score: ${bestScore.toFixed(2)})`);
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