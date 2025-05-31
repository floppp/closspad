// Completely deepseek model, too complex i have to review it.

type PlayerId = string;
type Couple = [PlayerId, PlayerId];
type GameSet = [number, number];
type GameResult = [GameSet, GameSet] | [GameSet, GameSet, GameSet];

// Match importance tiers
const MATCH_IMPORTANCE = {
  FRIENDLY: 0.8,
  LEAGUE: 1.0,
  TOURNAMENT: 1.3,
  CHAMPIONSHIP: 1.5
} as const;

type Player = {
  id: PlayerId;
  name: string;
  points: number;
  volatility: number;
  matchesPlayed: number;
  streak: number;
  lastPlayed: Date;
};

type Options = {
  defaultRating: number;
  maxRating: number;
  minRating: number;
  baseK: number;
  scaleFactor: number;
  players: Map<PlayerId, Player>;
  newPlayerKFactor?: number;
  veteranPlayerKFactor?: number;
};

type Match = {
  coupleA: Couple;
  coupleB: Couple;
  date: Date;
  result: GameResult;
  importance?: keyof typeof MATCH_IMPORTANCE;
};

const defaultOptions = {
  defaultRating: 50,
  maxRating: 100,
  minRating: 0,
  baseK: 10,
  scaleFactor: 200,
  players: new Map(),
  newPlayerKFactor: 1.5,
  veteranPlayerKFactor: 0.8
} as const;

class PadelRatingSystem {
  private defaultRating: number;
  private maxRating: number;
  private minRating: number;
  private baseK: number;
  private scaleFactor: number;
  private players: Map<PlayerId, Player>;
  private newPlayerKFactor: number;
  private veteranPlayerKFactor: number;

  constructor(options: Partial<Options> = {}) {
    const mergedOptions = { ...defaultOptions, ...options };
    this.defaultRating = mergedOptions.defaultRating;
    this.maxRating = mergedOptions.maxRating;
    this.minRating = mergedOptions.minRating;
    this.baseK = mergedOptions.baseK;
    this.scaleFactor = mergedOptions.scaleFactor;
    this.players = new Map(mergedOptions.players);
    this.newPlayerKFactor = mergedOptions.newPlayerKFactor;
    this.veteranPlayerKFactor = mergedOptions.veteranPlayerKFactor;
  }

  // --- Public Interface ---
  addPlayer(playerId: PlayerId, initialRating: number = this.defaultRating): PadelRatingSystem {
    const rating = this._clampRating(initialRating);
    const newPlayers = new Map(this.players);
    
    newPlayers.set(playerId, {
      id: playerId,
      name: playerId,
      points: rating,
      volatility: 1.2,
      matchesPlayed: 0,
      streak: 0,
      lastPlayed: new Date()
    });

    return this._newInstanceWithPlayers(newPlayers);
  }

  getRatings(playerIds?: PlayerId[]): Record<PlayerId, number> {
    const ids = playerIds || Array.from(this.players.keys());
    return ids.reduce((acc, id) => {
      acc[id] = this.players.get(id)?.points ?? 0;
      return acc;
    }, {} as Record<PlayerId, number>);
  }

  getPlayerDetails(playerId: PlayerId): Player | undefined {
    return this.players.get(playerId);
  }

  updateSystem(match: Match): PadelRatingSystem {
    const { coupleA, coupleB, result, importance = 'LEAGUE' } = match;
    const allPlayers = [...coupleA, ...coupleB];
    const winner = this._winner(result);
    const importanceFactor = MATCH_IMPORTANCE[importance];
    
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
      importanceFactor,
      result
    );
    system = system._updateCouple(
      "B",
      1 - expectedWinA,
      winner,
      coupleB,
      teamARating,
      importanceFactor,
      result
    );

