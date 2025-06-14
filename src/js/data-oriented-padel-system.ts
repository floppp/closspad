type PlayerId = string;
type Couple = [PlayerId, PlayerId];
type GameSet = [number, number];
type GameResult = [GameSet, GameSet] | [GameSet, GameSet, GameSet];

type Player = {
  id: PlayerId;
  name: string;
  points: number;
  volatility?: number;
};

type Options = {
  defaultRating: number;
  maxRating: number;
  minRating: number;
  baseK: number;
  scaleFactor: number;
  players: Record<PlayerId, Player>;
};

type Match = {
  coupleA: Couple;
  coupleB: Couple;
  date: Date;
  result: GameResult;
  importance?: number;
};

const defaultOptions: Options = {
  defaultRating: 50,
  maxRating: 100,
  minRating: 0,
  baseK: 10,
  scaleFactor: 200,
  players: {},
};

// --- Core Functions ---
function createSystem(options: Partial<Options> = {}): Options {
  return { ...defaultOptions, ...options };
}

function addPlayer(
  system: Options,
  playerId: PlayerId,
  initialRating: number = system.defaultRating
): Options {
  const rating = clampRating(system, initialRating);
  return {
    ...system,
    players: {
      ...system.players,
      [playerId]: {
        id: playerId,
        name: playerId,
        points: rating,
        volatility: 1.1,
      },
    },
  };
}

function updateSystem(system: Options, match: Match): Options {
  const { coupleA, coupleB, result, importance = 1 } = match;
  const allPlayers = [...coupleA, ...coupleB];
  const winner = determineWinner(result);

  // Pre-calculate all ratings
  const teamARating = getTeamRating(system, coupleA);
  const teamBRating = getTeamRating(system, coupleB);
  const expectedWinA = calculateExpectedWin(system, teamARating, teamBRating);

  // Add missing players
  let newSystem = system;
  for (const playerId of allPlayers) {
    if (!newSystem.players[playerId]) {
      newSystem = addPlayer(newSystem, playerId);
    }
  }

  // Update both teams with proper opponent ratings
  newSystem = updateCouple(
    newSystem,
    "A",
    expectedWinA,
    winner,
    coupleA,
    teamBRating,
    importance
  );
  newSystem = updateCouple(
    newSystem,
    "B",
    1 - expectedWinA,
    winner,
    coupleB,
    teamARating,
    importance
  );

  // Apply decay to inactive players
  return applyDecay(newSystem, allPlayers);
}

// --- Helper Functions ---
function calculateExpectedWin(
  system: Options,
  teamARating: number,
  teamBRating: number
): number {
  const exponent = (teamBRating - teamARating) / system.scaleFactor;
  return 1 / (1 + Math.pow(10, exponent));
}

function proximityFactor(system: Options, player: Player | undefined): number {
  const rating = player?.points ?? system.defaultRating;
  const fromTop = (system.maxRating - rating) / system.maxRating;
  const fromBottom = rating / system.maxRating;
  return Math.min(fromTop, fromBottom, 1) * 2;
}

function getKFactor(system: Options, player: Player | undefined): number {
  return system.baseK * (player?.volatility ?? 1);
}

function clampRating(system: Options, rating: number): number {
  return Math.max(system.minRating, Math.min(system.maxRating, rating));
}

function determineWinner(result: GameResult): "A" | "B" {
  const setsWon = result.map(([a, b]) => a > b).filter(Boolean).length;
  return setsWon > 1 ? "A" : "B";
}

function getTeamRating(system: Options, couple: Couple): number {
  return couple.reduce((sum, id) => sum + (system.players[id]?.points ?? 0), 0);
}

function updateCouple(
  system: Options,
  coupleId: "A" | "B",
  expectedWin: number,
  winner: string,
  couple: Couple,
  opponentTeamRating: number,
  importance: number
): Options {
  const [player1Id, player2Id] = couple;
  const isWinner = winner === coupleId;

  const player1 = system.players[player1Id];
  const player2 = system.players[player2Id];

  const change1 = getAdjustedPointsChange(
    system,
    player1,
    player2,
    opponentTeamRating,
    isWinner,
    expectedWin,
    importance
  );
  
  const change2 = getAdjustedPointsChange(
    system,
    player2,
    player1,
    opponentTeamRating,
    isWinner,
    expectedWin,
    importance
  );

  return {
    ...system,
    players: {
      ...system.players,
      [player1Id]: {
        ...player1,
        points: clampRating(system, player1.points + change1),
        volatility: Math.max(0.8, (player1.volatility ?? 1) * 0.99),
      },
      [player2Id]: {
        ...player2,
        points: clampRating(system, player2.points + change2),
        volatility: Math.max(0.8, (player2.volatility ?? 1) * 0.99),
      },
    },
  };
}

function getAdjustedPointsChange(
  system: Options,
  player: Player,
  teammate: Player,
  opponentTeamRating: number,
  isWinner: boolean,
  expectedWin: number,
  importance: number
): number {
  const normalize = (points: number) =>
    Math.max(0, Math.min(1, (points - system.minRating) / (system.maxRating - system.minRating)));

  const playerNorm = normalize(player.points);
  const teammateNorm = normalize(teammate.points);
  const opponentNorm = normalize(opponentTeamRating / 2);

  const partnerFactor = 0.7 + (0.5 - teammateNorm) * 0.6;
  const opponentFactor = 0.6 + opponentNorm * 0.8;
  const performanceFactor = 1.1 - (playerNorm - 0.5) * 0.4;

  let adjustment = importance;
  if (isWinner) {
    adjustment *= partnerFactor * opponentFactor * performanceFactor;
  } else {
    adjustment *= (2 - partnerFactor) * (2 - opponentFactor) * (2 - performanceFactor) / 4;
  }

  const k = getKFactor(system, player);
  const proximity = proximityFactor(system, player);
  const baseChange = k * (isWinner ? (1 - expectedWin) : -expectedWin);
  
  return baseChange * proximity * Math.max(0.5, Math.min(1.5, adjustment));
}

function applyDecay(system: Options, activePlayers: PlayerId[]): Options {
  const decayFactor = 0.98;
  const updatedPlayers = { ...system.players };

  for (const id in updatedPlayers) {
    if (!activePlayers.includes(id)) {
      updatedPlayers[id] = {
        ...updatedPlayers[id],
        points: clampRating(system, updatedPlayers[id].points * decayFactor),
      };
    }
  }

  return {
    ...system,
    players: updatedPlayers,
  };
}

// --- Example Usage ---

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
    date: new Date("2025-04-07"),
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

const initialState = createSystem();
const states = matches.reduce((acc, match) => {
  const lastState = acc[0] || initialState;
  const newState = updateSystem(lastState, match);
  return [newState, ...acc];
}, [] as Options[]);

const formattedStates = states.map((state) =>
  Object.entries(state.players)
    .map(([id, player]) => [id, Math.round(2 * player.points) / 2])
    .sort((a, b) => (b[1] as number) - (a[1] as number))
);

console.log(formattedStates);