type PlayerId = string;
type Couple = [PlayerId, PlayerId];
type GameSet = [number, number];
type GameResult = [GameSet, GameSet] | [GameSet, GameSet, GameSet];

type Player = {
  id: PlayerId;
  name: string;
  points: number;
  volatility?: number; // New: Player consistency factor (0.8-1.2)
};

type Options = {
  defaultRating: number;
  maxRating: number;
  minRating: number;
  baseK: number;
  scaleFactor: number;
  players: Map<PlayerId, Player>;
};

type Match = {
  coupleA: Couple;
  coupleB: Couple;
  date: Date;
  result: GameResult;
  importance?: number; // New: Match importance multiplier (0.5-2.0)
};

const defaultOptions = {
  defaultRating: 50,
  maxRating: 100,
  minRating: 0,
  baseK: 10,
  scaleFactor: 200,
  players: new Map(),
} as const;

class PadelRatingSystem {
  private defaultRating: number;
  private maxRating: number;
  private minRating: number;
  private baseK: number;
  private scaleFactor: number;
  private players: Map<PlayerId, Player>;

  constructor(options: Options = defaultOptions) {
    this.defaultRating = options.defaultRating;
    this.maxRating = options.maxRating;
    this.minRating = options.minRating;
    this.baseK = options.baseK;
    this.scaleFactor = options.scaleFactor;
    this.players = new Map(options.players);
  }

  // --- Core Methods ---
  addPlayer(playerId: PlayerId, initialRating: number = this.defaultRating): PadelRatingSystem {
    const rating = this._clampRating(initialRating);
    const newPlayers = new Map(this.players);
    
    newPlayers.set(playerId, {
      id: playerId,
      name: playerId,
      points: rating,
      volatility: 1.1 // New players start with higher volatility
    });

    return this._newInstanceWithPlayers(newPlayers);
  }

  getRatings(playerIds: PlayerId[]): Record<PlayerId, number> {
    return playerIds.reduce((acc, id) => {
      acc[id] = this.players.get(id)?.points ?? 0;
      return acc;
    }, {} as Record<PlayerId, number>);
  }

  updateSystem(match: Match): PadelRatingSystem {
    const { coupleA, coupleB, result, importance = 1 } = match;
    const allPlayers = [...coupleA, ...coupleB];
    const winner = this._winner(result);
    
    // Pre-calculate all ratings
    const teamARating = this._teamRating(coupleA);
    const teamBRating = this._teamRating(coupleB);
    const expectedWinA = this._calculateExpectedWin(teamARating, teamBRating);

    // Add missing players
    let system = this;
    for (const playerId of allPlayers) {
      if (!system.players.has(playerId)) {
        system = system.addPlayer(playerId);
      }
    }

    // Update both teams with proper opponent ratings
    system = system._updateCouple(
      "A",
      expectedWinA,
      winner,
      coupleA,
      teamBRating,
      importance
    );
    system = system._updateCouple(
      "B",
      1 - expectedWinA, // More accurate than recalculating
      winner,
      coupleB,
      teamARating,
      importance
    );

    // Apply decay to inactive players (immutably)
    return system._applyDecay(allPlayers);
  }

  // --- Helper Methods ---
  private _newInstanceWithPlayers(players: Map<PlayerId, Player>): PadelRatingSystem {
    return new PadelRatingSystem({
      defaultRating: this.defaultRating,
      maxRating: this.maxRating,
      minRating: this.minRating,
      baseK: this.baseK,
      scaleFactor: this.scaleFactor,
      players
    });
  }

  private _calculateExpectedWin(teamARating: number, teamBRating: number): number {
    const exponent = (teamBRating - teamARating) / this.scaleFactor;
    return 1 / (1 + Math.pow(10, exponent));
  }

  private _proximityFactor(player: Player | undefined): number {
    const rating = player?.points ?? this.defaultRating;
    const fromTop = (this.maxRating - rating) / this.maxRating;
    const fromBottom = rating / this.maxRating;
    return Math.min(fromTop, fromBottom, 1) * 2;
  }

  private _getKFactor(player: Player | undefined): number {
    return (this.baseK * (player?.volatility ?? 1));
  }

  private _clampRating(rating: number): number {
    return Math.max(this.minRating, Math.min(this.maxRating, rating));
  }

  private _winner(result: GameResult): "A" | "B" {
    const setsWon = result.map(([a, b]) => a > b).filter(Boolean).length;
    return setsWon > 1 ? "A" : "B";
  }

  private _teamRating(couple: Couple): number {
    return couple.reduce((sum, id) => sum + (this.players.get(id)?.points ?? 0), 0);
  }

  private _updateCouple(
    coupleId: "A" | "B",
    expectedWin: number,
    winner: string,
    couple: Couple,
    opponentTeamRating: number,
    importance: number
  ): PadelRatingSystem {
    const newPlayers = new Map(this.players);
    const [player1, player2] = couple;
    const isWinner = winner === coupleId;

    const updatePlayer = (playerId: PlayerId, teammate: Player) => {
      const player = newPlayers.get(playerId)!;
      const change = this._getAdjustedPointsChange(
        player,
        teammate,
        opponentTeamRating,
        isWinner,
        expectedWin,
        importance
      );
      
      newPlayers.set(playerId, {
        ...player,
        points: this._clampRating(player.points + change),
        volatility: Math.max(0.8, player.volatility ? player.volatility * 0.99 : 1) // Reduce volatility over time
      });
    };

    updatePlayer(player1, newPlayers.get(player2)!);
    updatePlayer(player2, newPlayers.get(player1)!);

    return this._newInstanceWithPlayers(newPlayers);
  }

