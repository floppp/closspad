// base/scale : 30/150 más o menos bien
const defaultOptions = {
    defaultRating: 50,
    maxRating: 100,
    minRating: 0,
    baseK: 30, // a >, > variabilidad cada partida, 40 se pasa, hay cambios muy salvajes, 8 hay muy poquita variación
    scaleFactor: 150,
    oneSetImportance: 0.6,
    players: {},
};

function calculatePointDecay(player, matchDate) {
    const timeDiff = matchDate - new Date(player.lastMatchDate);
    const monthsDiff = timeDiff / (1000 * 3600 * 24 * 30);

    if (monthsDiff <= 1) {
        return 0;
    }

    const decayPercentage = monthsDiff <= 2
          ? 0.02 * (Math.log10(1 + (9 * monthsDiff/2)) / Math.log10(10))
          : 0.02;

    return player.points * decayPercentage;
}

function createSystem(options = {}) {
    return { ...defaultOptions, ...options };
}

function addPlayer(
    system,
    playerId,
    matchDate,
    initialRating = system.defaultRating
) {
    const rating = clampRating(system, initialRating);
    return {
        ...system,
        players: {
            ...system.players,
            [playerId]: {
                id: playerId,
                name: playerId,
                points: rating,
                // volatility: 1.1,
                lastMatchDate: matchDate
            },
        },
    };
}

function updateSystem (system, match) {
    const { couple_a, couple_b, result, played_at } = match;
    const importance = computeImportance(match);
    const matchPlayers = [...couple_a, ...couple_b];
    const winner = determineWinner(result);

    const teamARating = getTeamRating(system, couple_a);
    const teamBRating = getTeamRating(system, couple_b);
    const expectedWinA = calculateExpectedWin(system, teamARating, teamBRating);

    // Add missing players
    let newSystem = system;
    for (const playerId of matchPlayers) {
        if (!newSystem.players[playerId]) {
            newSystem = addPlayer(newSystem, playerId, played_at);
        }
    }

    const matchDate = new Date(played_at);

    // Update both teams with proper opponent ratings
    const pointsTeamA = computeVariation(
        system,
        teamARating,
        teamBRating,
        winner === 'A',
        expectedWinA,
        importance
    );
    const pointsTeamB = computeVariation(
        system,
        teamBRating,
        teamARating,
        winner === 'B',
        1 - expectedWinA,
        importance
    );

    newSystem = updateSystemCouple(newSystem, couple_a, pointsTeamA);
    newSystem = updateSystemCouple(newSystem, couple_b, pointsTeamB);

    const inactivePlayersForDecay = Object.keys(newSystem.players).filter(
        (id) => !matchPlayers.includes(id),
    );
    return {
        ...applyDecay(newSystem, inactivePlayersForDecay, matchDate),
        date: matchDate,
    };
}

function updateSystemCouple(system, couple, pointsChange) {
    const [p1, p2] = couple;
    const changePerPlayer = pointsChange / 2; // Equal split

    return {
        ...system,
        players: {
            ...system.players,
            [p1]: {
                ...system.players[p1],
                points: clampRating(system, system.players[p1].points + changePerPlayer)
            },
            [p2]: {
                ...system.players[p2],
                points: clampRating(system, system.players[p2].points + changePerPlayer)
            }
        }
    };
}


// --- Helper Functions ---
function calculateExpectedWin(system, teamARating, teamBRating) {
    const exponent = (teamBRating - teamARating) / system.scaleFactor;
    return 1 / (1 + Math.pow(10, exponent));
}

function proximityFactor(system, player) {
    const rating = player?.points ?? system.defaultRating;
    const fromTop = (system.maxRating - rating) / system.maxRating;
    const fromBottom = rating / system.maxRating;
    return Math.min(fromTop, fromBottom, 1) * 2;
}

function getKFactor(system, player) {
    return system.baseK * (player?.volatility ?? 1);
}

function clampRating(system, rating) {
    return Math.max(system.minRating, Math.min(system.maxRating, rating));
}

function determineWinner(result) {
    const points = result.map(([a, b]) => a > b ? 1 : -1).reduce((acc, e) => acc + e, 0);
    return points > 0 ? "A" : "B";
}

function getTeamRating(system, couple) {
    return couple.reduce((sum, id) => sum + (system.players[id]?.points ?? 0), 0);
}

function computeVariation(
    system,
    coupleRating,
    otherCoupleRating,
    isWinner,
    expectedWin,
    importance,
) {
    const baseK = system.baseK;

    const actualResult = isWinner ? 1 : 0;

    const variation = importance * baseK * (actualResult - expectedWin);

    return Math.round(variation);
}


function applyDecay(system, currentMatchPlayers, matchDate) {
    const updatedPlayers = { ...system.players };
    const matchDateObj = new Date(matchDate);

    // Only decay players NOT in current match
    const playersToDecay = Object.keys(updatedPlayers)
          .filter(id => !currentMatchPlayers.includes(id));

    for (const id of playersToDecay) {
        const player = updatedPlayers[id];

        const decayPoints = calculatePointDecay(
            player,
            matchDateObj,
        );

        updatedPlayers[id] = {
            ...player,
            points: clampRating(system, player.points - decayPoints)
        };
    }

    return {
        ...system,
        players: updatedPlayers
    };
}

const computeImportance = (match) => {
    const { importance = 1 } = match;
    return importance * (match.result.length === 1 ? defaultOptions.oneSetImportance : 1);
}

const processMatches = (matches) => {
    const initialState = createSystem();

    const states = matches.reduce(
        (acc, match) => {
            const [lastState] = acc;
            const newState = updateSystem(lastState, match);
            return [newState, ...acc];
        },
        [initialState]
    );

    const formattedStates = states.map(state => {
        return [state.date,
                Object.entries(state.players)
                .map(([id, player]) => [id, Math.round(2 * player.points) / 2])
                .sort((a, b) => b[1] - a[1])];
    });

    return formattedStates;
};

// processMatches(matches);

// module.exports = { processMatches };
export { processMatches };