    // Apply decay to inactive players
    return system._applyDecay(allPlayers);
  }

  // --- Core Calculation Methods ---
  private _updateCouple(
    coupleId: "A" | "B",
    expectedWin: number,
    winner: string,
    couple: Couple,
    opponentTeamRating: number,
    importance: number,
    result: GameResult
  ): PadelRatingSystem {
    const newPlayers = new Map(this.players);
    const [player1Id, player2Id] = couple;
    const isWinner = winner === coupleId;

    const updatePlayer = (playerId: PlayerId, teammateId: PlayerId) => {
      const player = newPlayers.get(playerId)!;
      const teammate = newPlayers.get(teammateId)!;
      
      const change = this._getAdjustedPointsChange(
        player,
        teammate,
        opponentTeamRating,
        isWinner,
        expectedWin,
        importance,
        result
      );
      
      const updatedPlayer = {
        ...this._updatePlayerStats(player, isWinner),
        points: this._clampRating(player.points + change)
      };
      
      newPlayers.set(playerId, updatedPlayer);
    };

    updatePlayer(player1Id, player2Id);
    updatePlayer(player2Id, player1Id);

    return this._newInstanceWithPlayers(newPlayers);
  }

  private _getAdjustedPointsChange(
    player: Player,
    teammate: Player,
    opponentTeamRating: number,
    isWinner: boolean,
    expectedWin: number,
    importance: number,
    result: GameResult
  ): number {
    // Base calculation
    const k = this._getKFactor(player);
    const proximity = this._proximityFactor(player);
    const baseChange = k * (isWinner ? (1 - expectedWin) : -expectedWin);
    
    // Adjustment factors
    const partnerFactor = this._getPartnerFactor(player, teammate);
    const opponentFactor = this._getOpponentFactor(opponentTeamRating);
    const performanceFactor = this._getPerformanceFactor(player);
    const streakFactor = this._getStreakMultiplier(player);
    const scoreImpact = this._getSetScoreImpact(result, isWinner);
    
    // Composite adjustment with reasonable caps
    const adjustment = Math.max(0.5, Math.min(2.0,
      importance *
      partnerFactor *
      opponentFactor *
      performanceFactor *
      streakFactor *
      scoreImpact
    ));
    
    return baseChange * proximity * adjustment;
  }

  // --- Rating Adjustment Factors ---
  private _getKFactor(player: Player): number {
    const base = this.baseK;
    
    // New players adjust faster
    if (player.matchesPlayed < 10) {
      return base * this.newPlayerKFactor;
    }
    
    // Veteran players adjust slower
    if (player.matchesPlayed > 50) {
      return base * this.veteranPlayerKFactor;
    }
    
    return base * player.volatility;
  }

  private _getPartnerFactor(player: Player, teammate: Player): number {
    const ratingDiff = teammate.points - player.points;
    // If partner is stronger, your changes are smaller (and vice versa)
    return 1 - (ratingDiff / (this.maxRating * 2));
  }

  private _getOpponentFactor(opponentTeamRating: number): number {
    const avgOpponentRating = opponentTeamRating / 2;
    const ratingRatio = avgOpponentRating / this.maxRating;
    // Beating stronger opponents gives more points
    return 0.8 + (ratingRatio * 0.4);
  }

  private _getPerformanceFactor(player: Player): number {
    // Players far from their "true" rating adjust faster
    const matches = player.matchesPlayed || 1;
    return 1.2 - (Math.min(matches, 30) / 30) * 0.4;
  }

  private _getStreakMultiplier(player: Player): number {
    if (!player.streak) return 1.0;
    
    // Winning streak bonus
    if (player.streak > 0) {
      return 1 + (Math.min(player.streak, 5) * 0.05);
    }
    
    // Losing streak protection
    return 1 - (Math.min(-player.streak, 5) * 0.025);
  }

  private _getSetScoreImpact(result: GameResult, isWinner: boolean): number {
    const setDiffs = result.map(([a, b]) => isWinner ? a - b : b - a);
    const avgDiff = setDiffs.reduce((sum, diff) => sum + diff, 0) / setDiffs.length;
    
    // Close matches matter more than blowouts
    return 1.1 - (Math.min(avgDiff, 5) * 0.06);
  }

  // --- Helper Methods ---
  private _newInstanceWithPlayers(players: Map<PlayerId, Player>): PadelRatingSystem {
    return new PadelRatingSystem({
      defaultRating: this.defaultRating,
      maxRating: this.maxRating,
      minRating: this.minRating,
      baseK: this.baseK,
      scaleFactor: this.scaleFactor,
      players,
      newPlayerKFactor: this.newPlayerKFactor,
      veteranPlayerKFactor: this.veteranPlayerKFactor
    });
  }

  private _updatePlayerStats(player: Player, isWinner: boolean): Player {
    const newStreak = isWinner
      ? Math.max(1, (player.streak || 0) + 1)
      : Math.min(-1, (player.streak || 0) - 1);
    
    return {
      ...player,
      matchesPlayed: player.matchesPlayed + 1,
      streak: newStreak,
      lastPlayed: new Date(),
      volatility: Math.max(0.8, player.volatility * 0.99)
    };
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

  private _applyDecay(activePlayers: PlayerId[]): PadelRatingSystem {
    const decayedPlayers = new Map(this.players);
    const now = new Date();
    const decayPeriod = 30; // Days before decay starts
    
    for (const [id, player] of decayedPlayers) {
      if (!activePlayers.includes(id)) {
        const daysInactive = Math.floor(
          (now.getTime() - player.lastPlayed.getTime()) / (1000 * 60 * 60 * 24)
        );
        
        if (daysInactive > decayPeriod) {
          const decayFactor = 1 - (Math.min(daysInactive - decayPeriod, 60) * 0.005);
          decayedPlayers.set(id, {
            ...player,
            points: this._clampRating(player.points * decayFactor)
          });
        }
      }
    }
    
    return this._newInstanceWithPlayers(decayedPlayers);
  }
}