  private _getAdjustedPointsChange(
    player: Player,
    teammate: Player,
    opponentTeamRating: number,
    isWinner: boolean,
    expectedWin: number,
    importance: number
  ): number {
    // Normalize ratings (0-1 scale)
    const normalize = (points: number) => 
      Math.max(0, Math.min(1, (points - this.minRating) / (this.maxRating - this.minRating)));

    const playerNorm = normalize(player.points);
    const teammateNorm = normalize(teammate.points);
    const opponentNorm = normalize(opponentTeamRating / 2); // Average opponent rating

    // Adjustment factors (0.5-1.5 range)
    const partnerFactor = 0.7 + (0.5 - teammateNorm) * 0.6;
    const opponentFactor = 0.6 + opponentNorm * 0.8;
    const performanceFactor = 1.1 - (playerNorm - 0.5) * 0.4;

    // Composite adjustment
    let adjustment = importance;
    if (isWinner) {
      adjustment *= partnerFactor * opponentFactor * performanceFactor;
    } else {
      adjustment *= (2 - partnerFactor) * (2 - opponentFactor) * (2 - performanceFactor) / 4;
    }

    // Final calculation with caps
    const k = this._getKFactor(player);
    const proximity = this._proximityFactor(player);
    const baseChange = k * (isWinner ? (1 - expectedWin) : -expectedWin);
    
    return baseChange * proximity * Math.max(0.5, Math.min(1.5, adjustment));
  }

  private _applyDecay(activePlayers: PlayerId[]): PadelRatingSystem {
    const decayedPlayers = new Map(this.players);
    const decayFactor = 0.98;
    
    for (const [id, player] of decayedPlayers) {
      if (!activePlayers.includes(id)) {
        decayedPlayers.set(id, {
          ...player,
          points: this._clampRating(player.points * decayFactor)
        });
      }
    }

    return this._newInstanceWithPlayers(decayedPlayers);
  }
}

const matches = [
  {
    coupleA: ["Álex", "Dirk"],
    coupleB: ["Guapo", "Fernando"],
    date: new Date("2025-02-10"),
    result: [
      [6, 4],
      [3, 6],
      [6, 2],
    ],
  },
  {
    coupleA: ["Raúl", "Dirk"],
    coupleB: ["Álex", "Fernando"],
    date: new Date("2025-03-01"),
    result: [
      [6, 7],
      [6, 1],
      [4, 6],
    ],
  },
  {
    coupleA: ["Becario", "Dirk"],
    coupleB: ["Fernando", "Guapo"],
    date: new Date("2025-03-10"),
    result: [
      [6, 2],
      [6, 1],
    ],
  },
  {
    coupleA: ["Becario", "Alberto"],
    coupleB: ["Raúl", "Amador"],
    date: new Date("2025-03-17"),
    result: [
      [6, 2],
      [6, 1],
    ],
  },
  {
    coupleA: ["Álex", "Fernando"],
    coupleB: ["Raúl", "Carlos"],
    date: new Date("2025-03-24"),
    result: [
      [4, 6],
      [2, 6],
      [3, 6],
    ],
  },
  {
    coupleA: ["Alberto", "Fernando"],
    coupleB: ["Raúl", "Guapo"],
    date: new Date("2025-03-24"),
    result: [
      [7, 6],
      [2, 6],
      [7, 6],
    ],
  },
  {
    coupleA: ["Álex", "Murciano"],
    coupleB: ["Raúl", "Carlos"],
    date: new Date("2025-03-24"),
    result: [
      [3, 6],
      [7, 6],
      [3, 6],
    ],
  },
  {
    coupleA: ["Álex", "Fernando"],
    coupleB: ["Raúl", "Carlos"],
    date: new Date("2025-03-24"),
    result: [
      [4, 6],
      [6, 7],
      [4, 6],
    ],
  },
  {
    coupleA: ["Becario", "Fernando"],
    coupleB: ["Raúl", "Guapo"],
    date: new Date("2025-03-24"),
    result: [
      [6, 4],
      [7, 6],
      [6, 2],
    ],
  },
  {
    coupleA: ["Carlos", "Fernando"],
    coupleB: ["Raúl", "Alberto"],
    date: new Date("2025-05-21"),
    result: [
      [6, 2],
      [6, 3],
    ],
  },
  {
    coupleA: ["Raúl", "Fernando"],
    coupleB: ["Carlos", "Alberto"],
    date: new Date("2025-05-21"),
    result: [
      [6, 2],
      [6, 3],
    ],
  },
];

const states = matches
  .reduce((acc, e) => {
    const last = acc[0];
    const newState =
      last === undefined
        ? new PadelRatingSystem().updateSystem(e)
        : last.updateSystem(e);
    return [newState, ...acc];
  }, [] as PadelRatingSystem[])
  .map((s) => s.players)
  .map((ps) =>
    Array.from(ps)
      .map(([id, player]) => [id, Math.round(2 * player.points) / 2])
      .sort((a, b) => b[1] - a[1])
  );

states;
